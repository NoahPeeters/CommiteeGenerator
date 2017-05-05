package to.us.peeters.committeegenerator;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Created by noah on 5/3/17.
 * The code belongs to it's creator. Please don't steal it!
 **/

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("mainView.fxml"));
        primaryStage.setTitle("Committee Generator");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

    static void askToExit() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Confirm Exit");
        alert.setHeaderText("Do you want to exit? All changes will be lost.");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES){
            Platform.exit();
        }
    }

    static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
