package com.bob.blauncher.query;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bob on 6/10/13.
 */
public class ContactsSource extends PhonesSource {
    private static final String FORMAT_TITLE = "%s %s";
    private Map<Item, String> contactId;
    private List<Action> actions;
    private List<Action> queryActions;

    public ContactsSource(Context context) {
        super(context);
    }

    @Override
    public char getKey() {
        return 'c';
    }

    @Override
    public String getName() {
        return "Contacts";
    }

    @Override
    protected boolean showShowOrCreateAction() {
        return false;
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
        }
        return queryActions;
    }

    @Override
    protected List<Item> buildItems() {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};

        Cursor people = context.getContentResolver().query(uri, projection, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

        int indexId = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
        int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        if (contactId == null) {
            contactId = new HashMap<Item, String>();
        } else {
            contactId.clear();
        }
        List<Item> contacts = new ArrayList<Item>();
        people.moveToFirst();
        while (people.moveToNext()) {
            Item i = new Item();
            i.data = people.getString(indexNumber);
            i.title = String.format(FORMAT_TITLE, people.getString(indexName), i.data);
            i.source = this;

            contactId.put(i, people.getString(indexId));
            contacts.add(i);
        }
        people.close();

        return contacts;
    }

    class ViewAction implements Action {

        @Override
        public char getKey() {
            return 'v';
        }

        @Override
        public CharSequence getName() {
            return "View Contact";
        }

        @Override
        public void runWith(Item item) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactId.get(item))));
        }
    }

    class NewAction implements Action {

        @Override
        public char getKey() {
            return 'N';
        }

        @Override
        public CharSequence getName() {
            return "New Contact";
        }

        @Override
        public void runWith(Item item) {
            context.startActivity(new Intent(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT, Uri.fromParts("tel", item.data, null)));
        }
    }
}
