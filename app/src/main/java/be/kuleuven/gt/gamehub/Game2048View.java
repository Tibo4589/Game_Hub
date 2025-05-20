package be.kuleuven.gt.gamehub;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class Game2048View extends View {

    private Paint paint;
    private Game2048Logic logic;
    protected static final int GRID_SIZE = 4;
    private boolean isPlaying = true;


    public Game2048View(Context context) {
        super(context);
        init();
    }

    public Game2048View(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public void setLogic(Game2048Logic logic){
        this.logic = logic;
        invalidate();
    }

    protected void init() {
        paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int cellSize = Math.min(width, height) / GRID_SIZE;
        canvas.drawColor(Color.WHITE);

        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                drawTile(canvas, x, y, logic.getTileValue(x, y), cellSize);
            }
        }
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(8);
        for (int x = 0; x<=GRID_SIZE;x++) {
            canvas.drawLine(0,cellSize*x,cellSize*GRID_SIZE,cellSize*x,paint);
            canvas.drawLine(cellSize*x,0,cellSize*x,cellSize*GRID_SIZE,paint);
        }
    }

    private void drawTile(Canvas canvas, int x, int y, int value, int cellSize) {
        paint.setColor(TileColor2048.getColor(value));
        int left = x * cellSize;
        int top = y * cellSize;
        int right = left + cellSize;
        int bottom = top + cellSize;
        canvas.drawRect(left, top, right, bottom, paint);

        if (value != 0 && value < 10000) {
            paint.setColor(Color.BLACK);
            paint.setTextSize(cellSize / 3f);}
        else {
            paint.setColor(Color.WHITE);
            paint.setTextSize(cellSize/4);
        }
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(String.valueOf(value), left + cellSize / 2f, top + cellSize / 1.7f, paint);

    }

    public void pause() {
        isPlaying = false;
    }

    public void resume() {
        isPlaying = true;
    }
}
