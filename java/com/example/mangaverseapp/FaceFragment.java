package com.example.mangaverseapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FaceFragment extends Fragment implements FaceDetectorHelper.DetectorListener {
    private static final String TAG = "FaceFragment";
    public static final int DELEGATE_CPU = 0;
    public static final int DELEGATE_GPU = 1;
    public static final int OTHER_ERROR = 0;
    public static final int GPU_ERROR = 1;

    private androidx.camera.view.PreviewView previewView;
    private OverlayView overlayView;
    private ExecutorService cameraExecutor;
    private ExecutorService backgroundExecutor;
    private FaceDetectorHelper faceDetectorHelper;
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private Preview preview;
    private ImageAnalysis imageAnalyzer;
    private FaceViewModel viewModel;

    private final androidx.activity.result.ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "Camera permission granted");
                    startCamera();
                } else {
                    Log.w(TAG, "Camera permission denied");
                    Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_LONG).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(FaceViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "Creating FaceFragment view");
        View view = inflater.inflate(R.layout.fragment_face, container, false);

        previewView = view.findViewById(R.id.previewView);
        overlayView = view.findViewById(R.id.overlayView);

        if (previewView == null || overlayView == null) {
            Log.e(TAG, "PreviewView or OverlayView not found in layout");
            Toast.makeText(requireContext(), "UI initialization error", Toast.LENGTH_LONG).show();
            return view;
        }

        cameraExecutor = Executors.newSingleThreadExecutor();
        backgroundExecutor = Executors.newSingleThreadExecutor();

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission already granted");
            startCamera();
        } else {
            Log.d(TAG, "Requesting camera permission");
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }

        initBottomSheetControls(view);
        return view;
    }

    private void initBottomSheetControls(View view) {
        try {
            View bottomSheet = view.findViewById(R.id.bottomSheetLayout);
            if (bottomSheet == null) {
                Log.e(TAG, "BottomSheetLayout not found");
                Toast.makeText(requireContext(), "Bottom sheet UI not found", Toast.LENGTH_SHORT).show();
                return;
            }

            TextView thresholdValue = bottomSheet.findViewById(R.id.thresholdValue);
            if (thresholdValue != null) {
                thresholdValue.setText(String.format("%.2f", viewModel.getCurrentThreshold()));
            }

            View thresholdMinus = bottomSheet.findViewById(R.id.thresholdMinus);
            if (thresholdMinus != null) {
                thresholdMinus.setOnClickListener(v -> {
                    if (viewModel.getCurrentThreshold() >= 0.1f) {
                        viewModel.setThreshold(viewModel.getCurrentThreshold() - 0.1f);
                        updateControlsUi();
                    }
                });
            }

            View thresholdPlus = bottomSheet.findViewById(R.id.thresholdPlus);
            if (thresholdPlus != null) {
                thresholdPlus.setOnClickListener(v -> {
                    if (viewModel.getCurrentThreshold() <= 0.8f) {
                        viewModel.setThreshold(viewModel.getCurrentThreshold() + 0.1f);
                        updateControlsUi();
                    }
                });
            }

            Spinner spinnerDelegate = bottomSheet.findViewById(R.id.spinnerDelegate);
            if (spinnerDelegate != null) {
                spinnerDelegate.setSelection(viewModel.getCurrentDelegate(), false);
                spinnerDelegate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        viewModel.setDelegate(position);
                        updateControlsUi();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // No-op
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing bottom sheet controls: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error setting up controls", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateControlsUi() {
        try {
            TextView thresholdValue = requireView().findViewById(R.id.thresholdValue);
            if (thresholdValue != null) {
                thresholdValue.setText(String.format("%.2f", viewModel.getCurrentThreshold()));
            }
            backgroundExecutor.execute(() -> {
                if (faceDetectorHelper != null) {
                    faceDetectorHelper.clearFaceDetector();
                    faceDetectorHelper.setupFaceDetector();
                }
            });
            if (overlayView != null) {
                overlayView.clear();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating controls UI: " + e.getMessage(), e);
        }
    }

    private void startCamera() {
        Log.d(TAG, "Starting camera");
        ProcessCameraProvider.getInstance(requireContext()).addListener(() -> {
            try {
                cameraProvider = ProcessCameraProvider.getInstance(requireContext()).get();

                preview = new Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .setTargetRotation(previewView.getDisplay().getRotation())
                        .build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageAnalyzer = new ImageAnalysis.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .setTargetRotation(previewView.getDisplay().getRotation())
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build();
                imageAnalyzer.setAnalyzer(cameraExecutor, imageProxy -> {
                    if (faceDetectorHelper != null) {
                        faceDetectorHelper.detectLivestreamFrame(imageProxy);
                    } else {
                        imageProxy.close();
                        Log.w(TAG, "FaceDetectorHelper not initialized");
                    }
                });

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer);
                Log.d(TAG, "Camera started successfully");

                backgroundExecutor.execute(() -> {
                    faceDetectorHelper = new FaceDetectorHelper(
                            viewModel.getCurrentThreshold(),
                            viewModel.getCurrentDelegate(),
                            RunningMode.LIVE_STREAM,
                            requireContext(),
                            this
                    );
                    Log.d(TAG, "FaceDetectorHelper initialized");
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to start camera: " + e.getMessage(), e);
                Toast.makeText(requireContext(), "Failed to start camera: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        backgroundExecutor.execute(() -> {
            if (faceDetectorHelper == null || faceDetectorHelper.isClosed()) {
                faceDetectorHelper = new FaceDetectorHelper(
                        viewModel.getCurrentThreshold(),
                        viewModel.getCurrentDelegate(),
                        RunningMode.LIVE_STREAM,
                        requireContext(),
                        this
                );
                Log.d(TAG, "FaceDetectorHelper reinitialized in onResume");
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (faceDetectorHelper != null) {
            viewModel.setThreshold(faceDetectorHelper.getThreshold());
            viewModel.setDelegate(faceDetectorHelper.getCurrentDelegate());
            backgroundExecutor.execute(() -> faceDetectorHelper.clearFaceDetector());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "Destroying FaceFragment");
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        if (backgroundExecutor != null) {
            backgroundExecutor.shutdown();
            try {
                backgroundExecutor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Log.e(TAG, "Background executor shutdown interrupted", e);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (imageAnalyzer != null) {
            imageAnalyzer.setTargetRotation(previewView.getDisplay().getRotation());
        }
    }

    @Override
    public void onResults(FaceDetectorHelper.ResultBundle resultBundle) {
        requireActivity().runOnUiThread(() -> {
            if (!isAdded()) {
                return;
            }
            try {
                TextView inferenceTimeVal = requireView().findViewById(R.id.inferenceTimeVal);
                if (inferenceTimeVal != null) {
                    inferenceTimeVal.setText(String.format("%d ms", resultBundle.getInferenceTime()));
                }

                FaceDetectorResult detectionResult = resultBundle.getResults().get(0);
                if (overlayView != null) {
                    overlayView.setResults(
                            detectionResult,
                            resultBundle.getInputImageHeight(),
                            resultBundle.getInputImageWidth()
                    );
                }

                if (!detectionResult.detections().isEmpty()) {
                    RectF boundingBox = detectionResult.detections().get(0).boundingBox();
                    RectF normalizedRect = normalizeBoundingBox(
                            boundingBox,
                            resultBundle.getInputImageWidth(),
                            resultBundle.getInputImageHeight(),
                            previewView.getWidth(),
                            previewView.getHeight()
                    );
                    Log.d(TAG, "Normalized face rect: " + normalizedRect.toString());
                    checkFaceInReferenceRect(normalizedRect);
                } else {
                    if (overlayView != null) {
                        overlayView.setFaceRect(null);
                        overlayView.setRectColor(false);
                    }
                    Log.d(TAG, "No face detected, setting red");
                }

                if (overlayView != null) {
                    overlayView.invalidate();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing results: " + e.getMessage(), e);
                Toast.makeText(requireContext(), "Error processing detection results", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onError(String error, int errorCode) {
        requireActivity().runOnUiThread(() -> {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            if (errorCode == GPU_ERROR) {
                Spinner spinnerDelegate = requireView().findViewById(R.id.spinnerDelegate);
                if (spinnerDelegate != null) {
                    spinnerDelegate.setSelection(DELEGATE_CPU, false);
                }
            }
        });
    }

    private RectF normalizeBoundingBox(RectF box, int imageWidth, int imageHeight, int viewWidth, int viewHeight) {
        Log.d(TAG, "Normalizing bounding box: " + box.toString() + ", image: " + imageWidth + "x" + imageHeight + ", view: " + viewWidth + "x" + viewHeight);
        // MediaPipe bounding box is in pixel coordinates relative to input image
        // Scale to view dimensions and adjust for front camera mirroring
        float scaleX = (float) viewWidth / imageWidth;
        float scaleY = (float) viewHeight / imageHeight;
        return new RectF(
                box.left * scaleX,    // No mirroring needed since image is already mirrored
                box.top * scaleY,
                box.right * scaleX,
                box.bottom * scaleY
        );
    }

    private void checkFaceInReferenceRect(RectF faceRect) {
        if (faceRect == null || overlayView == null) {
            if (overlayView != null) {
                overlayView.setFaceRect(null);
                overlayView.setRectColor(false);
            }
            Log.d(TAG, "No face rect or OverlayView, setting red");
            return;
        }

        RectF referenceRect = overlayView.getReferenceRect();
        Log.d(TAG, "Reference rect: " + referenceRect.toString());

        // Relaxed bounds checking with tolerance
        boolean isWithin = faceRect.left >= referenceRect.left - 100 &&
                faceRect.right <= referenceRect.right + 100 &&
                faceRect.top >= referenceRect.top - 100 &&
                faceRect.bottom <= referenceRect.bottom + 100;

        overlayView.setFaceRect(faceRect);
        overlayView.setRectColor(isWithin);
        Log.d(TAG, "Face " + (isWithin ? "within" : "outside") + " reference rect");
    }
}