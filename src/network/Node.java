package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

public class Node {

    private final Socket socket;
    private final In in;
    private final Out out;

    public final HashMap<Integer, State> states;
    public State state;

    public Node() {
        try {
            this.states = new HashMap<>();
            this.state = new State();
            this.socket = new Socket(Relay.ADDRESS, Relay.PORT);
            this.out = new Out(socket);
            this.in = new In(socket);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    class In extends Thread {
        private final ObjectInputStream in;
        protected In(Socket socket) throws IOException {
            this.in = new ObjectInputStream(socket.getInputStream());
            this.start();
        }
        @Override
        public void run() {
            while (!socket.isClosed() && isAlive()) {
                try {
                    Object object = in.readObject();
                    System.out.println(this.getId() + " NODE: IN");
                    if (object instanceof network.State state) {
                        states.put(state.id, state);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    class Out extends Thread {
        private final ObjectOutputStream out;
        protected Out(Socket socket) throws IOException {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.start();
        }
        @Override
        public void run() {
            while (!socket.isClosed() && isAlive()) {
                try {
                    System.out.println(this.getId() + " NODE: OUT");
                    out.writeObject(state);
                    out.flush();
                    Thread.sleep(16);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}