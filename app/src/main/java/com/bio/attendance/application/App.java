package com.bio.attendance.application;

import android.app.Application;

import com.bio.attendance.BuildConfig;
import com.rey.material.app.ThemeManager;


/**
 * Created by Riz on 3/16/2016.
 */
public class App extends Application {
	/**
	 * Called when the application is starting, before any activity, service,
	 * or receiver objects (excluding content providers) have been created.
	 * Implementations should be as quick as possible (for example using
	 * lazy initialization of state) since the time spent in this function
	 * directly impacts the performance of starting the first activity,
	 * service, or receiver in a process.
	 * If you override this method, be sure to call super.onCreate().
	 */
	@Override
	public void onCreate() {

		super.onCreate();
		if ( BuildConfig.DEBUG)
			//refWatcher = LeakCanary.install(this);
			ThemeManager.init(this, 2, 0, null);
	}

	@Override
	public void setTheme(int resid) {
		super.setTheme(resid);
	}
}
