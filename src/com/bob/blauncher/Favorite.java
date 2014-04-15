package com.bob.blauncher;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import android.content.Context;

/**
 * @author Bob Qi
 * 
 */
public class Favorite
    extends Observable
{
    public static class Entry
        implements Serializable
    {
        private static final long serialVersionUID = 4379572901054199123L;

        public String packageName;

        public String activityName;

        public Entry( final String packageName, final String activityName )
        {
            this.packageName = packageName;
            this.activityName = activityName;
        }

        @Override
        public boolean equals( final Object o )
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof Entry))
            {
                return false;
            }

            Entry that = (Entry) o;
            return packageName.equals(that.packageName) && activityName.equals(that.activityName);
        }

        @Override
        public int hashCode()
        {
            return 31 * packageName.hashCode() + activityName.hashCode();
        }
    }

    private static final String FILE_NAME = "favorite";

    private static Favorite sInstance;

    public static void init( final Context context )
    {
        sInstance = new Favorite(context);
    }

    public static Favorite getInstance()
    {
        return sInstance;
    }

    private final Context mContext;

    private Favorite( final Context context )
    {
        mContext = context;
        restoreFromDisk();
    }

    private final LinkedList<Entry> mEntries = new LinkedList<Favorite.Entry>();

    public void add( final String packageName, final String activityName )
    {
        Entry entry = new Entry(packageName, activityName);
        if (!mEntries.contains(entry))
        {
            mEntries.add(entry);
            setChanged();
            notifyObservers();
        }
    }

    public void remove( final Entry entry )
    {
        mEntries.remove(entry);
        setChanged();
        notifyObservers();
    }

    public List<Entry> getItems()
    {
        return Collections.unmodifiableList(mEntries);
    }

    @Override
    protected void setChanged()
    {
        super.setChanged();
        if (hasChanged())
        {
            saveToDisk();
        }
    }

    private void restoreFromDisk()
    {
        mEntries.clear();
        FileInputStream fis;
        try
        {
            fis = mContext.openFileInput(FILE_NAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            mEntries.addAll((LinkedList<Entry>) is.readObject());
            is.close();
        }
        catch (FileNotFoundException e)
        {
        }
        catch (StreamCorruptedException e)
        {
        }
        catch (IOException e)
        {
        }
        catch (ClassNotFoundException e)
        {
        }
    }

    private void saveToDisk()
    {
        FileOutputStream fos;
        try
        {
            fos = mContext.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(mEntries);
            os.close();
        }
        catch (FileNotFoundException e1)
        {
        }
        catch (IOException e)
        {
        }
    }
}
