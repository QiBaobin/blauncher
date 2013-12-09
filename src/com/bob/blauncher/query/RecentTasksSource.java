package com.bob.blauncher.query;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob on 6/10/13.
 */
public class RecentTasksSource extends ApplicationsSource {
    private static final int MAX_RECENT_TASKS = 30;

    public RecentTasksSource(Context context) {
        super(context);
    }

    @Override
    protected List<Item> buildItems() {
        final PackageManager manager = context.getPackageManager();
        final ActivityManager tasksManager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        final ArrayList<Item> recents = new ArrayList<Item>();

        for (ActivityManager.RecentTaskInfo i : tasksManager.getRecentTasks(MAX_RECENT_TASKS, 0)) {
            final Intent intent = i.baseIntent;

            if (Intent.ACTION_MAIN.equals(intent.getAction()) &&
                    !intent.hasCategory(Intent.CATEGORY_HOME)) {

                ResolveInfo info = manager.resolveActivity(intent, 0);
                if (info != null)
                    recents.add(buildItem(info, manager));
            }
        }
        return recents;
    }

    @Override
    public char getKey() {
        return 'r';
    }

    @Override
    public String getName() {
        return "Recent tasks";
    }
}
