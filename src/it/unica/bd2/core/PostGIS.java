package it.unica.bd2.core;

import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.postgis.LineString;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.postgresql.util.PGobject;

import java.sql.*;
import java.util.List;

/**
 * Created by serus on 16/06/16.
 */
public class PostGIS {
    private static PostGIS instance;
    private java.sql.Connection connection;
    private boolean isConnected = false;

    private PostGIS() {
    }

    public static PostGIS getInstance() {

        if (instance == null) {
            instance = new PostGIS();
        }

        return instance;
    }

    /*
     * metodo connect
     */
    public void connect() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(Settings.POSTGIS_DB_URL, Settings.POSTGIS_DB_USERNAME, Settings.POSTGIS_DB_PASSWORD);

            ((org.postgresql.PGConnection) connection).addDataType("geometry", (Class<? extends PGobject>) Class.forName("org.postgis.PGgeometry"));
            ((org.postgresql.PGConnection) connection).addDataType("box3d", (Class<? extends PGobject>) Class.forName("org.postgis.PGbox3d"));
            this.isConnected = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean isConnected() {
        return isConnected;
    }

    public void disconnect() {
        if (isConnected) {
            try {
                this.connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void sync() {
        MongoConnector mongoConnector = null;
        try {

            mongoConnector = MongoConnector.getInstance();
            mongoConnector.connect();
            MongoCursor<Document> mongoCursor = mongoConnector.read();

            PreparedStatement preparedStatementDropTable = connection.prepareStatement("DELETE from flights");
            preparedStatementDropTable.execute();
            preparedStatementDropTable.close();

            while (mongoCursor.hasNext()) {
                Document flight = mongoCursor.next();
                Long flightId = flight.getLong("flightID");
                List<Document> pointList = (List<Document>) flight.get("points");

                if (pointList.size() > Settings.MIN_TRACK_SIZE) {
                    Point[] pointsVector = new Point[pointList.size()];

                    int i = 0;
                    for (Document point : pointList) {
                        pointsVector[i] = new Point(point.getDouble("longitude"), point.getDouble("latitude"), new Double(point.getInteger("altitude")));
                        i++;
                    }
                    LineString track = new LineString(pointsVector);
                    PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO flights (flightid, track) VALUES (?, ?)");
                    preparedStatement.setString(1, flightId.toString());
                    track.setSrid(4326);
                    preparedStatement.setObject(2, new PGgeometry(track));
                    int num = preparedStatement.executeUpdate();
                    preparedStatement.close();
                    if (num == 0) {
                        throw new RuntimeException("Insert failed");
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mongoConnector != null) {
                mongoConnector.disconnect();
            }
        }
    }

    /*
    * TO DO
     */
    public void query() {

        try (Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery("select c.nome, count(*) as sorvoli " +
                    "from comuni c " +
                    "join flights f " +
                    "on st_intersects(c.geom, f.track) " +
                    "group by c.nome " +
                    "order by sorvoli desc;");

            while (resultSet.next()) {
                String nomeComune = resultSet.getString(1);
                int sorvoli = resultSet.getInt(2);
                System.out.println("Il comune di " + nomeComune + " ha avuto " + sorvoli + " sorvoli.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /*
    * Dato un comune,
     */
    public void query2(String areaRicercata) {

        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("" +
                    "select c.nome, count(*) as sorvoli " +
                    "from comuni c " +
                    "join flights f " +
                    "on st_intersects(c.geom, f.track) " +
                    "where c.nome=='" + areaRicercata + "' " +
                    "group by c.nome " +
                    "order by sorvoli desc;");

            while (resultSet.next()) {
                String nomeComune = resultSet.getString(1);
                int sorvoli = resultSet.getInt(2);
                System.out.println("Il comune di " + nomeComune + " ha avuto " + sorvoli + " sorvoli.");
            }
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }




}
