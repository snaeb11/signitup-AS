package com.example.signitup;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.Mat;
import java.util.ArrayList;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.os.Build;

import org.opencv.android.CameraActivity;

public class LetterEFragment extends Fragment implements CameraBridgeViewBase.CvCameraViewListener2 {

    //starts here
    private static final String TAG = "HomeFragment";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private static final String CAMERA = android.Manifest.permission.CAMERA;

    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mRgba;
    private Mat mGray;
    private boolean mCameraPermissionGranted = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_letter_e, container, false);

        // Initialize OpenCV camera view
        mOpenCvCameraView = view.findViewById(R.id.camera_view);
        mOpenCvCameraView.setCvCameraViewListener(this);

        // Check for camera permission
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

    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        List<CameraBridgeViewBase> cameraViews = new ArrayList<>();
        if (mOpenCvCameraView != null)
            cameraViews.add(mOpenCvCameraView);
        return cameraViews;
    }

    protected void onCameraPermissionGranted() {
        List<? extends CameraBridgeViewBase> cameraViews = getCameraViewList();
        if (cameraViews == null) {
            return;
        }
        for (CameraBridgeViewBase cameraBridgeViewBase: cameraViews) {
            if (cameraBridgeViewBase != null) {
                cameraBridgeViewBase.setCameraPermissionGranted();
            }
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

    // CvCameraViewListener2 implementation
    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat();
        mGray = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        if (mRgba != null) mRgba.release();
        if (mGray != null) mGray.release();
        mRgba = null;
        mGray = null;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // Process your camera frame here
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        // Do image processing here

        return mRgba; // Return the processed frame
    }
}