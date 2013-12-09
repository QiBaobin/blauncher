package com.bob.blauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bob.blauncher.query.Action;
import com.bob.blauncher.query.AllApplicationsSource;
import com.bob.blauncher.query.Item;
import com.bob.blauncher.query.Source;
import com.bob.blauncher.query.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by Bob on 6/21/13.
 */
public class SourcesFragment extends ListFragment implements TextWatcher, AdapterView.OnItemLongClickListener {
    private static final String QUERY_DELIMETER = "/";
    private static final String KEY_QUERY = "frag_current_query";
    private static final String KEY_SOURCES = "frag_default_sources";
    private final List<Source> mSources = new ArrayList<Source>();
    private Utils mUtils;
    private String mSelectedSources;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mUtils = new Utils(getActivity());

        if (savedInstanceState != null) {
            String defaultSources = savedInstanceState.getString(KEY_SOURCES);
            if (defaultSources != null) {
                setSelectedSources(defaultSources);
            }
            getQueryTextView().setText(savedInstanceState.getString(KEY_QUERY));
        }
        bindEvents();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSources.isEmpty())
            initSources();
        for (Source s : mSources) {
            if (!(s instanceof AllApplicationsSource)) {
                s.reload();
            }
        }
        buildQuery();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sources, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_QUERY, getQueryTextView().getText().toString());
        outState.putString(KEY_SOURCES, getSelectedSources());
        super.onSaveInstanceState(outState);
    }

    public void invalidate() {
        if (getView() == null) return;
        buildQuery();
    }

    public void reset() {
        EditText queryText = getQueryTextView();
        if (queryText != null)
            queryText.setText(null);
    }

    public void startSearch() {
        View queryText = getQueryTextView();
        if (queryText == null) return;

        queryText.requestFocus();
        InputMethodManager im = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        im.showSoftInput(queryText, InputMethodManager.SHOW_FORCED);
    }

    private void bindEvents() {
        Collection<Source> sources = mUtils.getAllSources();
        final CharSequence[] names = new String[sources.size()];
        final Map<String, Source> mapping = new HashMap<String, Source>();
        int i = 0;
        for (Source s : sources) {
            names[i++] = s.getName();
            mapping.put(s.getName(), s);
        }
        Arrays.sort(names);
        String selectedSource = getSelectedSources();
        final boolean[] checkedSources = new boolean[sources.size()];
        for (i = 0; i < checkedSources.length; i++) {
            checkedSources[i] = selectedSource.indexOf(mapping.get(names[i]).getKey()) >= 0;
        }
        final TextView sourcesView = (TextView) getView().findViewById(R.id.sources);
        sourcesView.setText(selectedSource);
        sourcesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final StringBuilder sb = new StringBuilder(getSelectedSources());
                new AlertDialog.Builder(getActivity()).setTitle("Sources").setCancelable(true).setMultiChoiceItems(names, checkedSources, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        char key = mapping.get(names[i]).getKey();
                        int index = sb.indexOf(key + "");
                        if (b) {
                            if (index == -1)
                                sb.append(key);
                        } else {
                            if (index >= 0)
                                sb.deleteCharAt(index);
                        }
                    }
                }).setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String selectedSource = sb.toString();
                        for (i = 0; i < checkedSources.length; i++) {
                            checkedSources[i] = selectedSource.indexOf(mapping.get(names[i]).getKey()) >= 0;
                        }
                        setSelectedSources(selectedSource);
                        sourcesView.setText(selectedSource);
                        buildQuery();
                        dialogInterface.dismiss();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();

            }
        });

        final TextView queryText = getQueryTextView();
        queryText.addTextChangedListener(this);
        queryText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    int position = 0;
                    String[] input = textView.getText().toString().split(QUERY_DELIMETER);
                    if (input.length > 2) {
                        try {
                            position = Integer.parseInt(input[2]) - 1;
                        } catch (Exception e) {
                        }
                    }

                    ListAdapter adapter = getListAdapter();
                    if (position >= 0 && position < adapter.getCount()) {
                        Item item = (Item) adapter.getItem(position);
                        if (item != null && item.source != null && item.source.getActions().size() > 0) {
                            item.source.getActions().get(0).runWith(item);
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        getView().findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                queryText.setText(null);
            }
        });

        getListView().setOnItemLongClickListener(this);
    }

    private void initSources() {
        mSources.clear();
        for (char c : getSelectedSources().toCharArray())
            addSource(c);
    }

    private void addSource(char key) {
        Source s = mUtils.getSource(key);
        if (s != null)
            mSources.add(s);
    }

    private void bindItems(String query) {
        Pattern pattern;
        if (query != null && query.trim().length() > 0) {
            try {
                pattern = Pattern.compile(query, Pattern.CASE_INSENSITIVE);
            } catch (PatternSyntaxException e) {
                pattern = Pattern.compile(query, Pattern.LITERAL);
            }
        } else {
            pattern = null;
        }
        List<Item> items = new ArrayList<Item>();
        for (Source s : mSources) {
            items.addAll(s.getItems(pattern));
        }
        setListAdapter(new ItemsAdapter(getActivity(), items));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Item i = (Item) l.getItemAtPosition(position);
        if (i != null) {
            Source s = i.source;
            if (s != null && s.getActions().size() > 0) {
                s.getActions().get(0).runWith(i);
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        buildQuery();
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (getQueryTextView().getText().length() > 0)
            buildQuery();
    }

    private void buildQuery() {
        EditText view = getQueryTextView();
        if (view == null) return;

        String originInput = view.getText().toString().trim();
        String[] input = originInput.split(QUERY_DELIMETER, -1);
        String query = input[0];
        bindItems(query);

        if (input.length > 1) {
            String positionAndAction = input[1];
            int i;
            for (i = 0; i < positionAndAction.length(); i++) {
                char c = positionAndAction.charAt(i);
                if (c < '0' || c > '9')
                    break;
            }

            boolean hasPosition = i > 0;
            boolean hasAction = i < positionAndAction.length();
            int position = 0;
            if (hasPosition) {
                position = Integer.parseInt(positionAndAction.substring(0, i)) - 1;
            }
            getListView().setSelection(position);

            if (hasAction) {
                String actions = positionAndAction.substring(i);
                if (mSources.size() > 0) {
                    boolean hasExecutedQueryAction = false;
                    if (!hasPosition)
                        for (Source s : mSources) {
                            if (runQueryAction(query, actions, s)) {
                                hasExecutedQueryAction = true;
                                break;
                            }
                        }

                    if (!hasExecutedQueryAction) {
                        runAtPosition(actions, position);
                    }
                }
                //remove actions
                String text = originInput.substring(0, originInput.length() - actions.length());
                view.setText(text);
                view.setSelection(text.length());
            }
        }
    }

    private String getSelectedSources() {
        return mSelectedSources;
    }

    public void setSelectedSources(String sources) {
        mSelectedSources = sources;
        if (isResumed()) {
            initSources();
        }
    }

    private EditText getQueryTextView() {
        if (getView() == null)
            return null;
        return (EditText) getView().findViewById(R.id.query);
    }

    private void runAtPosition(String action, int position) {
        if (action == null && action.trim().length() == 0)
            return;

        ListAdapter adapter = getListAdapter();
        if (position >= 0 && position < adapter.getCount()) {
            Item selected = (Item) adapter.getItem(position);
            if (selected != null) {
                for (char k : action.toCharArray())
                    for (Action a : selected.source.getActions()) {
                        if (k == a.getKey()) {
                            a.runWith(selected);
                            return;
                        }
                    }
            }
        }
    }

    private boolean runQueryAction(String query, String action, Source s) {
        for (char k : action.toCharArray()) {
            for (Action a : s.getOnQueryActions()) {
                if (k == a.getKey()) {
                    a.runWith(s.buildQueryItem(query));
                }
            }
        }
        return false;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        final Item item = (Item) adapterView.getItemAtPosition(i);
        final List<Action> actions = item.source.getActions();

        CharSequence[] names = new CharSequence[actions.size()];
        int index = 0;
        for (Action a : actions) {
            names[index++] = String.format("%s (%s)", a.getName(), a.getKey());
        }
        new AlertDialog.Builder(getActivity()).setTitle("Actions").setItems(names, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                actions.get(i).runWith(item);
                buildQuery();
            }
        }).show();

        return true;
    }

    /**
     * Adapter to show the list of all installed applications.
     */
    private class ItemsAdapter extends ArrayAdapter<Item> {
        List<Item> mItems;

        public ItemsAdapter(Context context, List<Item> apps) {
            super(context, 0, apps);
            mItems = apps;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (convertView == null) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_launchable, parent, false);
            }

            int textColor = Color.parseColor(mUtils.getItemColor());
            Item i = getItem(position);
            TextView tv = ((TextView) v.findViewById(R.id.title));
            tv.setText(i.title);
            tv.setTextColor(textColor);

            Source s = i.source;
            StringBuffer sb = new StringBuffer();
            sb.append(position + 1);
            if (s.getActions().size() > 0) {
                int index;
                for (index = 0; index < s.getActions().size() - 1; index++)
                    sb.append(s.getActions().get(index).getKey()).append(", ");
                sb.append(s.getActions().get(index).getKey());
            }
            tv = ((TextView) v.findViewById(R.id.source));
            tv.setText(sb.toString());
            tv.setTextColor(textColor);
            return v;
        }
    }

}

