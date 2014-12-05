package gl.iglou.studio.nanosense;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by metatomato on 04.12.14.
 */
public class LauncherCompactFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "LauncherCompactFragment";

    private ArrayList<View> mRockets;

    private View mActiveRocket;

    private static final float ROCKET_ELEVATION = 4.0f;
    private static final int ROCKET_TINT = R.color.black_alpha;
    private static final int ROCKET_BACKGROUND = R.color.black_alpha_light;

    private LauncherCompactCallbacks mCallbacks;

    public LauncherCompactFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_launcher, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRockets = getRockets();

        if(!mRockets.isEmpty()) {
            mActiveRocket = mRockets.get(0);
        }

        for(View rocket : mRockets) {
            rocket.setOnClickListener(this);
        }

        updateRocket(mActiveRocket);
    }

    @Override
    public void onClick(View v) {
        updateRocket(v);
    }

    private ArrayList<View> getRockets() {
        ArrayList<View> result = new ArrayList<View>();
        ViewGroup viewGroup = (ViewGroup) getView();

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            result.add(child);
        }
        return result;
    }

    void updateRocket(View v) {
        if(mRockets.contains(v)) {
            mActiveRocket = v;

            setRocketActive(v);

            for(View rocket : mRockets) {
                if(rocket != mActiveRocket) {
                    setRocketInactive(rocket);
                }
            }
        }
    }

    void setRocketActive(View v) {
        ImageView rocket = (ImageView) v;

        final float scale = getResources().getDisplayMetrics().density;
        int elevation = (int) (ROCKET_ELEVATION * scale + 0.5f);

        //rocket.setElevation(elevation);
        rocket.getDrawable().setTint(Color.BLACK);
        rocket.setBackground(getResources().getDrawable(R.drawable.round_rect));
    }


    void setRocketInactive(View v) {
        ImageView rocket = (ImageView) v;
        //rocket.setElevation(0);
        rocket.getDrawable().setTint(getResources().getColor(ROCKET_TINT));
        rocket.setBackgroundColor(getResources().getColor(R.color.white_solid));
    }

    public static interface LauncherCompactCallbacks {
        void onRocketLaunch(int rocketId);
    }
}
