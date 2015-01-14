package gl.iglou.studio.nanosense.SETTINGS;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;

import gl.iglou.studio.nanosense.BT.BTCommunicationInterface;
import gl.iglou.studio.nanosense.BT.BTFragment;
import gl.iglou.studio.nanosense.BT.BTService;
import gl.iglou.studio.nanosense.NanoSenseActivity;
import gl.iglou.studio.nanosense.R;

/**
 * Created by metatomato on 17.12.14.
 */
public class SettingsFragment extends Fragment implements SettingsGUIFragment.SettingsControlCallback {

    public static final String TAG = "SettingsFragment";

    private static final String MSG_CALIBRATE = "K\n";
    private static final String MSG_SET_CURRENT = "Cint\n";
    private static final String MSG_SET_GAIN = "Gint\n";
    private static final String MSG_EMISSON_START = "R\n";
    private static final String MSG_EMISSON_STOP = "S\n";

    public static final float CURRENT_INITIAL_VALUE = 10.f;
    public static final float CURRENT_MAX_VALUE = 50.f;
    public static final float CURRENT_MIN_VALUE = 2.5f;
    public static final float CURRENT_STEP_VALUE = 2.5f;

    public static final float GAIN_INITIAL_VALUE = 25.f;
    public static final float GAIN_MAX_VALUE = 100.f;
    public static final float GAIN_MIN_VALUE = 25.f;
    public static final float GAIN_STEP_VALUE = 5.f;

    public static final float SWIPE_INITIAL_VALUE = 5.f;
    public static final float SWIPE_MAX_VALUE = 10.f;
    public static final float SWIPE_MIN_VALUE = 1.f;
    public static final float SWIPE_STEP_VALUE = 1.f;

    private float mCurrentValue = CURRENT_INITIAL_VALUE;
    private float mGainValue = GAIN_INITIAL_VALUE;
    private float mSwipeValue = SWIPE_INITIAL_VALUE;

    private float mCurrentRemoteValue = CURRENT_INITIAL_VALUE;
    private float mGainRemoteValue = GAIN_INITIAL_VALUE;
    private float mSwipeRemoteValue = SWIPE_INITIAL_VALUE;



    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_READY = 1;
    public static final int STATE_BROADCASTING = 2;

    private int mRemoteState;

    private int mCalculatedCurrent;
    private int mCalculatedGain;

    private float mCalibrationResistance = 0.f;
    private float mCalibrationCurrent = 0.f;

    //DataRate Processing Vars
    private Handler mDataRateScheduler;
    private Runnable mHighFrequncyTask;
    private Runnable mLowFrequncyTask;
    private float mDataHighRate = 0.f;
    private float mDataLowRate = 0.f;
    private ArrayList<Number> mHighFrequencyData;
    private ArrayList<Number> mLowFrequencyData;
    private long mHighFrequencyClock;
    private long mLowFrequencyClock;

    BTCommunicationInterface mMessagingManager;

