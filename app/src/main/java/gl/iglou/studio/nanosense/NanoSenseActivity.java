package gl.iglou.studio.nanosense;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.TextView;


public class NanoSenseActivity extends ActionBarActivity {

    private static final String TAG = "NanoSenseActivity";

    public static final int MP_FRAGMENT = 0;
    public static final int BT_FRAGMENT = 1;
    public static final int SETUP_FRAGMENT = 2;
    public static final int MONITOR_FRAGMENT = 3;

    String[] mFragmentLabel = {"MediaPlayer", "Bluetooth", "Device Setup", "Monitor"};

    private View mNavigationDrawer;
    private Toolbar mToolbar;
    private View mContentView;
    private DrawerLayout mDrawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nano_sense);


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

        contentViewResolver(MP_FRAGMENT);
    }


    public void contentViewResolver(int fragmentId) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment main_content_fragment;
        switch(fragmentId) {
            case MP_FRAGMENT :
                main_content_fragment = new MPFragment();
                break;
            case BT_FRAGMENT:
                main_content_fragment = new BTFragment();
                break;
            default:
                main_content_fragment = new MPFragment();
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
