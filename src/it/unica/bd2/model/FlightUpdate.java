package it.unica.bd2.model;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stefano on 08/06/16.
 */
public class FlightUpdate {
    private long flightID;
    private String hexIdent;
    private String callsing;
    private Point point;

    public FlightUpdate() {
    }

    public FlightUpdate(long flightID, String callsing, String hexIdent, Point point) {
        this.flightID = flightID;
        this.callsing = callsing;
        this.hexIdent = hexIdent;
        this.point = point;
    }

    public Document getDocumentWithoutPoint() {
        Document document = new Document("flightID", flightID).append("hexIdent", hexIdent);

        if (callsing != null) {
            if (!callsing.equals("")) {
                document.append("callsign", callsing);
            }
        }

        document.append("points", new ArrayList<Document>());

        return document;
    }

    public Document getPointDocument() {
        return new Document("altitude", point.altitude)
                .append("latitude", point.latitude)
                .append("longitude", point.longitude)
                .append("timestamp", point.timestamp);
    }

    public Document getDocument() {
        Document document = getDocumentWithoutPoint();
        if (point != null) {
            List<Document> points = new ArrayList<>();
            points.add(getPointDocument());
            document.append("points", points);
        } else {
            document.append("points", new ArrayList<Document>());
        }

        return document;
    }

    public long getFlightID() {
        return flightID;
    }

    public void setFlightID(long flightID) {
        this.flightID = flightID;
    }

    public String getCallsing() {
        return callsing;
    }

    public void setCallsing(String callsing) {
        this.callsing = callsing;
    }

    public String getHexIdent() {
        return hexIdent;
    }

    public void setHexIdent(String hexIdent) {
        this.hexIdent = hexIdent;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }
}
