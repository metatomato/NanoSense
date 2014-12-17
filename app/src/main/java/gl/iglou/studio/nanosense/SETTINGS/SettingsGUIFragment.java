package gl.iglou.studio.nanosense.SETTINGS;

import android.app.Fragment;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import gl.iglou.studio.nanosense.R;

/**
 * Created by metatomato on 17.12.14.
 */
public class SettingsGUIFragment extends Fragment {

    Button mBtnCalibrate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        mBtnCalibrate = (Button) rootView.findViewById(R.id.btn_calibrate);
        ((GradientDrawable)mBtnCalibrate.getBackground()).setColor(
                getResources().getColor(R.color.color_tertiary));


        return rootView;
    }
}
