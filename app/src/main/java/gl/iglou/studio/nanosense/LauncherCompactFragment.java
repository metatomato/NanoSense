package gl.iglou.studio.nanosense;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by metatomato on 04.12.14.
 */
public class LauncherCompactFragment extends Fragment {

    private View mLauncherView;

    public LauncherCompactFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mLauncherView = inflater.inflate(R.layout.fragment_launcher, container, false);

        return mLauncherView;
    }
}
