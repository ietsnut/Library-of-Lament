package network;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {

    public static final int PORT = 8080;

    public static List<Node> nodes = new ArrayList<>();

    public Server() {
        start();
    }

    @Override
    public void run() {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("SERVER:\t" + PORT);
            while (!server.isClosed()) {
                Socket socket = server.accept();
                System.out.println("SERVER:\tNEW NODE");
                nodes.add(new Node(socket));
            }
        } catch (IOException e) {
            System.out.println("EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
