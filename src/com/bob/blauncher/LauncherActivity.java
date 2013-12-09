package com.bob.blauncher;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import com.bob.blauncher.query.ApplicationsSource;
import com.bob.blauncher.query.Source;
import com.bob.blauncher.query.Utils;


/*
 * running application, recent application, contacts, settings
 */
public class LauncherActivity extends FragmentActivity implements ViewPager.OnPageChangeListener {
    private static final String KEY_CURRENT_PAGE = "current_page";
    private static final int DEFAULT_CURRENT_PAGE = 1;
    private final BroadcastReceiver mApplicationsReceiver = new ApplicationsIntentReceiver();
    private final Utils mUtils = new Utils(this);
    private String[] mFragSources;
    private ViewPager mContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_launcher);
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

        reset(savedInstanceState);
        registerIntentReceivers();
    }

    private void reset(Bundle savedInstanceState) {
        mFragSources = mUtils.getDefaultSources().split(Constants.SOURCES_DELIMETER);
        initViews(savedInstanceState);
    }

    private void initViews(Bundle savedInstanceState) {
        mContainer = (ViewPager) findViewById(R.id.container);
        mContainer.setOnPageChangeListener(this);
        mContainer.setAdapter(new SourcesFragmentAdapter(getSupportFragmentManager()));
        mContainer.setOffscreenPageLimit(mFragSources.length);

        int index = -1;
        if (savedInstanceState != null)
            index = savedInstanceState.getInt(KEY_CURRENT_PAGE, DEFAULT_CURRENT_PAGE);
        if (index < 0 || index >= mFragSources.length - 1)
            index = (mFragSources.length - 1) / 2;
        mContainer.setCurrentItem(index, false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_CURRENT_PAGE, mContainer.getCurrentItem());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Close the menu
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            getWindow().closeAllPanels();
        } else if (intent.getComponent() != null) {
            reset(null);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        //clear all items of sources which is not in current sources
        for (Source s : mUtils.getAllSources()) {
            s.reload();
        }
    }

    @Override
    protected void onPause() {
        InputMethodManager im = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mApplicationsReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.launcher, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_wallpaper:
                startWallpaper();
                break;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
        return true;
    }

    @Override
    public boolean onSearchRequested() {
        SourcesFragment current = getCurrentFragment();
        if (current != null)
            current.startSearch();
        return true;
    }

    @Override
    public void onBackPressed() {
        //don't finish on back pressed
        SourcesFragment current = getCurrentFragment();
        if (current != null)
            current.reset();
    }

    private SourcesFragment getCurrentFragment() {
        return ((SourcesFragmentAdapter) mContainer.getAdapter()).getCurrent();
    }

    private void invalidateFragments() {
        SourcesFragmentAdapter adapter = (SourcesFragmentAdapter) mContainer.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            SourcesFragment fragment = adapter.getExistItem(i);
            if (fragment != null) {
                fragment.invalidate();
            }
        }
    }

    /**
     * Registers various intent receivers. The current implementation registers
     * only a wallpaper intent receiver to let other applications change the
     * wallpaper.
     */
    private void registerIntentReceivers() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(mApplicationsReceiver, filter);
    }

    private void startWallpaper() {
        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
        startActivity(Intent.createChooser(pickWallpaper, "Choose wallpaper"));
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
    }

    @Override
    public void onPageSelected(int i) {
        WallpaperManager.getInstance(this).setWallpaperOffsets(mContainer.getWindowToken(), (float) i / (mFragSources.length - 1), 0);
    }


    @Override
    public void onPageScrollStateChanged(int i) {

    }

    private class SourcesFragmentAdapter extends FragmentPagerAdapter {
        private final SourcesFragment[] fragments = new SourcesFragment[mFragSources.length];
        private SourcesFragment current;

        public SourcesFragmentAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public SourcesFragment getItem(int i) {
            return new SourcesFragment();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            SourcesFragment frag = (SourcesFragment) super.instantiateItem(container, position);
            frag.setSelectedSources(mFragSources[position]);
            fragments[position] = frag;
            return frag;
        }

        @Override
        public int getCount() {
            return mFragSources.length;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            current = (SourcesFragment) object;
        }

        public SourcesFragment getExistItem(int i) {
            if (i < 0 || i >= fragments.length)
                return null;
            return fragments[i];
        }

        public SourcesFragment getCurrent() {
            return current;
        }
    }

    /**
     * Receives notifications when applications are added/removed.
     */
    private class ApplicationsIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            for (Source s : mUtils.getAllSources()) {
                if (s instanceof ApplicationsSource)
                    s.reload();
            }
            invalidateFragments();
        }
    }
}
