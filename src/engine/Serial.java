package engine;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import static resource.Resource.THREADS;

public class Serial implements Runnable {

    public static int[] states = new int[3];

    public static void listen() {
        Thread thread = new Thread(new Serial());
        thread.setName("Serial");
        //THREADS.submit(thread);
    }

    @Override
    public void run() {
        if (SerialPort.getCommPorts() == null || SerialPort.getCommPorts().length == 0) return;
        System.out.println(Arrays.toString(SerialPort.getCommPorts()));
        SerialPort comPort = SerialPort.getCommPorts()[0];
        if (comPort.openPort()) {
            System.out.println("Serial port opened: " + comPort.getSystemPortName());
            comPort.addDataListener(new SerialPortDataListener() {
                private final StringBuilder buffer = new StringBuilder();
                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                }
                @Override
                public void serialEvent(SerialPortEvent event) {
                    if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                        return;
                    byte[] newData = new byte[comPort.bytesAvailable()];
                    int numRead = comPort.readBytes(newData, newData.length);
                    String data = new String(newData, StandardCharsets.UTF_8);
                    buffer.append(data);
                    while (buffer.indexOf("\n") != -1) {
                        int newlineIndex = buffer.indexOf("\n");
                        String line = buffer.substring(0, newlineIndex).trim();
                        buffer.delete(0, newlineIndex + 1);
                        try {
                            String[] parts = line.split(" ");
                            if (parts.length == 3) {
                                states[0] = Integer.parseInt(parts[0]);
                                states[1] = Integer.parseInt(parts[1]);
                                states[2] = Integer.parseInt(parts[2]);
                                System.out.println(Arrays.toString(states));
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Failed to parse line: " + line);
                        }
                    }
                }
            });
        } else {
            System.err.println("Failed to open serial port.");
        }
    }
}