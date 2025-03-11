import game.Manager;
import game.Serial;

class LibraryOfLament {
    public static void main(String[] args) {
        Serial.start("COM6");
        Manager.run();
    }
}

