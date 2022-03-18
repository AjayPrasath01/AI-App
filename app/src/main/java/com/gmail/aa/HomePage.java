package com.gmail.aa;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Vibrator;
import android.text.Layout;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.os.HandlerCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

public class HomePage extends AppCompatActivity {

    Intent intent_DV;
    Intent intent_DA;
    final int REQUEST_CAMERA_CODE = 100;
    final int REQUEST_MICROPHONE_CODE = 200;
    Handler creator_auto_animator;
    Vibrator vibrator;
    HandlerThread handlerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.home_page);
        final TextView title = findViewById(R.id.title_);
        title.setText(R.string.home_page);
        final ImageButton back_button = findViewById(R.id.home_back_button);
        final CardView digital_audio = findViewById(R.id.audio_card);
        final CardView digital_vision = findViewById(R.id.vision_card);

        handlerThread = new HandlerThread("CreatorThread");
        handlerThread.start();
        creator_auto_animator = HandlerCompat.createAsync(handlerThread.getLooper());

        digital_vision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(HomePage.this, new String[]{
                        Manifest.permission.CAMERA
                }, REQUEST_CAMERA_CODE);
            }
        });

        digital_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(HomePage.this, new String[]{
                        Manifest.permission.RECORD_AUDIO
                }, REQUEST_MICROPHONE_CODE);
            }
        });
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        View Slideable_Aera = findViewById(R.id.parent_layout);
        LottieAnimationView creator_back_anim = findViewById(R.id.creator_info_anim);
        TextView creator_details = findViewById(R.id.creator_details);
        creator_back_anim.animate().translationYBy(1000f).setDuration(10);
        creator_details.animate().translationYBy(1000f).setDuration(10);
        Slideable_Aera.setOnTouchListener(new OnSwipeTouchListener(this){
            @Override
            public void onSwipeTop() {
                vibrator.vibrate(50);
                creator_back_anim.animate().translationYBy(-1000f).setDuration(500);
                creator_details.animate().translationYBy(-1000f).setDuration(500);
                creator_auto_animator.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        creator_back_anim.animate().translationYBy(1000f).setDuration(700);
                        creator_details.animate().translationYBy(1000f).setDuration(700);
                    }
                }, 3000);

//                creator_back_anim.startAnimation(slide_in);
//                creator_details.startAnimation(slide_in);
            }

            @Override
            public void onSwipeBottom() {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 100: {
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    intent_DV = new Intent(HomePage.this, DigitalVisionActivity.class);
                    startActivity(intent_DV);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
                break;
            }
            case 200: {
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    intent_DA = new Intent(HomePage.this, DigitalAudioActivity.class);
                    startActivity(intent_DA);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
                break;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        creator_auto_animator = null;
        handlerThread.quit();
    }
}
