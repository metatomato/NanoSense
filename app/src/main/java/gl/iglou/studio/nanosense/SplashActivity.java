package gl.iglou.studio.nanosense;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;


/**
 * Created by metatomato on 02.12.14.
 */
public class SplashActivity extends Activity{
    private static final String TAG = "SPALSH_ACTIVITY";

    private static int SPLASH_TIME_OUT = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                NextActivity();
            }
        }, SPLASH_TIME_OUT);
    }


    public void NextActivity() {
        Intent i = new Intent(SplashActivity.this, NanoSenseActivity.class);
        startActivity(i);
        // close this activity
        finish();
    }
}
