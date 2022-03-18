package com.gmail.aa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.OnSwipe;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.os.HandlerCompat;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

public class DigitalVisionActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    JavaCameraView cameraView;
    Mat mRGA;

    Handler model_handler;

    HandlerThread handlerThread;

    TextView pre;

    boolean is_slided_out = false;

    private ObjectDetectorClass objectDetectorClass;

    BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == BaseLoaderCallback.SUCCESS){
                cameraView.enableView();
            }else {
                super.onManagerConnected(status);
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digital_vision);

        final ImageButton back_button = findViewById(R.id.home_back_button);

        pre = findViewById(R.id.predicition_textview);



        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        pre.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent browser_Intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com/search?q=" + pre.getText()));
                startActivity(browser_Intent);
                return false;
            }
        });

        handlerThread = new HandlerThread("ModalThread");
        handlerThread.start();
        model_handler = HandlerCompat.createAsync(handlerThread.getLooper());



        try {
//            detector = ObjectDetector.createFromFileAndOptions(this, "salad.tflite", options);
            objectDetectorClass = new ObjectDetectorClass(getAssets(), "model.tflite", "labels.txt", 224);
        } catch (IOException e) {
            e.printStackTrace();
        }

        cameraView = findViewById(R.id.camera_view);
        cameraView.setVisibility(SurfaceView.VISIBLE);
        cameraView.setCvCameraViewListener(this);

        TextView title = findViewById(R.id.title_);
        title.setText(R.string.digital_vision);

        View title_bar = findViewById(R.id.include);

        final Animation slide_out = AnimationUtils.loadAnimation(this, R.anim.slide_out_up);
        final Animation slide_in = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);

        final Animation predition_anim_up = AnimationUtils.loadAnimation(this, R.anim.slide_up_predition_audio);
        final Animation predition_anim_down = AnimationUtils.loadAnimation(this, R.anim.slide_down_predition_audio);

        ImageView focus = findViewById(R.id.focus);

        cameraView.setOnTouchListener(new OnSwipeTouchListener(this){
            @Override
            public void onSwipeTop() {
                if (!is_slided_out){
                    title_bar.startAnimation(slide_out);
                    focus.startAnimation(predition_anim_up);
                    pre.startAnimation(predition_anim_up);
                    is_slided_out = true;
                }
            }

            @Override
            public void onSwipeBottom() {
                if (is_slided_out){
                    title_bar.startAnimation(slide_in);
                    focus.startAnimation(predition_anim_down);
                    pre.startAnimation(predition_anim_down);
                    is_slided_out = false;
                }
            }
        });
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d("width", String.valueOf(width));
        Log.d("height", String.valueOf(height));
        mRGA = new Mat(width, height, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mRGA.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRGA = inputFrame.rgba();

        String out = objectDetectorClass.recognizeImage(mRGA);
        System.out.println(out);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println(pre.getText());
                pre.setText(out);
            }
        });
        return mRGA;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onDestroy() {
        if (cameraView != null){
            cameraView.disableView();
//            handlerThread.quit();
//            handlerThread = null;
//            model_handler = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (cameraView != null){
            cameraView.disableView();
        }
//        try {
//            handlerThread.join();
////            handlerThread = null;
////            model_handler = null;
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()){
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }else {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, baseLoaderCallback);
        }
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (model_handler != null) {
            model_handler.post(r);
        }
    }

}