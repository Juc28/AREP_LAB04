package edu.eci;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class ServerExecuter {
    public static void main(String[] args) {
        HttpServer httpServer = HttpServer.getInstance();
        if(httpServer != null){
            try {
                httpServer.start(args);
            } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
