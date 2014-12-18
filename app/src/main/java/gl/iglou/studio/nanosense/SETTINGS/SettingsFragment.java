package gl.iglou.studio.nanosense.SETTINGS;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.nio.charset.Charset;

import gl.iglou.studio.nanosense.BT.BTCommunicationInterface;
import gl.iglou.studio.nanosense.BT.BTDataConverter;
import gl.iglou.studio.nanosense.BT.BTFragment;
import gl.iglou.studio.nanosense.NanoSenseActivity;

/**
 * Created by metatomato on 17.12.14.
 */
public class SettingsFragment extends Fragment implements SettingsGUIFragment.SettingsControlCallback {

    public static final String TAG = "SettingsFragment";

    private static final String CALIBRATE_MSG = "K\n";

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

    BTCommunicationInterface mMessagingManager;

    SettingsGUIFragment mSettingsGUIFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver,
                new IntentFilter(BTFragment.ACTION_REMOTE_RESPONSE));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mMessagingManager =  ((NanoSenseActivity)getActivity())
                .getBTController();
    }


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int cat = intent.getIntExtra(BTFragment.EXTRA_RESPONSE_CATEGORY,-1);
            switch(cat) {
                case BTFragment.EXTRA_CAT_CALIBRATION:
                    byte[] data = intent.getByteArrayExtra(BTFragment.EXTRA_CALIBRATION_FEEDBACK);
                    String sData = new String(data, Charset.forName("UTF-8"));
                    Log.d(TAG, "Local Broadcastreceived: " + sData);
                    break;
            }
        }
    };


    public void setSettingsGUIFrag(SettingsGUIFragment frag) {
        mSettingsGUIFragment = frag;
    }



    //SettingsControlCallback Impelmentation
    public void onCalibrateClick() {
        mMessagingManager.sendMessage(CALIBRATE_MSG);
    }

    public void onSetCurrent(float current) {
        mCurrentValue = current;
    }

    public void onSetGain(float gain) {
        mGainValue = gain;
    }

    public void onSetCurrentClick() {
        mCurrentRemoteValue = mCurrentValue;
    }

    public void onSetGainClick() {
        mGainRemoteValue = mGainValue;
    }

    public void onResetCurrentClick() {
        mSettingsGUIFragment.setCurrent(mCurrentRemoteValue);
    }

    public void onResetGainClick() {
        mSettingsGUIFragment.setGain(mGainRemoteValue);
    }
}