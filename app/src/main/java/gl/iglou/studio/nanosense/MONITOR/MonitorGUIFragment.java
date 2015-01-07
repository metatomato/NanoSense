package gl.iglou.studio.nanosense.MONITOR;


import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import gl.iglou.studio.nanosense.NanoSenseActivity;
import gl.iglou.studio.nanosense.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MonitorGUIFragment extends Fragment {

    private static final String TAG = "MonitorFragment";
    private XYPlot mPlot;
    private MonitorControlCallback mMonitorControlCallback;


    private Handler mPlotScheduler;
    private Runnable mPlotSchedulerTask;
    long mStartTime = 0L;

    public static final int PLOT_SIZE = 300;
    public static final long PLOT_INITIAL_STEP = 5L;

    public MonitorGUIFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMonitorControlCallback = ((NanoSenseActivity)getActivity()).getMonitorController();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        launchPlotScheduler();

        mMonitorControlCallback.onGUIStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mStartTime = System.currentTimeMillis();

        //initSerie();

        //launchGenerator();

        View rootView = inflater.inflate(R.layout.fragment_monitor,container,false);

        mPlot = (XYPlot) rootView.findViewById(R.id.mySimpleXYPlot);


        // Create a formatter to use for drawing a series using LineAndPointRenderer
        // and configure it from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.configure(getActivity().getApplicationContext(),
                R.xml.line_point_formatter_with_plf2);

        // add a new series' to the xyplot:
        mPlot.addSeries(mMonitorControlCallback.getData(), series1Format);

        // reduce the number of range labels
        mPlot.setTicksPerRangeLabel(3);
        mPlot.getGraphWidget().setDomainLabelOrientation(-45);

        return rootView;
    }


    private void launchPlotScheduler() {
        mPlotScheduler = new Handler();
        mPlotSchedulerTask = new Runnable() {
            @Override
            public void run() {
                mPlot.redraw();
                mPlotScheduler.postDelayed(this,100L);
            }
        };
        mPlotScheduler.postDelayed( mPlotSchedulerTask, 0L);
    }


    @Override
    public void onStop() {
        super.onStop();

        mMonitorControlCallback.onGUIStop();
        mPlotScheduler.removeCallbacks(mPlotSchedulerTask);
    }

    public interface MonitorControlCallback {
        public XYSeries getData();
        public void onGUIStart();
        public void onGUIStop();
    }

}
