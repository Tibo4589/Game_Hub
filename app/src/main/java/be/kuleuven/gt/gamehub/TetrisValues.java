package be.kuleuven.gt.gamehub;

public class TetrisValues {
    public static final int ROWS = 20;
    public static final int COLS = 10;

    public static final int[][][] TETROMINOES = {
            {{1, 1, 1, 1}},                     // I
            {{2, 2}, {2, 2}},                   // O
            {{0, 3, 0}, {3, 3, 3}},             // T
            {{0, 4, 4}, {4, 4, 0}},             // S
            {{5, 5, 0}, {0, 5, 5}},             // Z
            {{6, 0, 0}, {6, 6, 6}},             // J
            {{0, 0, 7}, {7, 7, 7}}              // L
    };

    public static int getColorForValue(int value) {
        switch (value) {
            case 1: return 0xFF00FFFF; // Cyan
            case 2: return 0xFFFFFF00; // Yellow
            case 3: return 0xFF800080; // Purple
            case 4: return 0xFF00FF00; // Green
            case 5: return 0xFFFF0000; // Red
            case 6: return 0xFF0000FF; // Blue
            case 7: return 0xFFFFA500; // Orange
            default: return 0xFF888888;
        }
    }
}