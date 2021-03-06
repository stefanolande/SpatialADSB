package it.unica.bd2.core;

import com.mongodb.MongoInterruptedException;
import it.unica.bd2.model.FlightUpdate;
import it.unica.bd2.model.Point;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;


/**
 * Created by stefano on 08/06/16.
 */
public class ADSBClient {
    private static ADSBClient instance;
    private boolean connected = false;
    private MongoConnector mongoConnector;
    private Thread receptionThread;

    private ADSBClient() {

    }

    public static ADSBClient getInstance() {
        if (instance == null) {
            instance = new ADSBClient();
        }

        return instance;
    }

    public void connect() {
        try {
            Socket socket = new Socket(Settings.ADSB_SERVER_IP, Settings.ADSB_SERVER_PORT);
            this.connected = true;

            mongoConnector = MongoConnector.getInstance();
            mongoConnector.connect();

            receive(socket);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (receptionThread != null) {
            receptionThread.interrupt();

        }
        if (mongoConnector != null) {
            mongoConnector.disconnect();
        }

        this.connected = false;
    }

    public boolean isConnected() {
        return connected;
    }

    /**
     * Start a reception cycle in a thread
     */
    private void receive(Socket socket) {
        this.receptionThread = new Thread() {
            @Override
            public void run() {

                try (InputStream inputStream = socket.getInputStream();
                     BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {

                    while (connected) {
                        //read a new adsb message and split its fields
                        String line = bufferedReader.readLine();
                        String[] fields = line.split(",");

                        try {
                            String messageType = fields[0];
                            String transmissionType = fields[1];

                            //we are only interested in MSG1 (Identification) a
                            // nd MSG3 messages (Airborne Position Message)
                            if (messageType.equals("MSG") && (transmissionType.equals("1") || transmissionType.equals("3"))) {

                                FlightUpdate flightUpdate = new FlightUpdate();
                                flightUpdate.setHexIdent(fields[4]);
                                flightUpdate.setFlightID(Long.parseLong(fields[5]));


                                //add callsign if present
                                if (transmissionType.equals("1")) {
                                    flightUpdate.setCallsing(fields[10]);


                                } else if (transmissionType.equals("3")) { //add new point if the position is present
                                    int altitude = Integer.parseInt(fields[11]);
                                    double latitude = Double.parseDouble(fields[14]);
                                    double longitude = Double.parseDouble(fields[15]);
                                    long timestamp = System.currentTimeMillis() / 1000L;

                                    flightUpdate.setPoint(new Point(altitude, latitude, longitude, timestamp));
                                }

                                mongoConnector.update(flightUpdate);

                            }

                        } catch (ArrayIndexOutOfBoundsException e) {
                            //mah
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }

        };

        receptionThread.setDaemon(true);

        try {
            receptionThread.start();
        } catch (MongoInterruptedException e) {

        }
    }

}
