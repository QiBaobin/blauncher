package com.bob.blauncher.query;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob on 6/10/13.
 */
public abstract class ApplicationsSource extends BaseSource {
    private static final String SCHEME = "package";
    private List<Action> actions;

    public ApplicationsSource(Context context) {
        super(context);
    }

    @Override
    public List<Action> getActions() {
        if (actions == null) {
            actions = new ArrayList<Action>();
            actions.add(new OpenAction());
            actions.add(new UninstallAction());
            actions.add(new InfoAction());
            actions.addAll(super.getActions());
        }
        return actions;
    }

    protected abstract List<Item> buildItems();

    protected Item buildItem(ResolveInfo info, PackageManager manager) {
        Item item = new Item();
        item.source = this;
        item.title = info.loadLabel(manager);
        item.data = info.activityInfo.packageName + "/" + info.activityInfo.name;

        return item;
    }

    protected String getPackageName(Item item) {
        return item.data.split("/")[0];
    }

    protected String getActivityName(Item item) {
        return item.data
                .split("/")[1];
    }

    public class OpenAction implements Action {

        @Override
        public char getKey() {
            return 'o';
        }

        @Override
        public CharSequence getName() {
            return "Launch";
        }

        @Override
        public void runWith(Item item) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setClassName(getPackageName(item), getActivityName(item));
            context.startActivity(intent);
        }
    }

    public class UninstallAction implements Action {

        @Override
        public char getKey() {
            return 'u';
        }

        @Override
        public CharSequence getName() {
            return "Uninstall";
        }

        @Override
        public void runWith(Item item) {
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.fromParts(SCHEME, getPackageName(item), null));
            context.startActivity(intent);
        }
    }

    public class InfoAction implements Action {
        private static final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";
        private static final String APP_PKG_NAME_22 = "pkg";
        private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
        private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";

        @Override
        public char getKey() {
            return 'i';
        }

        @Override
        public CharSequence getName() {
            return "Info";
        }

        @Override
        public void runWith(Item item) {
            String pkgName = getPackageName(item);
            Intent intent = new Intent();
            final int apiLevel = Build.VERSION.SDK_INT;
            if (apiLevel >= 9) { // above 2.3
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts(SCHEME, pkgName, null));
            } else { // below 2.3
                final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22
                        : APP_PKG_NAME_21);
                intent.setAction(Intent.ACTION_VIEW);
                intent.setClassName(APP_DETAILS_PACKAGE_NAME,
                        APP_DETAILS_CLASS_NAME);
                intent.putExtra(appPkgName, pkgName);
            }
            context.startActivity(intent);
        }
    }
}
