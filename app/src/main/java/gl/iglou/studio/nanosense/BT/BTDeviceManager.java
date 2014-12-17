package gl.iglou.studio.nanosense.BT;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gl.iglou.studio.nanosense.R;

/**
 * Created by metatomato on 15.12.14.
 */
public class BTDeviceManager extends Fragment {

    public static String NULL_MAC_ADDRESS = "00:00:00:00:00:00";
    public static String NULL_UUID = "00000000-0000-0000-0000-000000000000";

    public class BTDevice {
        public String mName;
        public String mAddress;
        public String mUuid;
        public int mState;

        public BTDevice() {
            mName = "";
            mAddress = NULL_MAC_ADDRESS;
            mUuid = NULL_UUID;
            mState = BTService.STATE_NONE;
        }
    }

    private final String TAG = "BTFragment";

    //BT addresses from App SharedData
    private HashSet<String> mPreferedDevices;
    //BT device for current session
    private ArrayList<String> mSessionDevices;
    //Remote Map<Address,BTDevice>
    private HashMap<String, BTDevice> mDevices;
    // Address of the last remote
    private String mLastRemote;
    // Address of current device
    private String mCurrentDevice;

    private BluetoothAdapter mBTAdapater;

    public BTDeviceManager() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBTAdapater = BluetoothAdapter.getDefaultAdapter();

        mPreferedDevices = new HashSet<>();
        mSessionDevices = new ArrayList<>();
        mDevices = new HashMap<>();

        initDevices();
    }


    public void initDevices() {
        fetchDevicesFromSharedData();
        fetchLastRemoteFromSharedData();
        fetchDevices();
        if(mLastRemote != null) {
            mCurrentDevice = mLastRemote;
        } else if(!mDevices.isEmpty()) {
            mCurrentDevice = getAddress(0);
        } else {
            mCurrentDevice = null;
        }
    }


    public void clearSharedPreference() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();

        fetchDevicesFromSharedData();
        fetchLastRemoteFromSharedData();
    }


    private void fetchLastRemoteFromSharedData() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        mLastRemote = sharedPref.getString(getString(R.string.last_remote), null);
    }


    public void saveLastRemoteToSharedData() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        mLastRemote = mCurrentDevice;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.last_remote), mLastRemote);
        if(!mPreferedDevices.contains(mLastRemote))
            saveDeviceToSharedData(mLastRemote);
        editor.apply();
    }


    private void fetchDevicesFromSharedData() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        Set<String> defaultSet = new HashSet<>();
        mPreferedDevices = new HashSet( sharedPref.getStringSet(getString(R.string.saved_devices), defaultSet) );
    }

    private void saveDeviceToSharedData(String deviceAddress) {

        fetchDevicesFromSharedData();
        mPreferedDevices.add(deviceAddress);

        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet(getString(R.string.saved_devices), mPreferedDevices);
        editor.apply();
    }


    public void addAddressToSession(String address) {
        if(!mPreferedDevices.contains(address))
            mSessionDevices.add(address);

        fetchDevices();
    }

    private void fetchDevices() {
        ArrayList<String> list = new ArrayList<>(mPreferedDevices);
        list.addAll(mSessionDevices);
        mDevices.clear();
        for(String address : list) {
            BTDevice device = new BTDevice();
            String name = mBTAdapater.getRemoteDevice(address).getName();
            if(name != null) {
                device.mName = name;
            }
            device.mAddress = address;

            mDevices.put(address, device);
        }
    }

    public void clearAllDevices() {
        clearSharedPreference();
        mDevices.clear();
        mSessionDevices.clear();
        mCurrentDevice = null;
    }

    public String[] getNames() {
        ArrayList<String> list = new ArrayList<>();
        for(Map.Entry<String, BTDevice> entry : mDevices.entrySet()) {
            list.add(entry.getValue().mName);
        }
        return list.toArray(new String[list.size()]);
    }

    public String[] getAddresses() {
        ArrayList<String> list = new ArrayList<>();
        for(Map.Entry<String, BTDevice> entry : mDevices.entrySet()) {
            list.add(entry.getValue().mAddress);
        }
        return list.toArray(new String[list.size()]);
    }


    public String getAddress(int pos) {
        String[] addresses = getAddresses();
        return addresses[pos];
    }


    public boolean isCurrentDeviceInit() {
        return mCurrentDevice != null;
    }

    public String setCurrentDevice(int pos) {
        mCurrentDevice = getAddress(pos);
        return mCurrentDevice;
    }

    public void setCurrentDevice(String address) {
        mCurrentDevice = address;
    }

    public BTDevice getCurrentDevice() {
        return mDevices.get(mCurrentDevice);
    }

    public void setCurrentDeviceUuid(String uuid) {
        mDevices.get(mCurrentDevice).mUuid = uuid;
    }

    public void setCurrentDeviceState(int state) {
        mDevices.get(mCurrentDevice).mState = state;
    }
}
