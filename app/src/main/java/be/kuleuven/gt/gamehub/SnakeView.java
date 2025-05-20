package be.kuleuven.gt.gamehub;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.LinkedList;
import java.util.Random;

public class SnakeView extends SurfaceView implements SurfaceHolder.Callback, Runnable, Listeners {
    private Thread thread;
    private boolean isPlaying = false;
    private SurfaceHolder holder;
    private Paint paint;

    private int blockSize;
    private final int numBlocksWide = 16;
    private final int numBlocksHigh = 22;
    public int scoresnake = 0;
    public static int highscoresnake;
    private LinkedList<Point> snake;
    private Point food, place;
    private enum Direction {UP, DOWN, LEFT, RIGHT}
    private Direction direction = Direction.RIGHT;
    private long lastMoveTime;
    private final int moveDelay = 500;
    private GameOverListener gameOverListener;
    private OnScoreChangeListener scoreChangeListener;
    public void setGameOverListener(GameOverListener listener) {
        this.gameOverListener = listener;
    }
    public void setScoreChangeListenerSnake(OnScoreChangeListener listener) {
        this.scoreChangeListener = listener;
    }
    public SnakeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        holder.addCallback(this);
        paint = new Paint();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        blockSize = getWidth() / numBlocksWide;

        initGame();

        if (scoreChangeListener != null) {
            scoreChangeListener.onScoreChanged(scoresnake);
        }
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    private void initGame() {
        snake = new LinkedList<>();
        int startX = numBlocksWide / 4;
        int startY = numBlocksHigh / 2;

        snake.add(new Point(startX, startY));
        snake.add(new Point(startX - 1, startY));
        snake.add(new Point(startX - 2, startY));

        direction = Direction.RIGHT;
        placeFood();
        lastMoveTime = System.currentTimeMillis();
    }

    private void placeFood() {
        Random rand = new Random();
        do {
            place = new Point(rand.nextInt(numBlocksWide), rand.nextInt(numBlocksHigh));
        } while (snake.contains(place));
        food = place;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        while (isPlaying) {
            if (System.currentTimeMillis() - lastMoveTime > moveDelay) {
                update();
                draw();
                lastMoveTime = System.currentTimeMillis();
            }
        }
    }

    private void update() {
        Point head = snake.getFirst();
        Point newHead = new Point(head.x, head.y);

        switch (direction) {
            case UP: newHead.y--; break;
            case DOWN: newHead.y++; break;
            case LEFT: newHead.x--; break;
            case RIGHT: newHead.x++; break;
        }

        if (newHead.x < 0 || newHead.x >= numBlocksWide ||
                newHead.y < 0 || newHead.y >= numBlocksHigh ||
                snake.contains(newHead)) {
            isPlaying = false;
            if (gameOverListener != null) {
                gameOverListener.onGameOver(scoresnake);
            }
            return;
        }

        snake.addFirst(newHead);

        if (newHead.equals(food)) {
            placeFood();// grow
            scoresnake+=1;
            if (scoreChangeListener != null) {
                scoreChangeListener.onScoreChanged(scoresnake);
            }
        } else {
            snake.removeLast(); // move
        }
    }

    private void draw() {
        Canvas canvas = holder.lockCanvas();
        if (canvas != null) {
            canvas.drawColor(0xFF03DAC5);

            // Draw snake
            paint.setColor(Color.GREEN);
            for (Point p : snake) {
                canvas.drawRect(p.x * blockSize, p.y * blockSize,
                        (p.x + 1) * blockSize, (p.y + 1) * blockSize, paint);
            }

            // Draw food
            paint.setColor(Color.RED);
            canvas.drawRect(food.x * blockSize, food.y * blockSize,
                    (food.x + 1) * blockSize, (food.y + 1) * blockSize, paint);

            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(10);
            canvas.drawLine(0,0,0,blockSize*numBlocksHigh,paint);
            canvas.drawLine(getWidth(),0,getWidth(),blockSize*numBlocksHigh,paint);
            canvas.drawLine(0,0,getWidth(),0,paint);
            canvas.drawLine(0,blockSize*numBlocksHigh,getWidth(),blockSize*numBlocksHigh,paint);

            paint.setColor(0xFF32618E);
            canvas.drawRect(0,blockSize*numBlocksHigh,getWidth(),getHeight(),paint);

            holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isPlaying = false;
        try {
            if (thread != null) thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    // Direction control methods
    public void moveleft() {
        if (direction != Direction.RIGHT) direction = Direction.LEFT;
        invalidate();
    }

    public void moveright() {
        if (direction != Direction.LEFT) direction = Direction.RIGHT;
        invalidate();
    }

    public void moveup() {
        if (direction != Direction.DOWN) direction = Direction.UP;
        invalidate();
    }

    public void movedown() {
        if (direction != Direction.UP) direction = Direction.DOWN;
        invalidate();
    }
    public void restart() {
        clear();
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }
    public void clear() {
        snake.clear();
        scoresnake = 0;
        isPlaying = false;
        initGame();
        invalidate();
        if (scoreChangeListener != null) {
            scoreChangeListener.onScoreChanged(scoresnake);
        }
    }

    public void pause() {
        isPlaying = false;
    }

    public void resume() {
        isPlaying = true;
    }
}
