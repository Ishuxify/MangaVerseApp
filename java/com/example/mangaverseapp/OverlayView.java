package com.example.mangaverseapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult;

public class OverlayView extends View {
    private static final String TAG = "OverlayView";
    private FaceDetectorResult results;
    private final Paint boxPaint;
    private final Paint textBackgroundPaint;
    private final Paint textPaint;
    private final Paint referenceRectPaint;
    private final Rect bounds = new Rect();
    private RectF faceRect;
    private boolean isWithinReference;
    private RectF referenceRect;

    public OverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        boxPaint = new Paint();
        textBackgroundPaint = new Paint();
        textPaint = new Paint();
        referenceRectPaint = new Paint();
        initPaints();
    }

    private void initPaints() {
        boxPaint.setStrokeWidth(8f);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark));

        textBackgroundPaint.setColor(Color.BLACK);
        textBackgroundPaint.setStyle(Paint.Style.FILL);
        textBackgroundPaint.setTextSize(50f);

        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(50f);

        referenceRectPaint.setStyle(Paint.Style.STROKE);
        referenceRectPaint.setStrokeWidth(5f);
        referenceRectPaint.setColor(Color.WHITE);
    }

    public void clear() {
        synchronized (this) {
            results = null;
            faceRect = null;
            isWithinReference = false;
            initPaints(); // Reset paint attributes safely
            invalidate();
        }
    }

    public void setResults(FaceDetectorResult detectionResults, int imageHeight, int imageWidth) {
        synchronized (this) {
            this.results = detectionResults;
            Log.d(TAG, "setResults called with " + detectionResults.detections().size() + " detections");
            invalidate();
        }
    }

    public void setFaceRect(RectF rect) {
        synchronized (this) {
            this.faceRect = rect;
            Log.d(TAG, "setFaceRect: " + (rect != null ? rect.toString() : "null"));
        }
    }

    public void setRectColor(boolean isWithin) {
        synchronized (this) {
            this.isWithinReference = isWithin;
            boxPaint.setColor(isWithin ? Color.GREEN : Color.RED);
            Log.d(TAG, "setRectColor: isWithin=" + isWithin);
            invalidate();
        }
    }

    public RectF getReferenceRect() {
        if (referenceRect == null || getWidth() <= 0 || getHeight() <= 0) {
            float width = getWidth() * 0.5f;
            float height = getHeight() * 0.5f;
            float left = (getWidth() - width) / 2;
            float top = (getHeight() - height) / 2;
            referenceRect = new RectF(left, top, left + width, top + height);
            Log.d(TAG, "getReferenceRect: " + referenceRect.toString());
        }
        return referenceRect;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        synchronized (this) {
            // Draw reference rectangle
            RectF refRect = getReferenceRect();
            canvas.drawRect(refRect, referenceRectPaint);

            // Draw face rectangle
            if (faceRect != null) {
                canvas.drawRect(faceRect, boxPaint);
                Log.d(TAG, "Drawing faceRect: " + faceRect.toString() + ", isWithinReference=" + isWithinReference);
            } else {
                // Draw default red rectangle when no face is detected
                canvas.drawRect(refRect, boxPaint);
                Log.d(TAG, "Drawing default red rect: " + refRect.toString());
            }

            // Draw confidence score if detection exists
            if (results != null && !results.detections().isEmpty()) {
                RectF detectionRect = results.detections().get(0).boundingBox();
                String drawableText = "";
                if (!results.detections().get(0).categories().isEmpty()) {
                    drawableText = String.format("%.2f", results.detections().get(0).categories().get(0).score());
                } else {
                    drawableText = "Face detected";
                }

                textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length(), bounds);
                int textWidth = bounds.width();
                int textHeight = bounds.height();
                canvas.drawRect(
                        detectionRect.left,
                        detectionRect.top,
                        detectionRect.left + textWidth + BOUNDING_RECT_TEXT_PADDING,
                        detectionRect.top + textHeight + BOUNDING_RECT_TEXT_PADDING,
                        textBackgroundPaint
                );

                canvas.drawText(drawableText, detectionRect.left, detectionRect.top + bounds.height(), textPaint);
            }
        }
    }

    private static final int BOUNDING_RECT_TEXT_PADDING = 8;
}