package network;

import property.Machine;

import java.io.*;
import java.net.Socket;

public class Node {

    public static final String ADDRESS = "127.0.0.1";
    public static final int PORT = 8080;

    private final Socket    socket;
    private final In        in;
    private final Out       out;

    public Node(Socket socket) {
        try {
            this.socket = socket;
            this.out    = new Out(socket);
            this.in     = new In(socket);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Node() {
        try {
            this.socket = new Socket(ADDRESS, PORT);
            this.out    = new Out(socket);
            this.in     = new In(socket);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    class In implements Machine {

        private final InputStream in;
        byte[] buffer = new byte[32];
        int bytesRead;

        protected In(Socket socket) throws IOException {
            this.in = socket.getInputStream();
            this.start(64);
        }

        @Override
        public void process() {
            if (!socket.isClosed()) {
                try {
                    if ((bytesRead = in.read(buffer)) != -1) {
                        String receivedData = new String(buffer, 0, bytesRead);
                        System.out.println("NODE IN:\t" + receivedData);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    class Out implements Machine {

        private final OutputStream out;

        protected Out(Socket socket) throws IOException {
            this.out = socket.getOutputStream();
            this.start(4);
        }

        @Override
        public void process() {
            if (!socket.isClosed()) {
                System.out.println("NODE OUT:\tHello from the big server!");
                try {
                    byte[] message = "Hello from the big server!".getBytes();
                    out.write(message);
                    out.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

}