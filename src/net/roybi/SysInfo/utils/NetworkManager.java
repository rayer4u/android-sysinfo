package net.roybi.SysInfo.utils;

import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

public class NetworkManager {
	public static final int NONE = 0;
	public static final int CMWAP = 1;
	public static final int CMNET = 2;
	public static final int WIFI = 3;
	
	final private boolean  mOphone = BeOphone();
	
	private ConnectivityManager mConnMgr = null;
	private static NetworkManager gThis = null;
	
	public static NetworkManager GetInstance(Context ct) {
		if (gThis == null) {
			gThis = new NetworkManager(ct);
		}
		return gThis;
	}
	
	private NetworkManager(Context ct)
	{
		mConnMgr = (ConnectivityManager)ct.getSystemService(Activity.CONNECTIVITY_SERVICE);
	}
	
	public boolean IsOphone() {
		return mOphone;
	}
	
	//通过检测api和接口存不存在判断是不是ophone
	private boolean BeOphone() {
		try
		{
			//此处的行为如此，无视警告
			if (Integer.valueOf(Build.VERSION.SDK) == 2) {
				@SuppressWarnings("unused")
				Class<?> network = Class.forName("android.net.DataConnection");
			}
			else {
				Class<?> network = Class.forName("android.net.NetworkInfo");
				@SuppressWarnings("unused")
				Method getApType = network.getDeclaredMethod("getApType");
			}
			return true;
		} catch (Exception e) {
//			e.printStackTrace();
			return false;
		}
	}
	
	public int GetActiveConnection()
	{
		NetworkInfo nInfo = mConnMgr.getActiveNetworkInfo();
		if(nInfo != null && nInfo.isConnected()) {
			Log.d("CMNetWork", nInfo.toString());
			
					
			if (nInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				return WIFI;
			}
			else {
				String s = nInfo.getExtraInfo();
				boolean bCMWap = s != null && (s.equalsIgnoreCase("cmwap") || 
						s.equalsIgnoreCase("ctwap") ||
						s.equalsIgnoreCase("uniwap") ||
						s.equalsIgnoreCase("3gwap"));
				return bCMWap?CMWAP:CMNET;
			}
		}		
		
		return NONE;
	}
}
