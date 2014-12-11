package gl.iglou.studio.nanosense.BT;

import android.app.Fragment;
import android.graphics.Outline;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
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
import java.util.List;

import gl.iglou.studio.nanosense.NanoSenseActivity;
import gl.iglou.studio.nanosense.R;

/**
 * Created by metatomato on 08.12.14.
 */
public class BTGUIFragment extends Fragment implements AdapterView.OnItemSelectedListener,
        CompoundButton.OnCheckedChangeListener {

    private boolean D = true;

    private final String SCAN_MSG = "Scan for devices...";

    private String mTextContent = "BT";

    private Button mDeviceListButton;

    ArrayList<String> mDeviceSlectionList;

    private BTControlCallback mCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_bt, container, false);
        TextView text = (TextView)rootView.findViewById(R.id.label_section_bt_adapter);
        text.setText(mTextContent);

        ((Spinner)rootView.findViewById(R.id.spinner_remote)).setOnItemSelectedListener(this);
/*
        View connectButton = rootView.findViewById(R.id.btn_device_list);

        ViewOutlineProvider viewOutlineProvider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int size = getResources().getDimensionPixelSize(R.dimen.diameter);
                outline.setOval(0, 0, size, size);
            }
        };
        connectButton.setOutlineProvider(viewOutlineProvider);
        //connectButton.setClipToOutline(true);
*/
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

        setSpinner( getView().findViewById(R.id.spinner_remote) );

        Switch toggle = (Switch) getView().findViewById(R.id.switch_adapter_io);
        toggle.setOnCheckedChangeListener(this);
        toggle = (Switch) getView().findViewById(R.id.switch_discover_io);
        toggle.setOnCheckedChangeListener(this);
    }


    private void setSpinner(View v) {
        Spinner spinner = (Spinner) v;
        updateDeviceList();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, mDeviceSlectionList );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }


    private void updateDeviceList() {
        mDeviceSlectionList = new ArrayList<>( Arrays.asList(mCallback.getDeviceList()) );
        if(mDeviceSlectionList.get(0) == "") {
            mDeviceSlectionList.clear();
            mDeviceSlectionList.add("- Select a remote to connect -");
        }
        mDeviceSlectionList.add(SCAN_MSG);
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        String selected = (String) parent.getItemAtPosition(pos);
        if(selected.contentEquals(SCAN_MSG)) {
            mCallback.onScanClick();
        }
        if(D)
        Toast.makeText(getActivity().getApplicationContext(),  selected + " remote selected",
                Toast.LENGTH_SHORT).show();
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
            }
        }
    }


    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }


    public interface BTControlCallback {

        public void onScanClick();

        public String[] getDeviceList();

        public boolean isBTEnabled();

        public void setBTActivated(boolean state) ;

        public boolean isDiscoverable();

        public void setDiscoverable();

    }
}
