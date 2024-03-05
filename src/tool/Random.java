package tool;

import java.awt.*;

public class Random {

    public static Color color() {
        return new Color((int) (Math.random() * 0x1000000));
    }

    public static int integer(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }

    public static int integer(int max) {
        return (int) (Math.random() * (max + 1));
    }

}
