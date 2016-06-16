package it.unica.bd2.core;

import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.postgis.Geometry;
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
    private java.sql.Connection connection;
    private boolean isConnected = false;

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

    public void insert() {
        try {

            MongoConnector mongoConnector = MongoConnector.getInstance();
            mongoConnector.connect();
            MongoCursor<Document> mongoCursor = mongoConnector.read();

            PreparedStatement preparedStatementDropTable = connection.prepareStatement("DELETE from flights");
            preparedStatementDropTable.execute();
            preparedStatementDropTable.close();

            while (mongoCursor.hasNext()) {
                Document flight = mongoCursor.next();
                Long flightId = flight.getLong("flightID");
                List<Document> pointList = (List<Document>) flight.get("points");
                Point[] pointsVector = new Point[pointList.size()];

                int i = 0;
                for (Document point : pointList) {
                    pointsVector[i] = new Point(point.getDouble("latitude"), point.getDouble("longitude"), point.getDouble("altitude"));
                    i++;
                }
                LineString track = new LineString(pointsVector);
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO flights (flightId, track) VALUES (?, ?)");
                preparedStatement.setString(1, flightId.toString());
                preparedStatement.setObject(2, new PGgeometry(track));
                int num = preparedStatement.executeUpdate();
                preparedStatement.close();
                if (num == 0) {
                    throw new RuntimeException("Insert failed");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    /*
    * TO DO
     */
    public void query() {

        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT flightid, track FROM flights");
            while (resultSet.next()) {
                /*
				* Retrieve the geometry as an object then cast it to the geometry type.
				* Print things out.
				*/
                String flightID = resultSet.getString(1);
                PGgeometry track = (PGgeometry) resultSet.getObject(2);

                if (track.getGeoType() == Geometry.LINESTRING) {
                    System.out.println("FlightID " + flightID);
                } else {
                    System.out.println("Track " + track.getGeoType() + " having flightID " + flightID);
                }
            }
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


}
