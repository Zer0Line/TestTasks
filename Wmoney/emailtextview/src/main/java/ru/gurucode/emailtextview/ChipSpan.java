package ru.gurucode.emailtextview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.style.ImageSpan;
import android.util.Log;

public class ChipSpan extends ImageSpan {
    //Уменьшение аватара
    private static final float SCALE_PERCENT_OF_CHIP_HEIGHT = 0.8f;

    private int mPadding = 5;

    private final Drawable mIcon;
    private final CharSequence mText;
    private String mTextToDraw;

    //Текст
    private int mTextSize = -1;
    private final int mTextColor;

    private final int mBackgroundColor;

    //Паддинги
    private final int mPaddingEdgePx;
    private final int mPaddingBetweenImagePx;
    private final int mLeftMarginPx;
    private final int mRightMarginPx;

    private int mChipWidth = -1;
    private int mIconWidth;
    private int mCachedSize = -1;

    private Drawable mDrawableBackground = null;

    //Конструктор
    public ChipSpan(
            @NonNull Context context,
            @NonNull CharSequence text,
            @Nullable Drawable icon,
            @Nullable Drawable drawableBackground) {

        super(icon);
        mIcon = icon;

        mText = text;

        mTextColor = ContextCompat.getColor(context, R.color.chip_default_text_color);
        mTextToDraw = mText.toString();

        mBackgroundColor = ContextCompat.getColor(
                context,
                R.color.chip_default_background_color);

        String mEllipsis = context.getString(R.string.chip_ellipsis);

        Resources resources = context.getResources();
        mPaddingEdgePx = resources.getDimensionPixelSize(R.dimen.chip_default_padding_edge);
        mPaddingBetweenImagePx = resources.getDimensionPixelSize(R.dimen.chip_default_padding_between_image);
        mLeftMarginPx = resources.getDimensionPixelSize(R.dimen.chip_default_left_margin);
        mRightMarginPx = resources.getDimensionPixelSize(R.dimen.chip_default_right_margin);

        mDrawableBackground = (drawableBackground != null) ?
                drawableBackground : null;
    }

    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        boolean usingFontMetrics = (fm != null);

        if (mCachedSize == -1 && usingFontMetrics) {
            mIconWidth = (mIcon != null) ? calculateChipHeight(fm.top, fm.bottom) : 0;

            mCachedSize = calculateActualWidth(paint);

        }

        return mCachedSize;
    }

    public int getWidth() {
        return mLeftMarginPx + mChipWidth + mRightMarginPx;
    }

    private int calculateChipHeight(int top, int bottom) {
        // If a chip height was set we can return that, otherwise calculate it from top and bottom
        return bottom - top;
    }

    private int calculateActualWidth(Paint paint) {
        if (mTextSize != -1) {
            paint.setTextSize(mTextSize);
        }

        int totalPadding = mPaddingEdgePx;

        Rect bounds = new Rect();
        paint.getTextBounds(mTextToDraw, 0, mTextToDraw.length(), bounds);
        int textWidth = bounds.width();

        if (mIcon != null) {
            totalPadding += mPaddingBetweenImagePx;
        } else {
            totalPadding += mPaddingEdgePx;
        }

        mChipWidth = totalPadding + textWidth + mIconWidth;
        return getWidth();
    }

    //Отрисовка всего чипа. Бэкграунд, автар, текст.
    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, Paint paint) {

        x += mLeftMarginPx;

        if (mDrawableBackground != null) {
            drawDrawableBackground(canvas, x, top, bottom, paint);
        } else {
            drawBackground(canvas, x, top, bottom, paint);
        }

        drawText(canvas, x, top, bottom, paint, mTextToDraw);

        if (mIcon != null) {
            drawIconBitmap(canvas, x, top, bottom, paint);
        }
    }

    private void drawDrawableBackground(Canvas canvas, float x, int top, int
            bottom, Paint paint) {
        paint.setColor(mBackgroundColor);
        mDrawableBackground.setBounds((int) x, top + mPadding, (int) x +
                        mChipWidth, bottom - mPadding);
        mDrawableBackground.draw(canvas);
        paint.setColor(mTextColor);
    }

    private void drawBackground(Canvas canvas, float x, int top, int bottom, Paint paint) {
        paint.setColor(mBackgroundColor);
        int height = calculateChipHeight(top, bottom);
        RectF rect = new RectF(x, top + mPadding, x + mChipWidth, bottom - mPadding);
        int cornerRadius = height / 2;
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
        paint.setColor(mTextColor);
    }

    private void drawText(Canvas canvas, float x, int top, int bottom, Paint paint, CharSequence text) {
        if (mTextSize != -1) {
            paint.setTextSize(mTextSize);
        }

        int height = calculateChipHeight(top, bottom);
        Paint.FontMetrics fm = paint.getFontMetrics();

        float adjustedY = top + ((height / 2) + ((-fm.top - fm.bottom) / 2));

        float adjustedX = x + mIconWidth + mPaddingBetweenImagePx;

        canvas.drawText(text, 0, text.length(), adjustedX, adjustedY -
                mPadding, paint);
    }


    private void drawIconBitmap(Canvas canvas, float x, int top, int bottom, Paint paint) {
        int height = calculateChipHeight(top, bottom);

        Bitmap iconBitmap = Bitmap.createBitmap(
                mIcon.getIntrinsicWidth(),
                mIcon.getIntrinsicHeight(),
                Bitmap.Config.ARGB_4444);

        Bitmap scaledIconBitMap = scaleDown(
                iconBitmap,
                (float) height * SCALE_PERCENT_OF_CHIP_HEIGHT,
                true);

        iconBitmap.recycle();
        Canvas bitmapCanvas = new Canvas(scaledIconBitMap);
        mIcon.setBounds(0, 0, bitmapCanvas.getWidth(), bitmapCanvas.getHeight());
        mIcon.draw(bitmapCanvas);

        float yInsetWithinCircle = (height - bitmapCanvas.getHeight()) / 2;
        float iconY = top + yInsetWithinCircle;

        canvas.drawBitmap(scaledIconBitMap, x, iconY, paint);
    }

    private Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min(
                maxImageSize / realImage.getWidth(),
                maxImageSize / realImage.getHeight());

        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());
        return Bitmap.createScaledBitmap(realImage, width, height, filter);
    }

}
