package com.bob.blauncher.query;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;

import com.bob.blauncher.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bob on 6/11/13.
 */
public class CallLogSource extends PhonesSource {
    private static final Map<Integer, String> TYPE_STRING = new HashMap<Integer, String>();

    static {
        TYPE_STRING.put(CallLog.Calls.INCOMING_TYPE, "->");
        TYPE_STRING.put(CallLog.Calls.OUTGOING_TYPE, "<-");
        TYPE_STRING.put(CallLog.Calls.MISSED_TYPE, "--");
    }

    public CallLogSource(Context context) {
        super(context);
    }

    @Override
    protected List<Item> buildItems() {
        Uri uri = CallLog.Calls.CONTENT_URI;
        String[] projection = new String[]{CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME, CallLog.Calls.TYPE, CallLog.Calls.DATE};

        Cursor log = context.getContentResolver().query(uri, projection, null, null, CallLog.Calls.DATE + " desc");

        int indexNumber = log.getColumnIndex(CallLog.Calls.NUMBER);
        int indexName = log.getColumnIndex(CallLog.Calls.CACHED_NAME);
        int indexType = log.getColumnIndex(CallLog.Calls.TYPE);
        int indexDate = log.getColumnIndex(CallLog.Calls.DATE);

        List<Item> items = new ArrayList<Item>();
        DateFormat formatter = new SimpleDateFormat(Constants.TIME_FORMAT);
        Date d = new Date();
        while (log.moveToNext()) {
            Item i = new Item();
            i.data = log.getString(indexNumber);

            String type = TYPE_STRING.get(log.getInt(indexType));
            if (type == null) type = "";
            String name = log.getString(indexName);
            d.setTime(log.getLong(indexDate));
            String date = formatter.format(d);
            if (name == null || name.length() == 0) {
                i.title = String.format("%s %s %s", type, i.data, date);
            } else {
                i.title = String.format("%s %s %s %s", type, name, i.data, date);
            }
            i.source = this;

            items.add(i);
        }
        log.close();

        return items;
    }

    @Override
    public char getKey() {
        return 'l';
    }

    @Override
    public String getName() {
        return "Call Log";
    }
}
