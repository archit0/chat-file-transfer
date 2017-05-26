package prgrm.in.chatFile.services;

import prgrm.in.chatFile.controller.IndexController;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by archit on 20/5/17.
 */
class ServerClientHandler extends Thread {
    Socket socket;
    private  IndexController indexController;
    private ServerThread serverThread;
    String clientId;
    ObjectInputStream in;
    ObjectOutputStream out;

    public ServerClientHandler(String clientId, Socket socket, IndexController indexController,ServerThread serverThread){
        try {
            this.clientId=clientId;
            this.socket=socket;
            out = new ObjectOutputStream(socket.getOutputStream());
            this.indexController=indexController;
            this.serverThread=serverThread;
        }
        catch (Exception e){

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
                String messageFromClient=((String)in.readObject());
                indexController.addMessageToLog("Message from "+clientId+": "+messageFromClient);

                int spaceIndex=messageFromClient.indexOf(":");

                String sp[]=messageFromClient.substring(0,spaceIndex).split("@");

                String type=sp[0];
                String rec=sp[1];
                String message=messageFromClient.substring(spaceIndex+1);
                if(type.equals("TEXT")){
                    serverThread.sendMessage(rec,"TEXT@"+clientId+":"+message);
                }
                else if(type.equals("FILE")){
                    serverThread.sendMessage(rec,"FILE@"+clientId+":"+message);

                    int index = message.indexOf("@");
                    String fn = message.substring(0, index);
                    System.out.println("DOWNLOADING STARTED of "+fn);
                    try {
                        FileOutputStream fileWrite = new FileOutputStream(fn);
                        byte[] bytes = new byte[16*1024];
                        int count;
                        while ((count = in.read(bytes)) > 0) {
                            System.out.println("DOWNLOADING IN PROGRESS");
                            fileWrite.write(bytes, 0, count);
                        }
                        fileWrite.close();
                    }
                    catch (Exception e){
                        System.out.println("DOWNLOADING ERROR");
                        e.printStackTrace();
                    }
                    System.out.println("DOWNLOADING FINISH");

                }


            }
            catch (Exception e){

            }
        }
    }

}
