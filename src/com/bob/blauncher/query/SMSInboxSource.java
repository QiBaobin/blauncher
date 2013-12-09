package com.bob.blauncher.query;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bob on 6/12/13.
 */
public class SMSInboxSource extends BaseSource {
    private List<Action> actions;
    private List<Action> queryActions;

    public SMSInboxSource(Context c) {
        super(c);
    }

    @Override
    public char getKey() {
        return 'm';
    }

    @Override
    public String getName() {
        return "SMS Inbox";
    }

    @Override
    public List<Action> getActions() {
        if (actions == null) {
            actions = super.getActions();
            actions.add(0, new ViewAction());
        }
        return actions;
    }

    @Override
    public List<Action> getOnQueryActions() {
        if (queryActions == null) {
            queryActions = super.getOnQueryActions();
            queryActions.add(0, new NewAction());
            queryActions.add(1, new MessageToAction());
        }
        return queryActions;
    }

    @Override
    protected List<Item> buildItems() {
        List<Item> items = new ArrayList<Item>();
        Uri uriSms = Uri.parse("content://sms/inbox");
        Cursor cursor = context.getContentResolver().query(uriSms, new String[]{"thread_id", "address", "body"}, null, null, null);
        int indexId = cursor.getColumnIndex("thread_id");
        int indexAddress = cursor.getColumnIndex("address");
        int indexBody = cursor.getColumnIndex("body");

        Map<String, String> titleCache = new HashMap<String, String>();
        while (cursor.moveToNext()) {
            Item i = new Item();
            String address = cursor.getString(indexAddress);
            String who = titleCache.get(address);
            if (who == null) {
                who = Utils.getPersonName(context, address);
                if (who != null) {
                    who = String.format("%s(%s)", who, address);
                } else {
                    who = address;
                }
                titleCache.put(address, who);
            }
            i.title = String.format("%s -> %s", who, cursor.getString(indexBody));
            i.data = cursor.getString(indexId);
            i.source = this;

            items.add(i);
        }
        cursor.close();

        return items;
    }

    class ViewAction implements Action {

        @Override
        public char getKey() {
            return 'v';
        }

        @Override
        public CharSequence getName() {
            return "View Message";
        }

        @Override
        public void runWith(Item item) {
            Intent defineIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("content://mms-sms/conversations/" + item.data));
            context.startActivity(defineIntent);
        }
    }

    class MessageToAction implements Action {

        @Override
        public char getKey() {
            return 'T';
        }

        @Override
        public CharSequence getName() {
            return "Send Message to";
        }

        @Override
        public void runWith(Item item) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", item.data, null)));
        }
    }

    class NewAction implements Action {

        @Override
        public char getKey() {
            return 'N';
        }

        @Override
        public CharSequence getName() {
            return "New Message";
        }

        @Override
        public void runWith(Item item) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"));
            intent.putExtra("sms_body", item.title);
            context.startActivity(intent);
        }
    }
}
