package com.bob.blauncher;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

/**
 * @author Bob Qi
 *
 */
public class QuickBarFragment
    extends ListFragment
    implements Observer
{
    private static final int MAX_RECENT_TASKS = 30;

    private static final class Entry
        extends Favorite.Entry
    {

        private static final long serialVersionUID = 1750082660409004367L;

        public Drawable icon;

        public Entry( final String packageName, final String activityName )
        {
            super(packageName, activityName);
        }

        public Entry( final Favorite.Entry e )
        {
            super(e.packageName, e.activityName);
        }
    }

    private class Adapter
        extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            return mFavorites.size() + mRecents.size();
        }

        @Override
        public Entry getItem( final int position )
        {
            return position < mFavorites.size() ? mFavorites.get(position) : mRecents.get(position - mFavorites.size());
        }

        @Override
        public long getItemId( final int position )
        {
            return position;
        }

        @Override
        public View getView( final int position, final View convertView, final ViewGroup parent )
        {
            View v;
            if (convertView == null)
            {
                v = getActivity().getLayoutInflater().inflate(R.layout.item_quickbar, parent, false);
            }
            else
            {
                v = convertView;
            }
            ((ImageView) v.findViewById(R.id.icon)).setImageDrawable(getItem(position).icon);
            return v;
        }
    }

    private final List<Entry> mFavorites = new ArrayList<QuickBarFragment.Entry>();

    private final List<Entry> mRecents = new ArrayList<QuickBarFragment.Entry>();

    @Override
    public void onActivityCreated( final Bundle savedInstanceState )
    {
        super.onActivityCreated(savedInstanceState);
        Favorite.getInstance().addObserver(this);
        buildFavorites();
        buildRecent();
        setListAdapter(new Adapter());
    }

    @Override
    public void onResume()
    {
        super.onResume();
        buildRecent();
        if (getListAdapter() != null)
        {
            ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy()
    {
        Favorite.getInstance().deleteObserver(this);
        mFavorites.clear();
        mRecents.clear();
        super.onDestroy();
    }

    @Override
    public void onListItemClick( final ListView l, final View v, final int position, final long id )
    {
        Entry entry = (Entry) getListAdapter().getItem(position);
        if (entry != null)
        {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setClassName(entry.packageName, entry.activityName);
            startActivity(intent);
        }
    }

    @Override
    public void update( final Observable observable, final Object data )
    {
        if (getListAdapter() != null)
        {
            buildFavorites();
            ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
        }
    }

    private void buildFavorites()
    {
        mFavorites.clear();
        List<Favorite.Entry> items = Favorite.getInstance().getItems();
        if (items.size() > 0)
        {
            PackageManager pm = getActivity().getPackageManager();
            Intent intent = new Intent();
            for (Favorite.Entry e : items)
            {
                intent.setClassName(e.packageName, e.activityName);
                List<ResolveInfo> infos = pm.queryIntentActivities(intent, 0);
                if (infos.size() > 0)
                {
                    Entry entry = new Entry(e);
                    entry.icon = infos.get(0).loadIcon(pm);
                    mFavorites.add(entry);
                    mRecents.remove(e);
                }
            }
        }
    }

    private void buildRecent()
    {
        mRecents.clear();
        final PackageManager pm = getActivity().getPackageManager();
        final ActivityManager tasksManager = (ActivityManager) getActivity().getSystemService(Activity.ACTIVITY_SERVICE);

        for (ActivityManager.RecentTaskInfo i : tasksManager.getRecentTasks(MAX_RECENT_TASKS, 0))
        {
            final Intent intent = i.baseIntent;

            if (Intent.ACTION_MAIN.equals(intent.getAction()) && !intent.hasCategory(Intent.CATEGORY_HOME))
            {

                ResolveInfo info = pm.resolveActivity(intent, 0);
                if (info != null)
                {
                    Entry entry = new Entry(info.activityInfo.packageName, info.activityInfo.name);
                    if (mFavorites.contains(entry))
                    {
                        continue;
                    }

                    entry.icon = info.loadIcon(pm);
                    mRecents.add(entry);
                }
            }
        }
    }
}
