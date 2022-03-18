package com.gmail.aa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.os.HandlerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.material.internal.TextScale;

import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DigitalAudioActivity extends AppCompatActivity {

    AudioClassifier audioClassifier;
    AudioRecord audioRecord;
    Handler handler;

    TextView center_, top_left, top_right, bottom_left, bottom_right, outer_top_left, outer_top_right, outer_bottom_left, outer_bottom_right, outer_top_middle, outer_bottom_middle;

    ConstraintLayout prediciton_displayer;

    final long interval = 1000L;

    Animation fade_out, fade_in;

    boolean is_slided_out = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_digital_audio);

        fade_out = AnimationUtils.loadAnimation(this, R.anim.fade_out_digital_pre);
        fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in_digital_pre);

        center_ = findViewById(R.id.textView_center);

        outer_top_left = findViewById(R.id.outer_top_left);
        outer_top_middle = findViewById(R.id.outer_top_middle);
        outer_top_right = findViewById(R.id.outer_top_right);

        top_left = findViewById(R.id.textView_left_top);
        top_right = findViewById(R.id.textView_right_top);

        bottom_right = findViewById(R.id.textView_right_bottom);
        bottom_left = findViewById(R.id.textView_left_bottom);

        outer_bottom_left = findViewById(R.id.outer_down_left);
        outer_bottom_middle = findViewById(R.id.outer_down_middle);
        outer_bottom_right = findViewById(R.id.outer_down_right);

        prediciton_displayer = findViewById(R.id.predicition_container);

        HideOuterLayer();

        HandlerThread handlerthread = new HandlerThread("backgroundThread");
        handlerthread.start();
        handler = HandlerCompat.createAsync(handlerthread.getLooper());

        final TextView title = findViewById(R.id.title_);
        title.setText(R.string.digital_audio);

        View title_bar = findViewById(R.id.include);

        final Animation slide_out = AnimationUtils.loadAnimation(this, R.anim.slide_out_up);
        final Animation slide_in = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);

        final Animation predition_anim_up = AnimationUtils.loadAnimation(this, R.anim.slide_up_predition_audio);
        final Animation predition_anim_down = AnimationUtils.loadAnimation(this, R.anim.slide_down_predition_audio);

        final LottieAnimationView anim_area = findViewById(R.id.animaion_area);


        anim_area.setOnTouchListener(new OnSwipeTouchListener(this){
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onSwipeTop() {
                if (!is_slided_out){
                    title_bar.startAnimation(slide_out);
                    prediciton_displayer.startAnimation(predition_anim_up);
                    anim_area.startAnimation(predition_anim_up);
                    is_slided_out = true;
//                    ShowOuterLayer();
                }
            }

            @Override
            public void onSwipeBottom() {
                if (is_slided_out){
                    title_bar.startAnimation(slide_in);
                    HideOuterLayer();
                    prediciton_displayer.startAnimation(predition_anim_down);
                    anim_area.startAnimation(predition_anim_down);
                    is_slided_out = false;
                }
            }
        });

        final ImageButton back_button = findViewById(R.id.home_back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)){
            try {
                startAudioClassifer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Toast tost =  Toast.makeText(this, "The device does not hav microphone", Toast.LENGTH_SHORT);
            tost.show();
        }
    }


    private void startAudioClassifer() throws IOException {
        AudioClassifier classifier = AudioClassifier.createFromFile(this, "audioClassifier.tflite");
        TensorAudio audioTensor = classifier.createInputTensorAudio();
        AudioRecord record = classifier.createAudioRecord();
        record.startRecording();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                audioTensor.load(record);
                List<Classifications> output = classifier.classify(audioTensor);
                List<Category> result = output.get(0).getCategories().subList(0, 15);
                predicition_viewer(result);
                handler.postDelayed(this, interval);
            }
        };
        handler.post(run);
        audioClassifier = classifier;
        audioRecord = record;
    }

    private void stopAudioClassifier(){
        handler.removeCallbacksAndMessages(null);
        audioRecord.stop();
        audioClassifier = null;
        audioRecord = null;
    }

    void predicition_viewer(List<Category> predicitons){
        List<TextView> textViews = Arrays.asList(center_, top_left, top_right, bottom_left, bottom_right, outer_top_left, outer_top_middle, outer_top_right, outer_bottom_left, outer_bottom_middle, outer_bottom_right);
        final float max_alpha = 1.0f;
        final float max_text_size = 35;
        final String msg = "center";
        final int layer_0 = 0, layer_1 = 4, layer_2 = 11;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (predicitons != null) {
                    for (int i=0;i<11;i++){
//                        System.out.println(i);
                        TextView workingview = textViews.get(i);
                        if (!(String.valueOf(workingview.getText()).equals(String.valueOf(predicitons.get(i).getLabel())))) {
                            if ( i == layer_0){
                                workingview.startAnimation(fade_out);
                                workingview.setText(String.valueOf(predicitons.get(i).getLabel()));
                                workingview.startAnimation(fade_in);
                                workingview.setAlpha(max_alpha);
                                workingview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, max_text_size);
                            }else if (i <= layer_1){
                                workingview.startAnimation(fade_out);
                                workingview.setText(String.valueOf(predicitons.get(i).getLabel()));
                                workingview.startAnimation(fade_in);
                                workingview.setAlpha(max_alpha/2);
                                workingview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, max_text_size/2);
                            }else if ((i <= layer_2) && (is_slided_out)){
                                ShowOuterLayer();
                                workingview.startAnimation(fade_out);
                                workingview.setText(String.valueOf(predicitons.get(i).getLabel()));
                                workingview.startAnimation(fade_in);
                                workingview.setAlpha(max_alpha/3);
                                workingview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, max_text_size/3);
                            }
//                            workingview.setAlpha(max_alpha / (i + 1));
//                            workingview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, max_text_size / (i + 1));
//                        score[i] = (int) predicitons.get(i).getScore();
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (audioRecord != null){
            stopAudioClassifier();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioRecord != null){
            stopAudioClassifier();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            startAudioClassifer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ShowOuterLayer(){
        if (outer_top_left != null){
            outer_top_left.setVisibility(View.VISIBLE);
        }
        if (outer_top_middle != null){
            outer_top_middle.setVisibility(View.VISIBLE);
        }
        if (outer_top_right != null){
            outer_top_right.setVisibility(View.VISIBLE);
        }
        if (outer_bottom_left != null){
            outer_bottom_left.setVisibility(View.VISIBLE);
        }
        if (outer_bottom_middle != null){
            outer_bottom_middle.setVisibility(View.VISIBLE);
        }
        if (outer_bottom_right != null){
            outer_bottom_right.setVisibility(View.VISIBLE);
        }
    }

    private void HideOuterLayer(){
        if (outer_top_left != null){
            outer_top_left.setVisibility(View.INVISIBLE);
        }
        if (outer_top_middle != null){
            outer_top_middle.setVisibility(View.INVISIBLE);
        }
        if (outer_top_right != null){
            outer_top_right.setVisibility(View.INVISIBLE);
        }
        if (outer_bottom_left != null){
            outer_bottom_left.setVisibility(View.INVISIBLE);
        }
        if (outer_bottom_middle != null){
            outer_bottom_middle.setVisibility(View.INVISIBLE);
        }
        if (outer_bottom_right != null){
            outer_bottom_right.setVisibility(View.INVISIBLE);
        }
    }
}