package be.kuleuven.gt.gamehub;

import static be.kuleuven.gt.gamehub.TetrisPreviewView.getColorForValue;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;
import java.util.function.Consumer;

public class TetrisView extends SurfaceView implements Runnable {
    private Thread gameThread;
    private boolean isPlaying;
    private SurfaceHolder holder;
    private Paint paint;

    private static final int ROWS = 20, COLS = 10;
    private static int[][] grid = new int[ROWS][COLS];

    public static int[][] currentPiece;
    public static int[][] nextPiece;
    private TetrisPreviewView holdPreviewView;
    private TetrisPreviewView nextPreviewView;
    private static int[][] heldPiece = null;
    private static boolean hasHeld = false;
    private static int pieceRow;
    private static int pieceCol;
    private Random random = new Random();
    private GameOverListener gameOverListener;
    protected static int scoreTetris = 0;
    public static int highscoreTetris;
    private int level = 1;
    private int totalLinesCleared = 0;


    public interface GameOverListener {
        void onGameOver(int scoreTetris);
    }

    public interface OnScoreChangeListener {
        void onScoreChanged(int newScore);
    }

    private OnScoreChangeListener scoreChangeListener;

    public TetrisView(Context context) {
        super(context);
        init();
    }

    public TetrisView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TetrisView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        holder = getHolder();
        paint = new Paint();
        nextPiece = TETROMINOES[random.nextInt(TETROMINOES.length)];
        spawnPiece();
    }

    public void setGameOverListener(GameOverListener listener) {
        this.gameOverListener = listener;
    }

    public void setScoreChangeListenerTetris(OnScoreChangeListener listener) {
        this.scoreChangeListener = listener;
    }

    private static final int[][][] TETROMINOES = {
            {{1, 1, 1, 1}},                     // I - value 1
            {{2, 2}, {2, 2}},                   // O - value 2
            {{0, 3, 0}, {3, 3, 3}},             // T - value 3
            {{0, 4, 4}, {4, 4, 0}},             // S - value 4
            {{5, 5, 0}, {0, 5, 5}},             // Z - value 5
            {{6, 0, 0}, {6, 6, 6}},             // J - value 6
            {{0, 0, 7}, {7, 7, 7}}              // L - value 7
    };

    private void spawnPiece() {
        currentPiece = nextPiece;
        nextPiece = TETROMINOES[random.nextInt(TETROMINOES.length)];
        if (nextPieceChangeListener != null) nextPieceChangeListener.accept(nextPiece);
        pieceRow = 0;
        pieceCol = Math.max(0, COLS / 2 - currentPiece[0].length / 2);
        hasHeld = false;

        if (collides(pieceRow, pieceCol)) {
            isPlaying = false;
            if (gameOverListener != null) {
                gameOverListener.onGameOver(scoreTetris);
            }
        }
    }

    private static boolean collides(int row, int col, int[][] piece) {
        for (int r = 0; r < piece.length; r++) {
            for (int c = 0; c < piece[0].length; c++) {
                if (piece[r][c] != 0) {
                    int newR = row + r;
                    int newC = col + c;
                    if (newR < 0 || newR >= ROWS || newC < 0 || newC >= COLS || grid[newR][newC] != 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean collides(int row, int col) {
        return collides(row, col, currentPiece);
    }

    private void lockPiece() {
        for (int r = 0; r < currentPiece.length; r++) {
            for (int c = 0; c < currentPiece[0].length; c++) {
                if (currentPiece[r][c] != 0) {
                    grid[pieceRow + r][pieceCol + c] = currentPiece[r][c];
                }
            }
        }
        clearLines();
        spawnPiece();
    }

    private void clearLines() {
        int linesClearedThisTurn = 0;

        for (int r = 0; r < ROWS; r++) {
            boolean full = true;
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] == 0) {
                    full = false;
                    break;
                }
            }

            if (full) {
                linesClearedThisTurn++;
                for (int row = r; row > 0; row--) {
                    grid[row] = grid[row - 1].clone();
                }
                grid[0] = new int[COLS];
            }
        }

        if (linesClearedThisTurn > 0) {
            totalLinesCleared += linesClearedThisTurn;
            level = (totalLinesCleared / 10) + 1;

            int pointsAwarded = 0;
            switch (linesClearedThisTurn) {
                case 1: pointsAwarded = 40 * level; break;
                case 2: pointsAwarded = 100 * level; break;
                case 3: pointsAwarded = 300 * level; break;
                case 4: pointsAwarded = 1200 * level; break;
            }

            scoreTetris += pointsAwarded;

            if (scoreChangeListener != null) {
                scoreChangeListener.onScoreChanged(scoreTetris);
            }
        }
    }


    public void holdPiece() {
        if (hasHeld) return;
        hasHeld = true;

        if (heldPiece == null) {
            heldPiece = currentPiece;
            if (heldPieceChangeListener != null) heldPieceChangeListener.accept(heldPiece);
            spawnPiece();
        } else {
            int[][] temp = currentPiece;
            currentPiece = heldPiece;
            heldPiece = temp;
            pieceRow = 0;
            pieceCol = Math.max(0, COLS / 2 - currentPiece[0].length / 2);
            if (heldPieceChangeListener != null) heldPieceChangeListener.accept(heldPiece); // <-- Add this line
        }

        invalidate();
    }


    public int[][] getNextPiece() {

        return nextPiece;
    }

    public int[][] getHeldPiece() {
        return heldPiece;
    }

    private static void movePiece(int rowOffset, int colOffset) {
        if (!collides(pieceRow + rowOffset, pieceCol + colOffset)) {
            pieceRow += rowOffset;
            pieceCol += colOffset;
        }
    }

    protected static int[][] rotatePiece(int[][] piece) {
        int rows = piece.length;
        int cols = piece[0].length;
        int[][] rotated = new int[cols][rows];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                rotated[c][rows - 1 - r] = piece[r][c];
            }
        }
        return rotated;
    }

    public void rotateCurrentPiece() {
        int[][] rotated = rotatePiece(currentPiece);
        if (!collides(pieceRow, pieceCol, rotated)) {
            currentPiece = rotated;
        } else if (!collides(pieceRow, pieceCol - 1, rotated)) {
            pieceCol -= 1;
            currentPiece = rotated;
        } else if (!collides(pieceRow, pieceCol + 1, rotated)) {
            pieceCol += 1;
            currentPiece = rotated;
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            if (y < getHeight() / 3f) {
                rotateCurrentPiece();
            } else if (x < getWidth() / 2f) {
                moveLeft();
            } else {
                moveRight();
            }

            invalidate();
        }
        return true;
    }

    public void moveLeft() {
        movePiece(0, -1);
        invalidate();
    }

    public void moveRight() {
        movePiece(0, 1);
        invalidate();
    }

    public void moveDown() {
        movePiece(1, 0);
        invalidate();
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
        }
    }

    private void update() {
        if (!collides(pieceRow + 1, pieceCol)) {
            pieceRow++;
        } else {
            lockPiece();
        }
        try {
            Thread.sleep(Math.max(300,(1050-level*50)));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

//    private void drawPiecePreview(Canvas canvas, int[][] piece, int offsetX, int offsetY, int size) {
//        if (piece == null) return;
//        int blockSize = size / 4;
//        for (int r = 0; r < piece.length; r++) {
//            for (int c = 0; c < piece[0].length; c++) {
//                if (piece[r][c] != 0) {
//                    canvas.drawRect(offsetX + c * blockSize, offsetY + r * blockSize,
//                            offsetX + (c + 1) * blockSize, offsetY + (r + 1) * blockSize, paint);
//                }
//            }
//        }
//    }

    private void draw() {
        if (!holder.getSurface().isValid()) return;
        Canvas canvas = holder.lockCanvas();
        canvas.drawColor(Color.WHITE);

        int cellSize = getWidth() / COLS;

        paint.setColor(Color.LTGRAY);
        canvas.drawRect(0, 0, getWidth(), cellSize * ROWS, paint);

        paint.setColor(Color.DKGRAY);
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] != 0) {
                    canvas.drawRect(c * cellSize, r * cellSize, (c + 1) * cellSize, (r + 1) * cellSize, paint);
                }
            }
        }

        paint.setStrokeWidth(4);
        paint.setColor(Color.BLACK);
        for (int r = 0; r <= ROWS; r++) {
            canvas.drawLine(0, cellSize * r, getWidth(), cellSize * r, paint);
        }
        for (int c = 0; c <= COLS; c++) {
            canvas.drawLine(cellSize * c, 0, cellSize * c, cellSize * ROWS, paint);
        }

        for (int r = 0; r < currentPiece.length; r++) {
            for (int c = 0; c < currentPiece[0].length; c++) {
                if (currentPiece[r][c] != 0) {
                    paint.setColor(getColorForValue(currentPiece[r][c]));
                    canvas.drawRect((pieceCol + c) * cellSize, (pieceRow + r) * cellSize,
                            (pieceCol + c + 1) * cellSize, (pieceRow + r + 1) * cellSize, paint);
                }
            }
        }

//        Draw next piece
//        paint.setColor(Color.BLUE);
//        drawPiecePreview(canvas, nextPiece, getWidth() - 180, 20, 120);
//
//        Draw held piece
//        paint.setColor(Color.MAGENTA);
//        drawPiecePreview(canvas, heldPiece, getWidth() - 180, 160, 120);

        holder.unlockCanvasAndPost(canvas);
    }

    public void pause() {
        isPlaying = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        resetGame();
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    void resetGame() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c] = 0;
            }
        }
        scoreTetris = 0;
        totalLinesCleared = 0;
        level = 1;
        heldPiece = null;
        hasHeld = false;
        nextPiece = TETROMINOES[random.nextInt(TETROMINOES.length)];
        spawnPiece();
    }
    private Consumer<int[][]> nextPieceChangeListener;
    private Consumer<int[][]> heldPieceChangeListener;

    public void setNextPieceChangeListener(Consumer<int[][]> listener) {
        this.nextPieceChangeListener = listener;
    }

    public void setHeldPieceChangeListener(Consumer<int[][]> listener) {
        this.heldPieceChangeListener = listener;
    }
}
