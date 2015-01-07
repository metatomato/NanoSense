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

    private float mCurrentValue = CURRENT_INITIAL_VALUE;
    private float mGainValue = GAIN_INITIAL_VALUE;

    private float mCurrentRemoteValue = CURRENT_INITIAL_VALUE;
    private float mGainRemoteValue = GAIN_INITIAL_VALUE;

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_READY = 1;
    public static final int STATE_BROADCASTING = 2;

    private int mRemoteState;

    private int mCalculatedCurrent;
    private int mCalculatedGain;


    //DataRate Processing Vars
    private Handler mDataRateScheduler;
    private Runnable mSchedulerTask;
    private float mDataRate = 0.f;
    private ArrayList<Number> mData;
    private long timer;

    BTCommunicationInterface mMessagingManager;

    SettingsGUIFragment mSettingsGUIFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver,
                new IntentFilter(BTFragment.ACTION_REMOTE_RESPONSE));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver,
                new IntentFilter(BTFragment.ACTION_REMOTE_STATE_CHANGE));

        mData = new ArrayList<>();
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
                    switch (cat) {
                        case BTFragment.EXTRA_CAT_CALIBRATION:
                            byte[] data = intent.getByteArrayExtra(BTFragment.EXTRA_CALIBRATION_FEEDBACK);
                            String sData = new String(data, Charset.forName("UTF-8"));
                            Log.d(TAG, "Local Broadcastreceived: " + sData);
                            break;
                        case BTFragment.EXTRA_CAT_SENSOR_DATA:
                            mData.add(intent.getFloatExtra(BTFragment.EXTRA_SENSOR_DATA_FEEDBACK, 0.f));
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
        mSchedulerTask = new Runnable() {
            @Override
            public void run() {
                updateDataRate();
                mDataRateScheduler.postDelayed(this, 100);
            }
        };
    }

    private void dataRateStart() {
        timer = System.currentTimeMillis();
        mDataRateScheduler.postDelayed(mSchedulerTask, 0);
    }

    private void dataRateStop() {
        mDataRateScheduler.removeCallbacks(mSchedulerTask);
        mDataRate = 0.f;
        mSettingsGUIFragment.setDataRateValue(mDataRate);
    }

    private void updateDataRate() {
        timer = System.currentTimeMillis() - timer;
        mDataRate = mData.size() *  1000.f / timer;
        mSettingsGUIFragment.setDataRateValue(mDataRate);
        timer = System.currentTimeMillis();
        mData.clear();
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

//SettingsControlCallback Impelmentation
    public void onCalibrateClick() {
        mMessagingManager.sendMessage(MSG_CALIBRATE);
    }

    public void onSetCurrent(float current) {
        mCurrentValue = current;
    }

    public void onSetGain(float gain) {
        mGainValue = gain;
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

    public void onResetCurrentClick() {
        mSettingsGUIFragment.setCurrent(mCurrentRemoteValue);
    }

    public void onResetGainClick() {
        mSettingsGUIFragment.setGain(mGainRemoteValue);
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


    public int getState() { return mRemoteState; }
    public boolean isReceiving() { return false; }
    public float getRemoteResistance() { return 0.f; }
    public float getRemoteMaxCurrent() { return 0.f; }
    public float getCurrent() { return mCurrentValue; }
    public float getGain() { return mGainValue; }
    public float getDataRate() {return mDataRate;}
}