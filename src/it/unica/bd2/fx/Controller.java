package it.unica.bd2.fx;

import it.unica.bd2.core.ADSBClient;
import it.unica.bd2.core.PostGIS;
import it.unica.bd2.model.Comune;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;

public class Controller {

    @FXML
    private Button syncButton;
    @FXML
    private Button queryButton;
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
        listaComuni = postGIS.query();
        queryButton.setText("Query");
        postGIS.disconnect();

        comuniTable.setItems(listaComuni);

    }
}
