package com.example.rotationmanager;

import android.os.Debug;

public class CommonUtil{

	public static boolean Write(){
		return Debug.isDebuggerConnected();
	}
	
	public static int pmod(int x,int mod){
		return (x%mod+mod)%mod;
	}
}