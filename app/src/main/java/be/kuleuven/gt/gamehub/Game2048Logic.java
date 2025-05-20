package be.kuleuven.gt.gamehub;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;
public class Game2048Logic implements Listeners {
    private int[][] board;
    private final int size;
    private final Random random = new Random();
    protected static int score2048=0;
    protected static int highscore2048;
    private boolean isGameOver = false;
    public boolean moved = false;
    private GameOverListener gameOverListener;
    private OnScoreChangeListener scoreChangeListener;
    private OnGridChangedListener listener;
    public void setGameOverListener(GameOverListener listener) {
        this.gameOverListener = listener;
    }

    public void setScoreChangeListener2048(OnScoreChangeListener listener) {
        this.scoreChangeListener = listener;
    }

    public Game2048Logic(int size) {
        this.size = size;
        board = new int[size][size];
    }

    public void startGame() {
        board = new int[size][size];
        score2048 = 0;
        isGameOver = false;
        spawnRandomTile();
        spawnRandomTile();
        if (scoreChangeListener != null) {
            scoreChangeListener.onScoreChanged(score2048);
        }
        notifyGridChanged();
    }

    public void move(Direction direction) {
        if (direction == Direction.LEFT) moveLeft();
        else if (direction == Direction.RIGHT) moveRight();
        else if (direction == Direction.UP) moveUp();
        else if (direction == Direction.DOWN) moveDown();
        if (moved) spawnRandomTile();
        notifyGridChanged();
        checkGameOver();
    }

    public void setOnGridChangedListener(OnGridChangedListener listener) {
        this.listener = listener;
    }

    private void notifyGridChanged() {
        if (listener != null) listener.onGridChanged();
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private void rotateBoard() {
        int[][] newBoard = new int[size][size];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                newBoard[x][size - 1 - y] = board[y][x];
            }
        }
        board = newBoard;
    }

    private void moveLeft() {
        int[][] oldBoard = cloneBoard(board);
        for (int y = 0; y < size; y++) {
            int[] row = board[y];
            int[] newRow = new int[size];
            int index = 0;

            for (int x = 0; x < size; x++) {
                if (row[x] != 0) newRow[index++] = row[x];
            }
            for (int x = 0; x < size - 1; x++) {
                if (newRow[x] != 0 && newRow[x] == newRow[x + 1]) {
                    newRow[x] *= 2;
                    newRow[x + 1] = 0;
                    score2048 += newRow[x];
                    if (scoreChangeListener != null) {
                        scoreChangeListener.onScoreChanged(score2048);
                    }

                }
            }

            index = 0;
            int[] finalRow = new int[size];
            for (int x = 0; x < size; x++) {
                if (newRow[x] != 0) finalRow[index++] = newRow[x];
            }

            board[y] = finalRow;
        }
        moved = !boardsAreEqual(oldBoard, board);
    }
    protected void moveRight() {
        rotateBoard();
        rotateBoard();
        moveLeft();
        rotateBoard();
        rotateBoard();
    }

    protected void moveUp() {
        rotateBoard();
        rotateBoard();
        rotateBoard();
        moveLeft();
        rotateBoard();
    }

    protected void moveDown() {
        rotateBoard();
        moveLeft();
        rotateBoard();
        rotateBoard();
        rotateBoard();
    }

    private void spawnRandomTile() {
        int emptyCount = 0;
        for (int[] row : board) {
            for (int cell : row) {
                if (cell == 0) emptyCount++;
            }
        }

        if (emptyCount == 0) return;

        int position = random.nextInt(emptyCount);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (board[y][x] == 0) {
                    if (position == 0) {
                        board[y][x] = random.nextInt(10) == 0 ? 4 : 2;
                        return;
                    }
                    position--;
                }
            }
        }
    }

    public int getTileValue(int x, int y) {
        return board[y][x];
    }

    private int[][] cloneBoard(int[][] original) {
        int[][] copy = new int[size][size];
        for (int y = 0; y < size; y++) {
            System.arraycopy(original[y], 0, copy[y], 0, size);
        }
        return copy;
    }

    private boolean boardsAreEqual(int[][] board1, int[][] board2) {
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (board1[y][x] != board2[y][x]) return false;
            }
        }
        return true;
    }
    private void checkGameOver() {
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (board[y][x] == 0) return; // Empty space found
                if (x < size - 1 && board[y][x] == board[y][x + 1]) return; // Mergeable horizontally
                if (y < size - 1 && board[y][x] == board[y + 1][x]) return; // Mergeable vertically
            }
        }
        isGameOver = true;
        if (gameOverListener != null) {
            gameOverListener.onGameOver(score2048);
        }
    }
    public JSONObject saveState2048() throws JSONException {
        JSONObject state = new JSONObject();
        JSONArray boardArray = new JSONArray();
        for (int y = 0; y < size; y++) {
            JSONArray row = new JSONArray();
            for (int x = 0; x < size; x++) {
                row.put(board[y][x]);
            }
            boardArray.put(row);
        }
        state.put("board", boardArray);
        state.put("score", score2048);
        return state;
    }
    public void loadState2048(JSONObject state) throws JSONException {
        JSONArray boardArray = state.getJSONArray("board");
        for (int y = 0; y < size; y++) {
            JSONArray row = boardArray.getJSONArray(y);
            for (int x = 0; x < size; x++) {
                board[y][x] = row.getInt(x);
            }
        }
        score2048 = state.getInt("score");
        notifyGridChanged();
        if (scoreChangeListener != null) scoreChangeListener.onScoreChanged(score2048);
    }

}
