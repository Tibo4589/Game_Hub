package be.kuleuven.gt.gamehub;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.util.TimeUtils;

import java.lang.Math;

// This constructor is required for inflation from XML!
public class PongView extends SurfaceView implements Runnable, Listeners {
    private Thread gameThread;
    private boolean isPlaying;
    private Paint paint;
    private float ballX = getWidth()/2f, ballY=200, ballRadius = 20;
    private float ballSpeedX , ballSpeedY;
    private float paddleX, paddleY, paddleWidth = 300, paddleHeight = 30;
    private SurfaceHolder holder;

    private boolean isGameOver = false;
    public int score = 0;
    public static int highscore;

    private OnScoreChangeListener scoreChangeListener;
    private GameOverListener gameOverListener;
    public void setScoreChangeListenerPong(OnScoreChangeListener listener) {
        this.scoreChangeListener = listener;
    }

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
            control();
            update();
            draw();
            setHighscore();
            invalidate();
        }
        setHighscore();
    }

    public void setGameOverListener(GameOverListener listener) {
        this.gameOverListener = listener;
    }

    private void update() {
        if (isGameOver) return;

        float nextBallX = ballX + ballSpeedX;
        float nextBallY = ballY + ballSpeedY;

        boolean isHittingLeftWall = nextBallX <= ballRadius;
        boolean isHittingRightWall = nextBallX >= getWidth() - ballRadius;
        boolean isHittingTopWall = nextBallY <= ballRadius;

        if (isHittingLeftWall || isHittingRightWall) {
            ballSpeedX *= -1;
        }

        if (isHittingTopWall) {
            ballSpeedY *= -1;
        }

        boolean isCrossingPaddle = ballY + ballRadius <= paddleY && nextBallY + ballRadius >= paddleY;

        if (isCrossingPaddle && ballX >= paddleX  && ballX <= paddleX + paddleWidth) {
            ballY = paddleY - ballRadius;
            ballSpeedY *= (float) -(1 + Math.random()*0.2);

            ballSpeedX += (float) (Math.random() * 10 - 5);

            ballSpeedX = Math.max(-80, Math.min(80, ballSpeedX));
            ballSpeedY = Math.max(-80, Math.min(80, ballSpeedY));

            score++;
            if (scoreChangeListener != null) {
                scoreChangeListener.onScoreChanged(score);
            }
            if (score > 20) {
                paddleWidth -= 10;
            }
            if (paddleWidth < 100) {
                paddleWidth = 100;
            }
        }

        ballX += ballSpeedX;
        ballY += ballSpeedY;

        if (ballY > paddleY + ballRadius) {
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


        holder.unlockCanvasAndPost(canvas);
        postInvalidateOnAnimation();
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
        paddleY = getHeight() - 300;
        ballSpeedY=20;
        ballSpeedX=20;
    }
    private void setHighscore(){
        if (score>highscore){
            highscore=score;
        }
    }
}

