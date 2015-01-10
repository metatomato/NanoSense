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
import gl.iglou.studio.nanosense.MP.MPInterface;
import gl.iglou.studio.nanosense.NanoSenseActivity;
import gl.iglou.studio.nanosense.SETTINGS.SettingsGUIFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class MonitorFragment extends Fragment implements MonitorGUIFragment.MonitorControlCallback{

    static private final String TAG = "MonitorFragment";
    static private final int SERIE_MODE_REMOTE = 0;
    static private final int SERIE_MODE_GENERATOR = 1;

    static public final int SERIE_PRIMARY = 0;
    static public final int SERIE_SECONDARY = 1;

    private MonitorGUIFragment mMonitorGUIFragment;
    private SimpleXYSeries mPrimarySerie;
    private SimpleXYSeries mSecondarySerie;
    private int mSerieMode = SERIE_MODE_REMOTE;

    private int mPlotCount = 0;
    private long mStartTime = 0L;
    private Handler mGeneratorScheduler;
    private Runnable mGeneratorSchedulerTask;

    private final double AMP_MAX = 4.0;

    private ArrayList<Number> mBuffer;
    private int mBufferSize = 7;
    private boolean mBuffered = false;
    private long mDataStart = 0L;

    private float mYMin,mYMax;

    private boolean mReverseData = false;

    private MPInterface mMPCallback;
    private float mMinTimeElapsed = 300.f;
    private long mLastTrigger = 0L;

    private SettingsGUIFragment.SettingsControlCallback mSettingsControlCallback;

    public MonitorFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBuffer = new ArrayList<>();

        initSerie(SERIE_PRIMARY);
        initSerie(SERIE_SECONDARY);
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

        mMPCallback = ((NanoSenseActivity)getActivity()).getMPController();
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
                            //processData(value);
                            break;
                    }
                    break;
            }
        }
    };





// MonitorControlCallback Methods Implementation
    public XYSeries getData(int serieId) {
        if(serieId == SERIE_PRIMARY) {
            return mPrimarySerie;
        } else {
            return mSecondarySerie;
        }
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


    public void processData(float value) {
        float data;
        if(!mReverseData) {
            data = 5 - value;
        } else {
            data = value;
        }
        if(!mBuffered){
            mBuffer.add(data);
            if(mBuffer.size() == mBufferSize) {
                mBuffered = true;
                mDataStart = System.currentTimeMillis();
            }
        }else {
            /*
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
            */
            updateSerie(SERIE_PRIMARY,smoothData());
            float derivate = smoothDerivateData();
            updateSerie(SERIE_SECONDARY,derivate);

            if(Math.abs(derivate) > mSettingsControlCallback.getSwipe()) {
                if(System.currentTimeMillis() - mLastTrigger > mMinTimeElapsed) {
                    mMPCallback.onNextClick();
                    mLastTrigger = System.currentTimeMillis();
                }
            }

            updateBuffer(data);
        }
    }

    void updateBuffer(float value) {
        mBuffer.remove(0);
        mBuffer.add(value);
    }

    float meanData() {
        float sum = 0.f;
        for(Number n : mBuffer) {
            sum += n.floatValue();
        }
        return sum / mBufferSize;
    }


    private float derivateData() {
        float derivate =  mBuffer.get(4).floatValue() -  mBuffer.get(0).floatValue();
        return derivate / (4.f * MonitorGUIFragment.PLOT_INITIAL_STEP) * 1000.f;
    }

    private float smoothDerivateData() {
        float smoothDerivate =    1.f * ( mBuffer.get(1).floatValue() - mBuffer.get(0).floatValue())
                                + 2.f * ( mBuffer.get(2).floatValue() - mBuffer.get(1).floatValue())
                                + 3.f * ( mBuffer.get(3).floatValue() - mBuffer.get(2).floatValue())
                                + 3.f * ( mBuffer.get(4).floatValue() - mBuffer.get(3).floatValue())
                                + 2.f * ( mBuffer.get(5).floatValue() - mBuffer.get(4).floatValue())
                                + 1.f * ( mBuffer.get(6).floatValue() - mBuffer.get(5).floatValue());
        return smoothDerivate / ( 12.f * 6.f * MonitorGUIFragment.PLOT_INITIAL_STEP) * 1000.f;
    }

    float smoothData() {
        float smoothData = mBuffer.get(1).floatValue() + 2.f * mBuffer.get(2).floatValue()
                + 3.f * mBuffer.get(3).floatValue() + 2.f * mBuffer.get(4).floatValue()
                + mBuffer.get(5).floatValue();
        return smoothData / 9.f;
    }

    private float meanDerivateData() {
        float sum = 0.f;
        for(int i = 1; i < mBufferSize; i++) {
            sum += mBuffer.get(i).floatValue() - mBuffer.get(i - 1).floatValue();
        }

        return sum / mBufferSize;
    }

// Serie Helper Methods
    private void initSerie(int serieId) {
        ArrayList<Number> listY = new ArrayList<>();
        ArrayList<Number> listX = new ArrayList<>();
        for(int i = 0 ; i < MonitorGUIFragment.PLOT_SIZE; i++) {
            listX.add(0.0);
            listY.add(0.0);
        }
        if(serieId == SERIE_PRIMARY) {
            mPrimarySerie = new SimpleXYSeries(listX, listY, "data");
        } else {
            mSecondarySerie = new SimpleXYSeries(listX, listY, "data");
        }
        mPlotCount = 0;
    }

    private void updateSerie(int serieId, Number Y) {
       SimpleXYSeries currentSerie = getSerie(serieId);
        currentSerie.removeFirst();
        Number lastX = currentSerie.getX(currentSerie.size() - 1 ).doubleValue() + MonitorGUIFragment.PLOT_INITIAL_STEP;
        currentSerie.addLast(lastX, Y);
        //Log.d(TAG,"X: " + String.valueOf(X) + "     Y: " + String.valueOf(Y));
    }

    private void resetSerie(int serieId) {
        for(int i = 0 ; i < MonitorGUIFragment.PLOT_SIZE ; i++) {
            getSerie(serieId).setXY(0.0, 0.0, i);
        }
    }


    private SimpleXYSeries getSerie(int serieId) {
        if(serieId == SERIE_PRIMARY) {
            return mPrimarySerie;
        } else {
            return mSecondarySerie;
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
                updateSerie(SERIE_PRIMARY, dataY);

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
