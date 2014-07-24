package com.example.rotationmanager;

import java.util.List;

import android.app.ActivityManager;
import android.app.Application;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncStatusObserver;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

public class RotateService extends Service implements SensorEventListener,SharedPreferences.OnSharedPreferenceChangeListener{
	 
    private static final String TAG="LocalService";
    private static final String PACKAGE_INSTALLER = "com.android.packageinstaller";
    
    private AppSpecificOrientation appSpecificOrientation;
    private LinearLayout orientationChanger;
    private WindowManager.LayoutParams orientationLayout;

    //    private String beforeApp;
    private WindowManager wm;
    
    SharedPreferences pref;
    private boolean use_se;
    private SoundPool se;
    private int se_id;
    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        Toast.makeText(this, "RotationService#onCreate", Toast.LENGTH_SHORT).show();   
        appSpecificOrientation =(AppSpecificOrientation) getApplication();
        AppSpecificOrientation.setServiceRunning(true);
        wm=(WindowManager) this.getSystemService(Service.WINDOW_SERVICE);
        orientationChanger = new LinearLayout(this);
		orientationLayout = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, 0, PixelFormat.RGBA_8888);
		orientationLayout.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

		Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
		
		wm.addView(orientationChanger, orientationLayout);	
		setSensorEvent();

		pref = getSharedPreferences("pref",Context.MODE_PRIVATE);
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		
		se=new SoundPool(2, AudioManager.STREAM_MUSIC,0);
		se_id=se.load(getApplicationContext(),R.raw.katana,0);		
		v=Float.parseFloat(pref.getString(getString(R.string.gyro), "10.0"));
		use_se=pref.getBoolean(getString(R.string.use_se),false);
		
    }
 
    private void setSensorEvent(){
    	SensorManager sm=(SensorManager)getSystemService(SENSOR_SERVICE);
		List<Sensor> sensors=sm.getSensorList(Sensor.TYPE_GYROSCOPE);
		for(Sensor s:sensors)
			sm.registerListener(this, s,SensorManager.SENSOR_DELAY_UI);
    }

    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand Received start id " + startId + ": " + intent);
        Toast.makeText(this, "#onStartCommand", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        Toast.makeText(this, "#onDestroy", Toast.LENGTH_SHORT).show();
    }

    public class RotateBinder extends Binder{
    	RotateService getService(){
    		return RotateService.this;
    	}
    }
    //Binderの生成
    private final IBinder mBinder = new RotateBinder();
    
	@Override
	public IBinder onBind(Intent intent) {
		 Toast.makeText(this, "#onBind"+ ": " + intent, Toast.LENGTH_SHORT).show();
	     Log.i(TAG, "onBind" + ": " + intent);
	     return mBinder;
	}

	long duration=(long)Math.pow(10,9);

	long prev_t=0;
	
	float v;
	
	private static final int[] rots=new int[]{ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
		ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
		ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
		ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE};
	
	private void rotate(int v){
		int nowdir=orientationLayout.screenOrientation;int nowdiri=0;

		for(int i=0;i<4;i++)if(rots[i]==nowdir)nowdiri=i;
		orientationLayout.screenOrientation=rots[CommonUtil.pmod(nowdiri+v,4)];

		wm.updateViewLayout(orientationChanger, orientationLayout);
		if (orientationChanger.getVisibility() == View.GONE) orientationChanger.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		if(event.timestamp-prev_t<duration)return;
		final float[] values=event.values;
		if(values[2]>v){
			prev_t=event.timestamp;
			rotate(1);
			Log.i("+ dir","rotate");
			if(use_se)se.play(se_id, 1.0F, 1.0F, 0, 0, 1.0F);
		}
		if(values[2]<-v){
			prev_t=event.timestamp;
			rotate(-1);
			Log.i("- dir","rotate");
			if(use_se)se.play(se_id, 1.0F, 1.0F, 0, 0, 1.0F);
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		pref=sharedPreferences;
		v=Float.parseFloat(pref.getString(getString(R.string.gyro), "10.0"));
		use_se=pref.getBoolean(getString(R.string.use_se),false);
		Log.i("v",v+"");Log.i("use_se",""+use_se);
	}
	
	  @Override
	    public void onConfigurationChanged(Configuration newConfig) {
	        super.onConfigurationChanged(newConfig);
	 
	        // Checks the orientation of the screen
	        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
	        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
	        }
	    }
}