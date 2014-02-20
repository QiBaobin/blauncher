package com.bob.blauncher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bob.blauncher.query.Action;
import com.bob.blauncher.query.Source;
import com.bob.blauncher.query.Utils;

/**
 * Created by Bob on 6/12/13.
 */
public class SettingsActivity extends Activity {

    private static final String FORMAT_NAME_KEY = "%s (%s)";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initView();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, LauncherActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu( final Menu menu )
    {
        super.onCreateOptionsMenu(menu);
        menu.add("Set Wallpaper");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( final MenuItem item )
    {
        if ("Set Wallpaper".equals(item.getTitle()))
        {
            startActivity(Intent.createChooser(new Intent(Intent.ACTION_SET_WALLPAPER), "Choose wallpaper"));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        new Utils(this).saveDefaultSources(((EditText) findViewById(R.id.sources)).getText().toString());
        super.finish();
    }

    private void initView() {
        final Utils utils = new Utils(this);
        String textColor = utils.getItemColor();

        final TextView textColorSampleView = (TextView) findViewById(R.id.textColorLabel);
        textColorSampleView.setTextColor(Color.parseColor(textColor));

        TextView textColorView = (TextView) findViewById(R.id.textColor);
        textColorView.setText(textColor);
        textColorView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence charSequence, final int i, final int i2, final int i3) {

            }

            @Override
            public void onTextChanged(final CharSequence charSequence, final int i, final int i2, final int i3) {

            }

            @Override
            public void afterTextChanged(final Editable editable) {
                try {
                    String text = editable.toString();
                    int color = Color.parseColor(text);
                    textColorSampleView.setTextColor(color);
                    utils.saveItemColor(text);
                } catch (Exception e) {

                }
            }
        });

        final EditText sources = (EditText) findViewById(R.id.sources);
        sources.setText(utils.getDefaultSources());

        ((ListView) findViewById(android.R.id.list)).setAdapter(
                new SimpleAdapter(this, getData(utils.getAllSources()), android.R.layout.simple_list_item_2, new String[]{"source", "action"}, new int[]{android.R.id.text1, android.R.id.text2}));
    }

    private List<Map<String, String>> getData(final Collection<Source> sources) {
        List<Map<String, String>> data = new ArrayList<Map<String, String>>(sources.size());
        for (Source s : sources) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("source", String.format(FORMAT_NAME_KEY, s.getName(), s.getKey()));
            StringBuilder sb = new StringBuilder("Items Actions: ");
            List<Action> actions = s.getActions();
            if (actions.size() > 0) {
                int i = 0;
                for (; i < actions.size() - 1; i++) {
                    sb.append(String.format(FORMAT_NAME_KEY, actions.get(i).getName(), actions.get(i).getKey())).append(", ");
                }
                sb.append(String.format(FORMAT_NAME_KEY, actions.get(i).getName(), actions.get(i).getKey()));
            }

            sb.append("\nQuery Actions: ");
            actions = s.getOnQueryActions();
            if (actions.size() > 0) {
                int i = 0;
                for (; i < actions.size() - 1; i++) {
                    sb.append(String.format(FORMAT_NAME_KEY, actions.get(i).getName(), actions.get(i).getKey())).append(", ");
                }
                sb.append(String.format(FORMAT_NAME_KEY, actions.get(i).getName(), actions.get(i).getKey()));
            }

            map.put("action", sb.toString());

            data.add(map);
        }

        return data;
    }
}
