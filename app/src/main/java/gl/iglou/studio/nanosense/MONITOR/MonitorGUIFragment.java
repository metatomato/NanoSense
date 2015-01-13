package gl.iglou.studio.nanosense.MONITOR;


import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.xy.*;

import gl.iglou.studio.nanosense.NanoSenseActivity;
import gl.iglou.studio.nanosense.R;
import gl.iglou.studio.nanosense.SETTINGS.SettingsFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class MonitorGUIFragment extends Fragment {

    private static final String TAG = "MonitorFragment";
    private XYPlot mPrimaryPlot;
    private XYPlot mSecondaryPlot;
    private Button mStartStopBtn;
    private MonitorControlCallback mMonitorControlCallback;


    private Handler mPlotScheduler;
    private Runnable mPlotSchedulerTask;
    long mStartTime = 0L;

    public static final int PLOT_SIZE = 500;
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

        mPrimaryPlot = (XYPlot) rootView.findViewById(R.id.primary_plot);
        //mSecondaryPlot = (XYPlot) rootView.findViewById(R.id.secondary_plot);
        mStartStopBtn = (Button) rootView.findViewById(R.id.btn_start_stop);

        mStartStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int state = mMonitorControlCallback.onStartStopClick();
                if(state == SettingsFragment.STATE_BROADCASTING) {
                    ((GradientDrawable)mStartStopBtn.getBackground()).setColor(
                            getResources().getColor(R.color.color_tertiary));
                    mStartStopBtn.setText(getResources().getString(R.string.btn_stop));
                } else {
                    ((GradientDrawable)mStartStopBtn.getBackground()).setColor(
                            getResources().getColor(R.color.color_primary));
                    mStartStopBtn.setText(getResources().getString(R.string.btn_start));
                }
            }
        });

        // Create a formatter to use for drawing a series using LineAndPointRenderer
        // and configure it from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.configure(getActivity().getApplicationContext(),
                R.xml.line_point_formatter_with_plf2);

        // add a new series' to the xyplot:
        mPrimaryPlot.addSeries(mMonitorControlCallback.getData(MonitorFragment.SERIE_PRIMARY), series1Format);
       // mSecondaryPlot.addSeries(mMonitorControlCallback.getData(MonitorFragment.SERIE_SECONDARY), series1Format);

        // reduce the number of range labels
        mPrimaryPlot.setTicksPerRangeLabel(3);
        mPrimaryPlot.getGraphWidget().setDomainLabelOrientation(-45);

        mPrimaryPlot.setRangeBoundaries(1.0, 5.0, BoundaryMode.FIXED);
        //mSecondaryPlot.setRangeBoundaries(-20.0, 20.0, BoundaryMode.FIXED);

        mPrimaryPlot.getBackgroundPaint().setColor(Color.WHITE);
        mPrimaryPlot.getGraphWidget().getGridBackgroundPaint().setColor(getResources().getColor(R.color.color_secondary));
        mPrimaryPlot.getGraphWidget().getBackgroundPaint().setColor(Color.WHITE);

        mPrimaryPlot.setPlotPadding(30, 30, 30, 30);

        mPrimaryPlot.getLayoutManager()
                .remove(mPrimaryPlot.getLegendWidget());

        mPrimaryPlot.getDomainLabelWidget().position(0, XLayoutStyle.RELATIVE_TO_CENTER,
                0, YLayoutStyle.ABSOLUTE_FROM_BOTTOM,
                AnchorPosition.LEFT_BOTTOM);

       // mPrimaryPlot.getDomainLabelWidget().getLabelPaint().setColor(Color.YELLOW);

        mPrimaryPlot.getRangeLabelWidget().position(0, XLayoutStyle.ABSOLUTE_FROM_LEFT,
                0, YLayoutStyle.RELATIVE_TO_CENTER,
                AnchorPosition.LEFT_BOTTOM);

        return rootView;
    }


    private void launchPlotScheduler() {
        mPlotScheduler = new Handler();
        mPlotSchedulerTask = new Runnable() {
            @Override
            public void run() {
                mPrimaryPlot.redraw();
               // mSecondaryPlot.redraw();
                mPlotScheduler.postDelayed(this,100L);
            }
        };
        mPlotScheduler.postDelayed( mPlotSchedulerTask, 0L);
    }


    public void updateExtrema(float yMin, float yMax) {
        mPrimaryPlot.setRangeBoundaries(yMin, yMax, BoundaryMode.AUTO);
    }

    @Override
    public void onStop() {
        super.onStop();

        mMonitorControlCallback.onGUIStop();
        mPlotScheduler.removeCallbacks(mPlotSchedulerTask);
    }

    public interface MonitorControlCallback {
        public XYSeries getData(int serieId);
        public void onGUIStart();
        public void onGUIStop();
        public int onStartStopClick();
    }

}
