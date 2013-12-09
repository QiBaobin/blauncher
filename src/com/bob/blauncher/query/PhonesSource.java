package com.bob.blauncher.query;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.List;

/**
 * Created by Bob on 6/11/13.
 */
public abstract class PhonesSource extends BaseSource {
    private List<Action> actions;
    private List<Action> queryActions;

    public PhonesSource(Context context) {
        super(context);
    }

    protected boolean showShowOrCreateAction() {
        return true;
    }

    @Override
    public List<Action> getActions() {
        if (actions == null) {
            actions = super.getActions();
            if (showShowOrCreateAction())
                actions.add(0, new ShowOrCreateAction());
            actions.add(0, new MessageAction());
            actions.add(0, new CallAction());
        }
        return actions;
    }

    @Override
    public List<Action> getOnQueryActions() {
        if (queryActions == null) {
            queryActions = super.getOnQueryActions();
            queryActions.add(0, new CallAction());
            queryActions.add(1, new MessageAction());
            if (showShowOrCreateAction())
                queryActions.add(2, new ShowOrCreateAction());
        }
        return queryActions;
    }

    class ShowOrCreateAction implements Action {

        @Override
        public char getKey() {
            return 's';
        }

        @Override
        public CharSequence getName() {
            return "Show Or Create Contact";
        }

        @Override
        public void runWith(Item item) {
            context.startActivity(new Intent(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT, Uri.fromParts("tel", item.data, null)));
        }
    }

    class CallAction implements Action {

        @Override
        public char getKey() {
            return 'c';
        }

        @Override
        public CharSequence getName() {
            return "Call";
        }

        @Override
        public void runWith(Item item) {
            context.startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", item.data, null)));
        }
    }

    class MessageAction implements Action {

        @Override
        public char getKey() {
            return 'm';
        }

        @Override
        public CharSequence getName() {
            return "Send Message";
        }

        @Override
        public void runWith(Item item) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", item.data, null)));
        }
    }
}
