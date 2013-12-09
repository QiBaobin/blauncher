package com.bob.blauncher.query;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.bob.blauncher.Constants;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Bob on 6/12/13.
 */
public class Utils {
    private static Map<Character, Source> sources;
    private Context context;
    private SharedPreferences sharedPreferences;

    public Utils(Context c) {
        context = c;
    }


    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        return packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
    }

    public static String getPersonName(Context ct, String addr) {
        Uri person = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode(addr));
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
        Cursor cursor = ct.getContentResolver().query(person,
                projection, null, null, null);
        String name;
        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
        } else {
            name = null;
        }
        cursor.close();
        return name;
    }

    private SharedPreferences getSharedPreferences() {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(Constants.SHAREDPREFERENCES_NAME, 0);
        }
        return sharedPreferences;
    }

    public String getItemColor() {
        return getSharedPreferences().getString(Constants.KEY_ITEM_COLOR, Constants.VALUE_ITEM_COLOR);
    }

    public void saveItemColor(String color) {
        getSharedPreferences().edit().putString(Constants.KEY_ITEM_COLOR, color).commit();
    }

    public String getDefaultSources() {
        return getSharedPreferences().getString(Constants.KEY_DEFAULT_SOURCES, Constants.VALUE_DEFAULT_SOURCES);
    }

    public void saveDefaultSources(String sources) {
        StringBuilder sb = new StringBuilder();
        boolean hasValideSources = false;
        if (sources != null || sources.trim().length() > 0) {
            String[] groups = sources.split(Constants.SOURCES_DELIMETER);
            if (groups.length > 0) {
                for (int i = 0; i < groups.length - 1; i++) {
                    if (addSources(groups[i], sb)) {
                        sb.append(Constants.SOURCES_DELIMETER);
                        hasValideSources = true;
                    }
                }
                hasValideSources |= addSources(groups[groups.length - 1], sb);
            }
        }

        if (!hasValideSources) {
            sb.append(Constants.VALUE_DEFAULT_SOURCES);
        }
        getSharedPreferences().edit().putString(Constants.KEY_DEFAULT_SOURCES, sb.toString()).commit();
    }

    public Source getSource(char key) {
        return getSources().get(key);
    }

    public Collection<Source> getAllSources() {
        return getSources().values();
    }

    private Map<Character, Source> getSources() {
        if (sources == null) {
            sources = new HashMap<Character, Source>();
            for (Class<? extends Source> clazz : Constants.AVAILABLE_SOURCES) {
                try {
                    Source s = clazz.getDeclaredConstructor(Context.class).newInstance(context);
                    sources.put(s.getKey(), s);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }

        return sources;
    }

    private boolean addSources(String keys, StringBuilder buff) {
        final int start = buff.length();
        for (int i = 0; i < keys.length(); i++) {
            char c = keys.charAt(i);

            boolean isNewOne = true;
            for (int j = start; j < buff.length(); j++) {
                if (buff.charAt(j) == c) {
                    isNewOne = false;
                    break;
                }
            }

            if (isNewOne && getSource(c) != null) {
                buff.append(c);
            }
        }

        return buff.length() > start;
    }
}
