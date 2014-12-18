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
public class SettingsGUIFragment extends Fragment implements View.OnClickListener {

    SettingsControlCallback mSettingsControlCallback;

    Button mBtnCalibrate;
    SeekBar mSeekBarCurrent;
    TextView mLabelCurrentValue;
    SeekBar mSeekBarGain;
    TextView mLabelGainValue;
    Button mBtnSetCurrent;
    Button mBtnResetCurrent;
    Button mBtnSetGain;
    Button mBtnResetGain;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        mBtnCalibrate = (Button) rootView.findViewById(R.id.btn_calibrate);
        ((GradientDrawable)mBtnCalibrate.getBackground()).setColor(
                getResources().getColor(R.color.color_tertiary));


        mSeekBarCurrent = (SeekBar) rootView.findViewById(R.id.seekBar_current);
        mLabelCurrentValue = (TextView) rootView.findViewById(R.id.label_current_value);
        mSeekBarGain = (SeekBar) rootView.findViewById(R.id.seekBar_gain);
        mLabelGainValue = (TextView) rootView.findViewById(R.id.label_gain_value);
        mBtnSetCurrent = (Button) rootView.findViewById(R.id.btn_set_current);
        mBtnResetCurrent = (Button) rootView.findViewById(R.id.btn_current_reset);
        mBtnSetGain = (Button) rootView.findViewById(R.id.btn_set_gain);
        mBtnResetGain = (Button) rootView.findViewById(R.id.btn_gain_reset);

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

       setCurrent(SettingsFragment.CURRENT_INITIAL_VALUE);
        mSeekBarCurrent.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            float current;
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                current = SettingsHelper.convertCurrent(progress);
                mLabelCurrentValue.setText(String.valueOf(current));
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSettingsControlCallback.onSetCurrent(current);
            }
        });

        setGain(SettingsFragment.GAIN_INITIAL_VALUE);
        mSeekBarGain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            float gain;
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                gain = SettingsHelper.convertGain(progress);
                mLabelGainValue.setText(String.valueOf(gain));
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSettingsControlCallback.onSetGain(gain);
            }
        });

        mBtnSetCurrent.setOnClickListener(this);
        mBtnSetGain.setOnClickListener(this);
        mBtnResetCurrent.setOnClickListener(this);
        mBtnResetGain.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_set_current:
                mSettingsControlCallback.onSetCurrentClick();
                break;
            case R.id.btn_set_gain:
                mSettingsControlCallback.onSetGainClick();
                break;
            case R.id.btn_current_reset:
                mSettingsControlCallback.onResetCurrentClick();
                break;
            case R.id.btn_gain_reset:
                mSettingsControlCallback.onResetGainClick();
                break;
        }

    }

    public void setCurrent(float current) {
        mSeekBarCurrent.setProgress(SettingsHelper.revertCurrent(current));
        mLabelCurrentValue.setText(String.valueOf(current));
    }

    public void setGain(float gain) {
        mSeekBarGain.setProgress(SettingsHelper.revertGain(gain));
        mLabelGainValue.setText(String.valueOf(gain));
    }

    public interface SettingsControlCallback {
        public void onCalibrateClick();
        public void onSetCurrentClick();
        public void onSetGainClick();
        public void onSetCurrent(float current);
        public void onSetGain(float gain);
        public void onResetCurrentClick();
        public void onResetGainClick();
    }
}
