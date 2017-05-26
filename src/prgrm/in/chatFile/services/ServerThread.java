package prgrm.in.chatFile.services;

import prgrm.in.chatFile.controller.IndexController;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by archit on 20/5/17.
 */
public class ServerThread extends Thread{
    private final IndexController indexController;
    private final int portNumber;
    private ServerSocket listener;
    private int totalConnection=0;
    private Map<String,ServerClientHandler> map;
    public ServerThread(IndexController indexController, int portNumber){
        this.indexController=indexController;
        this.portNumber=portNumber;
        this.map=new HashMap<String,ServerClientHandler>();
    }

    @Override
    public void run(){
        try{
            listener=new ServerSocket(this.portNumber);
            indexController.addMessageToLog("Server running at localhost:"+portNumber);
            while (true){
                Socket client=listener.accept();
                totalConnection++;
                String clientId="client"+totalConnection;
                indexController.addMessageToLog("Client connected: "+clientId);

                ServerClientHandler clientHandler=new ServerClientHandler(clientId,client,indexController,this);
                clientHandler.start();
                this.map.put(clientId,clientHandler);

                //Getting List of Active Id's and broadcasting it to every of them
                List<String> activeIds=new ArrayList<String>();
                for(String id:map.keySet()){
                    try{
                        ServerClientHandler data=map.get(id);
                        if(data.socket.isClosed()||!data.socket.isConnected()){
                            map.remove(id);
                            continue;
                        }
                        activeIds.add(id);
                    }
                    catch (Exception e){

                    }
                }
                String activeid=String.join(",", activeIds);

                sendMessage(clientId,"ID:"+clientId); //Sending the Id
                broadCastMessage("ACTIVE:"+activeid); //Sending information about all active connection

            }
        }
        catch(BindException e){
            indexController.addMessageToLog("ERROR: [PORT ALREADY IN USE]");
        }
        catch (Exception e){
            indexController.addMessageToLog("ERROR: [SOME I/O ERROR OCCURED]");
        }
        finally{
            try {
                System.out.println("Closing");
                listener.close();
                indexController.addMessageToLog("Server Closed");
            } catch (IOException e) {
            }
        }
    }

    public void broadCastMessage(String message){
        for(String id:map.keySet()){
            try {
                ServerClientHandler serverClientHandler=map.get(id);
                serverClientHandler.sendMessage(message);
            }
            catch (Exception e){

            }
        }
    }

    public void sendMessage(String clientId,String message){
        map.get(clientId).sendMessage(message);
    }
    public void sendFile(String clientId,String fn){
        map.get(clientId).sendFile(fn);
    }
}


