package com.bob.blauncher.query;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Bob on 6/10/13.
 */
public interface Source {
    char getKey();
    String getName();
    List<Action> getActions();
    List<Action> getOnQueryActions();
    List<Item> getItems(Pattern pattern);
    Item buildQueryItem(String query);
    void reload();
}
