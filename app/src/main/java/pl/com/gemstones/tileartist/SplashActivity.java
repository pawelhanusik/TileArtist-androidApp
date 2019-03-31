package pl.com.gemstones.tileartist;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;

public class SplashActivity extends Activity {

    ImageView animationImageView;
    boolean animationHalfDone = false;
    Handler animationHandler;
    Runnable animationRunnable;
    float animationStep = .01f;
    int animationSpeed = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);

        animationImageView = (ImageView)findViewById(R.id.splash_img);
        animationImageView.setAlpha(0.f);
        animationImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endAnimation();
            }
        });

        animationHandler = new Handler();
        animationRunnable = new Runnable() {
            @Override
            public void run() {
                if(!animationHalfDone) {
                    animationImageView.setAlpha(animationImageView.getAlpha() + animationStep);

                    if(animationImageView.getAlpha() >= 1.f) {
                        animationHalfDone = true;
                        animationHandler.postDelayed(animationRunnable, animationSpeed+200);
                    }else{
                        animationHandler.postDelayed(animationRunnable, animationSpeed);
                    }

                } else {
                    animationImageView.setAlpha(animationImageView.getAlpha() - animationStep);

                    if(animationImageView.getAlpha() <= 0.f) {
                        endAnimation();
                    }else{
                        animationHandler.postDelayed(animationRunnable, animationSpeed);
                    }
                }
            }
        };
        animationHandler.post(animationRunnable);

        //InitAdds initAdds = new InitAdds();
        //initAdds.execute();
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (
                PreferenceManager.getDefaultSharedPreferences(this).getBoolean("menu_settings_6", true)
                && hasFocus
                ) {
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            if(Build.VERSION.SDK_INT >= 19) {
                flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    private void endAnimation() {
        animationHandler.removeCallbacks(animationRunnable);

        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);

        finish();
    }

    /*private class InitAdds extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {
            MobileAds.initialize(getBaseContext(), "ca-app-pub-3940256099942544~3347511713"); ///TODO: change to mine ID
            return null;
        }
    }*/

}
