package com.gmail.aa;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        final TextView ai_ = findViewById(R.id.AI);
        final TextView powered_ = findViewById(R.id.powered);
        final TextView app_name = findViewById(R.id.app_name);

        final Animation move = AnimationUtils.loadAnimation(this, R.anim.splash_half_move);
        final Animation fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in_powered);

        final LottieAnimationView AI_anim = findViewById(R.id.ai_anim);

        final Intent home_page = new Intent(SplashActivity.this, HomePage.class);
//        final Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(SplashActivity.this).toBundle();
        YoYo.with(Techniques.FadeIn).duration(5000).playOn(app_name);
        AI_anim.playAnimation();
        ai_.startAnimation(move);
        powered_.startAnimation(fade_in);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(home_page);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        }, 5000);
    }
}
