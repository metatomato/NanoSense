package gl.iglou.studio.nanosense.BT;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import gl.iglou.studio.nanosense.MONITOR.MonitorFragment;
import gl.iglou.studio.nanosense.NanoSenseActivity;
import gl.iglou.studio.nanosense.SETTINGS.SettingsFragment;


/**
 * Created by metatomato on 07.12.14.
 */
public class BTFragment extends Fragment implements BTGUIFragment.BTControlCallback,
        BTCommunicationInterface{

    // Debugging
    public static final String TAG = "BTFragment";
    private static final boolean D = true;

    // Message types sent from the BTService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    public static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_ENABLE_DISCOVERY = 3;

    private static final int BT_DISCOVERABLE_DURATION = 30;

    public static final String ACTION_REMOTE_STATE_CHANGE = "remote_state_change";
    public static final String EXTRA_REMOTE_STATE = "remote_state";

    public static final String ACTION_REMOTE_RESPONSE = "remote_response";
    public static final String EXTRA_RESPONSE_CATEGORY = "category";
    public static final int EXTRA_CAT_SENSOR_DATA = 0;
    public static final int EXTRA_CAT_CALIBRATION = 1;
    public static final String EXTRA_DATA_FEEDBACK = "data_feedback";

    private int mCurrentDataMode = 0;

    private final String SENSOR_DATA_PATTERN = "\\d\\.[\\d]{3}";
    private final String CALIBRATION_DATA_PATTERN = "\\d+\\.[\\d]{3}";
    private String[] mProcessPattern  = {SENSOR_DATA_PATTERN,CALIBRATION_DATA_PATTERN};


    private MonitorFragment mDataManager;
    private SettingsFragment mDataProcessor;

    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the BTservices
    private BTService mBTService = null;
    // Member object for the BTGUIFragment
    private BTGUIFragment mBTGUIFrag = null;
    // Member object for the BTDeviceManager
    private BTDeviceManager mDeviceManager = null;

    // Received data processing Vars
    private String mData;

    public BTFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(D) Log.e(TAG, "+++ ON CREATE +++");

                // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            getActivity().finish();
            return;
        }

        FragmentManager fm = getFragmentManager();
        if(mDeviceManager == null) {
            mDeviceManager = new BTDeviceManager();
            fm.beginTransaction().add(mDeviceManager, "BTDeviceManager").commit();
        }

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        getActivity().registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothDevice.ACTION_UUID);
        getActivity().registerReceiver(mReceiver, filter);

        mData = new String();
    }


    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mBTService == null) setupBTService();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras().getString(BTDeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    mDeviceManager.addAddressToSession(address);
                    mDeviceManager.setCurrentDevice(address);
                    updateBTGUIRemote();
                } else {
                    mBTGUIFrag.setRemoteSpinnerSelection(getPosition());
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupBTService();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    mBTGUIFrag.setAdapterSwitch(false);
                }
                break;
            case REQUEST_ENABLE_DISCOVERY:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(getActivity(), "device discoverable!", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    mBTGUIFrag.setDiscoverySwitch(false);
                }
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mDataManager = ((NanoSenseActivity)getActivity()).getMonitorController();
        mDataProcessor = ((NanoSenseActivity)getActivity()).getSettingsController();
    }

    public void setBTGUIFrag(BTGUIFragment frag) {
        if(frag != null) {
            mBTGUIFrag = frag;
        }
    }

    private void setupBTService() {
        Log.d(TAG, "setupBTService()");

        // Initialize the BTService to perform bluetooth connections
        mBTService = new BTService(getActivity(), mHandler);
    }


    public void updateBTGUIRemote() {
        mBTGUIFrag.updateRemote(true);
    }


    private void connectDevice() {
        BluetoothDevice remote = mBluetoothAdapter.
                getRemoteDevice(mDeviceManager.getCurrentDevice().mAddress);
        String uuid = mDeviceManager.getCurrentDevice().mUuid;
        mBTService.connect(remote, uuid);
    }


    private void disconnectDevice() {
        mBTService.stop();
    }

    //BTGUIInterface implementation
    public void onScanClick() {
        Log.d(TAG,"Scan for BT devices!");
        Intent serverIntent = new Intent(getActivity(),BTDeviceListActivity.class);
        startActivityForResult(serverIntent, BTFragment.REQUEST_CONNECT_DEVICE);
    }


    public String[] getDeviceList() {
        return mDeviceManager.getNames();
    }


    public boolean isBTEnabled(){
        return mBluetoothAdapter.isEnabled();
    }


    public void setBTActivated(boolean state) {
        if (state && !isBTEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else if(!state && isBTEnabled()) {
            mBluetoothAdapter.disable();
        }
    }

    public boolean isDiscoverable() {
        return getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE;
    }


    public int getScanMode() {
        return mBluetoothAdapter.getScanMode();
    }


    public void setDiscoverable() {
        Log.d(TAG,"ScanMode: "+ String.valueOf(getScanMode()));
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BT_DISCOVERABLE_DURATION);
            startActivityForResult(discoverableIntent, REQUEST_ENABLE_DISCOVERY);
        } else {
            Toast.makeText(getActivity().getApplicationContext(), " Device is already Discoverable! "
                , Toast.LENGTH_SHORT).show();
        }
    }

    //Remote Callbacks
    public void setCurrentDevice(int pos) {
        String address = mDeviceManager.setCurrentDevice(pos);
    }


    public int getPosition() {
        if(mDeviceManager.isCurrentDeviceInit()) {
            ArrayList<String> addresses = new ArrayList<>(Arrays.asList(mDeviceManager.getAddresses()));
            return addresses.indexOf(mDeviceManager.getCurrentDevice().mAddress);
        } else {
            return 0;
        }
    }

    public String getName() {
        if(mDeviceManager.isCurrentDeviceInit())
            return mDeviceManager.getCurrentDevice().mName;
        else
            return "";
    }

    public String getAddress(){
        if(mDeviceManager.isCurrentDeviceInit())
            return mDeviceManager.getCurrentDevice().mAddress;
        else
            return "-";
    }

    public String[] getUUID() {
        ArrayList<String> uuid = new ArrayList<>();
        if(mDeviceManager.isCurrentDeviceInit()) {
            Parcelable[] uuids = mBluetoothAdapter.getRemoteDevice(
                    mDeviceManager.getCurrentDevice().mAddress).getUuids();
            if(uuids != null) {
                for (Parcelable id : uuids) {
                    uuid.add(id.toString());
                }
            }
        }
        if(!uuid.isEmpty())
            return uuid.toArray(new String[uuid.size()]);
        else
            return null;
    }


    public int getConnectionState() {
        if(mDeviceManager.isCurrentDeviceInit())
            return mDeviceManager.getCurrentDevice().mState;
        else
            return BTService.STATE_NONE;
    }


    public void onUuidSelected(String uuid) {
        mDeviceManager.setCurrentDeviceUuid(uuid);
    }


    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    setState(msg.arg1);
                    break;
                case MESSAGE_WRITE:
                    String sendMsg = BTDataConverter.decodeMessage((byte[])msg.obj,"UTF_8");
                    Log.d(TAG, "WRITE " + sendMsg);
                    if(sendMsg.startsWith("K")) {
                        Log.d(TAG, "CALIBRATION!");
                        mCurrentDataMode = EXTRA_CAT_CALIBRATION;
                    } else {
                        mCurrentDataMode = EXTRA_CAT_SENSOR_DATA;
                    }
                    Log.d(TAG, "MESSAGE_WRITE");
                    break;
                case MESSAGE_READ:
                    //Log.d(TAG,"MESSAGE_READ");

                    String processPattern = mProcessPattern[mCurrentDataMode];

                    byte[] data = Arrays.copyOf((byte[])msg.obj,msg.arg1);

                    String message = BTDataConverter.decodeMessage(data, "UTF-8");
                    Log.d(TAG,"RAW MSG " + message);
                    mData += message;

                    if(mData.contains("\n")) {
                        int mark = mData.lastIndexOf("\n");
                        String extracted = mData.substring(0,mark);
                        String[] values = extracted.split("[\\r\\n]+");
                        for(String s : values){
                            if(s.matches(processPattern)) {
                                try {
                                    float value = Float.parseFloat(s);
                                    mDataManager.processData(value);
                                    broadcastRemoteData(mCurrentDataMode,value);
                                    Log.d(TAG, "Broadcasted: " + String.valueOf(value));
                                } catch (NumberFormatException e) {
                                    Log.d(TAG, "String2Float FAILED! ");
                                }
                            }
                        }
                        if(!mData.endsWith("\n")) {
                            mData = mData.substring(mark + 1);
                        } else {
                            mData = "";
                        }
                    }

                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getActivity().getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"MESSAGE_TOAST");
                    break;
            }
        }
    };



    public void onCalibrateClick()
    {
        sendMessage("K\n");
    }


    private void setState(int state) {
        mDeviceManager.setCurrentDeviceState(state);
        switch (state) {
            case BTService.STATE_CONNECTED:
                mDeviceManager.saveLastRemoteToSharedData();
                Log.d(TAG,"STATE_CONNECTED");
                break;
            case BTService.STATE_CONNECTING:
                Log.d(TAG, "STATE_CONNECTING");
                break;
            case BTService.STATE_NONE:
                Log.d(TAG, "STATE_NONE");
                break;
        }
        mBTGUIFrag.setConnectionState();
        broadcastRemoteState(state);
    }


    public void onConnectClick() {
        if(mDeviceManager.isCurrentDeviceInit()) {
            switch (mDeviceManager.getCurrentDevice().mState) {
                case BTService.STATE_NONE:
                    connectDevice();
                    break;
                case BTService.STATE_CONNECTED:
                    disconnectDevice();
                    break;
                case BTService.STATE_CONNECTING:
                    disconnectDevice();
                    break;
            }
        }
    }


    public void clearAllRemotes() {
        mDeviceManager.clearAllDevices();
        updateBTGUIRemote();
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch(action) {
                case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
                    if (mBTGUIFrag != null) {
                        int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, 0);
                        switch (scanMode) {
                            case BluetoothAdapter.SCAN_MODE_NONE:
                                Log.d(TAG, "SCAN_MODE_NONE");
                                mBTGUIFrag.setDiscoverySwitch(false);
                                break;
                            case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                                Log.d(TAG, "SCAN_MODE_CONNECTABLE");
                                mBTGUIFrag.setDiscoverySwitch(false);
                                break;
                            case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                                Log.d(TAG, "SCAN_MODE_CONNECTABLE_DISCOVERABLE");
                        }
                    }
                    break;
                case BluetoothDevice.ACTION_UUID:
                    Log.d(TAG, "Get an UUID_ACTION!!");
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                    if(uuidExtra != null) {
                        for (int i = 0; i < uuidExtra.length; i++) {
                            Log.d(TAG, "\n  Device: " + device.getName() + ", " + device + ", Service: " + uuidExtra[i].toString());
                        }
                    }
                    break;
            }
        }
    };


    //BTCommunicationInterface implementation

    public boolean isRemoteConnected() {
        if(mDeviceManager.isCurrentDeviceInit()) {
            return mDeviceManager.getCurrentDevice().mState == BTService.STATE_CONNECTED;
        }
        else {
            return false;
        }
    }

    //SettingsRemoteCallback implementation

    public void sendMessage(String message) {
        try {
            byte[] b = message.getBytes("UTF-8");
            mBTService.write(b);
            Log.d(TAG, "REMOTE SEND MESSAGE " + message);
        } catch (UnsupportedEncodingException e)
        {
            Log.d(TAG, "REMOTE SEND MESSAGE " + message + " FAILED: string encoding error", e);
        }
    }

    private void broadcastRemoteData(int mode, float value) {
        Intent intent = new Intent(ACTION_REMOTE_RESPONSE);
        intent.putExtra(EXTRA_RESPONSE_CATEGORY, mode);
        intent.putExtra(EXTRA_DATA_FEEDBACK,value);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    private void broadcastRemoteState(int state) {
        Intent intent = new Intent(ACTION_REMOTE_STATE_CHANGE);
        intent.putExtra(EXTRA_REMOTE_STATE,state);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }
}
