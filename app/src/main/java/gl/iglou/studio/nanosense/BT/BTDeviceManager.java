package gl.iglou.studio.nanosense.BT;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gl.iglou.studio.nanosense.R;

/**
 * Created by metatomato on 15.12.14.
 */
public class BTDeviceManager extends Fragment {

    private final String TAG = "BTFragment";

    //Get BT addresses from App SharedData
    private HashSet<String> mPreferedDevices;
    //Remote Map<Address,Name>
    private HashMap<String, String> mRemoteMap;
    private String mLastRemote;

    private BluetoothAdapter mBTAdapater;

    public BTDeviceManager() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBTAdapater = BluetoothAdapter.getDefaultAdapter();

        mPreferedDevices = new HashSet<>();
        mRemoteMap = new HashMap<>();

        fetchDevicesFromSharedData();
        fetchLastRemoteFromSharedData();
    }

    private void fetchLastRemoteFromSharedData() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        mLastRemote = sharedPref.getString(getString(R.string.last_remote), null);

        Log.d(TAG, "Fetched address: " + mLastRemote);
    }


    public void saveLastRemoteToSharedData(String address) {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        mLastRemote = address;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.last_remote), mLastRemote);
        editor.commit();

        Log.d(TAG, "Saved address: " + mLastRemote);
    }


    private void fetchDevicesFromSharedData() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        Set<String> defaultSet = new HashSet<>();
        mPreferedDevices = new HashSet( sharedPref.getStringSet(getString(R.string.saved_devices), defaultSet) );

        fetchRemoteName();
    }

    private void saveDeviceToSharedData(String deviceAddress) {

        fetchDevicesFromSharedData();
        mPreferedDevices.add(deviceAddress);

        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet(getString(R.string.saved_devices), mPreferedDevices);
        editor.commit();

        fetchRemoteName();
    }

    public void addAddressToPrefered(String address) {
        if(!mPreferedDevices.contains(address)) {
            saveDeviceToSharedData(address);
        }
    }

    private void fetchRemoteName() {
        mRemoteMap.clear();
        for(String address : mPreferedDevices) {
            String name = mBTAdapater.getRemoteDevice(address).getName();
            if(name != null) {
                int cloneNum = checkUniqueness(name);
                if(cloneNum == 0)
                    mRemoteMap.put(address,name);
                else
                    mRemoteMap.put(address,name+"_"+cloneNum);
            }
        }
    }

    private int checkUniqueness(String name) {
        int count = 0;
        for(String value : mRemoteMap.values()) {
            if( value.contentEquals(name))
                count++;
        }
        return count;
    }


    public String[] getNames() {
        return mRemoteMap.values().toArray(new String[mRemoteMap.size()]);
    }


    public String getName(String address) {
       return mRemoteMap.get(address);
    }


    public String getAddress(String name) {
        String address = null;
        for(String  key : mRemoteMap.keySet()) {
            if(mRemoteMap.get(key).contentEquals(name))
                address = key;
        }
        return address;
    }


    public String getLastRemote() {
        return mLastRemote;
    }

    public String getDefaultRemote() {
        String defaultRemote = null;
        if(mRemoteMap != null) {
            if (!mRemoteMap.isEmpty()) {
                defaultRemote = getAddress(getNames()[0]);
            }
        }
        return defaultRemote;
    }
}
