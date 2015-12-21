package com.ccn.zxing.zcode;

import java.util.Collection;
import java.util.HashSet;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;

public final class ViewfinderView extends View {
    /**
     * 动画延迟
     */
    private static final long ANIMATION_DELAY = 10L;
    private static final int OPAQUE = 0xFF;// 不透明

    /**
     * 四个蓝色边角对应的宽度
     */
    private static final int CORNER_WIDTH = 16;
    /**
     * 扫描框中的中间线的与扫描框左右的间隙
     */
    private static final int MIDDLE_LINE_PADDING = 3;

    /**
     * 中间那条线每次刷新移动的距离
     */
    private static final int SPEEN_DISTANCE = 5;

    /**
     * 手机的屏幕密度
     */
    private static float density;
    /**
     * 字体大小
     */
    private static final int TEXT_SIZE = 13;
    /**
     * 字体距离扫描框下面的距离
     */
    private static final int TEXT_PADDING_TOP = 30;

    private final Paint paint;
    /**
     * 返回的照片
     */
    private Bitmap resultBitmap;

    /**
     * 透明
     */
    private final int transparent;
    /**
     * 半透明
     */
    private final int translucence;
    /**
     * 结果点的颜色
     */
    private final int resultPointColor;
    /**
     * 可能的结果点数
     */
    private Collection<ResultPoint> possibleResultPoints;
    /**
     * 最后的结果点数
     */
    private Collection<ResultPoint> lastPossibleResultPoints;

    /**
     * 中间滑动线的最顶端位置
     */
    private int slideTop;

    /**
     * 中间滑动线的最底端位置
     */
    private int slideBottom;
    private boolean isFirst;

    // This constructor is used when the class is built from an XML resource.
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = context.getResources().getDisplayMetrics().density;
        paint = new Paint();
        Resources resources = getResources();
        transparent = resources.getColor(R.color.transparent);
        translucence = resources.getColor(R.color.translucence);
        resultPointColor = resources.getColor(R.color.possible_result_points);
        possibleResultPoints = new HashSet<ResultPoint>(5);

    }

    public ViewfinderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // TODO Auto-generated constructor stub
        density = context.getResources().getDisplayMetrics().density;
        paint = new Paint();
        Resources resources = getResources();
        transparent = resources.getColor(R.color.transparent);
        translucence = resources.getColor(R.color.translucence);
        resultPointColor = resources.getColor(R.color.possible_result_points);
        possibleResultPoints = new HashSet<ResultPoint>(5);
    }

    public ViewfinderView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        density = context.getResources().getDisplayMetrics().density;
        paint = new Paint();
        Resources resources = getResources();
        transparent = resources.getColor(R.color.transparent);
        translucence = resources.getColor(R.color.translucence);
        resultPointColor = resources.getColor(R.color.possible_result_points);
        possibleResultPoints = new HashSet<ResultPoint>(5);
    }

    @Override
    public void onDraw(Canvas canvas) {
        // 中间的扫描框，你要修改扫描框的大小，去CameraManager里面修改
        Rect frame = CameraManager.get().getFramingRect();
        if (frame == null) {
            return;
        }

        // 初始化中间线滑动的最上边和最下边
        if (!isFirst) {
            isFirst = true;
            slideTop = frame.top + CORNER_WIDTH;
            slideBottom = frame.bottom - CORNER_WIDTH;
        }

        // 获取屏幕的宽和高
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // 画出扫描框外面的阴影部分，共四个部分，扫描框的上面到屏幕上面，扫描框的下面到屏幕下面
        // 扫描框的左边面到屏幕左边，扫描框的右边到屏幕右边
        paint.setColor(translucence);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left + 2, frame.bottom + 1, paint);
        canvas.drawRect(frame.right - 4, frame.top, width, frame.bottom + 1,
                paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);
        paint.setColor(translucence);
        if (resultBitmap != null) {
            paint.setAlpha(OPAQUE);
            canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
        } else {
            // 直接用图片
//			Rect bigRect = new Rect();
//			bigRect.left = frame.left;
//			bigRect.right = frame.right;
//			bigRect.top = frame.top;
//			bigRect.bottom = frame.bottom;
//			Drawable drawable = getResources().getDrawable(
//					R.mipmap.scanning_frame);
//			BitmapDrawable b = (BitmapDrawable) drawable;
//			canvas.drawBitmap(b.getBitmap(), null, bigRect, paint);

            // 画中间移动的线
            slideTop += SPEEN_DISTANCE;
            if (slideTop >= slideBottom) {
                slideTop = frame.top + CORNER_WIDTH;
            }

            // 用图片
            Rect lineRect = new Rect();
            lineRect.left = frame.left;
            lineRect.right = frame.right;
            lineRect.top = slideTop;
            lineRect.bottom = slideTop + MIDDLE_LINE_PADDING;
            /*自定义直线*/
            Paint paint1 = new Paint();
            int[] SHADOWS_COLORS = new int[]{0x000000ff, 0xdd0000ff,
                    0xff0000ff, 0xdd0000ff, 0x000000ff};
            paint1.setStyle(Style.FILL);
            Shader mShader = new LinearGradient(lineRect.left, lineRect.top,
                    lineRect.right, lineRect.bottom, SHADOWS_COLORS, null,
                    Shader.TileMode.REPEAT);
            paint1.setShader(mShader);
            canvas.drawRect(lineRect, paint1);

//			canvas.drawBitmap(((BitmapDrawable) (getResources()
//					.getDrawable(R.drawable.scanning_line))).getBitmap(), null,
//					lineRect, paint);

            // 画扫描框下面的字
            paint.setColor(Color.WHITE);
            paint.setTextSize(TEXT_SIZE * density);
            paint.setTextAlign(Align.CENTER);
            paint.setAlpha(0x80);
            paint.setTypeface(Typeface.create("System", Typeface.BOLD));
            canvas.drawText(getResources().getString(R.string.scanning_hint),
                    width / 2, frame.bottom + TEXT_PADDING_TOP * density, paint);

            Collection<ResultPoint> currentPossible = possibleResultPoints;
            Collection<ResultPoint> currentLast = lastPossibleResultPoints;
            if (currentPossible.isEmpty()) {
                lastPossibleResultPoints = null;
            } else {
                possibleResultPoints = new HashSet<ResultPoint>(5);
                lastPossibleResultPoints = currentPossible;
                paint.setAlpha(OPAQUE);
                paint.setColor(resultPointColor);
                for (ResultPoint point : currentPossible) {
                    canvas.drawCircle(frame.left + point.getX(), frame.top
                            + point.getY(), 6.0f, paint);// 画扫描到的可能的点
                }
            }
            if (currentLast != null) {
                paint.setAlpha(OPAQUE / 2);
                paint.setColor(resultPointColor);
                for (ResultPoint point : currentLast) {
                    canvas.drawCircle(frame.left + point.getX(), frame.top
                            + point.getY(), 3.0f, paint);
                }
            }

            // 只刷新扫描框的内容，其他地方不刷新
            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top,
                    frame.right, frame.bottom);
        }
    }

    public void drawViewfinder() {
        resultBitmap = null;
        invalidate();
    }

    /**
     * Draw a bitmap with the result points highlighted instead of the live
     * scanning display.
     * @param barcode An image of the decoded barcode.
     */
    public void drawResultBitmap(Bitmap barcode) {
        resultBitmap = barcode;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        possibleResultPoints.add(point);
    }

}
