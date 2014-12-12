package gl.iglou.studio.nanosense.BT;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import gl.iglou.studio.nanosense.NanoSenseActivity;
import gl.iglou.studio.nanosense.R;

/**
 * Created by metatomato on 08.12.14.
 */
public class BTGUIFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {

    private boolean D = true;

    private final String SCAN_MSG = "Scan for devices...";

    private String mTextContent = "BT";
    ArrayList<String> mDeviceSelectionList;

    private BTControlCallback mCallback;

    private Button mDeviceListButton;
    private Switch mSwitchAdapter;
    private Switch mSwitchDiscover;
    private TextView mLabelConenctionState;
    private TextView mLabelAddress;
    private Spinner mSpinnerUuids;
    private Spinner mSpinnerRemotes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_bt, container, false);
        TextView text = (TextView)rootView.findViewById(R.id.label_section_bt_adapter);
        text.setText(mTextContent);

        mSwitchAdapter = (Switch) rootView.findViewById(R.id.switch_adapter_io);

        mSwitchDiscover = (Switch) rootView.findViewById(R.id.switch_discover_io);

        mSpinnerRemotes = ((Spinner)rootView.findViewById(R.id.spinner_remote));

        mSpinnerUuids = ((Spinner)rootView.findViewById(R.id.spinner_uuids));

        mLabelAddress = (TextView)rootView.findViewById(R.id.label_current_address);

        mLabelConenctionState = (TextView)rootView.findViewById(R.id.label_current_state);

        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mCallback = ((NanoSenseActivity)getActivity()).getBTController();

        mDeviceListButton = (Button) getView().findViewById(R.id.btn_device_list);
        mDeviceListButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onScanClick();
            }
        });

        setRemoteSpinner();

        mSwitchAdapter.setOnCheckedChangeListener(this);


        mSwitchDiscover.setOnCheckedChangeListener(this);

        mSpinnerRemotes.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selected = (String) parent.getItemAtPosition(pos);
                if(selected.contentEquals(SCAN_MSG)) {
                    mCallback.onScanClick();
                } else {
                    updateRemote();
                }
                if(D) {
                    Toast.makeText(getActivity().getApplicationContext(), selected + " remote selected",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        mSpinnerUuids.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selected = (String) parent.getItemAtPosition(pos);
                if(selected.contentEquals(SCAN_MSG)) {
                    mCallback.onScanClick();
                }
                if(D) {
                    Toast.makeText(getActivity().getApplicationContext(), selected + " remote selected",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }


    private void setRemoteSpinner() {
        updateDeviceList();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, mDeviceSelectionList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerRemotes.setAdapter(adapter);
    }


    private void updateDeviceList() {
        mDeviceSelectionList = new ArrayList<>( Arrays.asList(mCallback.getDeviceList()) );
        if(mDeviceSelectionList.get(0) == "") {
            mDeviceSelectionList.clear();
            mDeviceSelectionList.add("- Select a remote to connect -");
        }
        mDeviceSelectionList.add(SCAN_MSG);
    }


    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        String switchName = "";
        if (isChecked) {
            switch(buttonView.getId()) {
                case R.id.switch_adapter_io :
                    mCallback.setBTActivated(true);
                    break;
                case R.id.switch_discover_io:
                    mCallback.setDiscoverable();
                    break;
            }
            Toast.makeText(getActivity().getApplicationContext(),  switchName + " ON",
                    Toast.LENGTH_SHORT).show();
        } else {
            switch(buttonView.getId()) {
                case R.id.switch_adapter_io:
                    mCallback.setBTActivated(false);
                    break;
                case R.id.switch_discover_io:
                    if(mCallback.isDiscoverable()) {
                        mSwitchDiscover.setChecked(true);
                    }
            }
        }
    }





    public void setDiscoverySwitch(boolean state) {
        mSwitchDiscover.setChecked(state);
    }

    public void setAdapterSwitch(boolean state) {
        mSwitchAdapter.setChecked(state);
    }


    public void updateRemote() {
        mLabelAddress.setText( mCallback.getAddress() );
        setConnectionState(mCallback.getConnectionState());
        setRemoteSpinner();
    }

    public void setConnectionState(int state) {
        String stateLabel = "NONE";
        switch(state) {
            case BTService.STATE_NONE:
                stateLabel = "DISCONNECTED";
                mLabelConenctionState.setTextColor(Color.RED);
                break;
            case BTService.STATE_CONNECTING:
                stateLabel = "CONNECTING";
                mLabelConenctionState.setTextColor(Color.LTGRAY);
                break;
            case BTService.STATE_CONNECTED:
                stateLabel = "CONNECTED";
                mLabelConenctionState.setTextColor(Color.GREEN);
                break;
        }
        mLabelConenctionState.setText(stateLabel);
    }


    public interface BTControlCallback {

        //Adapter Callbacks
        public void onScanClick();

        public String[] getDeviceList();

        public boolean isBTEnabled();

        public void setBTActivated(boolean state) ;

        public boolean isDiscoverable();

        public void setDiscoverable();


        //Remote Callbacks

        public void setCurrentDevice(String deviceName);

        public int getConnectionState();

        public String getName();

        public String getAddress();

        public String[] getUUID();

        public void onUuidSelected(String uuid);
    }
}
