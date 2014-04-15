package com.bob.blauncher.query;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;

import com.bob.blauncher.Favorite;

/**
 * Created by Bob on 6/10/13.
 */
public abstract class ApplicationsSource extends BaseSource {
    private static final String SCHEME = "package";
    private List<Action> actions;

    public ApplicationsSource(final Context context) {
        super(context);
    }

    @Override
    public List<Action> getActions() {
        if (actions == null) {
            actions = new ArrayList<Action>();
            actions.add(new OpenAction());
            actions.add(new UninstallAction());
            actions.add(new InfoAction());
            actions.add(new FavoriteAction());
            actions.addAll(super.getActions());
        }
        return actions;
    }

    @Override
    protected abstract List<Item> buildItems();

    protected Item buildItem(final ResolveInfo info, final PackageManager manager) {
        Item item = new Item();
        item.source = this;
        item.title = info.loadLabel(manager);
        item.data = info.activityInfo.packageName + "/" + info.activityInfo.name;

        return item;
    }

    protected String getPackageName(final Item item) {
        return item.data.split("/")[0];
    }

    protected String getActivityName(final Item item) {
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
        public void runWith(final Item item) {
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
        public void runWith(final Item item) {
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.fromParts(SCHEME, getPackageName(item), null));
            context.startActivity(intent);
        }
    }

    public class InfoAction implements Action {
        @Override
        public char getKey() {
            return 'i';
        }

        @Override
        public CharSequence getName() {
            return "Info";
        }

        @Override
        public void runWith(final Item item) {
            String pkgName = getPackageName(item);
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts(SCHEME, pkgName, null));
            context.startActivity(intent);
        }
    }

    public class FavoriteAction implements Action {

        @Override
        public char getKey() {
            return 'f';
        }

        @Override
        public CharSequence getName() {
            return "Add to favorite";
        }

        @Override
        public void runWith(final Item item) {
            Favorite.getInstance().add(getPackageName(item), getActivityName(item));
        }
    }
}
