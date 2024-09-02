package property;

import java.io.Serializable;

public class State implements Cloneable, Serializable {

    private final boolean[] bits;

    public State(int states) {
        int bits = 0;
        int mstates = 1;
        while (mstates < states) {
            bits++;
            mstates = 1 << bits;  // 2^bits
        }
        this.bits = new boolean[bits];
    }

    @Override
    public State clone() {
        try {
            State cloned = (State) super.clone();
            System.arraycopy(this.bits, 0, cloned.bits, 0, this.bits.length);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public void increment() {
        int i = 0;
        while (i < bits.length && bits[i]) {
            bits[i] = false;
            i++;
        }
        if (i < bits.length) {
            bits[i] = true;
        }
    }

    public void decrement() {
        int i = 0;
        while (i < bits.length && !bits[i]) {
            bits[i] = true;
            i++;
        }
        if (i < bits.length) {
            bits[i] = false;
        }
    }

    public int state() {
        int state = 0;
        for (int i = 0; i < bits.length; i++) {
            state |= bits[i] ? 1 << i : 0;
        }
        return state;
    }

    public void state(int state) {
        for (int i = 0; i < bits.length; i++) {
            bits[i] = (state & (1 << i)) != 0;
        }
    }

    public int length() {
        return bits.length;
    }

    public void reset() {
        for (int i = 0; i < bits.length; i++) {
            bits[i] = false;
        }
    }

    @Override
    public String toString() {
        char[] result = new char[bits.length];
        for (int i = 0; i < bits.length; i++) {
            result[bits.length - 1 - i] = bits[i] ? '1' : '0';
        }
        return new String(result);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof State && ((State) obj).length() == length() && ((State) obj).state() == state();
    }

    public boolean equals(int state) {
        return state == state();
    }

}
