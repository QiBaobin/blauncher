package com.bob.blauncher.query;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import com.bob.blauncher.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Bob on 6/13/13.
 */
public class CalendarSource extends BaseSource {
    public static final String[] INSTANCE_PROJECTION = new String[]{
            CalendarContract.Instances.EVENT_ID,      // 0
            CalendarContract.Instances.BEGIN,         // 1
            CalendarContract.Instances.TITLE          // 2
    };
    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_BEGIN_INDEX = 1;
    private static final int PROJECTION_TITLE_INDEX = 2;
    private List<Action> actions;
    private List<Action> queryActions;

    public CalendarSource(Context c) {
        super(c);
    }

    @Override
    public char getKey() {
        return 'e';
    }

    @Override
    public String getName() {
        return "Calendar";
    }

    @Override
    public List<Action> getActions() {
        if (actions == null) {
            actions = super.getActions();
            actions.add(0, new ViewAction());
            actions.add(1, new EditAction());
            actions.add(2, new DeleteAction());
        }
        return actions;
    }

    @Override
    protected List<Item> buildItems() {
        Calendar calendar = Calendar.getInstance();
        long startMillis = calendar.getTimeInMillis();
        calendar.add(Calendar.MONTH, 1);
        long endMillis = calendar.getTimeInMillis();

        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);
        Cursor cursor = context.getContentResolver().query(builder.build(), INSTANCE_PROJECTION, null, null, CalendarContract.Instances.BEGIN);
        List<Item> items = new ArrayList<Item>();
        DateFormat formatter = new SimpleDateFormat(Constants.TIME_FORMAT);
        Date d = new Date();
        while (cursor.moveToNext()) {
            Item item = new Item();
            item.data = Long.toString(cursor.getLong(PROJECTION_ID_INDEX));
            d.setTime(cursor.getLong(PROJECTION_BEGIN_INDEX));
            item.title = String.format("%s %s", formatter.format(d), cursor.getString(PROJECTION_TITLE_INDEX));
            item.source = this;

            items.add(item);
        }
        cursor.close();

        return items;
    }

    @Override
    public List<Action> getOnQueryActions() {
        if (queryActions == null) {
            queryActions = super.getOnQueryActions();
            queryActions.add(0, new NewAction());
        }
        return queryActions;
    }

    class ViewAction implements Action {

        @Override
        public char getKey() {
            return 'v';
        }

        @Override
        public CharSequence getName() {
            return "View";
        }

        @Override
        public void runWith(Item item) {
            Uri data = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.valueOf(item.data));
            context.startActivity(new Intent(Intent.ACTION_VIEW, data));
        }
    }

    class EditAction implements Action {

        @Override
        public char getKey() {
            return 'e';
        }

        @Override
        public CharSequence getName() {
            return "Edit";
        }

        @Override
        public void runWith(Item item) {
            // Intent.Action_EDIT doesn't work for now
            Intent intent = new Intent(Intent.ACTION_VIEW, CalendarContract.Events.CONTENT_URI.buildUpon().appendPath(item.data).build());
            context.startActivity(intent);
        }
    }

    class DeleteAction implements Action {

        @Override
        public char getKey() {
            return 'd';
        }

        @Override
        public CharSequence getName() {
            return "Delete";
        }

        @Override
        public void runWith(Item item) {
            context.getContentResolver().delete(CalendarContract.Events.CONTENT_URI.buildUpon().appendPath(item.data).build(), null, null);
            if (item.source != null) {
                item.source.reload();
            }
        }
    }

    class NewAction implements Action {

        @Override
        public char getKey() {
            return 'N';
        }

        @Override
        public CharSequence getName() {
            return "New Event";
        }

        @Override
        public void runWith(Item item) {
            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.Events.TITLE, item.title);
            context.startActivity(intent);
        }
    }
}
