package gl.iglou.studio.nanosense.MONITOR;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import gl.iglou.studio.nanosense.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MonitorFragment extends Fragment {

    private MonitorGUIFragment mMonitorGUIFragment;

    public MonitorFragment() {
        // Required empty public constructor
    }


   public void setMonitorGUIFrag(MonitorGUIFragment fragment) {
       mMonitorGUIFragment = fragment;
   }


}
