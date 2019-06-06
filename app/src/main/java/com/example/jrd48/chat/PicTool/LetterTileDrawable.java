package com.example.jrd48.chat.PicTool;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import com.luobin.dvr.R;

import junit.framework.Assert;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LetterTileDrawable extends Drawable {

    //private final String TAG = LogUtil.PREPEND_TAG + "LetterTileDrawable";

    private final Paint mPaint;

    private static TypedArray sColors;
    private static int sDefaultColor;
    private static int sTileFontColor;
    private static float sLetterToTileRatio;
    private static float sChineseToTileRatio;
    private static Bitmap DEFAULT_PERSON_AVATAR;
    private static Bitmap DEFAULT_BUSINESS_AVATAR;
    private static Bitmap DEFAULT_VOICEMAIL_AVATAR;

    private static final Paint sPaint = new Paint();
    private static final Rect sRect = new Rect();
    private static final char[] sLastChar = new char[1];

    public static final int TYPE_PERSON = 1;
    public static final int TYPE_BUSINESS = 2;
    public static final int TYPE_VOICEMAIL = 3;
    public static final int TYPE_DARK_PERSON = 4;
    public static final int TYPE_DEFAULT = TYPE_PERSON;

    private String mDisplayName;
    private String mIdentifier;
    private int mContactType = TYPE_DEFAULT;
    private float mScale = 1.0f;
    private float mOffset = 0.0f;

    private boolean hasChinese;
    private Typeface typeFaceDefault;
    private Typeface typeFaceEng;

    //@SuppressLint("Recycle")
    public LetterTileDrawable(final Resources res) {
        mPaint = new Paint();
        mPaint.setFilterBitmap(true);
        mPaint.setDither(true);

        if (sColors == null) {
            sColors = res.obtainTypedArray(R.array.letter_tile_colors);
            sDefaultColor = res.getColor(R.color.letter_tile_default_color);
            sTileFontColor = res.getColor(R.color.letter_tile_font_color);
            sLetterToTileRatio = res.getFraction(R.fraction.letter_to_tile_ratio, 1, 1);
            sChineseToTileRatio = res.getFraction(R.fraction.chinese_to_tile_ratio, 1, 1);

            // wangmin modify for change default avatar
            /*
             * DEFAULT_PERSON_AVATAR = BitmapFactory.decodeResource(res,
             * R.drawable.ic_list_item_avatar);
             */
            DEFAULT_PERSON_AVATAR = BitmapFactory.decodeResource(res, R.drawable.man);
            // wangmin modify for change default avatar

            DEFAULT_BUSINESS_AVATAR = BitmapFactory.decodeResource(res, R.drawable.ic_list_item_businessavatar);
            DEFAULT_VOICEMAIL_AVATAR = BitmapFactory.decodeResource(res, R.drawable.ic_list_item_voicemailavatar);

            typeFaceDefault = Typeface.create(res.getString(R.string.letter_tile_letter_font_family), Typeface.NORMAL);
            typeFaceEng = Typeface.create(res.getString(R.string.letter_tile_letter_font_english), Typeface.NORMAL);
            sPaint.setTextAlign(Align.CENTER);
            sPaint.setAntiAlias(true);
        }
    }

    @Override
    public void draw(final Canvas canvas) {
        final Rect bounds = getBounds();
        if (!isVisible() || bounds.isEmpty()) {
            return;
        }

        drawLetterTile(canvas, false);
    }

    private void drawBitmap(final Bitmap bitmap, final int width, final int height, final Canvas canvas) {

        final Rect destRect = copyBounds();

        final int halfLength = (int) (mScale * Math.min(destRect.width(), destRect.height()) / 2);

        destRect.set(destRect.centerX() - halfLength, (int) (destRect.centerY() - halfLength + mOffset * destRect.height()), destRect.centerX() + halfLength,
                (int) (destRect.centerY() + halfLength + mOffset * destRect.height()));

        sRect.set(0, 0, width, height);

        canvas.drawBitmap(bitmap, sRect, destRect, mPaint);
    }

    private void drawLetterTile(final Canvas canvas, boolean circle) {
        // Draw background color.
        sPaint.setColor(pickColor(mIdentifier));

        sPaint.setAlpha(mPaint.getAlpha());
        final Rect bounds = getBounds();
        final int minDimension = Math.min(bounds.width(), bounds.height());

        if (circle) {
            canvas.drawCircle(bounds.centerX(), bounds.centerY(), minDimension / 2, sPaint);
        } else {
            canvas.drawRect(bounds, sPaint);
        }

        // Draw letter/digit only if the first character is an english letter
        if (!TextUtils.isEmpty(mDisplayName)/*
                                             * &&
                                             * isEnglishLetter(mDisplayName.charAt
                                             * (0))
                                             */) {
            String name = mDisplayName.trim();
            if (name.length() > 0) {

                String lastName = getLastName(name);
                sLastChar[0] = Character.toUpperCase(lastName.charAt(0));

                if (hasChinese) {
                    sPaint.setTypeface(typeFaceDefault);
                } else {
                    sPaint.setTypeface(typeFaceEng);
                }

                if (hasChinese) {
                    sPaint.setTextSize(mScale * sChineseToTileRatio * minDimension);
                } else {
                    sPaint.setTextSize(mScale * sLetterToTileRatio * minDimension);
                }

                sPaint.setColor(sTileFontColor);

                FontMetricsInt fontMetrics = sPaint.getFontMetricsInt();
                float baseline = bounds.top + (bounds.bottom - bounds.top) / 2 - (fontMetrics.bottom - fontMetrics.ascent) / 2 - fontMetrics.ascent;

                canvas.drawText(sLastChar, 0, 1, bounds.centerX(), baseline, sPaint);

            } else {

                final Bitmap bitmap = getBitmapForContactType(mContactType);
                drawBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), canvas);
            }
        } else {

            final Bitmap bitmap = getBitmapForContactType(mContactType);
            if (bitmap != null) {
                drawBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), canvas);
            }
        }
    }

    public int getColor() {
        return pickColor(mIdentifier);
    }

    private int pickColor(final String identifier) {
        if (TextUtils.isEmpty(identifier) || mContactType == TYPE_VOICEMAIL) {
            return sDefaultColor;
        }

        final int color = Math.abs(identifier.hashCode()) % sColors.length();
        return sColors.getColor(color, sDefaultColor);
    }

    private static Bitmap getBitmapForContactType(int contactType) {
        switch (contactType) {
        case TYPE_PERSON:
            return DEFAULT_PERSON_AVATAR;
        case TYPE_BUSINESS:
            return DEFAULT_BUSINESS_AVATAR;
        case TYPE_VOICEMAIL:
            return DEFAULT_VOICEMAIL_AVATAR;
        default:
            return DEFAULT_PERSON_AVATAR;
        }
    }

    @SuppressWarnings("unused")
    private static boolean isEnglishLetter(final char c) {
        return ('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z');
    }

    @Override
    public void setAlpha(final int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(final ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return android.graphics.PixelFormat.OPAQUE;
    }

    public void setScale(float scale) {
        mScale = scale;
    }

    public void setOffset(float offset) {
        Assert.assertTrue(offset >= -0.5f && offset <= 0.5f);
        mOffset = offset;
    }

    public void setContactDetails(final String displayName, final String identifier) {
        mDisplayName = displayName;
        mIdentifier = identifier;
    }

    public void setContactType(int contactType) {
        mContactType = contactType;
    }

    public boolean hasChinese(String value) {
        if (value == null)
            return false;

        byte[] valueByte = value.getBytes();
        return valueByte.length != value.length();
    }

    public String getLastName(String name) {
        if (TextUtils.isEmpty(name)) {
            return "";
        }

        if (!hasChinese(name)) {
            hasChinese = false;

            String regEx = "[~!@#$%^&*()+=|{}\':;,\\[\\].<>/?~]";
            Pattern pat = Pattern.compile(regEx);
            Matcher mat = pat.matcher(name);
            String leftName = mat.replaceAll("").trim();
            if (!TextUtils.isEmpty(leftName)) {
                return leftName.trim().substring(0, 1);
            }

            return name.trim().substring(0, 1);
        } else {

            hasChinese = true;
        }

        return name.trim();
    }
}
