package com.bob.blauncher.query;

/**
 * Created by Bob on 6/10/13.
 */
public class Item {
    /**
     * The application name.
     */
    public CharSequence title;
    /**
     * The uri data which doesn't contain scheme
     */
    public String data;
    /**
     * The data source
     */
    public Source source;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Item)) {
            return false;
        }

        Item that = (Item) o;
        return (source != null ? source == that.source : that.source == null) && (title != null ? title.equals(that.title) : that.title == null) &&
                (data != null ? data.equals(that.data) : that.data == null);
    }

    @Override
    public int hashCode() {
        return 31 * 31 * (source != null ? source.hashCode() : 0) + 31 * (title != null ? title.hashCode() : 0) + (data != null ? data.hashCode() : 0);
    }
}
