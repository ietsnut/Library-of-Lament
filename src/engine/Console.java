package engine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Console {

    public static final String ANSI_RESET  = "\u001B[0m";
    public static final String ANSI_BLACK  = "\u001B[30m";
    public static final String ANSI_RED    = "\u001B[31m";
    public static final String ANSI_GREEN  = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE   = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN   = "\u001B[36m";
    public static final String ANSI_WHITE  = "\u001B[37m";

    private static final int CELL_WIDTH = 20;
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private static ArrayList<String> messages = new ArrayList<>();

    private static String getCurrentTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMAT);
    }

    private static String getCallerSimpleName() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stack) {
            String className = element.getClassName();
            if (className.equals(Thread.class.getName()) || className.equals(Console.class.getName())) {
                continue;
            }
            int lastDot = className.lastIndexOf('.');
            return lastDot != -1 ? className.substring(lastDot + 1) : className;
        }
        return "Unknown";
    }

    private static void printFormatted(String color, String... messages) {
        System.out.printf(color
                + String.join("", Collections.nCopies(messages.length, "%-" + CELL_WIDTH + "s"))
                + ANSI_RESET + "\n", (Object[]) messages);
    }

    private static void printWithTimestampCallerAndThread(String color, String... messages) {
        String timestamp = getCurrentTimestamp();
        String caller = getCallerSimpleName();
        String threadId = "Thread #" + Thread.currentThread().getId();
        String[] args = new String[messages.length + 3];
        args[0] = timestamp;
        args[1] = threadId;
        args[2] = caller;
        System.arraycopy(messages, 0, args, 3, messages.length);
        printFormatted(color, args);
    }

    public static void debug(Object message) {
        printWithTimestampCallerAndThread(ANSI_GREEN, String.valueOf(message));
    }

    public static void debug(boolean message) {
        printWithTimestampCallerAndThread(ANSI_GREEN, Boolean.toString(message));
    }

    public static void debug(int message) {
        printWithTimestampCallerAndThread(ANSI_GREEN, Integer.toString(message));
    }

    public static void debug(double message) {
        printWithTimestampCallerAndThread(ANSI_GREEN, Double.toString(message));
    }

    public static void debug(float message) {
        printWithTimestampCallerAndThread(ANSI_GREEN, Float.toString(message));
    }

    public static void debug(String... messages) {
        printWithTimestampCallerAndThread(ANSI_GREEN, messages);
    }

    public static void debug(Object... objects) {
        String[] messages = new String[objects.length];
        for (int i = 0; i < objects.length; i++) {
            messages[i] = String.valueOf(objects[i]);
        }
        printWithTimestampCallerAndThread(ANSI_GREEN, messages);
    }

    public static void notify(String message) {
        if (messages.contains(message)) return;
        messages.add(message);
        printWithTimestampCallerAndThread(ANSI_CYAN, message);
    }

    public static void log(String... messages) {
        printWithTimestampCallerAndThread(ANSI_WHITE, messages);
    }

    public static void error(String... messages) {
        printWithTimestampCallerAndThread(ANSI_RED, messages);
    }

    public static void warning(String... messages) {
        printWithTimestampCallerAndThread(ANSI_YELLOW, messages);
    }

    public static void ln() {
        System.out.println();
    }

}