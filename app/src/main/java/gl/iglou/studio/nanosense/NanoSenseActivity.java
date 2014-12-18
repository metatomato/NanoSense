package gl.iglou.studio.nanosense;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;

import gl.iglou.studio.nanosense.BT.BTFragment;
import gl.iglou.studio.nanosense.BT.BTGUIFragment;
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

    private BTFragment mBTFragment;
    private SettingsFragment mSettingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nano_sense);

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

        if (mToolbar  != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

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

        contentViewResolver(MP_FRAGMENT);
    }


    public void contentViewResolver(int fragmentId) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment main_content_fragment;
        switch(fragmentId) {
            case MP_FRAGMENT :
                main_content_fragment = new MPFragment();
                mBTFragment.setBTGUIFrag(null);
                mSettingsFragment.setSettingsGUIFrag(null);
                break;
            case BT_FRAGMENT:
                main_content_fragment = new BTGUIFragment();
                mBTFragment.setBTGUIFrag((BTGUIFragment)main_content_fragment);
                mSettingsFragment.setSettingsGUIFrag(null);
                break;
            case SETTINGS_FRAGMENT:
                main_content_fragment = new SettingsGUIFragment();
                mSettingsFragment.setSettingsGUIFrag((SettingsGUIFragment)main_content_fragment);
                mBTFragment.setBTGUIFrag(null);
                break;
            default:
                main_content_fragment = new MPFragment();
                mBTFragment.setBTGUIFrag(null);
                mSettingsFragment.setSettingsGUIFrag(null);
        }

        fragmentManager.beginTransaction()
                .replace(R.id.main_content, main_content_fragment)
                .commit();

        getSupportActionBar().setTitle(mFragmentLabel[fragmentId]);
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
}
