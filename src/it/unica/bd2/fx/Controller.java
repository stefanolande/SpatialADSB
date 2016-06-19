package it.unica.bd2.fx;

import it.unica.bd2.core.ADSBClient;
import it.unica.bd2.core.PostGIS;
import it.unica.bd2.model.Comune;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;

public class Controller {

    @FXML
    private ComboBox comuniChoice;
    @FXML
    private Button comuneQueryButton;
    @FXML
    private Button localQueryButton;
    @FXML
    private Text localQueryLabel;
    @FXML
    private TextField latText;
    @FXML
    private TextField lonText;
    @FXML
    private Button syncButton;
    @FXML
    private Button globalQueryButton;
    @FXML
    private TableView comuniTable;
    @FXML
    private TableColumn<Comune, String> nomeColumn;
    @FXML
    private TableColumn<Comune, String> sorvoliColumn;
    @FXML
    private Text adsbStatusString;
    @FXML
    private Button adsbButton;

    private ObservableList<Comune> listaComuni = FXCollections.observableArrayList();


    private Main mainApp;

    @FXML
    private void initialize() {
        nomeColumn.setCellValueFactory(cellData -> cellData.getValue().nomeProperty());
        sorvoliColumn.setCellValueFactory(cellData -> cellData.getValue().sorvoliProperty());
    }

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
        new Thread() {
            @Override
            public void run() {
                Platform.runLater(() -> syncButton.setText("Syncing..."));
                PostGIS postGIS = PostGIS.getInstance();
                postGIS.connect();
                postGIS.sync();
                postGIS.disconnect();
                Platform.runLater(() -> syncButton.setText("Sync data"));
            }
        }.start();

    }

    public void destroy() {
        ADSBClient.getInstance().disconnect();

    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    public void globalQuery(ActionEvent event) {
        new Thread() {
            @Override
            public void run() {
                PostGIS postGIS = PostGIS.getInstance();
                postGIS.connect();

                Platform.runLater(() -> globalQueryButton.setText("Querying..."));

                listaComuni = postGIS.globalQuery();

                Platform.runLater(() -> globalQueryButton.setText("Query"));

                postGIS.disconnect();
                comuniTable.setItems(listaComuni);
            }
        }.start();

    }


    public void localQuery(ActionEvent actionEvent) {

        latText.setStyle("");
        lonText.setStyle("");

        String latString = latText.getText();
        String lonString = lonText.getText();
        double lat, lon;

        try {
            lat = new Double(latString);
        } catch (NumberFormatException e) {
            latText.setStyle("-fx-background-color: yellow");
            return;
        }

        try {
            lon = new Double(lonString);
        } catch (NumberFormatException e) {
            lonText.setStyle("-fx-background-color: yellow");
            return;
        }

        new Thread() {
            @Override
            public void run() {
                PostGIS postGIS = PostGIS.getInstance();
                postGIS.connect();

                Platform.runLater(() -> localQueryButton.setText("Querying..."));

                int sorvoli = postGIS.localQuery(lat, lon);

                Platform.runLater(() -> localQueryButton.setText("Query"));

                postGIS.disconnect();
                localQueryLabel.setText("Numero di sorvoli: " + sorvoli);
            }
        }.start();
    }

    public void comuneQuery(ActionEvent actionEvent) {
        String comune = (String) comuniChoice.getValue();

        new Thread() {
            @Override
            public void run() {
                PostGIS postGIS = PostGIS.getInstance();
                postGIS.connect();

                Platform.runLater(() -> comuneQueryButton.setText("Querying..."));

                int sorvoli = postGIS.localQuery(comune);

                Platform.runLater(() -> comuneQueryButton.setText("Query"));

                postGIS.disconnect();
                localQueryLabel.setText("Numero di sorvoli: " + sorvoli);
            }
        }.start();
    }
}
