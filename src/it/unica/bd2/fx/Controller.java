package it.unica.bd2.fx;

import it.unica.bd2.core.ADSBClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

public class Controller {

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

    public void destroy() {
        ADSBClient.getInstance().disconnect();

    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }
}
