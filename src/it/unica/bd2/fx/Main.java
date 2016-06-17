package it.unica.bd2.fx;

import it.unica.bd2.model.Comune;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class Main extends Application {

    private Controller controller;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("interface.fxml"));
        primaryStage.setTitle("SpatialADSB");
        primaryStage.setScene(new Scene(root, 400, 275));

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("interface.fxml"));
        fxmlLoader.load();

        //set the width of the table columns dynamically
        TableView comuniTable = (TableView) primaryStage.getScene().lookup("#comuniTable");

        TableColumn<Comune, String> nomeColumn = (TableColumn) comuniTable.getColumns().get(0);
        TableColumn<Comune, String> sorvoliColumn = (TableColumn) comuniTable.getColumns().get(1);

        nomeColumn.prefWidthProperty().bind(comuniTable.widthProperty().multiply(0.6));
        sorvoliColumn.prefWidthProperty().bind(comuniTable.widthProperty().multiply(0.40));


        // Give the controller access to the main app.
        controller = fxmlLoader.getController();
        controller.setMainApp(this);

        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        controller.destroy();
    }

}
