package com.bob.blauncher.query;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Bob on 6/10/13.
 */
public class AllApplicationsSource extends ApplicationsSource {

    public AllApplicationsSource(Context context) {
        super(context);
    }

    @Override
    protected List<Item> buildItems() {
        List<Item> items = new ArrayList<Item>();
        PackageManager manager = context.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));

        for (ResolveInfo info : apps) {
            items.add(buildItem(info, manager));
        }
        return items;
    }

    @Override
    public char getKey() {
        return 'a';
    }

    @Override
    public String getName() {
        return "Applications";
    }
}
