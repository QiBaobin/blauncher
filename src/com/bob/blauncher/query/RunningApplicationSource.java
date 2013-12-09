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
public class RunningApplicationSource extends ApplicationsSource {
    private List<Action> mActions;

    public RunningApplicationSource(Context context) {
        super(context);
    }

    @Override
    protected List<Item> buildItems() {
        final PackageManager manager = context.getPackageManager();
        final ActivityManager tasksManager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        final ArrayList<Item> running = new ArrayList<Item>();

        for (ActivityManager.RunningAppProcessInfo i : tasksManager.getRunningAppProcesses()) {
            if (i.processName.equals("system") || i.processName.equals("com.android.phone")) {
                continue;
            }

            if (i.pkgList != null)
                for (String pkg : i.pkgList) {
                    final Intent intent = manager.getLaunchIntentForPackage(pkg);

                    if (intent != null && Intent.ACTION_MAIN.equals(intent.getAction())) {
                        ResolveInfo info = manager.resolveActivity(intent, 0);
                        if (info != null) {
                            Item item = buildItem(info, manager);
                            if (!running.contains(item))
                                running.add(item);
                        }
                    }
                }
        }
        return running;
    }

    @Override
    public List<Action> getActions() {
        if (mActions == null) {
            mActions = new ArrayList<Action>(super.getActions());
            mActions.add(new KillAction());
        }
        return mActions;
    }

    @Override
    public char getKey() {
        return 'R';
    }

    @Override
    public String getName() {
        return "Running applications";
    }

    private class KillAction implements Action {

        @Override
        public char getKey() {
            return 'k';
        }

        @Override
        public CharSequence getName() {
            return "Kill";
        }

        @Override
        public void runWith(Item item) {
            final ActivityManager tasksManager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
            tasksManager.killBackgroundProcesses(getPackageName(item));
            item.source.reload();
        }
    }
}
