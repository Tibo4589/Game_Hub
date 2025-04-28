package be.kuleuven.gt.gamehub;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import java.util.Random;

public class Game2048View extends View {

    private int[][] board = new int[4][4];
    private Paint paint;
    private GestureDetector gestureDetector;
    private boolean isPlaying = true;
    private Random random = new Random();
    public static int score2048 = 0;
    private GameOverListener gameOverListener;
    private boolean isGameOver = false;
    public static int highscore2048;

    public interface GameOverListener {
        void onGameOver(int score2048);
    }

    public interface OnScoreChangeListener {
        void onScoreChanged(int newScore);
    }

    private OnScoreChangeListener scoreChangeListener;

    public Game2048View(Context context) {
        super(context);
        init();
    }

    public Game2048View(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    protected void init() {
        paint = new Paint();
        gestureDetector = new GestureDetector(getContext(), new GestureListener());
        spawnRandom();
        spawnRandom();
    }

    public void setGameOverListener(GameOverListener listener) {
        this.gameOverListener = listener;
    }

    public void setScoreChangeListener(OnScoreChangeListener listener) {
        this.scoreChangeListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getWidth(); // Keep square
        int cellSize = Math.min(width, height) / 4;
        canvas.drawColor(Color.LTGRAY);

        paint.setTextSize(cellSize / 2);
        paint.setTextAlign(Paint.Align.CENTER);

        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                drawTile(canvas, x, y, cellSize);
            }
        }

        paint.setColor(Color.BLACK);
        // Horizontal lines
        canvas.drawLine(0, height / 4f, width, height / 4f, paint);
        canvas.drawLine(0, height / 2f, width, height / 2f, paint);
        canvas.drawLine(0, height * 0.75f, width, height * 0.75f, paint);
        // Vertical lines
        canvas.drawLine(width / 4f, 0, width / 4f, height, paint);
        canvas.drawLine(width / 2f, 0, width / 2f, height, paint);
        canvas.drawLine(width * 0.75f, 0, width * 0.75f, height, paint);
    }

    private void drawTile(Canvas canvas, int x, int y, int cellSize) {
        int value = board[y][x];

        switch (value) {
            case 0:
                paint.setColor(Color.LTGRAY);
                break;
            case 2:
                paint.setColor(Color.rgb(238, 228, 218)); // light color
                break;
            case 4:
                paint.setColor(Color.rgb(237, 224, 200));
                break;
            case 8:
                paint.setColor(Color.rgb(242, 177, 121));
                break;
            case 16:
                paint.setColor(Color.rgb(245, 149, 99));
                break;
            case 32:
                paint.setColor(Color.rgb(246, 124, 95));
                break;
            case 64:
                paint.setColor(Color.rgb(246, 94, 59));
                break;
            case 128:
                paint.setColor(Color.rgb(237, 207, 114));
                break;
            case 256:
                paint.setColor(Color.rgb(237, 204, 97));
                break;
            case 512:
                paint.setColor(Color.rgb(237, 200, 80));
                break;
            case 1024:
                paint.setColor(Color.rgb(237, 197, 63));
                break;
            case 2048:
                paint.setColor(Color.rgb(237, 194, 46));
                break;
            case 4096:
                paint.setColor(Color.rgb(255,102,255));
                break;
            case 8192:
                paint.setColor(Color.rgb(0,0,255));
                break;
            case 16384:
                paint.setColor(Color.rgb(0,255,0));
                break;
            default:
                paint.setColor(Color.BLACK);
                break;
        }

        int left = x * cellSize;
        int top = y * cellSize;
        int right = left + cellSize;
        int bottom = top + cellSize;

        canvas.drawRect(left, top, right, bottom, paint);

        if (value != 0 && value<1000) {
            paint.setColor(Color.BLACK);
            canvas.drawText(String.valueOf(value),
                    left + cellSize / 2f,
                    top + cellSize / 1.7f,
                    paint);
        }
        else {
            paint.setColor(Color.BLACK);
            paint.setTextSize(cellSize/3);
            canvas.drawText(String.valueOf(value),
                    left + cellSize / 2f,
                    top + cellSize / 1.7f,
                    paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 50; // make swipe easier to trigger

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            handleSwipe(e1, e2);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            handleSwipe(e1, e2);
            return true;
        }

        private void handleSwipe(MotionEvent e1, MotionEvent e2) {
            float dx = e2.getX() - e1.getX();
            float dy = e2.getY() - e1.getY();

            if (Math.abs(dx) > Math.abs(dy)) {
                if (dx > SWIPE_THRESHOLD) moveRight();
                else if (dx < -SWIPE_THRESHOLD) moveLeft();
            } else {
                if (dy > SWIPE_THRESHOLD) moveDown();
                else if (dy < -SWIPE_THRESHOLD) moveUp();
            }
        }
    }

    protected void moveLeft() {
        boolean moved = false;
        for (int y = 0; y < 4; y++) {
            int[] newRow = new int[4];
            int index = 0;
            for (int x = 0; x < 4; x++) {
                if (board[y][x] != 0) {
                    if (index > 0 && newRow[index - 1] == board[y][x]) {
                        newRow[index - 1] *= 2;
                        score2048 += newRow[index - 1];
                        if (scoreChangeListener != null) {
                            scoreChangeListener.onScoreChanged(score2048);
                        }
                    } else {
                        newRow[index++] = board[y][x];
                    }
                }
            }
            for (int x = 0; x < 4; x++) {
                if (board[y][x] != newRow[x]) moved = true;
                board[y][x] = newRow[x];
            }
        }
        if (moved) spawnRandom();
        invalidate();
        checkGameOver();
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

    private void rotateBoard() {
        int[][] newBoard = new int[4][4];
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                newBoard[x][3 - y] = board[y][x];
            }
        }
        board = newBoard;
    }

    private void spawnRandom() {
        int emptyCount = 0;
        for (int[] row : board) {
            for (int value : row) {
                if (value == 0) emptyCount++;
            }
        }

        if (emptyCount == 0) return;

        int pos = random.nextInt(emptyCount);
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                if (board[y][x] == 0) {
                    if (pos == 0) {
                        board[y][x] = random.nextInt(10) == 0 ? 4 : 2;
                        return;
                    }
                    pos--;
                }
            }
        }
    }

    private void checkGameOver() {
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                if (board[y][x] == 0) return; // Empty space found
                if (x < 3 && board[y][x] == board[y][x + 1]) return; // Mergeable horizontally
                if (y < 3 && board[y][x] == board[y + 1][x]) return; // Mergeable vertically
            }
        }

        // No moves left
        isGameOver = true;
        if (gameOverListener != null) {
            gameOverListener.onGameOver(score2048);
        }
    }

    public void highscore2048() {
        if (score2048 > highscore2048) {
            highscore2048 = score2048;
        }
    }

    public void clear() {
        board = new int[4][4];
        score2048 = 0;
        invalidate();
        if (scoreChangeListener != null) {
            scoreChangeListener.onScoreChanged(score2048);
        }
    }

    public void pause() {
        isPlaying = false;
    }

    public void resume() {
        isPlaying = true;
    }
}