    SettingsGUIFragment mSettingsGUIFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver,
                new IntentFilter(BTFragment.ACTION_REMOTE_RESPONSE));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver,
                new IntentFilter(BTFragment.ACTION_REMOTE_STATE_CHANGE));

        mHighFrequencyData = new ArrayList<>();
        mLowFrequencyData = new ArrayList<>();
        initDataRateScheduler();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mMessagingManager =  ((NanoSenseActivity)getActivity()).getBTController();

        checkRemoteState();

    }


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BTFragment.ACTION_REMOTE_RESPONSE:
                    int cat = intent.getIntExtra(BTFragment.EXTRA_RESPONSE_CATEGORY, -1);
                    float data = intent.getFloatExtra(BTFragment.EXTRA_DATA_FEEDBACK, 0.f);
                    switch (cat) {
                        case BTFragment.EXTRA_CAT_CALIBRATION:
                            Log.d(TAG, "Local Broadcastreceived: " + String.valueOf(data));
                            updateCalibrationValues(data);
                            break;
                        case BTFragment.EXTRA_CAT_SENSOR_DATA:
                            float value = intent.getFloatExtra(BTFragment.EXTRA_DATA_FEEDBACK, 0.f);
                            mHighFrequencyData.add(value);
                            mLowFrequencyData.add(value);
                            break;
                    }
                    break;
                case BTFragment.ACTION_REMOTE_STATE_CHANGE:
                    Log.d(TAG, "SettingsFragment received state change broadcast");
                    int state = intent.getIntExtra(BTFragment.EXTRA_REMOTE_STATE,0);
                    switch(state) {
                        case BTService.STATE_NONE:
                            mRemoteState = STATE_DISCONNECTED;
                            break;
                        case BTService.STATE_CONNECTED:
                            mRemoteState = STATE_READY;
                            break;
                    }
                     break;
            }
        }
    };


    public void setSettingsGUIFrag(SettingsGUIFragment frag) {
        mSettingsGUIFragment = frag;
    }


    private String prepareMessage(String message, int value) {
            return message.replace("int",String.valueOf(value));
    }

    private void checkRemoteState() {
        if(mRemoteState != STATE_BROADCASTING) {
            if (mMessagingManager.isRemoteConnected()) {
                mRemoteState = STATE_READY;
            } else {
                mRemoteState = STATE_DISCONNECTED;
            }
        }
    }

    private void initDataRateScheduler() {
        mDataRateScheduler = new Handler();
        mHighFrequncyTask = new Runnable() {
            @Override
            public void run() {
                updateDataHighRate();
                mDataRateScheduler.postDelayed(this, 300L);
            }
        };

        mLowFrequncyTask = new Runnable() {
            @Override
            public void run() {
                updateDataLowRate();
                mDataRateScheduler.postDelayed(this, 3000L);
            }
        };
    }

    private void dataRateStart() {
        mHighFrequencyClock = System.currentTimeMillis();
        mLowFrequencyClock = System.currentTimeMillis();
        mDataRateScheduler.post(mHighFrequncyTask);
        mDataRateScheduler.post(mLowFrequncyTask);
    }

    private void dataRateStop() {
        mDataRateScheduler.removeCallbacks(mHighFrequncyTask);
        mDataRateScheduler.removeCallbacks(mLowFrequncyTask);
        mDataHighRate = 0.f;
        mDataLowRate = 0.f;
        mSettingsGUIFragment.setDataRateValue(mDataHighRate,mDataLowRate);
    }

    private void updateDataHighRate() {
        mHighFrequencyClock = System.currentTimeMillis() - mHighFrequencyClock;
        mDataHighRate = mHighFrequencyData.size() *  1000.f / mHighFrequencyClock;
        mSettingsGUIFragment.setDataRateValue(mDataHighRate,mDataLowRate);
        mHighFrequencyClock = System.currentTimeMillis();
        mHighFrequencyData.clear();
    }


    private void updateDataLowRate() {
        mLowFrequencyClock = System.currentTimeMillis() - mLowFrequencyClock;
        mDataLowRate = mLowFrequencyData.size() *  1000.f / mLowFrequencyClock;
        mSettingsGUIFragment.setDataRateValue(mDataHighRate,mDataLowRate);
        mLowFrequencyClock = System.currentTimeMillis();
        mLowFrequencyData.clear();
    }

    private void updateDataRateScheduler(int state) {
        switch(state) {
            case STATE_READY:
                dataRateStop();
                break;
            case STATE_DISCONNECTED:
                dataRateStop();
                break;
            case STATE_BROADCASTING:
                dataRateStart();
                break;
        }
    }

    private void updateCalibrationValues(float voltage) {
        calculateCalibrationValues(voltage);
        Log.d(TAG,"Resistance: " + String.valueOf(mCalibrationResistance) + "   current: "
                + String.valueOf(mCalibrationCurrent) );
        mSettingsGUIFragment.setCalibrationValues(mCalibrationCurrent,mCalibrationResistance);
    }

    private void calculateCalibrationValues(float voltage) {
        mCalibrationResistance  = voltage / mCurrentRemoteValue;
        mCalibrationCurrent = 22.f / mCalibrationResistance;
    }

