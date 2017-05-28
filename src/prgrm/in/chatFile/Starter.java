package prgrm.in.chatFile;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by archit on 20/5/17.
 */
public class Starter extends Application {
    public static int PORT;
    public static String HOST;

    @Override
    public void start(Stage primaryStage) throws Exception {
        new File("client").mkdirs();
        new File("server").mkdirs();
        Parent root = FXMLLoader.load(getClass().getResource("/prgrm/in/chatFile/ui/Index.fxml"));
        primaryStage.setTitle("Chat Server");
        Scene sc = new Scene(root);
        sc.setFill(null);

        primaryStage.setScene(sc);

        primaryStage.show();
    }
    public static void main(String[] args){
            launch(args);
    }
}
