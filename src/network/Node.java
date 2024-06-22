package network;

import property.Machine;

import java.io.*;
import java.net.Socket;

public class Node {

    public static final String ADDRESS = "192.168.1.177";
    public static final int PORT = 80;

    private final Socket    socket;
    private final In        in;
    private final Out       out;

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
            this.start(32);
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
            this.start();
        }

        @Override
        public void process() {
            if (!socket.isClosed()) {
                System.out.println("NODE OUT:\ttest");
                try {
                    byte[] message = "a".getBytes();
                    out.write(message);
                    out.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

}