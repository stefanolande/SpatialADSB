package it.unica.bd2.core;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import it.unica.bd2.model.FlightUpdate;
import org.bson.Document;

import java.util.List;


/**
 * Created by stefano on 08/06/16.
 */
public class MongoConnector {
    private static MongoConnector instance;
    private MongoClient mongoClient;
    private MongoDatabase db;
    private MongoCollection<Document> flightsCollection;
    private boolean connected = false;

    private MongoConnector() {

    }

    public static MongoConnector getInstance() {
        if (instance == null) {
            instance = new MongoConnector();
        }

        return instance;
    }

    public void connect() {
        if (!connected) {
            mongoClient = new MongoClient(Settings.MONGO_SERVER_IP, Settings.MONGO_SERVER_PORT);
            db = mongoClient.getDatabase(Settings.MONGO_DB_NAME);
            flightsCollection = db.getCollection(Settings.MONGO_COLLECTION_NAME);

            connected = true;
        }
    }

    public void disconnect() {
        if (connected) {
            mongoClient.close();
            connected = false;
        }
    }

    public boolean isConnected() {
        return connected;
    }

    /**
     * Receives the document modelling a flight. If the flight is already present, it updates its info
     *
     * @param flightUpdate
     */
    public void update(FlightUpdate flightUpdate) {
        if (connected) {
            long flightID = flightUpdate.getFlightID();

            //check if the flight is already present
            if (flightsCollection.find(new Document("flightID", flightID)).iterator().hasNext()) {

                Document oldFlight = flightsCollection.find(new Document("flightID", flightID)).iterator().next();
                //the document is already present, update it
                List<Document> oldPoints = (List<Document>) oldFlight.get("points");

                //check if we have to push a new point
                if (flightUpdate.getPoint() != null) {
                    oldPoints.add(flightUpdate.getPointDocument());
                }

                flightsCollection.updateOne(new Document("flightID", flightID),
                        new Document("$set", flightUpdate.getDocumentWithoutPoint().append("points", oldPoints)));


            } else {
                //we need to create a new document
                flightsCollection.insertOne(flightUpdate.getDocument());
            }
        }
    }

    public MongoCursor<Document> read() {
        if (connected) {
            return flightsCollection.find(new Document()).iterator();
        }

        return null;
    }
}
