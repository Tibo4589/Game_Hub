package be.kuleuven.gt.gamehub;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class TetrisPreviewView extends View {

    private int[][] piece;
    private final Paint paint = new Paint();

    public TetrisPreviewView(Context context) {
        super(context);
    }

    public TetrisPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TetrisPreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPiece(int[][] piece) {
        this.piece = piece;
        invalidate();  // Request redraw
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);

        if (piece == null) return;

        int rows = piece.length;
        int cols = piece[0].length;

        int viewWidth = getWidth();
        int viewHeight = getHeight();

        int cellSize = Math.min(viewWidth / cols, viewHeight / rows);

        int offsetX = (viewWidth - (cellSize * cols)) / 2;
        int offsetY = (viewHeight - (cellSize * rows)) / 2;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (piece[r][c] != 0) {
                    paint.setColor(getColorForValue(piece[r][c]));
                    canvas.drawRect(
                            offsetX + c * cellSize,
                            offsetY + r * cellSize,
                            offsetX + (c + 1) * cellSize,
                            offsetY + (r + 1) * cellSize,
                            paint
                    );
                }
            }
        }
    }

    public static int getColorForValue(int value) {
        switch (value) {
            case 1: return Color.CYAN;    // I
            case 2: return Color.BLUE;    // J
            case 3: return Color.rgb(255, 165, 0); // L
            case 4: return Color.YELLOW;  // O
            case 5: return Color.GREEN;   // S
            case 6: return Color.MAGENTA; // T
            case 7: return Color.RED;     // Z
            default: return Color.GRAY;
        }
    }
}
