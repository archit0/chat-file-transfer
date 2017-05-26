package prgrm.in.chatFile.services;

import prgrm.in.chatFile.controller.IndexController;

import java.io.*;
import java.net.Socket;

/**
 * Created by archit on 20/5/17.
 */
class ServerClientHandler extends Thread {
    Socket socket;
    private IndexController indexController;
    private ServerThread serverThread;
    String clientId;
    ObjectInputStream in;
    ObjectOutputStream out;

    public ServerClientHandler(String clientId, Socket socket, IndexController indexController, ServerThread serverThread) {
        try {
            this.clientId = clientId;
            this.socket = socket;
            out = new ObjectOutputStream(socket.getOutputStream());
            this.indexController = indexController;
            this.serverThread = serverThread;
        } catch (Exception e) {

        }
    }

    public void sendMessage(String message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (Exception e) {
        }
    }
    public void sendFile(String fn){
        try {
            System.out.println("SERVER UPLOADING STARTED");
            File transferFile = new File("server/"+fn);
            FileInputStream fin = new FileInputStream(transferFile);
            BufferedInputStream bin = new BufferedInputStream(fin);
            int count;
            byte[] buffer = new byte[8192];
            while ((count = bin.read(buffer)) > 0) {
                System.out.println("SERVER UPLOADING IN PROGRESS");
                out.write(buffer, 0, count);
            }
            out.flush();
        }
        catch (Exception e){
            System.out.println("SERVER UPLOADING ERROR");
            e.printStackTrace();
        }
        System.out.println("SERVER UPLOADING FINISH");
    }

    @Override
    public void run() {
        try {
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String fn="";
        String message="";
        String rec="";
        long fs=0;
        boolean flag=false;
        while (true) {
            try {
                System.out.println("Sensing...");

                if(flag){

                        System.out.println("SERVER DOWNLOADING STARTED of " + fn);
                        try {
                            FileOutputStream fileWrite = new FileOutputStream("server/"+fn);
                            byte[] bytes = new byte[16 * 1024];
                            int count;
                            long tc=0;
                            while ((count = in.read(bytes)) > 0) {
                                try {
                                    tc+=count;

                                    System.out.println("SERVER DOWNLOADING IN PROGRESS "+count);
                                    fileWrite.write(bytes, 0, count);
                                    if(tc>=fs)
                                        break;
                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                }
                            }

                            fileWrite.close();
                        } catch (Exception e) {
                            System.out.println("SERVER DOWNLOADING ERROR");
                            e.printStackTrace();
                        }
                        System.out.println("SERVER DOWNLOADING FINISH");

                    serverThread.sendMessage(rec, "FILE@" + clientId + ":" + message);


                    serverThread.sendFile(rec,fn);
                    flag=false;
                }
                else {
                    String messageFromClient = ((String) in.readObject());
                    indexController.addMessageToLog("Message from " + clientId + ": " + messageFromClient);

                    int spaceIndex = messageFromClient.indexOf(":");

                    String sp[] = messageFromClient.substring(0, spaceIndex).split("@");

                    String type = sp[0];
                    rec = sp[1];
                    message = messageFromClient.substring(spaceIndex + 1);

                    if (type.equals("TEXT")) {
                        serverThread.sendMessage(rec, "TEXT@" + clientId + ":" + message);
                    } else if (type.equals("FILE")) {
                        flag = true;
                        int index = message.indexOf("@");
                        fn = message.substring(0, index);
                        fs=Long.parseLong(message.substring(index+1));

                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
