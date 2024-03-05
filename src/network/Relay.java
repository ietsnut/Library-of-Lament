package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import network.State;

public class Relay extends Thread {

    public static final int PORT = 8888;
    public static final String ADDRESS = "localhost";

    private final ArrayList<Node> nodes;
    private final ServerSocket socket;

    public Relay() {
        try {
            this.socket = new ServerSocket(PORT);
            this.nodes = new ArrayList<>();
            this.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        System.out.println("RELAY: " + "STARTED");
        while (isAlive()) {
            try {
                Socket socket = this.socket.accept();
                System.out.println("RELAY: " + "ACCEPTED NODE");
                Node node = new Node(socket);
                nodes.add(node);
                node.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class Node extends Thread {

        protected final Socket socket;
        private final ObjectOutputStream out;
        private final ObjectInputStream in;

        public Node(Socket socket) throws IOException {
            this.socket = socket;
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
        }

        @Override
        public void run() {
            while (!socket.isClosed() && isAlive()) {
                try {
                    Object object = in.readObject();
                    if (object instanceof network.State state) {
                        state.id = nodes.indexOf(this);
                        System.out.println("RELAY: " + "INCOMING NODE STATE");
                        for (Node node : nodes) {
                            if (!node.socket.equals(this.socket)) {
                                out.writeObject(state);
                                out.flush();
                                System.out.println("RELAY: " + "RELAYING NODE STATE");
                            }
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}