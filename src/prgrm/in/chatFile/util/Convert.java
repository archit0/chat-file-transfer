package prgrm.in.chatFile.util;

/**
 * Created by archit on 20/5/17.
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
public class Convert{

    public static void main(String []args) throws FileNotFoundException {
        File file = new File("input.txt");
        Scanner sc = new Scanner(file);
        String word=sc.nextLine();
        String receivedBinary=stringToBinary(word);
        System.out.println();
        String receivedWord=BinaryToString(receivedBinary);
    }
    public static String stringToBinary(String text)
    {
        String bString="";
        String temp="";
        for(int i=0;i<text.length();i++)
        {
            temp=Integer.toBinaryString(text.charAt(i));
            for(int j=temp.length();j<8;j++)
            {
                temp="0"+temp;
            }
            bString+=temp+" ";
        }

        System.out.println(bString);
        return bString;
    }
    public static String BinaryToString(String binaryCode)
    {
        String[] code = binaryCode.split(" ");
        String word="";
        for(int i=0;i<code.length;i++)
        {
            word+= (char)Integer.parseInt(code[i],2);
        }
        System.out.println(word);
        return word;
    }
}

