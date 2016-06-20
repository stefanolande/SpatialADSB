package it.unica.bd2.core;

import com.mongodb.client.MongoCursor;
import it.unica.bd2.model.Comune;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    //write in the relational db all the new flights, and the one with new point since last sync
/*    CREATE TABLE public.stats
            (
                    id smallint NOT NULL PRIMARY KEY,
                    "timestamp" bigint
    )*/
    public void sync() {
        MongoConnector mongoConnector = null;
        try {
            //get the timestamp of last sync
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select timestamp from stats");
            rs.next();
            long timestamp = rs.getLong(1);
            System.out.println(timestamp);

            //read the flight with the last timestamp greater than the timestamp of the last sync
            mongoConnector = MongoConnector.getInstance();
            mongoConnector.connect();
            MongoCursor<Document> mongoCursor = mongoConnector.read(timestamp);

            while (mongoCursor.hasNext()) {
                Document flight = mongoCursor.next();
                Long flightId = flight.getLong("flightID");
                List<Document> pointList = (List<Document>) flight.get("points");

                PreparedStatement preparedStatementDropTable = connection.prepareStatement("DELETE from flights WHERE flightid = '" + flightId + "'");
                preparedStatementDropTable.execute();
                preparedStatementDropTable.close();

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

            //update the last sync timestamp
            Statement updateStatement = connection.createStatement();
            updateStatement.execute("UPDATE STATS SET timestamp =" + (System.currentTimeMillis() / 1000L));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mongoConnector != null) {
                mongoConnector.disconnect();
            }
        }

    }

    public ObservableList<Comune> globalQuery() {

        ObservableList<Comune> lista = FXCollections.observableArrayList();

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
                //System.out.println("Il comune di " + nomeComune + " ha avuto " + sorvoli + " sorvoli.");
                lista.add(new Comune(nomeComune, sorvoli + ""));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //voliPerPunto();
        return lista;
    }

    public int localQuery(double lat, double lon) {

        PreparedStatement statement = null;
        int sorvoli = 0;
        try {
            statement = connection.prepareStatement("SELECT count(*)" +
                    "FROM flights f where ST_DWithin(ST_GeographyFromText(?), f.track, " + Settings.DISTANCE + ", false)");

            String geo = "POINT(" + lat + " " + lon + ")";
            statement.setString(1, geo);

            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            sorvoli = resultSet.getInt(1);

            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sorvoli;
    }

    public int localQuery(String comune) {

        PreparedStatement statement = null;
        int sorvoli = 0;
        try {
            statement = connection.prepareStatement("SELECT count(*)" +
                    "FROM flights f where ST_DWithin(" +
                    "ST_CENTROID((SELECT geom from comuni where nome = ?))" +
                    ", f.track, " + Settings.DISTANCE + ", false)");

            statement.setString(1, comune);

            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            sorvoli = resultSet.getInt(1);

            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sorvoli;
    }

    public ObservableList<String> getComuni() {

        ObservableList<String> list = FXCollections.observableArrayList();

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select nome from comuni order by nome asc");

            while (rs.next()) {
                list.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


}
