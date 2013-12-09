package com.bob.blauncher.query;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bob on 7/15/13.
 */
public class SettingsSource extends BaseSource {
    private static final Map<String, String> SETTINGS_NAMES = new HashMap<String, String>();

    static {
        SETTINGS_NAMES.put("Application", Settings.ACTION_APPLICATION_SETTINGS);
        SETTINGS_NAMES.put("Application Development", Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
        SETTINGS_NAMES.put("Bluetooth", Settings.ACTION_BLUETOOTH_SETTINGS);
        SETTINGS_NAMES.put("Date", Settings.ACTION_DATE_SETTINGS);
        SETTINGS_NAMES.put("Device Info", Settings.ACTION_DEVICE_INFO_SETTINGS);
        SETTINGS_NAMES.put("Display", Settings.ACTION_DISPLAY_SETTINGS);
        SETTINGS_NAMES.put("Input Methods", Settings.ACTION_INPUT_METHOD_SETTINGS);
        SETTINGS_NAMES.put("Internal Storage", Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
        SETTINGS_NAMES.put("Locale", Settings.ACTION_LOCALE_SETTINGS);
        SETTINGS_NAMES.put("Location", Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        SETTINGS_NAMES.put("Sound", Settings.ACTION_SOUND_SETTINGS);
        SETTINGS_NAMES.put("Sync", Settings.ACTION_SYNC_SETTINGS);
        SETTINGS_NAMES.put("Wifi", Settings.ACTION_WIFI_SETTINGS);
        SETTINGS_NAMES.put("Wireless", Settings.ACTION_WIRELESS_SETTINGS);
    }

    private List<Action> actions;

    public SettingsSource(Context context) {
        super(context);
    }

    @Override
    public char getKey() {
        return 's';
    }

    @Override
    public String getName() {
        return "Settings";
    }

    @Override
    protected List<Item> buildItems() {
        List<Item> items = new ArrayList<Item>(SETTINGS_NAMES.size());
        List<String> names = new ArrayList<String>(SETTINGS_NAMES.keySet());
        Collections.sort(names);
        for (String name : names) {
            Item i = new Item();
            i.source = this;
            i.title = name;
            i.data = SETTINGS_NAMES.get(name);

            items.add(i);
        }
        return items;
    }

    @Override
    public List<Action> getActions() {
        if (actions == null) {
            actions = super.getActions();
            actions.add(0, new OpenAction());
        }
        return actions;
    }

    private class OpenAction implements Action {

        @Override
        public char getKey() {
            return 'o';
        }

        @Override
        public CharSequence getName() {
            return "Open";
        }

        @Override
        public void runWith(Item item) {
            Intent intent = new Intent(item.data);
            if (Utils.isIntentAvailable(context, intent))
                context.startActivity(intent);
        }
    }
}
