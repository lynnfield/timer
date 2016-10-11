package com.lynnfield.timer;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class TimerFragment extends Fragment {
    private static final int MINUTES_MAX = 10;
    private static final int FIRST_SECOND_MAX = 6;
    private static final int LAST_SECOND_MAX = 10;
    private static final int MAX = 10 * 60;

    private static final int DEFAULT_SECONDS = 0;
    private static final Direction DEFAULT_DIRECTION = Direction.BACKWARD;

    private static final String SECONDS_KEY = "seconds";
    private static final String IS_RUNNING_KEY = "is_running";
    private static final String DIRECTION_KEY = "direction";

    private RecyclerView minutesView;
    private RecyclerView firstSecondsView;
    private RecyclerView lastSecondsView;
    private View blockOverlay;

    private Direction direction = DEFAULT_DIRECTION;
    private int seconds = DEFAULT_SECONDS;
    private boolean isRunning;
    private final Runnable reset = new Runnable() {
        @Override
        public void run() {
            reset();
        }
    };
    private Listener listener;
    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            listener.onTick(seconds);

            final int dn = direction.asNumber();
            final int firstSecondsBefore = seconds / LAST_SECOND_MAX;
            final int minutesBefore = firstSecondsBefore / FIRST_SECOND_MAX;

            seconds += dn;

            final int firstSecondsAfter = seconds / LAST_SECOND_MAX;
            final int minutesAfter = firstSecondsAfter / FIRST_SECOND_MAX;

            lastSecondsView.smoothScrollBy(0, dn * lastSecondsView.getHeight());

            if (firstSecondsBefore != firstSecondsAfter)
                firstSecondsView.smoothScrollBy(0, dn * firstSecondsView.getHeight());

            if (minutesBefore != minutesAfter)
                minutesView.smoothScrollBy(0, dn * minutesView.getHeight());

            if (isEnd()) {
                notifyTimerDone();
                resetDelayed();
                return;
            }

            tick();
        }
    };

    public TimerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_timer, container, false);

        if (savedInstanceState != null) {
            seconds = savedInstanceState.getInt(SECONDS_KEY, DEFAULT_SECONDS);
            direction = Direction.valueOf(savedInstanceState.getString(DIRECTION_KEY));
            isRunning = savedInstanceState.getBoolean(IS_RUNNING_KEY, false);
        }

        minutesView = (RecyclerView) v.findViewById(R.id.minutes);
        firstSecondsView = (RecyclerView) v.findViewById(R.id.seconds_first);
        lastSecondsView = (RecyclerView) v.findViewById(R.id.seconds_last);
        blockOverlay = v.findViewById(R.id.block_overlay);

        minutesView.addOnScrollListener(new ScrollToNumber());
        firstSecondsView.addOnScrollListener(new ScrollToNumber());
        lastSecondsView.addOnScrollListener(new ScrollToNumber());

        minutesView.setAdapter(new NumbersAdapter(MINUTES_MAX));
        firstSecondsView.setAdapter(new NumbersAdapter(FIRST_SECOND_MAX));
        lastSecondsView.setAdapter(new NumbersAdapter(LAST_SECOND_MAX));

        resetMinutes();
        resetFirstSeconds();
        resetLastSeconds();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isRunning)
            start();
        else
            reset();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        resetFirstSeconds();
        resetLastSeconds();
        resetMinutes();

        outState.putBoolean(IS_RUNNING_KEY, isRunning);
        outState.putString(DIRECTION_KEY, direction.toString());
        outState.putInt(SECONDS_KEY, seconds);

        stop();
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(@NonNull final Direction direction) {
        stop();
        this.direction = direction;
    }

    public void start() {
        enableBlock();
        resetMinutes();
        resetFirstSeconds();
        resetLastSeconds();
        isRunning = true;
        tick();
    }

    public void stop() {
        disableBlock();
        isRunning = false;
        if (getView() != null)
            getView().removeCallbacks(tick);
    }

    public void reset() {
        stop();
        switch (direction) {
            case FORWARD:
                seconds = 0;
                break;
            case BACKWARD:
                seconds = MAX;
                break;
        }
        resetMinutes();
        resetFirstSeconds();
        resetLastSeconds();
    }

    private void resetDelayed() {
        if (getView() != null)
            getView().postDelayed(reset, 200);
    }

    public void setListener(final Listener listener) {
        this.listener = listener;
    }

    private void tick() {
        if (getView() != null && !isEnd())
            getView().postDelayed(tick, 1000);
    }

    private void resetLastSeconds() {
        lastSecondsView.scrollToPosition(seconds);
    }

    private void resetFirstSeconds() {
        firstSecondsView.scrollToPosition(seconds / LAST_SECOND_MAX);
    }

    private void resetMinutes() {
        minutesView.scrollToPosition(seconds / 60);
    }

    private void notifyTimerDone() {
        if (listener != null)
            listener.onTimerDone();
    }

    private boolean isEnd() {
        switch (direction) {
            case FORWARD:
                return seconds >= MAX;
            case BACKWARD:
                return seconds <= 0;
            default:
                return true;
        }
    }

    private void onItemSelected(RecyclerView rv, int adapterPosition) {
        if (rv == minutesView) {
            seconds = adapterPosition % 10 * 60 + seconds % 60;
        } else if (rv == firstSecondsView) {
            final int minutes = seconds / 60;
            final int lastSeconds = seconds % 10;
            seconds = minutes * 60 + adapterPosition % 6 * 10 + lastSeconds;
        } else if (rv == lastSecondsView) {
            seconds = seconds / 10 * 10 + adapterPosition % 10;
        }
        seconds = seconds > MAX ? seconds % MAX : seconds;
    }

    private void enableBlock() {blockOverlay.setVisibility(View.VISIBLE);}

    private void disableBlock() {blockOverlay.setVisibility(View.GONE);}

    public enum Direction {
        FORWARD(1), BACKWARD(-1);

        private final int number;

        Direction(final int number) {this.number = number;}

        int asNumber() {
            return number;
        }
    }

    public interface Listener {
        void onTick(int seconds);

        void onTimerDone();
    }

    private class ScrollToNumber extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(final RecyclerView rv, final int newState) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                final int cc = rv.getChildCount();
                if (cc > 1) {
                    final View view0 = rv.getChildAt(0);
                    final float y0 = view0.getY();
                    final int h0 = view0.getHeight();
                    final float vis0 = h0 - Math.abs(y0);
                    final View view1 = rv.getChildAt(1);
                    final float y1 = view1.getY();
                    final int h1 = view1.getHeight();
                    final float vis1 = h1 - Math.abs(y1);
                    rv.smoothScrollBy(0, (int) (vis0 > vis1 ? y0 : y1));
                    onItemSelected(rv, rv.getChildAdapterPosition(vis0 > vis1 ? view0 : view1));
                }
            }
        }
    }

    /*
    * Сделать небольшой UI Control таймер обратного отсчета (от 9 минут:59 секунд и до 0:00).
    * Особенности: цифры должны меняться как будто это барабан (примерно такого плана, как на картинке выше).
    * Примечание: эмуляции настоящего 3d не нужно (чтобы на цифрах вверху, внизу были 3ех мерные искажения), достаточно похожести.
    * Другими словами, кроме выставления таймера и направления отсчета ничего не нужно, только центральная строка, просто анимация барабана.
    * Дополнительно (счетчик может иметь как обратный отсчет так и прямой, барабан соответственно должен уметь вращаться в обе стороны).
    * */
}
