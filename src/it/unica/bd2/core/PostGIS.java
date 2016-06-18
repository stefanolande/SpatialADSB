package it.unica.bd2.core;

import com.mongodb.client.MongoCursor;
import it.unica.bd2.model.Comune;
import it.unica.bd2.model.Puntuale;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.bson.Document;
import org.postgis.*;
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
    public ObservableList<Comune> query() {

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


    /*
    * Dato un punto
    */
    public ObservableList<Puntuale> voliPerPunto(Point punto) {//Point punto = new Point(8.9807033, 39.2901871, 14.18); Punto in ASSEMINI//longitudine-latitudine
        ObservableList<Puntuale> sorvoliPunto = FXCollections.observableArrayList();
        PreparedStatement preparedStatementDropTable = null;
        try {
            preparedStatementDropTable = connection.prepareStatement("DELETE from areascelta");
            preparedStatementDropTable.execute();
            preparedStatementDropTable.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Point[] pointsVector = new Point[]{
                new Point(punto.getX() - 0.009, punto.getY() + 0.05, punto.getZ()),
                new Point(punto.getX() + 0.009, punto.getY() + 0.05, punto.getZ()),
                new Point(punto.getX() - 0.009, punto.getY() - 0.05, punto.getZ()),
                new Point(punto.getX() + 0.009, punto.getY() - 0.05, punto.getZ()),
                new Point(punto.getX() - 0.009, punto.getY() + 0.05, punto.getZ())
        };
        Polygon area = new Polygon(new LinearRing[]{new LinearRing(pointsVector)});
        area.setSrid(4326);
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO areascelta (area, track) VALUES (?, ?)");
            preparedStatement.setString(1, "000000001");
            preparedStatement.setObject(2, new PGgeometry(area));
            int num = preparedStatement.executeUpdate();
            preparedStatement.close();
            if (num == 0) {
                throw new RuntimeException("Insert failed");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select c.area, count(*) as sorvoli " +
                    "from areascelta c " +
                    "join flights f " +
                    "on st_intersects(c.track, f.track) " +
                    "group by c.area " +
                    "order by sorvoli desc;");

            while (resultSet.next()) {
                int sorvoli = resultSet.getInt(2);
                System.out.println("Il punto che hai scelto ha avuto " + sorvoli + " sorvoli.");
                String puntoName = punto.getX() + " " + punto.getX();
                sorvoliPunto.add(new Puntuale(puntoName, sorvoli + ""));
            }
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sorvoliPunto;

    }

    /*
    * Dato un comune,
     */
    public void query2(String area) {

        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("" +
                    "select c.nome, count(*) as sorvoli " +
                    "from comuni c " +
                    "join flights f " +
                    "on st_intersects(c.geom, f.track) " +
                    "where c.nome='" + area + "' " +
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
