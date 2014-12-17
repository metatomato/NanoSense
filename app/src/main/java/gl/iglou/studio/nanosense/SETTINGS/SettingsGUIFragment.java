package gl.iglou.studio.nanosense.SETTINGS;

import android.app.Fragment;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import gl.iglou.studio.nanosense.NanoSenseActivity;
import gl.iglou.studio.nanosense.R;

/**
 * Created by metatomato on 17.12.14.
 */
public class SettingsGUIFragment extends Fragment {

    SettingsControlCallback mSettingsControlCallback;

    Button mBtnCalibrate;
    SeekBar mSeekBarCurrent;
    TextView mLabelCurrentValue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        mBtnCalibrate = (Button) rootView.findViewById(R.id.btn_calibrate);
        ((GradientDrawable)mBtnCalibrate.getBackground()).setColor(
                getResources().getColor(R.color.color_tertiary));


        mSeekBarCurrent = (SeekBar) rootView.findViewById(R.id.seekBar_current);
        mLabelCurrentValue = (TextView) rootView.findViewById(R.id.label_current_value);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSettingsControlCallback = ((NanoSenseActivity)getActivity()).getSettingsController();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mBtnCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSettingsControlCallback.onCalibrateClick();
            }
        });

        mSeekBarCurrent.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                mLabelCurrentValue.setText(String.valueOf(progress));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public interface SettingsControlCallback {
        public void onCalibrateClick();
    }
}
