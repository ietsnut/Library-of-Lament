package property;

import java.io.Serializable;

public class Matrix implements Cloneable, Serializable {

    private byte[][] matrix = new byte[4][4];

    public Matrix() {
        for (byte i = 0; i < 4; i++) {
            for (byte j = 0; j < 4; j++) {
                matrix[i][j] = 0;
            }
        }
    }

    public Matrix(byte[][] elements) {
        if (elements.length == 4 && elements[0].length == 4) {
            this.matrix = elements;
        } else {
            throw new IllegalArgumentException("Matrix must be 4x4.");
        }
    }

    public byte getElement(byte row, byte column) {
        return matrix[row][column];
    }

    public void setElement(byte row, byte column, byte value) {
        matrix[row][column] = value;
    }

    public Matrix add(Matrix other) {
        Matrix result = new Matrix();
        for (byte i = 0; i < 4; i++) {
            for (byte j = 0; j < 4; j++) {
                result.setElement(i, j, (byte) (this.matrix[i][j] + other.matrix[i][j]));
            }
        }
        return result;
    }

    public Matrix subtract(Matrix other) {
        Matrix result = new Matrix();
        for (byte i = 0; i < 4; i++) {
            for (byte j = 0; j < 4; j++) {
                result.setElement(i, j, (byte) (this.matrix[i][j] - other.matrix[i][j]));
            }
        }
        return result;
    }

    public Matrix multiply(Matrix other) {
        Matrix result = new Matrix();
        for (byte i = 0; i < 4; i++) {
            for (byte j = 0; j < 4; j++) {
                byte sum = 0;
                for (byte k = 0; k < 4; k++) {
                    sum += (byte) (this.matrix[i][k] * other.matrix[k][j]);
                }
                result.setElement(i, j, sum);
            }
        }
        return result;
    }

    public Matrix multiply(byte scalar) {
        Matrix result = new Matrix();
        for (byte i = 0; i < 4; i++) {
            for (byte j = 0; j < 4; j++) {
                result.setElement(i, j, (byte) (this.matrix[i][j] * scalar));
            }
        }
        return result;
    }

    @Override
    public Matrix clone() {
        try {
            Matrix clone = (Matrix) super.clone();
            clone.matrix = new byte[4][4];
            for (byte i = 0; i < 4; i++) {
                System.arraycopy(this.matrix[i], 0, clone.matrix[i], 0, 4);
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}
