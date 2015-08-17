package org.warren.popularmovies;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Warren on 8/16/2015.
 */
public class TriangleView extends View {
    private static final int DEFAULT_SIZE = 50;
    private Paint mPaint;
    private Path mPath;

    public TriangleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        int scaledDefault = (int) (DEFAULT_SIZE * getResources().getDisplayMetrics().density);
        Point point1_draw = new Point(0, 0);
        Point point2_draw = new Point((int) (scaledDefault * 0.5f * (float) Math.sqrt(3)), (int)(scaledDefault * 0.5f));
        Point point3_draw = new Point(0, scaledDefault);

        mPath = new Path();
        mPath.setFillType(Path.FillType.EVEN_ODD);
        mPath.moveTo(point1_draw.x, point1_draw.y);
        mPath.lineTo(point2_draw.x, point2_draw.y);
        mPath.lineTo(point3_draw.x, point3_draw.y);
        mPath.close();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(calculateMeasure(widthMeasureSpec), calculateMeasure(heightMeasureSpec));
    }

    private int calculateMeasure(int measureSpec) {
        int result = (int) (DEFAULT_SIZE * getResources().getDisplayMetrics().density);
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(result, specSize);
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        int scale = getWidth();
        canvas.drawPath(mPath, mPaint);
        canvas.scale(scale, scale);
        canvas.restore();
    }
}
