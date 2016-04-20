package diuit.duolc.com.demopibot;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends Activity
{
    public final long COUNT_DOWN_TIME = 1000 * 2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor( this.getResources().getColor(R.color.grey_title));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new CountDownTimer(COUNT_DOWN_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {}
            @Override
            public void onFinish()
            {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                ActivityOptions options = ActivityOptions.makeCustomAnimation( SplashActivity.this, R.anim.in_from_right, R.anim.nothing);
                startActivity(intent);
                SplashActivity.this.finish();
            }
        }.start();
    }
}
