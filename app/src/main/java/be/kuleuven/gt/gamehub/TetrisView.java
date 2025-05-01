package be.kuleuven.gt.gamehub;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

public class TetrisView extends SurfaceView implements Runnable {
    private Thread gameThread;
    private boolean isPlaying;
    private SurfaceHolder holder;
    private Paint paint;

    private static final int ROWS = 20, COLS = 10;
    private static int[][] grid = new int[ROWS][COLS];

    public static int[][] currentPiece;
    private static int pieceRow;
    private static int pieceCol;
    private Random random = new Random();
    private GameOverListener gameOverListener;
    protected static int scoreTetris = 0;
    public static int highscoreTetris;

    public interface GameOverListener {
        void onGameOver(int score2048);
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
        spawnPiece();
    }

    public void setGameOverListener(GameOverListener listener) {
        this.gameOverListener = listener;
    }

    public void setScoreChangeListenerTetris(OnScoreChangeListener listener) {
        this.scoreChangeListener = listener;
    }

    private static final int[][][] TETROMINOES = {
            {{1, 1, 1, 1}},
            {{1, 1}, {1, 1}},
            {{0, 1, 0}, {1, 1, 1}},
            {{0, 1, 1}, {1, 1, 0}},
            {{1, 1, 0}, {0, 1, 1}},
            {{1, 0, 0}, {1, 1, 1}},
            {{0, 0, 1}, {1, 1, 1}}
    };

    private void spawnPiece() {
        currentPiece = TETROMINOES[random.nextInt(TETROMINOES.length)];
        pieceRow = 0;
        pieceCol = Math.max(0, COLS / 2 - currentPiece[0].length / 2);

        // Game over check: if the new piece immediately collides, end the game
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
        for (int r = 0; r < ROWS; r++) {
            boolean full = true;
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) {
                scoreTetris += 10;
                if (scoreChangeListener != null) {
                    scoreChangeListener.onScoreChanged(scoreTetris);
                }
                for (int row = r; row > 0; row--) {
                    grid[row] = grid[row - 1].clone();
                }
                grid[0] = new int[COLS];
            }
        }
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
                int[][] rotated = rotatePiece(currentPiece);
                if (!collides(pieceRow, pieceCol, rotated)) {
                    currentPiece = rotated;
                }
            } else if (x < getWidth() / 2f) {
                movePiece(0, -1);
            } else {
                movePiece(0, 1);
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
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void draw() {
        if (!holder.getSurface().isValid()) return;
        Canvas canvas = holder.lockCanvas();
        canvas.drawColor(Color.WHITE);

        int cellSize = getWidth() / COLS;

        paint.setColor(Color.LTGRAY);
        canvas.drawRect(0,0,getWidth(),cellSize*ROWS,paint);

        paint.setColor(Color.GREEN);
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] != 0) {
                    canvas.drawRect(c * cellSize, r * cellSize, (c + 1) * cellSize, (r + 1) * cellSize, paint);
                }
            }
        }
        paint.setColor(Color.BLACK);
        for (int r = 0; r <= ROWS ; r++) {
            canvas.drawLine(0,cellSize*r,getWidth(),cellSize*r,paint);
        }
        for (int c=0 ; c <= COLS; c++) {
            canvas.drawLine(cellSize*c,0,cellSize*c,cellSize*ROWS,paint);
        }

        paint.setColor(Color.RED);
        for (int r = 0; r < currentPiece.length; r++) {
            for (int c = 0; c < currentPiece[0].length; c++) {
                if (currentPiece[r][c] != 0) {
                    canvas.drawRect((pieceCol + c) * cellSize, (pieceRow + r) * cellSize,
                            (pieceCol + c + 1) * cellSize, (pieceRow + r + 1) * cellSize, paint);
                }
            }
        }

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
        resetGame(); // Add this line if you want to restart cleanly
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    private void resetGame() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c] = 0;
            }
        }
        scoreTetris = 0;
        spawnPiece();
    }
}
