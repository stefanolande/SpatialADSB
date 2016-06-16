package it.unica.bd2.fx;

import it.unica.bd2.core.ADSBClient;
import it.unica.bd2.core.PostGIS;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

public class Controller {

    public Button syncButton;
    public Button queryButton;
    @FXML
    private Text adsbStatusString;
    @FXML
    private Button adsbButton;

    private Main mainApp;

    @FXML
    private void adsbButtonClick(ActionEvent event) {

        ADSBClient adsbClient = ADSBClient.getInstance();

        if (!adsbClient.isConnected()) {
            adsbClient.connect();
            adsbButton.setText("Disconnect ADSB");
            adsbStatusString.setText("ADSB Status: connected");
        } else {
            adsbClient.disconnect();
            adsbButton.setText("Connect ADSB");
            adsbStatusString.setText("ADSB Status: disconnected");
        }
    }

    @FXML
    private void sync(ActionEvent event) {
        PostGIS postGIS = PostGIS.getInstance();
        postGIS.connect();
        syncButton.setText("Syncing...");
        postGIS.sync();
        syncButton.setText("Sync");
        postGIS.disconnect();
    }

    public void destroy() {
        ADSBClient.getInstance().disconnect();

    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    public void query(ActionEvent event) {
        PostGIS postGIS = PostGIS.getInstance();
        postGIS.connect();
        queryButton.setText("Querying..");
        postGIS.query();
        queryButton.setText("Query");
        postGIS.disconnect();

    }
}
