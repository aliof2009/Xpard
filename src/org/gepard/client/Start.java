package org.gepard.client;

// startup class, does nothing but instantiating the controller

import java.lang.reflect.Field;
import java.nio.charset.Charset;


public class Start {

    public static void main(String[] args) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
//        System.setProperty("file.encoding", "UTF-8");
//        Field charset = Charset.class.getDeclaredField("defaultCharset");
//        charset.setAccessible(true);
//        charset.set(null, null);
        new Controller();
    }

}
