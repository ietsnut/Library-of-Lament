package engine;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Serial implements SerialPortDataListener {

    public static final byte[] IN  = new byte[] {0b0000, 0b0000, 0b0000, 0b0000};
    public static final byte[] OUT = new byte[] {0b0001, 0b0001, 0b0000, 0b0000};
    public static final int LENGTH = 4; // number of nibbles

    private static SerialPort PORT;

    private static int expectedBytes;
    private static byte[] receivedBuffer;
    private static int receivedCount;

    // State flags and timer
    private static volatile boolean shiftingIn = false;
    private static volatile long lastWriteTime = 0;

    // Command types for the worker thread
    private enum CommandType { READ, WRITE }
    // Blocking queue to hold commands
    private static final BlockingQueue<CommandType> commandQueue = new LinkedBlockingQueue<>();
    // Dedicated worker thread to process commands sequentially
    private static final Thread commandWorker = new Thread(() -> {
        while (true) {
            try {
                CommandType command = commandQueue.take();
                switch (command) {
                    case READ:
                        performRead();
                        break;
                    case WRITE:
                        performWrite();
                        break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    });
    static {
        commandWorker.setDaemon(true);
        commandWorker.start();
    }

    public static void start() {
        if (PORT != null && PORT.isOpen()) {
            Console.warning("Port already open");
            return;
        }
        PORT = Arrays.stream(SerialPort.getCommPorts())
                .filter(port -> port.getVendorID() == 0x4D8
                        && port.getPortDescription().contains("MCP2221"))
                .findFirst().orElse(null);
        if (PORT == null) {
            Console.error("MCP not found");
            return;
        }
        PORT.setBaudRate(9600);
        PORT.setNumDataBits(8);
        PORT.setParity(SerialPort.NO_PARITY);
        PORT.setNumStopBits(SerialPort.ONE_STOP_BIT);
        PORT.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
        if (!PORT.openPort()) {
            Console.error("Failed to open port", PORT.getDescriptivePortName());
            return;
        }
        Console.log("Opened port", PORT.getSystemPortName());
        PORT.addDataListener(new Serial());
    }

    // Instead of creating a new runnable each time, simply enqueue a command.
    public static void read() {
        commandQueue.offer(CommandType.READ);
    }

    public static void write() {
        commandQueue.offer(CommandType.WRITE);
    }

    // Actual implementation of the read command.
    private static void performRead() {
        if (PORT == null) {
            Console.warning("Port not initialized");
            start();
            return;
        }
        if (!PORT.isOpen()) {
            Console.warning("Port not open");
            return;
        }
        if (shiftingIn) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastWriteTime < 3000) {
            return;
        }
        lastWriteTime = now;
        shiftingIn = true;
        PORT.flushIOBuffers();
        int numBytes = (LENGTH + 1) / 2;
        expectedBytes = numBytes;
        receivedBuffer = new byte[numBytes];
        receivedCount = 0;
        byte[] command = new byte[] { 1, (byte) numBytes };
        int bytesWritten = PORT.writeBytes(command, command.length);
        if (bytesWritten != command.length) {
            Console.error("Could not read");
        }
    }

    // Actual implementation of the write command.
    private static void performWrite() {
        if (PORT == null) {
            Console.warning("Port not initialized");
            start();
            return;
        }
        if (!PORT.isOpen()) {
            Console.warning("Port not open");
            return;
        }
        if (shiftingIn) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastWriteTime < 3000) {
            return;
        }
        lastWriteTime = now;
        PORT.flushIOBuffers();
        int numBytes = (LENGTH + 1) / 2;
        byte[] packedData = new byte[numBytes];
        for (int i = 0; i < numBytes; i++) {
            int indexHigh = 2 * i;
            int highNibble = OUT[indexHigh] & 0x0F;
            int lowNibble = 0;
            if (indexHigh + 1 < LENGTH) {
                lowNibble = OUT[indexHigh + 1] & 0x0F;
            }
            packedData[i] = (byte) ((highNibble << 4) | lowNibble);
        }
        byte[] command = new byte[2 + numBytes];
        command[0] = 2;
        command[1] = (byte) numBytes;
        System.arraycopy(packedData, 0, command, 2, numBytes);
        int bytesWritten = PORT.writeBytes(command, command.length);
        if (bytesWritten != command.length) {
            Console.error("Could not write");
        }
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE | SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() == SerialPort.LISTENING_EVENT_PORT_DISCONNECTED) {
            Console.warning("Port disconnected", PORT.getSystemPortName());
            PORT.closePort();
            PORT = null;
            return;
        }
        if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
            return;
        }
        int available = PORT.bytesAvailable();
        if (available <= 0) return;
        byte[] newData = new byte[available];
        int numRead = PORT.readBytes(newData, newData.length);
        if (numRead > 0 && receivedBuffer != null) {
            int bytesToCopy = Math.min(numRead, expectedBytes - receivedCount);
            System.arraycopy(newData, 0, receivedBuffer, receivedCount, bytesToCopy);
            receivedCount += bytesToCopy;
            if (receivedCount >= expectedBytes) {
                for (int i = 0; i < expectedBytes; i++) {
                    int highNibble = (receivedBuffer[i] >> 4) & 0x0F;
                    IN[2 * i] = (byte) highNibble;
                    if (2 * i + 1 < LENGTH) {
                        int lowNibble = receivedBuffer[i] & 0x0F;
                        IN[2 * i + 1] = (byte) lowNibble;
                    }
                }
                StringBuilder data = new StringBuilder();
                for (byte b : IN) {
                    data.append((b & 0x0F)).append(" ");
                }
                Console.log("Input", data.toString());
                shiftingIn = false;
            }
        }
    }

    public static boolean get(int nibbleIndex, int position) {
        return ((IN[nibbleIndex] >> position) & 1) == 1;
    }

    public static void set(int nibbleIndex, int position, int bitValue) {
        OUT[nibbleIndex] = bitValue == 1
                ? (byte)(OUT[nibbleIndex] | (1 << position))
                : (byte)(OUT[nibbleIndex] & ~(1 << position));
    }

    public static void close() {
        if (PORT != null && PORT.isOpen()) {
            PORT.removeDataListener();
            PORT.closePort();
        }
    }
}
