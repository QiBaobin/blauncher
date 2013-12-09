package com.bob.blauncher;

import com.bob.blauncher.query.AllApplicationsSource;
import com.bob.blauncher.query.CalendarSource;
import com.bob.blauncher.query.CallLogSource;
import com.bob.blauncher.query.ContactsSource;
import com.bob.blauncher.query.RecentTasksSource;
import com.bob.blauncher.query.RunningApplicationSource;
import com.bob.blauncher.query.SMSInboxSource;
import com.bob.blauncher.query.SettingsSource;
import com.bob.blauncher.query.Source;

/**
 * Created by Bob on 6/12/13.
 */
public final class Constants {
    public static final String SHAREDPREFERENCES_NAME = "blauncher";

    public static final String KEY_ITEM_COLOR = "source_item_color";
    public static final String KEY_DEFAULT_SOURCES = "default_source_keys";

    public static final String VALUE_ITEM_COLOR = "#00ff00";
    public static final String VALUE_DEFAULT_SOURCES = "c,a,e";
    public static final String SOURCES_DELIMETER = ",";

    public static final Class<? extends Source>[] AVAILABLE_SOURCES = new Class[]{AllApplicationsSource.class, RecentTasksSource.class, RunningApplicationSource.class, ContactsSource.class, CallLogSource.class, SMSInboxSource.class, CalendarSource.class, SettingsSource.class};

    public static final String TIME_FORMAT = "M/d h:mma";
}
