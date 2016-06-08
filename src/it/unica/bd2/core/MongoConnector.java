package it.unica.bd2.core;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import it.unica.bd2.model.FlightUpdate;
import org.bson.Document;

import javax.print.Doc;
import java.util.List;


/**
 * Created by stefano on 08/06/16.
 */
public class MongoConnector {
    private MongoClient mongoClient;
    private MongoDatabase db;
    private MongoCollection<Document> flightsCollection;

    public void connect() {
        mongoClient = new MongoClient();
        db = mongoClient.getDatabase("flights");
        flightsCollection = db.getCollection("flights");
    }

    /**
     * Receives the document modellign a flight. If the flight is alreay present, it updates its info
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
            flightsCollection.updateOne(new Document("flightID", flightID),
                    new Document("$set", flightUpdate.getDocumentWithoutPoint().append("points", oldPoints)));

            //check if we have to push a new point
            if (flightUpdate.getPoint() != null) {
                flightsCollection.updateOne(new Document("flightID", flightID),
                        new Document("$push", new Document("points", flightUpdate.getPointDocument())));
            }
        } else {
            //we need to create a new document
            flightsCollection.insertOne(flightUpdate.getDocument());
        }
    }
}
