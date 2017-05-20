package prgrm.in.chatFile.services;

import prgrm.in.chatFile.controller.ClientController;
import prgrm.in.chatFile.controller.IndexController;
import prgrm.in.chatFile.util.MyUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by archit on 20/5/17.
 */
class ClientHandler extends Thread {
    ClientController clientController;
    Socket socket;

    private ClientThread clientThread;
    ObjectInputStream in;
    ObjectOutputStream out;

    public ClientHandler(Socket socket, ClientThread clientThread, ClientController clientController){
        try {
            this.socket=socket;
        this.clientController=clientController;
            out = new ObjectOutputStream(socket.getOutputStream());
            this.clientThread=clientThread;
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public void sendMessage(String message){
        try{
            out.writeObject(message);
            out.flush();
        }catch (Exception e){
        }
    }
    @Override
    public void run(){
        try {
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(true){
            try{

                String messageFromServer=((String)in.readObject());
                int spaceIndex=messageFromServer.indexOf(":");
                String type=messageFromServer.substring(0,spaceIndex);
                String content=messageFromServer.substring(spaceIndex+1);
                if(type.startsWith("ID")){
                    this.clientController.setID(content);
                }
                else if(type.startsWith("ACTIVE")){
                    String connections[]=content.split(",");
                    for(String x:connections){
                        if(!clientController.map.containsKey(x)){
                            clientController.map.put(x,new ArrayList<String>());
                            clientController.fileMap.put(x,new ArrayList<String>());
                        }
                    }
                    clientController.updateList();
                }
                else if(type.startsWith("TEXT")){
                    String typeSp[]=type.split("@");
                    List<String> previousMessage=clientController.map.get(typeSp[1]);
                    previousMessage.add(typeSp[1]+": "+content);
                    clientController.map.put(typeSp[1],previousMessage);
                    this.clientController.update();
                }
                else if(type.startsWith("FILE")){
                    String typeSp[]=type.split("@");

                    int index=content.indexOf("@");
                    String fn=content.substring(0,index);
                    String fileContent=content.substring(index+1);
                    MyUtils.decode(fn,fileContent);
                    List<String> previousFiles=clientController.fileMap.get(typeSp[1]);
                    previousFiles.add(fn);
                    clientController.fileMap.put(typeSp[1],previousFiles);

                    clientController.fileUpdate();

                }



            }
            catch (Exception e){

            }
        }
    }

}
