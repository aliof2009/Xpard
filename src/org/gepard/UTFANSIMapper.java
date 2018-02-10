package org.gepard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import org.gepard.common.InvalidFASTAFileException;

public class UTFANSIMapper {

    public static final HashMap<String, Byte> UTF_TO_ANSI_MAP = new HashMap<>();
    public static final HashMap<Byte, String> ANSI_TO_UTF_MAP = new HashMap<>();

    public static byte[] UTFtoANSIConverterFASTA(String input) {
        byte[] output = new byte[input.length()];
        for (int i = 0; i < input.length(); i++) {
            if (UTF_TO_ANSI_MAP.containsKey(input.charAt(i) + "")) {
                output[i] = UTF_TO_ANSI_MAP.get(input.charAt(i) + "");
            } else {
                output[i] = 10;
            }
        }
        return output;
    }

    public static byte[] UTFtoANSIConverterMatrix(String input) {
        byte[] output = new byte[input.length()];
        for (int i = 0; i < input.length(); i++) {
            if (UTF_TO_ANSI_MAP.containsKey(input.charAt(i) + "")) {
                output[i] = UTF_TO_ANSI_MAP.get(input.charAt(i) + "");
            } else {
                output[i] = (byte) input.charAt(i);
            }
        }
        return output;
    }

    //warn:0x0X, 0x20, 0x23
    public static String UTFtoANSIMatrixCreator(String curLine, BufferedReader br) throws IOException {
        if ((byte) curLine.charAt(0) == -1) {
            curLine = curLine.substring(1);
        }
        if (curLine.startsWith("/*")) {
            while (!(curLine = new String(br.readLine().getBytes(),"UTF-8")).startsWith("*/")) System.out.println(curLine);
            curLine = br.readLine();
        }
        char[] chararr = curLine.replace("\t", "").toCharArray();
        byte i = (byte) 0x10;
        for (int j = 0; j < chararr.length; j++) {
            if (chararr[j] == -1 && j < 3) {
            } else {
                if (i >= 0x00 && i < 0x10) {
                    i = 0x10;
                } else if (i == 0x20 || i == 0x23) {
                    i++;
                }
                UTF_TO_ANSI_MAP.put(chararr[j] + "", i++);
                System.out.println((byte) chararr[j] + " >> " + i);
            }
        }
        for (String key : UTF_TO_ANSI_MAP.keySet()) {
            ANSI_TO_UTF_MAP.put(UTF_TO_ANSI_MAP.get(key), key);
        }
        return curLine;
    }

    public static String ANSItoUTFConverter(char[] input, int offset, int count) {
        String output = new String();
        for (int i = offset; i < count; i++) {
            if (ANSI_TO_UTF_MAP.containsKey((byte) input[i])) {
                output += ANSI_TO_UTF_MAP.get((byte) input[i]);
            } else {
                output += " ";
            }
        }
        return output;
    }

    public static void main(String[] args) throws UnsupportedEncodingException, InvalidFASTAFileException, IOException {
//        byte[] v1 = UTFtoANSIConverterFASTA("ضصثقفغعهخحجچشسیبلا تنمکگظطزرذد پوژ");
//        char[] v3 = new char[v1.length];
//        for (int i = 0; i < v1.length; i++) {
//            v3[i] = (char) v1[i];
//        }
//        String v2 = ANSItoUTFConverter(v3,0,v1.length);
//        System.out.println(Arrays.toString(v1));
//        System.out.println(v2);
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        String file = "C:\\Users\\110\\Documents\\NetBeansProjects\\Xgepard\\src\\resources\\matrices\\langidentity.mat";
//
//        File f = new File(file);
//        int filelen = (int) f.length();
//        DataInputStream in = new DataInputStream(new FileInputStream(file));
//        byte[] contentsUTF = new byte[filelen];
//        in.readFully(contentsUTF, 0, filelen);
//        in.close();
//
//        // UTF to ANSI
//        byte[] contents = UTFANSIMapper.UTFtoANSIConverterMatrix(new String(contentsUTF, "UTF-8"));
//        
//        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(contents)));
//        String curLine;
//        while ((curLine = br.readLine()) != null) {
//            System.out.println(curLine);
//        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        for (String key : UTF_TO_ANSI_MAP.keySet()) {
//            System.out.println(key+":"+(char) UTF_TO_ANSI_MAP.get(key).byteValue());
//        }
//
//        for (char key : ANSI_TO_UTF_MAP.keySet()) {
//            System.out.println(key+":"+ ANSI_TO_UTF_MAP.get(key));
//        }
    }
}
