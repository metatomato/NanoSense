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
import gl.iglou.studio.nanosense.NanoSenseActivity;
import gl.iglou.studio.nanosense.SETTINGS.SettingsGUIFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class MonitorFragment extends Fragment implements MonitorGUIFragment.MonitorControlCallback{

    static private final String TAG = "MonitorFragment";
    static private final int SERIE_MODE_REMOTE = 0;
    static private final int SERIE_MODE_GENERATOR = 1;

    private MonitorGUIFragment mMonitorGUIFragment;
    private SimpleXYSeries mSerie;
    private int mSerieMode = SERIE_MODE_REMOTE;

    private int mPlotCount = 0;
    private long mStartTime = 0L;
    private Handler mGeneratorScheduler;
    private Runnable mGeneratorSchedulerTask;

    private final double AMP_MAX = 4.0;

    private ArrayList<Number> mBuffer;
    private int mBufferSize = 5;
    private boolean mBuffered = false;
    private long mDataStart = 0L;

    private SettingsGUIFragment.SettingsControlCallback mSettingsControlCallback;

    public MonitorFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBuffer = new ArrayList<>();

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


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSettingsControlCallback = ((NanoSenseActivity)getActivity()).getSettingsController();
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
                            float value = (intent.getFloatExtra(BTFragment.EXTRA_SENSOR_DATA_FEEDBACK, 0.f));
                            processData(value);
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
        //resetSerie();

        mStartTime = System.currentTimeMillis();

        if(mSerieMode == SERIE_MODE_GENERATOR) {
            launchGenerator();
        }
    }

    public void onGUIStop() {
        mGeneratorScheduler.removeCallbacks(mGeneratorSchedulerTask);
    }

    public int onStartStopClick() {
        mSettingsControlCallback.onEmissionClick();
        return mSettingsControlCallback.getState();
    }


    long getTick() {
        return System.currentTimeMillis() - mStartTime;
    }


    void processData(float value) {
        if(!mBuffered){
            mBuffer.add(value);
            if(mBuffer.size() == mBufferSize) {
                mBuffered = true;
                mDataStart = System.currentTimeMillis();
            }
        }else {
            if (System.currentTimeMillis() - mDataStart > 1000L){
                if (value < 10.f) {
                    float deltaLimit = 1.f;
                    float lastData = mSerie.getY(mSerie.size() - 1).floatValue();
                    float delta = Math.abs(value - lastData);
                    if (delta < deltaLimit) {
                        updateSerie(bufferMean());
                        updateBuffer(value);
                    }
                } else {
                    Log.d(TAG, "Catch out-of-range value: " + String.valueOf(value));
                }
            } else {
                updateBuffer(value);
            }
        }
    }

    void updateBuffer(float value) {
        mBuffer.remove(0);
        mBuffer.add(value);
    }

    float bufferMean() {
        float sum = 0.f;
        for(Number n : mBuffer) {
            sum += n.floatValue();
        }
        return sum / mBufferSize;
    }

    float bufferMeanDerivative() {
        float sum = 0.f;
        for(int i = 1; i < mBufferSize; i++) {
            sum += mBuffer.get(i).floatValue() - mBuffer.get(i - 1).floatValue();
        }

        return sum / mBufferSize;
    }

// Serie Helper Methods
    private void initSerie() {
        ArrayList<Number> listY = new ArrayList<>();
        ArrayList<Number> listX = new ArrayList<>();
        for(int i = 0 ; i < MonitorGUIFragment.PLOT_SIZE; i++) {
            listX.add(0.0);
            listY.add(0.0);
        }
        mSerie = new SimpleXYSeries(listX,listY,"data");
        mPlotCount = 0;
    }

    private void updateSerie(Number Y) {
        mSerie.removeFirst();
        Number lastX = mSerie.getX(mSerie.size() - 1 ).doubleValue() + MonitorGUIFragment.PLOT_INITIAL_STEP;
        mSerie.addLast(lastX,Y);
        //Log.d(TAG,"X: " + String.valueOf(X) + "     Y: " + String.valueOf(Y));
    }

    private void resetSerie() {
        for(int i = 0 ; i < MonitorGUIFragment.PLOT_SIZE ; i++) {
            mSerie.setXY(0.0,0.0,i);
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
                updateSerie(dataY);

                mGeneratorScheduler.postDelayed(this,MonitorGUIFragment.PLOT_INITIAL_STEP);
            }
        };
    }

    private void launchGenerator() {
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
