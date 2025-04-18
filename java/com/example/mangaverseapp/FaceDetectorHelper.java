package com.example.mangaverseapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.VisibleForTesting;
import androidx.camera.core.ImageProxy;

import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.core.Delegate;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector;
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult;

import java.util.Collections;
import java.util.List;

public class FaceDetectorHelper {
    private static final String TAG = "FaceDetectorHelper";
    private float threshold;
    private int currentDelegate;
    private RunningMode runningMode;
    private final Context context;
    private DetectorListener faceDetectorListener;
    private FaceDetector faceDetector;

    public FaceDetectorHelper(float threshold, int currentDelegate, RunningMode runningMode, Context context, DetectorListener faceDetectorListener) {
        this.threshold = threshold;
        this.currentDelegate = currentDelegate;
        this.runningMode = runningMode;
        this.context = context;
        this.faceDetectorListener = faceDetectorListener;
        setupFaceDetector();
    }

    public float getThreshold() {
        return threshold;
    }

    public int getCurrentDelegate() {
        return currentDelegate;
    }

    public void clearFaceDetector() {
        if (faceDetector != null) {
            faceDetector.close();
            faceDetector = null;
        }
    }

    public void setupFaceDetector() {
        BaseOptions.Builder baseOptionsBuilder = BaseOptions.builder();

        switch (currentDelegate) {
            case FaceFragment.DELEGATE_CPU:
                baseOptionsBuilder.setDelegate(Delegate.CPU);
                break;
            case FaceFragment.DELEGATE_GPU:
                baseOptionsBuilder.setDelegate(Delegate.GPU);
                break;
        }

        String modelName = "face_detection_short_range.tflite";
        baseOptionsBuilder.setModelAssetPath(modelName);

        if (runningMode == RunningMode.LIVE_STREAM && faceDetectorListener == null) {
            throw new IllegalStateException("faceDetectorListener must be set when runningMode is LIVE_STREAM.");
        }

        try {
            FaceDetector.FaceDetectorOptions.Builder optionsBuilder = FaceDetector.FaceDetectorOptions.builder()
                    .setBaseOptions(baseOptionsBuilder.build())
                    .setMinDetectionConfidence(threshold)
                    .setRunningMode(runningMode);

            if (runningMode == RunningMode.LIVE_STREAM) {
                optionsBuilder.setResultListener(this::returnLivestreamResult)
                        .setErrorListener(this::returnLivestreamError);
            }

            FaceDetector.FaceDetectorOptions options = optionsBuilder.build();
            faceDetector = FaceDetector.createFromOptions(context, options);
            Log.d(TAG, "FaceDetector initialized successfully");
        } catch (IllegalStateException e) {
            if (faceDetectorListener != null) {
                faceDetectorListener.onError("Face detector failed to initialize. See error logs for details", FaceFragment.OTHER_ERROR);
            }
            Log.e(TAG, "TFLite failed to load model with error: " + e.getMessage());
        } catch (RuntimeException e) {
            if (faceDetectorListener != null) {
                faceDetectorListener.onError("Face detector failed to initialize. See error logs for details", FaceFragment.GPU_ERROR);
            }
            Log.e(TAG, "Face detector failed to load model with error: " + e.getMessage());
        }
    }

    public boolean isClosed() {
        return faceDetector == null;
    }

    public void detectLivestreamFrame(ImageProxy imageProxy) {
        if (runningMode != RunningMode.LIVE_STREAM) {
            throw new IllegalArgumentException("Attempting to call detectLivestreamFrame while not using RunningMode.LIVE_STREAM");
        }

        long frameTime = SystemClock.uptimeMillis();
        Bitmap bitmapBuffer = null;
        Bitmap rotatedBitmap = null;

        try {
            if (imageProxy.getWidth() <= 0 || imageProxy.getHeight() <= 0) {
                Log.w(TAG, "Invalid image dimensions: " + imageProxy.getWidth() + "x" + imageProxy.getHeight());
                return;
            }

            bitmapBuffer = Bitmap.createBitmap(
                    imageProxy.getWidth(),
                    imageProxy.getHeight(),
                    Bitmap.Config.ARGB_8888
            );
            bitmapBuffer.copyPixelsFromBuffer(imageProxy.getPlanes()[0].getBuffer());

            Matrix matrix = new Matrix();
            matrix.postRotate(imageProxy.getImageInfo().getRotationDegrees());
            matrix.postScale(-1f, 1f, imageProxy.getWidth(), imageProxy.getHeight());

            rotatedBitmap = Bitmap.createBitmap(
                    bitmapBuffer,
                    0,
                    0,
                    bitmapBuffer.getWidth(),
                    bitmapBuffer.getHeight(),
                    matrix,
                    true
            );

            MPImage mpImage = new BitmapImageBuilder(rotatedBitmap).build();
            detectAsync(mpImage, frameTime);
        } catch (Exception e) {
            Log.e(TAG, "Error processing ImageProxy: " + e.getMessage(), e);
            if (faceDetectorListener != null) {
                faceDetectorListener.onError("Image processing failed: " + e.getMessage(), FaceFragment.OTHER_ERROR);
            }
        } finally {
            if (bitmapBuffer != null && !bitmapBuffer.isRecycled()) {
                bitmapBuffer.recycle();
            }
            if (rotatedBitmap != null && !rotatedBitmap.isRecycled()) {
                rotatedBitmap.recycle();
            }
            imageProxy.close();
        }
    }

    @VisibleForTesting
    void detectAsync(MPImage mpImage, long frameTime) {
        if (faceDetector != null) {
            try {
                faceDetector.detectAsync(mpImage, frameTime);
            } catch (Exception e) {
                Log.e(TAG, "Error in detectAsync: " + e.getMessage(), e);
                if (faceDetectorListener != null) {
                    faceDetectorListener.onError("Detection failed: " + e.getMessage(), FaceFragment.OTHER_ERROR);
                }
            }
        } else {
            Log.w(TAG, "FaceDetector not initialized");
        }
    }

    private void returnLivestreamResult(FaceDetectorResult result, MPImage input) {
        long finishTimeMs = SystemClock.uptimeMillis();
        long inferenceTime = finishTimeMs - result.timestampMs();

        if (faceDetectorListener != null) {
            faceDetectorListener.onResults(
                    new ResultBundle(
                            Collections.singletonList(result),
                            inferenceTime,
                            input.getHeight(),
                            input.getWidth()
                    )
            );
        }
    }

    private void returnLivestreamError(RuntimeException error) {
        if (faceDetectorListener != null) {
            faceDetectorListener.onError(
                    error.getMessage() != null ? error.getMessage() : "An unknown error has occurred",
                    FaceFragment.OTHER_ERROR
            );
        }
    }

    public static class ResultBundle {
        private final List<FaceDetectorResult> results;
        private final long inferenceTime;
        private final int inputImageHeight;
        private final int inputImageWidth;

        public ResultBundle(List<FaceDetectorResult> results, long inferenceTime, int inputImageHeight, int inputImageWidth) {
            this.results = results;
            this.inferenceTime = inferenceTime;
            this.inputImageHeight = inputImageHeight;
            this.inputImageWidth = inputImageWidth;
        }

        public List<FaceDetectorResult> getResults() {
            return results;
        }

        public long getInferenceTime() {
            return inferenceTime;
        }

        public int getInputImageHeight() {
            return inputImageHeight;
        }

        public int getInputImageWidth() {
            return inputImageWidth;
        }
    }

    public interface DetectorListener {
        void onError(String error, int errorCode);
        void onResults(ResultBundle resultBundle);
    }
}