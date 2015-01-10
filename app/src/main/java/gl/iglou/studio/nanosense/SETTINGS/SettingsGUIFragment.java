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

import java.text.DecimalFormat;

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
    Button mBtnEmissionControl;
    TextView mLabelStateValue;
    TextView mLabelResistance;
    TextView mLabelCurrent;
    TextView mLabelReceivingValue;
    SeekBar mSeekBarSwipe;
    Button mBtnSetSwipe;
    Button mBtnResetSwipe;
    TextView mLabelSwipeValue;

    private DecimalFormat mRateFormatter;

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
        mBtnEmissionControl = (Button) rootView.findViewById(R.id.btn_emission_control);
        mLabelStateValue = (TextView) rootView.findViewById(R.id.label_state_value);
        mLabelResistance = (TextView) rootView.findViewById(R.id.label_resistance);
        mLabelCurrent = (TextView) rootView.findViewById(R.id.label_current);
        mLabelReceivingValue = (TextView) rootView.findViewById(R.id.label_receiving_value);
        //Swipe related Views
        mSeekBarSwipe = (SeekBar) rootView.findViewById(R.id.seekBar_swipe);
        mBtnSetSwipe = (Button)  rootView.findViewById(R.id.btn_set_swipe);
        mBtnResetSwipe = (Button) rootView.findViewById(R.id.btn_swipe_reset);
        mLabelSwipeValue = (TextView) rootView.findViewById(R.id.label_swipe_value);


        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRateFormatter = new DecimalFormat("000.0");
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

        mSeekBarSwipe.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            float swipe;
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                swipe = SettingsHelper.convertSwipe(progress);
                mLabelSwipeValue.setText(String.valueOf(swipe));
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSettingsControlCallback.onSetSwipe(swipe);
            }
        });

        mBtnSetCurrent.setOnClickListener(this);
        mBtnSetGain.setOnClickListener(this);
        mBtnSetSwipe.setOnClickListener(this);
        mBtnResetCurrent.setOnClickListener(this);
        mBtnResetGain.setOnClickListener(this);
        mBtnResetSwipe.setOnClickListener(this);
        mBtnEmissionControl.setOnClickListener(this);

        update();
    }

    @Override
    public void onResume() {
        super.onResume();

        mSettingsControlCallback.onGUIStart();
    }

    @Override
    public void onPause() {
        super.onPause();

        mSettingsControlCallback.onGUIPause();
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
            case R.id.btn_set_swipe:
                mSettingsControlCallback.onSetSwipeClick();
                break;
            case R.id.btn_current_reset:
                mSettingsControlCallback.onResetCurrentClick();
                break;
            case R.id.btn_gain_reset:
                mSettingsControlCallback.onResetGainClick();
                break;
            case R.id.btn_swipe_reset:
                mSettingsControlCallback.onResetSwipeClick();
                break;
            case R.id.btn_emission_control:
                mSettingsControlCallback.onEmissionClick();
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

    public void setSwipe(float swipe) {
        mSeekBarSwipe.setProgress(SettingsHelper.revertSwipe(swipe));
        mLabelSwipeValue.setText(String.valueOf(swipe));
    }

    public void updateState(int state) {
        setLabelState(state);
        setEmissionButton(state);
    }

    private void setLabelState(int state) {
        String content = getResources().getString(R.string.state_disconnected);
        if(state == SettingsFragment.STATE_READY) {
            content = getResources().getString(R.string.state_ready);
        } else if(state == SettingsFragment.STATE_BROADCASTING) {
            content = getResources().getString(R.string.state_broadcasting);
        }
        mLabelStateValue.setText(content);
    }

    private void setEmissionButton(int state) {
        if(state == SettingsFragment.STATE_READY || state == SettingsFragment.STATE_DISCONNECTED) {
            ((GradientDrawable)mBtnEmissionControl.getBackground()).setColor(
                    getResources().getColor(R.color.color_primary));
            mBtnEmissionControl.setText(getResources().getString(R.string.btn_start));
        } else if(state == SettingsFragment.STATE_BROADCASTING) {
            ((GradientDrawable)mBtnEmissionControl.getBackground()).setColor(
                    getResources().getColor(R.color.color_tertiary));
            mBtnEmissionControl.setText(getResources().getString(R.string.btn_stop));
        }
    }

    private void setCalibrationValues() {
        String content = getResources().getString(R.string.label_current);
        content += String.valueOf(mSettingsControlCallback.getRemoteMaxCurrent());
        mLabelCurrent.setText(content);
        content = getResources().getString(R.string.label_resistance);
        content += String.valueOf(mSettingsControlCallback.getRemoteResistance());
        mLabelResistance.setText(content);
    }

    public void update() {
        int state = mSettingsControlCallback.getState();
        setLabelState(state);
        setEmissionButton(state);
        setCalibrationValues();
        setCurrent(mSettingsControlCallback.getCurrent());
        setGain(mSettingsControlCallback.getGain());
        setSwipe(mSettingsControlCallback.getSwipe());
        setDataRateValue(mSettingsControlCallback.getDataHighRate(),mSettingsControlCallback.getDataLowRate());
    }

    public void setDataRateValue(float highRate, float lowRate) {
        mLabelReceivingValue.setText(mRateFormatter.format(highRate) + " | " +  mRateFormatter.format(lowRate));
    }

    public interface SettingsControlCallback {
        public void onCalibrateClick();
        public void onSetCurrentClick();
        public void onSetGainClick();
        public void onSetSwipeClick();
        public void onSetCurrent(float current);
        public void onSetGain(float gain);
        public void onSetSwipe(float swipe);
        public void onResetCurrentClick();
        public void onResetGainClick();
        public void onResetSwipeClick();
        public void onEmissionClick();

        public void onGUIPause();
        public void onGUIStart();

        public int getState();
        public boolean isReceiving();
        public float getRemoteResistance();
        public float getRemoteMaxCurrent();
        public float getCurrent();
        public float getGain();
        public float getSwipe();
        public float getDataHighRate();
        public float getDataLowRate();

    }
}
