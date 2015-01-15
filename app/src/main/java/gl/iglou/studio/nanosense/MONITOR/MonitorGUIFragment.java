package gl.iglou.studio.nanosense.MONITOR;


import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

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
    private int mCurrentSerie = MonitorFragment.SERIE_PRIMARY;
    private LineAndPointFormatter mSeries1Format;
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

        setHasOptionsMenu(true);
        mMonitorControlCallback = ((NanoSenseActivity)getActivity()).getMonitorController();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


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
            mMonitorControlCallback.onStartStopClick();
            updateButtonState();
            }
        });

        // Create a formatter to use for drawing a series using LineAndPointRenderer
        // and configure it from xml:
        mSeries1Format = new LineAndPointFormatter();
        mSeries1Format.setPointLabelFormatter(new PointLabelFormatter());
        mSeries1Format.configure(getActivity().getApplicationContext(),
                R.xml.line_point_formatter_with_plf2);

        // add a new series' to the xyplot:
        mPrimaryPlot.addSeries(mMonitorControlCallback.getData(MonitorFragment.SERIE_PRIMARY), mSeries1Format);
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

    @Override
    public void onStop() {
        super.onStop();

        mMonitorControlCallback.onGUIStop();
        mPlotScheduler.removeCallbacks(mPlotSchedulerTask);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateButtonState();
        launchPlotScheduler();
        mMonitorControlCallback.onGUIStart();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_monitor_frag,menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switch_plot:
                Toast.makeText(getActivity().getApplicationContext(), "SWITCH PLOT", Toast.LENGTH_SHORT)
                        .show();
                switchSerie();
                return true;
            case R.id.inverse_data:
                mMonitorControlCallback.onInverseDataClick();
                Toast.makeText(getActivity().getApplicationContext(), "DATA REVERSED", Toast.LENGTH_SHORT)
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    private void updateButtonState() {
        int state = mMonitorControlCallback.getEmissionState();

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

    public void updateExtrema(float yMin, float yMax) {
        mPrimaryPlot.setRangeBoundaries(yMin, yMax, BoundaryMode.AUTO);
    }


    public void switchSerie() {
        if(mCurrentSerie == MonitorFragment.SERIE_PRIMARY) {
            setSerie(MonitorFragment.SERIE_SECONDARY);
            mPrimaryPlot.setRangeBoundaries(-10.0, 10.0, BoundaryMode.AUTO);
            mPrimaryPlot.getRangeLabelWidget().setText("Derivative Voltage (V/s)");
        } else {
            setSerie(MonitorFragment.SERIE_PRIMARY);
            mPrimaryPlot.setRangeBoundaries(1.0, 5.0, BoundaryMode.FIXED);
            mPrimaryPlot.getRangeLabelWidget().setText("Voltage");
        }
    }

    public void setSerie(int serieId) {
        mPrimaryPlot.removeSeries(mMonitorControlCallback.getData(mCurrentSerie));
        mPrimaryPlot.addSeries(mMonitorControlCallback.getData(serieId), mSeries1Format);
        mCurrentSerie = serieId;
    }



    public interface MonitorControlCallback {
        public XYSeries getData(int serieId);
        public void onGUIStart();
        public void onGUIStop();
        public void onStartStopClick();
        public void onInverseDataClick();
        public int getEmissionState();
    }

}
