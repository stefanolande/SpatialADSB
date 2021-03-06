package it.unica.bd2.core;

/**
 * Created by stefano on 09/06/16.
 */
public class Settings {
    //costanti relative al ricevitore
    public static final String ADSB_SERVER_IP = "78.15.189.41";
    public static final int ADSB_SERVER_PORT = 443;

    //costanti relative a MONGO DB
    public static final String MONGO_SERVER_IP = "127.0.0.1";
    public static final int MONGO_SERVER_PORT = 27017;
    public static final String MONGO_DB_NAME = "flightsDB";
    public static final String MONGO_COLLECTION_NAME = "flightsCollection";


    //costanti relative a POSTGIS; L'user e pwd sono relative alla
    //mia instanza di DB, nel caso dovreste modificarle se sono diversi nei vostri
    public static final String POSTGIS_SERVER_IP = "127.0.0.1";
    public static final int POSTGIS_SERVER_PORT = 5432;
    public static final String POSTGIS_DB_NAME = "flightsDB";
    public static final String POSTGIS_DB_URL = "jdbc:postgresql://" + POSTGIS_SERVER_IP + ":" + POSTGIS_SERVER_PORT + "/" + POSTGIS_DB_NAME;
    public static final int MIN_TRACK_SIZE = 100;
    public static final String POSTGIS_DB_USERNAME = "postgres";
    public static final String POSTGIS_DB_PASSWORD = "cavallo";

    //costanti relative alle operazioni spaziali
    public static final int DISTANCE = 5000;


    /*    CREATE TABLE public.stats
            (
                    id smallint NOT NULL PRIMARY KEY,
                    "timestamp" bigint
    )*/

    /*
    *   CREATE TABLE flights
        (
          "flightid" character varying(10) NOT NULL,
          track geometry NOT NULL,
          CONSTRAINT simple_pl PRIMARY KEY (flightid)
        )
     */
}
