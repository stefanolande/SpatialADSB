package it.unica.bd2.fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
        primaryStage.setScene(new Scene(root, 300, 275));

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("interface.fxml"));
        fxmlLoader.load();

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
