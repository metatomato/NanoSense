package gl.iglou.studio.nanosense.BT;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import gl.iglou.studio.nanosense.R;

/**
 * Created by metatomato on 07.12.14.
 */
public class BTFragment extends Fragment implements BTGUIFragment.BTControlCallback {

    // Debugging
    private static final String TAG = "BTFragment";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
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

    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;

    // Connected device
    private BluetoothDevice mConnectedDevice = null;
    // Connection state of the current device
    private int mConnectionState = BTService.STATE_NONE;
    // Current UUID
    private String mSelectedUuid;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the BTservices
    private BTService mBTService = null;
    // Member object for the BTGUIFragment
    private BTGUIFragment mBTGUIFrag = null;

    private ArrayList<String> mPreferedDevices;

    public BTFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        mPreferedDevices = new ArrayList<>();

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            getActivity().finish();
            return;
        }

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        getActivity().registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothDevice.ACTION_UUID);
        getActivity().registerReceiver(mReceiver, filter);
    }


    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        fetchDevicesFromSharedData();

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
                    mConnectedDevice = mBluetoothAdapter.getRemoteDevice(address);
                    Log.d(TAG,"Device " + mConnectedDevice.getName() + " address " + mConnectedDevice.getAddress());
                    Toast.makeText(getActivity(), "Found a device to connect!!", Toast.LENGTH_SHORT)
                            .show();
                    addDeviceToPreferedList(mConnectedDevice.getName());
                    updateBTGUIRemote();
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


    private void fetchDevicesFromSharedData() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        String device = sharedPref.getString(getString(R.string.saved_devices), "");
        if(!device.contentEquals(""))
            mPreferedDevices.add(device);
    }

    private void saveDeviceToSharedData(String deviceName) {

        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.saved_devices), deviceName);
        editor.commit();
    }

    private void addDeviceToPreferedList(String name) {
        if(!mPreferedDevices.contains(name)) {
            mPreferedDevices.add(name);
            saveDeviceToSharedData(name);
        }
    }

    public void setBTGUIFrag(BTGUIFragment frag) {
        if(frag != null) {
            mBTGUIFrag = frag;
        }
    }

    private void setupBTService() {
        Log.d(TAG, "setupBTService()");

        // Initialize the BluetoothChatService to perform bluetooth connections
        mBTService = new BTService(getActivity(), mHandler);
    }

    public void updateBTGUIRemote() {
        mBTGUIFrag.updateRemote();
    }

    private void connectDevice(Intent data) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(BTDeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mBTService.connect(device);
    }

    //BTGUIInterface implementation
    public void onScanClick() {
        Log.d(TAG,"Scan for BT devices!");
        Intent serverIntent = new Intent(getActivity(),BTDeviceListActivity.class);
        startActivityForResult(serverIntent, BTFragment.REQUEST_CONNECT_DEVICE);
    }


    public String[] getDeviceList() {
        String[] devices = {""};
        if(!mPreferedDevices.isEmpty())
            devices = mPreferedDevices.toArray(new String[mPreferedDevices.size()]);
        return devices;
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
    public int getConnectionState() {
        return mConnectionState;
    }

    public String getName() {
        String name = "";
        if(mConnectedDevice != null) {
            name = mConnectedDevice.getName();
        }
        return name;
    }

    public String getAddress(){
        String address = "";
        if(mConnectedDevice != null)
            address = mConnectedDevice.getAddress();
        return address;
    }

    public String[] getUUID() {
        ArrayList<String> uuid = new ArrayList<>();
        if(mConnectedDevice != null) {
            Parcelable[] uuids = mConnectedDevice.getUuids();
            for(Parcelable id : uuids) {
                uuid.add(id.toString());
            }
        }
        return uuid.toArray(new String[uuid.size()]);
    }


    public void onUuidSelected(String uuid) {
        mSelectedUuid = uuid;
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BTService.STATE_CONNECTED:
                            Log.d(TAG,"STATE_CONNECTED");
                            break;
                        case BTService.STATE_CONNECTING:
                            Log.d(TAG, "STATE_CONNECTING");
                            break;
                        case BTService.STATE_LISTEN:
                        case BTService.STATE_NONE:
                            Log.d(TAG, "STATE_NONE");
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    Log.d(TAG, "MESSAGE_WRITE");
                    break;
                case MESSAGE_READ:
                    Log.d(TAG, "MESSAGE_READ");
                    break;
                case MESSAGE_DEVICE_NAME:
                    Log.d(TAG,"MESSAGE_DEVICE_NAME");
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getActivity().getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"MESSAGE_TOAST");
                    break;
            }
        }
    };


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
}
