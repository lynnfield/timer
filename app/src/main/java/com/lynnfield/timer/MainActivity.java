package com.lynnfield.timer;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements TimerFragment.Listener {

    private TimerFragment timer;
    private TextView directionView;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootView = findViewById(R.id.activity_main);
        directionView = (TextView) findViewById(R.id.change_direction);

        timer = (TimerFragment) getSupportFragmentManager().findFragmentById(R.id.timer);
        timer.setListener(this);
        refreshDirectionText();
    }

    public void onStartClick(View view) {
        timer.start();
    }

    public void onStopClick(View view) {
        timer.stop();
    }

    public void onResetClick(View view) {
        timer.reset();
    }

    public void onChangeDirection(View view) {
        switch (timer.getDirection()) {
            case FORWARD:
                timer.setDirection(TimerFragment.Direction.BACKWARD);
                break;
            case BACKWARD:
                timer.setDirection(TimerFragment.Direction.FORWARD);
                break;
        }
        refreshDirectionText();
    }

    private void refreshDirectionText() {
        switch (timer.getDirection()) {
            case FORWARD:
                directionView.setText(R.string.forward);
                break;
            case BACKWARD:
                directionView.setText(R.string.backward);
                break;
        }
    }

    @Override
    public void onTick(final int seconds) {

    }

    @Override
    public void onTimerDone() {
        Snackbar.make(rootView, R.string.timer_done, Snackbar.LENGTH_SHORT).show();
    }
}
