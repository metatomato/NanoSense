package gl.iglou.studio.nanosense;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarActivity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v4.widget.DrawerLayout;
import android.view.WindowManager;

import java.util.logging.LogRecord;

import gl.iglou.studio.nanosense.BT.BTFragment;
import gl.iglou.studio.nanosense.BT.BTGUIFragment;
import gl.iglou.studio.nanosense.MONITOR.MonitorFragment;
import gl.iglou.studio.nanosense.MONITOR.MonitorGUIFragment;
import gl.iglou.studio.nanosense.MP.MPFragment;
import gl.iglou.studio.nanosense.MP.MPGUIFragment;
import gl.iglou.studio.nanosense.SETTINGS.SettingsFragment;
import gl.iglou.studio.nanosense.SETTINGS.SettingsGUIFragment;


public class NanoSenseActivity extends ActionBarActivity {

    private static final String TAG = "NanoSenseActivity";

    public static final int MP_FRAGMENT = 0;
    public static final int BT_FRAGMENT = 1;
    public static final int SETTINGS_FRAGMENT = 2;
    public static final int MONITOR_FRAGMENT = 3;

    String[] mFragmentLabel;

    private View mNavigationDrawer;
    private Toolbar mToolbar;
    private View mContentView;
    private DrawerLayout mDrawerLayout;

    private MPFragment mMPFragment;
    private BTFragment mBTFragment;
    private SettingsFragment mSettingsFragment;
    private MonitorFragment mMonitorFragment;

    private MPGUIFragment mMPGUIFragment;
    private BTGUIFragment mBTGUIFragment;
    private SettingsGUIFragment mSettingsGUIFragment;
    private MonitorGUIFragment mMonitorGUIFragment;

    private DisplayMetrics mDisplayMetrics;
    private float mDpHeight;
    private float mDpWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nano_sense);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mFragmentLabel = new String[4];
        mFragmentLabel[0] = getResources().getString(R.string.fragment_label_mp);
        mFragmentLabel[1] = getResources().getString(R.string.fragment_label_bt);
        mFragmentLabel[2] = getResources().getString(R.string.fragment_label_settings);
        mFragmentLabel[3] = getResources().getString(R.string.fragment_label_monitor);


        //Layout Views Setup
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mContentView = findViewById(R.id.main_content);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationDrawer = findViewById(R.id.navigation_drawer);

        mDrawerLayout.setScrimColor(Color.TRANSPARENT);

        mDisplayMetrics = this.getResources().getDisplayMetrics();

        mDpHeight = mDisplayMetrics.heightPixels / mDisplayMetrics.density;
        mDpWidth = mDisplayMetrics.widthPixels / mDisplayMetrics.density;

        if (mToolbar  != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        )
        {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                return;
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                return;
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset)
            {
                super.onDrawerSlide(drawerView,slideOffset);
                float moveFactor = (mNavigationDrawer.getWidth() * slideOffset);

                mContentView.setTranslationX(moveFactor);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();



        //Start BTFragment
        FragmentManager fm = getFragmentManager();
        if(mBTFragment == null) {
            mBTFragment = new BTFragment();
            fm.beginTransaction().add(mBTFragment, "BTFrag").commit();
        }
        //Start SettingsFragment
        if(mSettingsFragment == null) {
            mSettingsFragment = new SettingsFragment();
            fm.beginTransaction().add(mSettingsFragment, "SettingsFrag").commit();
        }
        //Start MPFragment
        if(mMPFragment == null) {
            mMPFragment = new MPFragment();
            fm.beginTransaction().add(mMPFragment, "MPFrag").commit();
        }
        //Start MonitorFragment
        if(mMonitorFragment == null) {
            mMonitorFragment = new MonitorFragment();
            fm.beginTransaction().add(mMonitorFragment, "MonitorFrag").commit();
        }
        contentViewResolver(MP_FRAGMENT);
    }


    @Override
    protected void onStart() {
        super.onStart();

        if(mDpWidth >= 600.0) {
            //Disable the drawer shadow (shadow already attached to contentView)
            mNavigationDrawer.setBackgroundResource(0);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                mContentView.setBackgroundDrawable(getResources().getDrawable(R.drawable.drawer_shadow_reverse_left));
                mContentView.setPadding(Math.round(5 * mDisplayMetrics.density),0,0,0);
            }
        }
    }

    public void contentViewResolver(int fragmentId) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment main_content_fragment;
        switch(fragmentId) {
            default:
            case MP_FRAGMENT :
                if(mMPGUIFragment == null) {
                    mMPGUIFragment = new MPGUIFragment();
                    mMPFragment.setMPGUIFrag(mMPGUIFragment);
                }
                main_content_fragment = mMPGUIFragment;
                break;
            case BT_FRAGMENT:
                if(mBTGUIFragment == null) {
                    mBTGUIFragment = new BTGUIFragment();
                    mBTFragment.setBTGUIFrag(mBTGUIFragment);
                }
                main_content_fragment = mBTGUIFragment;
                break;
            case SETTINGS_FRAGMENT:
                if(mSettingsGUIFragment == null) {
                    mSettingsGUIFragment = new SettingsGUIFragment();
                    mSettingsFragment.setSettingsGUIFrag(mSettingsGUIFragment);
                }
                main_content_fragment = mSettingsGUIFragment;
                break;
            case MONITOR_FRAGMENT:
                if(mMonitorGUIFragment == null) {
                    mMonitorGUIFragment = new MonitorGUIFragment();
                    mMonitorFragment.setMonitorGUIFrag(mMonitorGUIFragment);
                }
                main_content_fragment = mMonitorGUIFragment;
                break;
        }

        fragmentManager.beginTransaction()
                .replace(R.id.main_content, main_content_fragment)
                .commit();

        getSupportActionBar().setTitle(mFragmentLabel[fragmentId]);

        closeDrawer();
    }


    private void closeDrawer() {
        Handler scheduler = new Handler();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                mDrawerLayout.closeDrawers();
            }
        };
        scheduler.postDelayed( task, 150L);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.global, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    public BTFragment getBTController() {
        return mBTFragment;
    }

    public SettingsFragment getSettingsController() {
        return mSettingsFragment;
    }

    public MPFragment getMPController() { return mMPFragment; }

    public MonitorFragment getMonitorController() { return mMonitorFragment; }
}
