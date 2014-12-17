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

    BTCommunicationInterface mMessagingManager;

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



    //SettingsControlCallback Impelmentation
    public void onCalibrateClick() {
        mMessagingManager.sendMessage(CALIBRATE_MSG);
    }

}