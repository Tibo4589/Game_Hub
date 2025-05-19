package be.kuleuven.gt.gamehub;

import static be.kuleuven.gt.gamehub.TetrisPreviewView.getColorForValue;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;
import java.util.function.Consumer;

public class TetrisView extends SurfaceView implements Runnable {
    private Thread gameThread;
    private boolean isPlaying;
    private SurfaceHolder holder;
    private Paint paint;
    private Random random = new Random();
    private GameOverListener gameOverListener;
    protected static int scoreTetris = 0;
    public static int highscoreTetris;
    private TetrisGameLogic logic;
    private Consumer<int[][]> nextPieceChangeListener;
    private Consumer<int[][]> heldPieceChangeListener;



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

    public void setLogic(TetrisGameLogic logic) {
        this.logic = logic;

        logic.onNextPieceChange = next -> {
            if (nextPieceChangeListener != null) nextPieceChangeListener.accept(next);
        };

        logic.onHoldPieceChange = hold -> {
            if (heldPieceChangeListener != null) heldPieceChangeListener.accept(hold);
        };

        logic.onScoreChange = score -> {
            scoreTetris = score;
            if (scoreChangeListener != null) scoreChangeListener.onScoreChanged(score);
        };
    }

    private void init() {
        holder = getHolder();
        paint = new Paint();

        logic = new TetrisGameLogic();
        logic.nextPiece = TetrisValues.TETROMINOES[random.nextInt(TetrisValues.TETROMINOES.length)];
        logic.reset();

        logic.onNextPieceChange = next -> {
            if (nextPieceChangeListener != null) nextPieceChangeListener.accept(next);
        };

        logic.onHoldPieceChange = hold -> {
            if (heldPieceChangeListener != null) heldPieceChangeListener.accept(hold);
        };

        logic.onScoreChange = score -> {
            scoreTetris = score;
            if (scoreChangeListener != null) scoreChangeListener.onScoreChanged(score);
        };
    }

    public void setGameOverListener(GameOverListener listener) {
        this.gameOverListener = listener;
    }

    public void setScoreChangeListenerTetris(OnScoreChangeListener listener) {
        this.scoreChangeListener = listener;
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
        }
    }

    private void update() {
        if (!logic.collides(1, 0, logic.currentPiece)) {
            logic.move(1, 0);
        } else {
            boolean gameOver = logic.lockPieceAndCheckGameOver();
            if (gameOver) {
                isPlaying = false;
                if (gameOverListener != null) gameOverListener.onGameOver(scoreTetris);
            }
        }

        try {
            Thread.sleep(Math.max(300, (850 - logic.level * 50)));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void draw() {
        if (!holder.getSurface().isValid()) return;
        Canvas canvas = holder.lockCanvas();
        canvas.drawColor(0xFF32618E);

        int cellSize = Math.min(getWidth() / TetrisValues.COLS, getHeight() / TetrisValues.ROWS);

        paint.setColor(Color.LTGRAY);
        canvas.drawRect(0, 0, cellSize * TetrisValues.COLS, cellSize * TetrisValues.ROWS, paint);

        paint.setColor(0xFF03DAC5);
        for (int r = 0; r < TetrisValues.ROWS; r++) {
            for (int c = 0; c < TetrisValues.COLS; c++) {
                if (logic.grid[r][c] != 0) {
                    canvas.drawRect(c * cellSize, r * cellSize, (c + 1) * cellSize, (r + 1) * cellSize, paint);
                }
            }
        }

        paint.setStrokeWidth(4);
        paint.setColor(Color.BLACK);
        for (int r = 0; r <= TetrisValues.ROWS; r++) {
            canvas.drawLine(0, cellSize * r, cellSize * TetrisValues.COLS, cellSize * r, paint);
        }
        for (int c = 0; c <= TetrisValues.COLS; c++) {
            canvas.drawLine(cellSize * c, 0, cellSize * c, cellSize * TetrisValues.ROWS, paint);
        }

        for (int r = 0; r < logic.currentPiece.length; r++) {
            for (int c = 0; c < logic.currentPiece[0].length; c++) {
                if (logic.currentPiece[r][c] != 0) {
                    paint.setColor(getColorForValue(logic.currentPiece[r][c]));
                    canvas.drawRect((logic.pieceCol + c) * cellSize, (logic.pieceRow + r) * cellSize,
                            (logic.pieceCol + c + 1) * cellSize, (logic.pieceRow + r + 1) * cellSize, paint);
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
        logic.reset();
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
}