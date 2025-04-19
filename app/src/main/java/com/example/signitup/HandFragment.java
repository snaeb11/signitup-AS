package com.example.signitup;

import static org.opencv.android.NativeCameraView.TAG;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import com.google.mediapipe.formats.proto.LandmarkProto.Landmark;

import java.util.List;

public class HandFragment extends Fragment implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private static final String CAMERA = Manifest.permission.CAMERA;

    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mRgba;
    private boolean mCameraPermissionGranted = false;

    private handDetector handDetector;
    private boolean isDetectorReleased = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hand, container, false);

        handDetector = new handDetector(requireContext());
        mOpenCvCameraView = view.findViewById(R.id.camera_view);
        mOpenCvCameraView.setCvCameraViewListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requireActivity().checkSelfPermission(CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                mCameraPermissionGranted = true;
                onCameraPermissionGranted();
            }
        } else {
            mCameraPermissionGranted = true;
            onCameraPermissionGranted();
        }

        return view;
    }

    private void onCameraPermissionGranted() {
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.setCameraPermissionGranted();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mOpenCvCameraView != null && mCameraPermissionGranted) {
            mOpenCvCameraView.enableView();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
        if (handDetector != null) {
            handDetector.release();
            handDetector = null;
            isDetectorReleased = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mCameraPermissionGranted = true;
            onCameraPermissionGranted();
            if (mOpenCvCameraView != null) {
                mOpenCvCameraView.enableView();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        if (mRgba != null) {
            mRgba.release();
            mRgba = null;
        }
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        if (mRgba == null || handDetector == null || isDetectorReleased) {
            Log.e(TAG, "Skipping frame processing: mRgba is null or detector is released.");
            return mRgba;
        }

        Bitmap bitmap;
        try {
            bitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mRgba, bitmap);
        } catch (Exception e) {
            Log.e(TAG, "Error converting Mat to Bitmap", e);
            return mRgba;
        }

        List<Landmark> landmarks = null;
        try {
            landmarks = handDetector.detect(bitmap);
        } catch (Exception e) {
            Log.e(TAG, "Error in handDetector.detect()", e);
        }

        if (landmarks != null && !landmarks.isEmpty()) {
            List<Landmark> finalLandmarks = landmarks;
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(getActivity(), "Landmarks Detected: " + finalLandmarks.size(), Toast.LENGTH_SHORT).show()
            );
            drawLandmarksOnFrame(landmarks, "Detected");
        } else {
            drawNoHandDetectedText();
        }

        return mRgba;
    }

    private void drawLandmarksOnFrame(List<Landmark> landmarks, String prediction) {
        String text = "Prediction: " + prediction;
        Point textOrg = new Point(30, 100);
        int font = org.opencv.imgproc.Imgproc.FONT_HERSHEY_SIMPLEX;
        double fontScale = 1.2;
        Scalar fontColor = new Scalar(0, 255, 255);
        int thickness = 2;
        org.opencv.imgproc.Imgproc.putText(mRgba, text, textOrg, font, fontScale, fontColor, thickness);

        for (Landmark landmark : landmarks) {
            float x = landmark.getX();
            float y = landmark.getY();

            int pixelX = (int) (x * mRgba.cols());
            int pixelY = (int) (y * mRgba.rows());

            Point point = new Point(pixelX, pixelY);
            Scalar color = new Scalar(0, 255, 0);
            int radius = 5;
            int landmarkThickness = -1;

            org.opencv.imgproc.Imgproc.circle(mRgba, point, radius, color, landmarkThickness);
        }
    }

    private void drawNoHandDetectedText() {
        if (mRgba == null) return;

        String text = "No hand detected";
        Point textOrg = new Point(mRgba.cols() / 2 - 100, mRgba.rows() / 2);
        int font = org.opencv.imgproc.Imgproc.FONT_HERSHEY_SIMPLEX;
        double fontScale = 1.2;
        Scalar fontColor = new Scalar(0, 0, 255);
        int thickness = 2;

        org.opencv.imgproc.Imgproc.putText(mRgba, text, textOrg, font, fontScale, fontColor, thickness);
    }

    private String mapPredictionToLetter(int classIndex) {
        switch (classIndex) {
            case 0: return "A";
            case 1: return "E";
            case 2: return "I";
            case 3: return "O";
            case 4: return "U";
            default: return "?";
        }
    }
}
