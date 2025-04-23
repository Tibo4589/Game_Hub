package be.kuleuven.gt.gamehub;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.lang.Math;

// This constructor is required for inflation from XML!
public class PongView extends SurfaceView implements Runnable {
    private Thread gameThread;
    private boolean isPlaying;
    private Paint paint;
    private float ballX = getWidth()/2f, ballY=200, ballRadius = 20;
    private float ballSpeedX = 20, ballSpeedY = 20;
    private float paddleX, paddleY, paddleWidth = 300, paddleHeight = 30;
    private SurfaceHolder holder;

    private boolean isGameOver = false;
    private int score = 0;
    private int highscore;

    public interface GameOverListener {
        void onGameOver(int score);
    }

    private GameOverListener gameOverListener;

    public PongView(Context context) {
        super(context);
        init();
    }

    public PongView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PongView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                resetPositions(); // SAFE to call now!
                resume();         // Start the game after layout
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                pause();
            }
        });
        paint = new Paint();
        setFocusable(true);
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
            setHighscore();
        }
    }

    public void setGameOverListener(GameOverListener listener) {
        this.gameOverListener = listener;
    }

    private void update() {
        if (isGameOver) return;

        ballX += ballSpeedX;
        ballY += ballSpeedY;

        if (ballX < ballRadius || ballX > getWidth() - ballRadius) {
            ballSpeedX *= -1;
        }

        if (ballY < ballRadius+120) {
            ballSpeedY *= -1;
        }

        if (ballY + ballRadius >= paddleY &&
                ballX >= paddleX && ballX <= paddleX + paddleWidth) {
            if (ballSpeedY<85){
                double randY = Math.random()*0.25;
                ballSpeedY *= (float) -(1+randY);}
            if (ballSpeedX<85){
                double randX = Math.random()*0.25;
                ballSpeedX *= (float) (1+randX);}
            score++;
        }

        if (ballY > paddleY+ballRadius) {
            isGameOver = true;
            if (gameOverListener != null) {
                gameOverListener.onGameOver(score);
            }
        }
    }

    private void draw() {
        if (!holder.getSurface().isValid()) return;

        Canvas canvas = holder.lockCanvas();
        canvas.drawColor(Color.BLACK);

        // Ball
        paint.setColor(Color.WHITE);
        canvas.drawCircle(ballX, ballY, ballRadius, paint);

        // Paddle
        canvas.drawRect(paddleX, paddleY, paddleX + paddleWidth, paddleY + paddleHeight, paint);

        //Line
        paint.setColor(Color.WHITE);
        canvas.drawLine(0,100,2000,100,paint);
        // Score
        paint.setTextSize(50);
        canvas.drawText("Score: " + score, 50, 80, paint);

        paint.setTextSize(50);
        canvas.drawText("High Score: "+ highscore, 600,80,paint);

        holder.unlockCanvasAndPost(canvas);
    }

    private void control() {
        try {
            Thread.sleep(17); // ~60fps
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_DOWN:
                float touchX = event.getX();
                // Center paddle on touch point
                paddleX = touchX - paddleWidth / 2;
                // Keep paddle within screen bounds
                if (paddleX < 0) paddleX = 0;
                if (paddleX + paddleWidth > getWidth()) paddleX = getWidth() - paddleWidth;
                break;
        }
        return true;
    }

    public void resetGame() {
        score = 0;
        isGameOver = false;
        resetPositions();
    }

    private void resetPositions() {
        ballX = getWidth() / 2f;
        ballY = 200;
        paddleX = (getWidth() - paddleWidth) / 2f;
        paddleY = getHeight() - 200;
        ballSpeedY=10;
        ballSpeedX=10;
    }
    private int setHighscore(){
        if (score>highscore){
            highscore=score;
        }
        return highscore;
    }
}

