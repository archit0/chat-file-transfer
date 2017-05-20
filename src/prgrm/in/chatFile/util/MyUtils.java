package prgrm.in.chatFile.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by archit on 20/5/17.
 */
public class MyUtils {
    public static String readSmallBinaryFile(String aFileName) {
        try {
            Path path = Paths.get(aFileName);
            return new String(Files.readAllBytes(path));

        } catch (Exception e) {

        }
        return null;
    }

    public static void decode(String aFileName, String args) throws Exception {
        try {
            FileOutputStream fos = new FileOutputStream(aFileName);
            fos.write(args.getBytes());
            fos.close();

        } catch (Exception e) {

        }
    }

}
