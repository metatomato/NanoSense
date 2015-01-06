package gl.iglou.studio.nanosense.MONITOR;


import android.os.Bundle;
import android.app.Fragment;
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

import gl.iglou.studio.nanosense.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MonitorGUIFragment extends Fragment {

    private static final String TAG = "MonitorFragment";
    private XYPlot mPlot;
    private SimpleXYSeries mSerie;

    int mPlotCount = 0;
    long mStartTime = 0L;
    Timer mGenerator;
    Timer mUpdateTimer;

    private final int PLOT_SIZE = 300;
    private final long PLOT_INITIAL_STEP = 5L;

    private final double AMP_MAX = 4.0;



    public MonitorGUIFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mStartTime = System.currentTimeMillis();

        initSerie();

        launchGenerator();

        View rootView = inflater.inflate(R.layout.fragment_monitor,container,false);

        mPlot = (XYPlot) rootView.findViewById(R.id.mySimpleXYPlot);


        // Create a formatter to use for drawing a series using LineAndPointRenderer
        // and configure it from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.configure(getActivity().getApplicationContext(),
                R.xml.line_point_formatter_with_plf2);

        // add a new series' to the xyplot:
        mPlot.addSeries(mSerie, series1Format);

        // reduce the number of range labels
        mPlot.setTicksPerRangeLabel(3);
        mPlot.getGraphWidget().setDomainLabelOrientation(-45);


        mUpdateTimer = new Timer();
        mUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mPlot.redraw();
            }
        }, new Date(mStartTime + 1000), 100);


        return rootView;
    }



    private void initSerie() {
        ArrayList<Number> listY = new ArrayList<>();
        ArrayList<Number> listX = new ArrayList<>();
        for(int i = 0 ; i < PLOT_SIZE; i++) {
            listX.add(i * PLOT_INITIAL_STEP );
            listY.add(0.0);
        }
        mSerie = new SimpleXYSeries(listX,listY,"data");
    }

    private void launchGenerator() {
        Log.d(TAG, String.valueOf(mStartTime));

        mGenerator = new Timer();
        mGenerator.schedule(new TimerTask() {
            @Override
            public void run() {
                Number dataX = generateX();
                Number dataY = generateY();
                updateSerie(dataX,dataY);
               // Log.d(TAG, String.valueOf(dataX) + "    " + String.valueOf(dataY));
            }
        }, new Date(mStartTime + 1000), PLOT_INITIAL_STEP);
    }


    private Number generateX() {
        return System.currentTimeMillis() - mStartTime - 1000;
    }

    private Number generateY() {
        Random mYGenerator = new Random();
        return (mYGenerator.nextDouble() * 2.0 - 1.0) * AMP_MAX;
    }

    private void updateSerie(Number X, Number Y) {
        if(mPlotCount < PLOT_SIZE) {
            mSerie.setXY(X,Y,mPlotCount);
        } else {
            mSerie.removeFirst();
            mSerie.addLast(X,Y);
        }
        mPlotCount++;
    }

    @Override
    public void onStop() {
        super.onStop();

        mUpdateTimer.cancel();
        mGenerator.cancel();
    }

}
