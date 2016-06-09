package it.unica.bd2.core;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import it.unica.bd2.model.FlightUpdate;
import org.bson.Document;

import java.util.List;


/**
 * Created by stefano on 08/06/16.
 */
public class MongoConnector {
    private MongoClient mongoClient;
    private MongoDatabase db;
    private MongoCollection<Document> flightsCollection;

    public void connect() {
        mongoClient = new MongoClient(Settings.MONGO_SERVER_IP, Settings.MONGO_SERVER_PORT);
        db = mongoClient.getDatabase(Settings.MONGO_DB_NAME);
        flightsCollection = db.getCollection(Settings.MONGO_COLLECTION_NAME);
    }

    /**
     * Receives the document modelling a flight. If the flight is already present, it updates its info
     *
     * @param flightUpdate
     */
    public void update(FlightUpdate flightUpdate) {
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
