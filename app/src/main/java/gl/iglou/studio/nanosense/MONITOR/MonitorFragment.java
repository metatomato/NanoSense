package gl.iglou.studio.nanosense.MONITOR;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Random;

import gl.iglou.studio.nanosense.BT.BTFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class MonitorFragment extends Fragment implements MonitorGUIFragment.MonitorControlCallback{

    private static final String TAG = "MonitorFragment";
    private MonitorGUIFragment mMonitorGUIFragment;
    private SimpleXYSeries mSerie;

    private int mPlotCount = 0;
    private long mStartTime = 0L;
    private Handler mGeneratorScheduler;
    private Runnable mGeneratorSchedulerTask;

    private final double AMP_MAX = 4.0;



    public MonitorFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initSerie();
        initGenerator();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver,
                new IntentFilter(BTFragment.ACTION_REMOTE_RESPONSE));
    }

    @Override
    public void onStop() {
        super.onStop();

        mGeneratorScheduler.removeCallbacks(mGeneratorSchedulerTask);
    }


    public void setMonitorGUIFrag(MonitorGUIFragment fragment) {
        mMonitorGUIFragment = fragment;
    }



    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BTFragment.ACTION_REMOTE_RESPONSE:
                    int cat = intent.getIntExtra(BTFragment.EXTRA_RESPONSE_CATEGORY, -1);
                    switch (cat) {
                        case BTFragment.EXTRA_CAT_CALIBRATION:
                            break;
                        case BTFragment.EXTRA_CAT_SENSOR_DATA:
                            Number value = (intent.getFloatExtra(BTFragment.EXTRA_SENSOR_DATA_FEEDBACK, 0.f));

                            break;
                    }
                    break;
            }
        }
    };





// MonitorControlCallback Methods Implementation
    public XYSeries getData() {
        return mSerie;
    }

    public void onGUIStart() {
        resetSerie();
        launchGenerator();
    }

    public void onGUIStop() {
        mGeneratorScheduler.removeCallbacks(mGeneratorSchedulerTask);
    }




// Serie Helper Methods
    private void initSerie() {
        ArrayList<Number> listY = new ArrayList<>();
        ArrayList<Number> listX = new ArrayList<>();
        for(int i = 0 ; i < MonitorGUIFragment.PLOT_SIZE; i++) {
            listX.add(i * MonitorGUIFragment.PLOT_INITIAL_STEP );
            listY.add(0.0);
        }
        mSerie = new SimpleXYSeries(listX,listY,"data");
        mPlotCount = 0;
    }

    private void updateSerie(Number X, Number Y) {
        if(mPlotCount < MonitorGUIFragment.PLOT_SIZE) {
            mSerie.setXY(X,Y,mPlotCount);
        } else {
            mSerie.removeFirst();
            mSerie.addLast(X,Y);
        }
        mPlotCount++;
    }

    private void resetSerie() {
        for(int i = 0 ; i < MonitorGUIFragment.PLOT_SIZE ; i++) {
            mSerie.setXY(i * MonitorGUIFragment.PLOT_INITIAL_STEP,0.0,i);
        }
    }



// Testing Purpose Serie Generator
    private void initGenerator() {
        mGeneratorScheduler = new Handler();
        mGeneratorSchedulerTask = new Runnable() {
            @Override
            public void run() {
                Number dataX = generateX();
                Number dataY = generateY();
                updateSerie(dataX, dataY);

                mGeneratorScheduler.postDelayed(this,MonitorGUIFragment.PLOT_INITIAL_STEP);
            }
        };
    }

    private void launchGenerator() {
        mStartTime = System.currentTimeMillis();
        mGeneratorScheduler.post(mGeneratorSchedulerTask);
    }

    private Number generateX() {
        return System.currentTimeMillis() - mStartTime;
    }


    private Number generateY() {
        Random mYGenerator = new Random();
        return (mYGenerator.nextDouble() * 2.0 - 1.0) * AMP_MAX;
    }





}
