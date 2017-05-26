package prgrm.in.chatFile.services;

import prgrm.in.chatFile.controller.ClientController;
import prgrm.in.chatFile.controller.IndexController;
import prgrm.in.chatFile.util.MyUtils;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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
    public void sendFile(String filePath){
        try {
            System.out.println("CLIENT UPLOADING STARTED");
            File transferFile = new File(filePath);
            FileInputStream fin = new FileInputStream(transferFile);
            BufferedInputStream bin = new BufferedInputStream(fin);
            int count;
            byte[] buffer = new byte[8192];
            while ((count = bin.read(buffer)) > 0) {
                System.out.println("CLIENT UPLOADING IN PROGRESS");
                out.write(buffer, 0, count);
            }
            out.flush();
            sendMessage("Ping");
            sendMessage("Ping");
        }
        catch (Exception e){
            System.out.println("CLIENT UPLOADING ERROR");
            e.printStackTrace();
        }
        System.out.println("CLIENT UPLOADING FINISH");
    }
    @Override
    public void run(){
        try {
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String fn="";
        boolean flag=false;
        while(true){
            try{
                if(flag){
                    System.out.println("CLIENT DOWNLOADING STARTED of "+fn);
                    try {
                        FileOutputStream fileWrite = new FileOutputStream("client/"+fn);
                        byte[] bytes = new byte[16*1024];
                        int count;
                        while ((count = in.read(bytes)) > 0) {
                            System.out.println("CLIENT DOWNLOADING IN PROGRESS");
                            fileWrite.write(bytes, 0, count);
                        }
                        fileWrite.close();
                    }
                    catch (Exception e){
                        System.out.println("CLIENT DOWNLOADING ERROR");
                        e.printStackTrace();
                    }
                    System.out.println("CLIENT DOWNLOADING FINISH");
                    flag=false;
                }else {
                    String messageFromServer = ((String) in.readObject());
                    int spaceIndex = messageFromServer.indexOf(":");
                    String type = messageFromServer.substring(0, spaceIndex);
                    String content = messageFromServer.substring(spaceIndex + 1);
                    if (type.startsWith("ID")) {
                        this.clientController.setID(content);
                    } else if (type.startsWith("ACTIVE")) {
                        String connections[] = content.split(",");
                        for (String x : connections) {
                            if (!clientController.map.containsKey(x)) {
                                clientController.map.put(x, new ArrayList<String>());
                                clientController.fileMap.put(x, new ArrayList<String>());
                            }
                        }
                        clientController.updateList();
                    } else if (type.startsWith("TEXT")) {
                        String typeSp[] = type.split("@");
                        List<String> previousMessage = clientController.map.get(typeSp[1]);
                        previousMessage.add(typeSp[1] + ": " + content);
                        clientController.map.put(typeSp[1], previousMessage);
                        this.clientController.update();
                    } else if (type.startsWith("FILE")) {
                        System.out.println("GOT A FILE");
                        String typeSp[] = type.split("@");

                        int index = content.indexOf("@");
                        fn = content.substring(0, index);
                        String fileSize = content.substring(index + 1);
                        flag = true;

                        List<String> previousFiles = clientController.fileMap.get(typeSp[1]);
                        previousFiles.add(fn);
                        clientController.fileMap.put(typeSp[1], previousFiles);

                        clientController.fileUpdate();

                    }
                }



            }
            catch (Exception e){

            }
        }
    }

}
