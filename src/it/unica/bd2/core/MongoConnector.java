package it.unica.bd2.core;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;


/**
 * Created by stefano on 08/06/16.
 */
public class MongoConnector {
    private MongoClient mongoClient;
    private MongoDatabase db;
    private MongoCollection<Document> flightsCollection;

    public void connect(){
        mongoClient = new MongoClient();
        db = mongoClient.getDatabase("flights");
        flightsCollection = db.getCollection("flights");
    }

    public void saveDocument(Document doc){
        if(flightsCollection != null){
            flightsCollection.insertOne(doc);
        }
    }
}
