package gl.iglou.studio.nanosense.BT;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    private final String SELECT_MSG = "- Select a remote to connect -";

    private String mTextContent = "BT";
    ArrayList<String> mDeviceSelectionList;
    ArrayList<String> mUuidSelectionList;

    private BTControlCallback mCallback;

    private Button mButtonConnectRemote;
    private Button mButtonCalibrate;
    private Switch mSwitchAdapter;
    private Switch mSwitchDiscover;
    private TextView mLabelConenctionState;
    private TextView mLabelAddress;
    private Spinner mSpinnerUuids;
    private Spinner mSpinnerRemotes;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

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

        mLabelConenctionState = (TextView)rootView.findViewById(R.id.label_state_value);

        mButtonConnectRemote = (Button) rootView.findViewById(R.id.btn_connect_remote);

        mButtonCalibrate = (Button) rootView.findViewById(R.id.btn_dummy);

        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mCallback = ((NanoSenseActivity)getActivity()).getBTController();


        mButtonConnectRemote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onConnectClick();
            }
        });

        mButtonCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onCalibrateClick();
            }
        });

        setRemoteSpinner();

        mSwitchAdapter.setOnCheckedChangeListener(this);

        mSwitchDiscover.setOnCheckedChangeListener(this);

        mSpinnerRemotes.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selected = (String) parent.getItemAtPosition(pos);
                switch(selected) {
                    case SCAN_MSG:
                        mCallback.onScanClick();
                        break;
                    case SELECT_MSG:
                        break;
                    default:
                        mCallback.setCurrentDevice(pos);
                        updateRemote(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        mSpinnerUuids.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selected = (String) parent.getItemAtPosition(pos);
                mCallback.onUuidSelected(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        if(mDeviceSelectionList.get(0) != SELECT_MSG && mCallback != null)
            setRemoteSpinnerSelection(mCallback.getPosition());
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_bt_frag,menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                Toast.makeText(getActivity().getApplicationContext(),"CLEAR ALL",Toast.LENGTH_SHORT)
                        .show();
                mCallback.clearAllRemotes();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //RemoteSpinner interface
    private void setRemoteSpinner() {
        updateDeviceList();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, mDeviceSelectionList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerRemotes.setAdapter(adapter);
    }


    public void setRemoteSpinnerSelection(int pos) {
        mSpinnerRemotes.setSelection(pos);
    }

    private void updateDeviceList() {
        mDeviceSelectionList = new ArrayList<>( Arrays.asList(mCallback.getDeviceList()) );
        if(mDeviceSelectionList.isEmpty()) {
            mDeviceSelectionList.add(SELECT_MSG);
        }
        mDeviceSelectionList.add(SCAN_MSG);
    }

//UUIDSpinner interface
    private void setUuidSpinner() {
        if( mCallback.getUUID() != null)
            mUuidSelectionList = new ArrayList<>( Arrays.asList(mCallback.getUUID()) );
        else
            mUuidSelectionList = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, mUuidSelectionList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerUuids.setAdapter(adapter);
        mSpinnerUuids.setSelection( mUuidSelectionList.size() - 1 );
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


    public void updateRemote(boolean reloadRemoteList) {
        mLabelAddress.setText( mCallback.getAddress() );
        setConnectionState();
        if(reloadRemoteList)
            setRemoteSpinner();
        setRemoteSpinnerSelection(mCallback.getPosition());
        setUuidSpinner();
    }

    public void setConnectionState() {
        int state = mCallback.getConnectionState();
        String stateLabel = "NONE";
        switch(state) {
            case BTService.STATE_NONE:
                stateLabel = "DISCONNECTED";
                mLabelConenctionState.setTextColor(getResources().getColor(R.color.color_tertiary));
                mButtonConnectRemote.setText(R.string.btn_connect_remote_connect);
                ((GradientDrawable)mButtonConnectRemote.getBackground()).setColor(
                        getResources().getColor(R.color.color_primary));
                break;
            case BTService.STATE_CONNECTING:
                stateLabel = "CONNECTING";
                mLabelConenctionState.setTextColor(getResources().getColor(R.color.color_secondary));
                mLabelConenctionState.setTextColor(Color.LTGRAY);
                break;
            case BTService.STATE_CONNECTED:
                stateLabel = "CONNECTED";
                mLabelConenctionState.setTextColor(getResources().getColor(R.color.color_primary));
                mButtonConnectRemote.setText(R.string.btn_connect_remote_disconnect);
                ((GradientDrawable)mButtonConnectRemote.getBackground()).setColor(
                        getResources().getColor(R.color.color_tertiary));
                break;
        }
        mLabelConenctionState.setText(stateLabel);
    }


    public interface BTControlCallback {

        //Adapter Callbacks
        public void onScanClick();

        public String[] getDeviceList();

        public void setBTActivated(boolean state) ;

        public boolean isDiscoverable();

        public void setDiscoverable();


        //Remote Callbacks
        public void setCurrentDevice(int pos);

        public int getConnectionState();

        public int getPosition();

        public String getName();

        public String getAddress();

        public String[] getUUID();

        public void onUuidSelected(String uuid);

        public void onConnectClick();

        public void onCalibrateClick();


        // Menu Callbacks
        public void clearAllRemotes();

    }
}
