package gl.iglou.studio.nanosense.BT;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import gl.iglou.studio.nanosense.NanoSenseActivity;
import gl.iglou.studio.nanosense.R;

/**
 * Created by metatomato on 08.12.14.
 */
public class BTGUIFragment extends Fragment {

    private String mTextContent = "BT";

    private Button mDeviceListButton;

    private BTControlCallback mCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_bt, container, false);
        TextView text = (TextView)rootView.findViewById(R.id.section_label);
        text.setText(mTextContent);
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
    }

    public interface BTControlCallback {

        public void onScanClick();
    }
}
