package be.kuleuven.gt.gamehub;

import java.util.Random;
import java.util.function.Consumer;

public class TetrisGameLogic {
    public int[][] grid = new int[TetrisValues.ROWS][TetrisValues.COLS];
    public int[][] currentPiece;
    public int[][] nextPiece;
    public int[][] heldPiece = null;
    public boolean hasHeld = false;
    public int pieceRow;
    public int pieceCol;

    public int score = 0;
    public int level = 1;
    public int totalLinesCleared = 0;
    private final Random random = new Random();

    public Consumer<int[][]> onNextPieceChange;
    public Consumer<int[][]> onHoldPieceChange;
    public Consumer<Integer> onScoreChange;

    public void reset() {
        for (int r = 0; r < TetrisValues.ROWS; r++) {
            for (int c = 0; c < TetrisValues.COLS; c++) {
                grid[r][c] = 0;
            }
        }

        score = 0;
        level = 1;
        totalLinesCleared = 0;
        heldPiece = null;
        hasHeld = false;
        nextPiece = randomTetromino();
        spawnPiece();
    }

    private int[][] randomTetromino() {
        return TetrisValues.TETROMINOES[random.nextInt(TetrisValues.TETROMINOES.length)];
    }

    public void spawnPiece() {
        currentPiece = nextPiece != null ? nextPiece : randomTetromino();
        nextPiece = randomTetromino();
        if (onNextPieceChange != null) onNextPieceChange.accept(nextPiece);

        pieceRow = 0;
        pieceCol = TetrisValues.COLS / 2 - currentPiece[0].length / 2;

        hasHeld = false;
    }

    public boolean collides(int rowOffset, int colOffset, int[][] piece) {
        for (int r = 0; r < piece.length; r++) {
            for (int c = 0; c < piece[0].length; c++) {
                if (piece[r][c] != 0) {
                    int newRow = pieceRow + rowOffset + r;
                    int newCol = pieceCol + colOffset + c;
                    if (newRow < 0 || newRow >= TetrisValues.ROWS || newCol < 0 || newCol >= TetrisValues.COLS || grid[newRow][newCol] != 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void move(int rowOffset, int colOffset) {
        if (!collides(rowOffset, colOffset, currentPiece)) {
            pieceRow += rowOffset;
            pieceCol += colOffset;
        }
    }

    public void rotate() {
        int[][] rotated = rotatePiece(currentPiece);
        if (!collides(0, 0, rotated)) {
            currentPiece = rotated;
        } else if (!collides(0, -1, rotated)) {
            pieceCol -= 1;
            currentPiece = rotated;
        } else if (!collides(0, 1, rotated)) {
            pieceCol += 1;
            currentPiece = rotated;
        }
    }

    private int[][] rotatePiece(int[][] piece) {
        int rows = piece.length;
        int cols = piece[0].length;
        int[][] rotated = new int[cols][rows];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                rotated[c][rows - 1 - r] = piece[r][c];
        return rotated;
    }

    public boolean lockPieceAndCheckGameOver() {
        for (int r = 0; r < currentPiece.length; r++) {
            for (int c = 0; c < currentPiece[0].length; c++) {
                if (currentPiece[r][c] != 0) {
                    int gr = pieceRow + r;
                    int gc = pieceCol + c;
                    if (gr >= 0 && gr < TetrisValues.ROWS && gc >= 0 && gc < TetrisValues.COLS)
                        grid[gr][gc] = currentPiece[r][c];
                }
            }
        }

        clearLines();
        spawnPiece();

        return collides(0, 0, currentPiece); // Game over if new piece collides
    }

    private void clearLines() {
        int linesCleared = 0;
        for (int r = 0; r < TetrisValues.ROWS; r++) {
            boolean full = true;
            for (int c = 0; c < TetrisValues.COLS; c++) {
                if (grid[r][c] == 0) {
                    full = false;
                    break;
                }
            }

            if (full) {
                linesCleared++;
                for (int row = r; row > 0; row--) {
                    grid[row] = grid[row - 1].clone();
                }
                grid[0] = new int[TetrisValues.COLS];
            }
        }

        if (linesCleared > 0) {
            totalLinesCleared += linesCleared;
            level = (totalLinesCleared / 10) + 1;

            int points;
            switch (linesCleared) {
                case 1:
                    points = 40;
                    break;
                case 2:
                    points = 100;
                    break;
                case 3:
                    points = 300;
                    break;
                case 4:
                    points = 1200;
                    break;
                default:
                    points = 0;
                    break;
            }

            score += points * level;
            if (onScoreChange != null) onScoreChange.accept(score);
        }
    }

    public void hold() {
        if (hasHeld) return;
        hasHeld = true;

        if (heldPiece == null) {
            heldPiece = currentPiece;
            if (onHoldPieceChange != null) onHoldPieceChange.accept(heldPiece);
            spawnPiece();
        } else {
            int[][] temp = currentPiece;
            currentPiece = heldPiece;
            heldPiece = temp;
            pieceRow = 0;
            pieceCol = TetrisValues.COLS / 2 - currentPiece[0].length / 2;
            if (onHoldPieceChange != null) onHoldPieceChange.accept(heldPiece);
        }
    }
}
