package prgrm.in.chatFile.services;

import prgrm.in.chatFile.controller.ClientController;

import java.net.Socket;

/**
 * Created by archit on 20/5/17.
 */
public class ClientThread extends Thread {
    private String host;
    private int port;
    private  ClientController clientController;
    public ClientHandler clientHandler;
    Socket socket;

    public ClientThread(int port, String host,ClientController clientController){
        try {
            this.port=port;
            this.host=host;
            this.clientController = clientController;
        }
        catch (Exception e){

        }
    }
    @Override
    public void run(){
        try {
            socket = new Socket(this.host, port);
            clientHandler =new ClientHandler(socket,this,clientController);
            clientHandler.start();
        }
        catch (Exception e){

        }
    }
    public void sendMessage(String sm){
        clientHandler.sendMessage(sm);
    }

    public void sendFile(String filePath){
       clientHandler.sendFile(filePath);

    }
}
