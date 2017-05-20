package prgrm.in.chatFile.controller;

import com.sun.corba.se.spi.activation.Server;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import prgrm.in.chatFile.Starter;
import prgrm.in.chatFile.services.ServerThread;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by archit on 20/5/17.
 */
public class IndexController implements Initializable {
    @FXML
    TextField PORT;
    @FXML
    TextArea LOGGER;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
    public void START_SERVER(){
        try {
            clearLog();
            int port = Integer.parseInt(PORT.getText());
            addMessageToLog("Starting Server at Port: " + port);
            new ServerThread(this,port).start();
        }
        catch (NumberFormatException e1){
            addMessageToLog("ERROR: [Invalid Port Number: "+PORT.getText()+"]");

        }
    }
    public void START_CLIENT(){
        try {
            Starter.PORT= Integer.parseInt(PORT.getText());

            Stage dialog = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/prgrm/in/chatFile/ui/client.fxml"));
            Parent root = (Parent)fxmlLoader.load();

            Scene scene = new Scene(root);
            dialog.setScene(scene);
            dialog.initOwner(PORT.getScene().getWindow());
            dialog.show();
        }
        catch (Exception e){

        }
    }

    public void addMessageToLog(String message){
        Platform.runLater(new Runnable() {
            public void run() {
                LOGGER.setText((LOGGER.getText().trim()+"\n"+message).trim());
                LOGGER.setScrollTop(Double.MAX_VALUE);
            }
        });

    }
    public void clearLog(){
        LOGGER.setText("");
    }

   }
