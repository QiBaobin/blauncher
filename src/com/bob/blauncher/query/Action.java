package com.bob.blauncher.query;

/**
 * Created by Bob on 6/10/13.
 */
public interface Action {
    char getKey();
    CharSequence getName();
    void runWith(Item item);
}
