package soutvoid.com.sudokusolver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

public class SudokuView extends View {
    private static final String TAG = "Sudoku";
    private int[][] game;

    public SudokuView(Context context, int[][] game) {
        super(context);
        this.game = game;
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    private float width;
    private float height;
    private int selX;
    private int selY;
    private final Rect selRect = new Rect();

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w / 9f;
        height = h / 9f;
        getRect(selX, selY, selRect);
        Log.d(TAG, "onSizeChanged: width " + width + " height " + height);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void getRect(int x, int y, Rect rect) {
        rect.set((int) (x * width), (int) (y * height), (int) (x * width + width), (int) (y * height + height));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the background
        Paint background = new Paint();
        background.setColor(getResources().getColor(android.R.color.white));
        canvas.drawRect(0, 0, getWidth(), getHeight(), background);

        // Draw the board
        // Definte colors for the grid lines
        Paint dark = new Paint();
        dark.setColor(getResources().getColor(android.R.color.black));

        Paint hilite = new Paint();
        hilite.setColor(getResources().getColor(android.R.color.holo_green_dark));

        Paint light = new Paint();
        light.setColor(getResources().getColor(android.R.color.holo_blue_light));

        // Draw the minor grid lines
        for (int i = 0; i < 9; i++) {
            canvas.drawLine(0, i * height, getWidth(), i * height, light);
            canvas.drawLine(0, i * height + 1, getWidth(), i * height + 1, hilite);
            canvas.drawLine(i * width, 0, i * width, getHeight(), light);
            canvas.drawLine(i * width + 1, 0, i * width + 1, getHeight(), hilite);
        }

        // Draw the major grid lines
        for (int i = 0; i < 9; i++) {
            if (i % 3 != 0)
                continue;
            canvas.drawLine(0, i * height, getWidth(), i * height, dark);
            canvas.drawLine(0, i * height + 1, getWidth(), i * height + 1, hilite);
            canvas.drawLine(i * width, 0, i * width, getHeight(), dark);
            canvas.drawLine(i * width + 1, 0, i * width + 1, getHeight(), hilite);
        }

        // Draw the numbers
        // Define color and style for numbers
        Paint foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
        foreground.setColor(getResources().getColor(android.R.color.black));
        foreground.setStyle(Style.FILL);
        foreground.setTextSize(height * 0.75f);
        foreground.setTextScaleX(width / height);
        foreground.setTextAlign(Paint.Align.CENTER);

        // Draw the number in the center of the tile
        FontMetrics fm = foreground.getFontMetrics();
        // Centering on X: use alignment (and X at midpoint)
        float x = width / 2;
        // Centering on Y: measure ascent/descent first
        float y = height / 2 - (fm.ascent + fm.descent) / 2;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                canvas.drawText(String.valueOf(this.game[j][i]), i * width + x, j * height + y, foreground);
            }
        }
    }

}