package com.bob.blauncher.query;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Bob on 6/12/13.
 */
public abstract class BaseSource implements Source {
    public static final String ACTION_COLORDICT_SEARCH = "colordict.intent.action.SEARCH";
    Context context;
    private List<Item> items;
    private List<Action> actions;
    private List<Action> queryActions;

    public BaseSource(Context c) {
        context = c;
    }

    public Item buildQueryItem(String query) {
        Item i = new Item();
        i.title = i.data = query;
        i.source = this;
        return i;
    }

    @Override
    public List<Action> getActions() {
        if (actions == null) {
            actions = new ArrayList<Action>();
            actions.add(new CopyAction());
            actions.add(new SendAction());
        }
        return actions;
    }

    @Override
    public List<Action> getOnQueryActions() {
        if (queryActions == null) {
            queryActions = new ArrayList<Action>();
            queryActions.add(new CopyAction());
            queryActions.add(new SendAction());
            queryActions.add(new InternetAction('G', "Google", "http://www.google.com/search?q=%s"));
            queryActions.add(new InternetAction('W', "Wikipedia", "http://zh.wikipedia.org/w/index.php?title=Special:Search&search=%s"));
            queryActions.add(new InternetAction('B', "Baidu Baike", "http://baike.baidu.com/search/word?word=%s"));
            if (Utils.isIntentAvailable(context, new Intent(ACTION_COLORDICT_SEARCH)))
                queryActions.add(new ColorDictAction());
        }
        return queryActions;
    }

    @Override
    public List<Item> getItems(Pattern pattern) {
        if (items == null)
            items = buildItems();

        if (pattern == null)
            return items;

        List<Item> matches = new ArrayList<Item>();
        for (Item i : items) {
            if (match(pattern, i))
                matches.add(i);
        }
        return matches;
    }

    @Override
    public void reload() {
        items = null;
    }

    protected List<Item> buildItems() {
        return Collections.emptyList();
    }

    protected boolean match(Pattern p, Item i) {
        return p.matcher(i.title).find();
    }

    class CopyAction implements Action {

        @Override
        public char getKey() {
            return 'C';
        }

        @Override
        public CharSequence getName() {
            return "Copy to clipboard";
        }

        @SuppressLint("NewApi")
        @Override
        public void runWith(Item item) {
            ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("blauncher", item.title));
        }
    }

    class SendAction implements Action {

        @Override
        public char getKey() {
            return 'S';
        }

        @Override
        public CharSequence getName() {
            return "Share";
        }

        @Override
        public void runWith(Item item) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, item.title);
            context.startActivity(intent);
        }
    }

    class ColorDictAction implements Action {

        @Override
        public char getKey() {
            return 'D';
        }

        @Override
        public CharSequence getName() {
            return "Search ColorDict";
        }

        @Override
        public void runWith(Item item) {
            Intent intent = new Intent(ACTION_COLORDICT_SEARCH);
            intent.putExtra("EXTRA_QUERY", item.title);
            context.startActivity(intent);
        }
    }

    class InternetAction implements Action {
        private final char key;
        private final String name;
        private final String template;

        public InternetAction(char key, String name, String template) {
            this.key = key;
            this.name = name;
            this.template = template;
        }

        @Override
        public char getKey() {
            return key;
        }

        @Override
        public CharSequence getName() {
            return String.format("Search %s", name);
        }

        @Override
        public void runWith(Item item) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(template, item.title))));
        }
    }
}
