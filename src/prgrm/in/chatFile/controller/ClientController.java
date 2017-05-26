package prgrm.in.chatFile.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import prgrm.in.chatFile.Starter;
import prgrm.in.chatFile.services.ClientThread;
import prgrm.in.chatFile.util.MyUtils;

import java.awt.*;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.RunnableFuture;

/**
 * Created by archit on 20/5/17.
 */
public class ClientController implements Initializable {
    @FXML
    ListView<String> CONNECTIONS,FILESD;
    @FXML
    TextArea MESSAGE;
    @FXML
    Label CONID;
    @FXML
    TextField TEXT;
    int PORT;

    Socket socket;

    public Map<String, List<String>> map;
    public Map<String, List<String>> fileMap;

    ClientThread cthread;

    public void setPORT(int PORT) {
        this.PORT = PORT;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            setPORT(Starter.PORT);

            cthread = new ClientThread(this.PORT, this);
            cthread.start();

            map = new HashMap<String, List<String>>();
            fileMap = new HashMap<String, List<String>>();
        } catch (Exception e) {
        }
    }

    String ID;

    public void setID(String id) {
        this.ID = id;
        Platform.runLater(new Runnable() {
            public void run() {
                CONID.setText("CONNECTION ID: " + id);
            }
        });
    }

    public void updateList() {
        Set<String> ar = map.keySet();
        ar.remove(this.ID);
        Platform.runLater(new Runnable() {
            public void run() {
                ObservableList<String> items = FXCollections.observableArrayList();
                items.addAll(ar);
                CONNECTIONS.setItems(items);
            }
        });
    }
    public void OPENFILE(){
        try {
            Desktop.getDesktop().open(new File("client/"+FILESD.getSelectionModel().getSelectedItem()));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public void SELECTED() {
        update();
        fileUpdate();
    }

    public void update() {
        Platform.runLater(new Runnable() {
            public void run() {
                try {
                    String t = CONNECTIONS.getSelectionModel().getSelectedItem();
                    List<String> list = map.get(t);
                    String message = "";
                    for (String x : list) {
                        message += x + "\n\n";
                    }
                    message = message.trim();
                    MESSAGE.setText(message);
                    MESSAGE.setScrollTop(Double.MAX_VALUE);

                } catch (Exception e) {

                }
            }
        });
    }


    public void fileUpdate() {

        Platform.runLater(new Runnable() {
            public void run() {
                try {
                    String t = CONNECTIONS.getSelectionModel().getSelectedItem();
                    List<String> list = fileMap.get(t);


                    ObservableList<String> items = FXCollections.observableArrayList();
                    items.addAll(list);
                    FILESD.setItems(items);

                } catch (Exception e) {

                }
            }
        });
    }

    public void SENDFILE() {
        String id = CONNECTIONS.getSelectionModel().getSelectedItem();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File f = fileChooser.showOpenDialog(MESSAGE.getScene().getWindow());
        String message = "FILE@" + id + ":" + f.getName()+"@"+f.getTotalSpace();
        cthread.sendMessage(message);
        cthread.sendFile(f.getPath());
        fileUpdate();
    }

    public void SEND_MESSAGE() {
        String t = TEXT.getText();
        String id = CONNECTIONS.getSelectionModel().getSelectedItem();
        String message = "TEXT@" + id + ":" + t;

        List<String> mapId = map.get(id);
        mapId.add("ME: " + t);
        map.put(id, mapId);

        update();
        cthread.sendMessage(message);


    }
}