//SettingsControlCallback Impelmentation
    public void onCalibrateClick() {
        if( getState() == STATE_BROADCASTING) {
            Toast.makeText(getActivity(),getResources().getString(R.string.alert_calibration)
                    ,Toast.LENGTH_SHORT).show();
        } else {
            mSettingsGUIFragment.resetCalibrationValues();
            mMessagingManager.sendMessage(MSG_CALIBRATE);
        }
    }

    public void onSetCurrent(float current) {
        mCurrentValue = current;
    }

    public void onSetGain(float gain) {
        mGainValue = gain;
    }

    public void onSetSwipe(float swipe) {
        mSwipeValue = swipe;
    }

    public void onSetCurrentClick() {
        mCurrentRemoteValue = mCurrentValue;
        mCalculatedCurrent = SettingsHelper.calculateCurrent(mCurrentRemoteValue);

        String message = prepareMessage(MSG_SET_CURRENT,mCalculatedCurrent);
        mMessagingManager.sendMessage(message);

        Log.d(TAG,"Current set to " + String.valueOf(mCalculatedCurrent) + " from " +
                String.valueOf(mCurrentRemoteValue));

    }

    public void onSetGainClick() {
        mGainRemoteValue = mGainValue;
        mCalculatedGain = SettingsHelper.calculateGain(mGainRemoteValue);

        String message = prepareMessage(MSG_SET_GAIN,mCalculatedGain);
        mMessagingManager.sendMessage(message);

        Log.d(TAG,"Gain set to " + String.valueOf(mCalculatedGain) + " from " +
                String.valueOf(mGainRemoteValue));
    }

    public void onSetSwipeClick() {
        mSwipeRemoteValue = mSwipeValue;

        Log.d(TAG,"Swipe set to " + String.valueOf(mSwipeRemoteValue));
    }

    public void onResetCurrentClick() {
        mSettingsGUIFragment.setCurrent(mCurrentRemoteValue);
    }

    public void onResetGainClick() {
        mSettingsGUIFragment.setGain(mGainRemoteValue);
    }

    public void onResetSwipeClick() {
        mSettingsGUIFragment.setGain(mSwipeRemoteValue);
    }

    public void onEmissionClick() {
        switch(mRemoteState) {
            case STATE_DISCONNECTED :
                Toast.makeText(getActivity(), getResources().getString(R.string.connect_first)
                        ,Toast.LENGTH_SHORT).show();
                break;
            case STATE_READY:
                mMessagingManager.sendMessage(MSG_EMISSON_START);
                setState(STATE_BROADCASTING);
                break;
            case STATE_BROADCASTING:
                mMessagingManager.sendMessage(MSG_EMISSON_STOP);
                setState(STATE_READY);
                break;
        }
    }

    private void setState(int state) {
        mRemoteState = state;
        if(mSettingsGUIFragment.isAdded()) {
            mSettingsGUIFragment.updateState(state);
            updateDataRateScheduler(state);
        }
    }

    public void onGUIPause() {
        dataRateStop();
    }
    public void onGUIStart() {
        dataRateStart();
    }



    public int getState() { return mRemoteState; }
    public boolean isReceiving() { return false; }
    public float getRemoteResistance() { return 0.f; }
    public float getRemoteMaxCurrent() { return 0.f; }
    public float getCurrent() { return mCurrentValue; }
    public float getGain() { return mGainValue; }
    public float getSwipe() {return mSwipeRemoteValue;}
    public float getDataHighRate() {return mDataHighRate;}
    public float getDataLowRate() {return mDataLowRate;}

}