package com.bob.blauncher;

/**
 * @author Bob Qi
 * 
 */
public class Application
    extends android.app.Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        Favorite.init(getApplicationContext());
    }
}
