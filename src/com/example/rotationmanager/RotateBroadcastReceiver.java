package com.example.rotationmanager;

import android.content.*;

public class RotateBroadcastReceiver extends BroadcastReceiver {
	 
    @Override
    public void onReceive(Context context, Intent intent){
    	if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
            Intent intent1=new Intent(context, RotateService.class);
        	context.startService(intent1);
        	// Boot completed!
        }
    }
 
}
