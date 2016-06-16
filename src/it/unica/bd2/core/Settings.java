package it.unica.bd2.core;

/**
 * Created by stefano on 09/06/16.
 */
public class Settings {
    public static final String ADSB_SERVER_IP = "78.15.189.41";
    public static final int ADSB_SERVER_PORT = 443;

    public static final String MONGO_SERVER_IP = "127.0.0.1";
    public static final int MONGO_SERVER_PORT = 27017;
    public static final String MONGO_DB_NAME = "flightsDB";
    public static final String MONGO_COLLECTION_NAME = "flightsCollection";

    public static final String POSTGIS_SERVER_IP = "127.0.0.1";
    public static final int POSTGIS_SERVER_PORT = 5432;
    public static final String POSTGIS_DB_NAME = "flightsDB";
    public static final String POSTGIS_DB_URL = "jdbc:postgresql://" + POSTGIS_SERVER_IP + ":" + POSTGIS_SERVER_PORT + "/" + POSTGIS_DB_NAME;
    public static String POSTGIS_DB_USERNAME = "postgres";
    public static String POSTGIS_DB_PASSWORD = "cavallo";
}
