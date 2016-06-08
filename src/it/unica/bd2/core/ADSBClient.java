package it.unica.bd2.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by stefano on 08/06/16.
 */
public class ADSBClient {
    public static final String SERVER = "78.15.189.41";
    public static final int PORT = 443;

    public void connect(){
        try(Socket socket = new Socket(SERVER, PORT)){

            InputStream inputStream = socket.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            while (true){
                String line = bufferedReader.readLine();
                System.out.println(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
