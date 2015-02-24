package net.roybi.SysInfo;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import net.roybi.SysInfo.ui.PageFragment;
import net.roybi.SysInfo.utils.NetworkManager;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Proxy;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.ClipboardManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import net.roybi.SysInfo.R;

@SuppressWarnings("deprecation")
public class FragmentSysInfo extends PageFragment {
	ListView mList;
	NetworkManager mConnMgr;
	ArrayList<String> mLst = new ArrayList<String>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(getClass().getName(), "SysInfoFragment onCreate"+savedInstanceState);
		
		mLst.add(Secure.ANDROID_ID+":"+Secure.getString(getActivity().getContentResolver(), Secure.ANDROID_ID));
		
		TelephonyManager tm = (TelephonyManager)getActivity().getSystemService(Activity.TELEPHONY_SERVICE);		
		mLst.add(getString(R.string.device_id) + tm.getDeviceId()); 
		mLst.add("SimSerialNumber:" + tm.getSimSerialNumber()); 
		
		mLst.add("SDK:"+Build.VERSION.SDK);

		Display display = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
        mLst.add("Display info -----------------");
		mLst.add("DisplayId:"+display.getDisplayId());
		mLst.add("Screen Size:"+display.getWidth()+"X"+display.getHeight());
		mLst.add("Orientation:"+display.getOrientation());
		mLst.add("PixelFormat:"+display.getPixelFormat());
		mLst.add("RefreshRate:"+display.getRefreshRate());
	    // includes window decorations (statusbar bar/menu bar)
	    if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17)
	    {
	        try
	        {
	            mLst.add("RawWidth:"+Display.class.getMethod("getRawWidth").invoke(display));
	            mLst.add("RawHeight:"+Display.class.getMethod("getRawHeight").invoke(display));
	        }
	        catch (Exception ignored)
	        {
	        }
	    }

	    // includes window decorations (statusbar bar/menu bar)
	    if (Build.VERSION.SDK_INT >= 17)
	    {
	        try
	        {
	            Point realSize = new Point();
	            display.getRealSize(realSize);
	            mLst.add("Real Size:"+realSize.toString());
	        }
	        catch (Exception ignored)
	        {
	        }
	    }		
		
		//not get DisplayMetrics from display. http://stackoverflow.com/questions/22916490/what-does-displaymetrics-scaleddensity-actually-return-in-android
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		double sz = Math.sqrt(Math.pow(metrics.widthPixels, 2)+Math.pow(metrics.heightPixels, 2)) / (160 * metrics.density);
		mLst.add("SizeCopycat(inch):"+sz);
		double dx = Math.pow(metrics.widthPixels / metrics.xdpi, 2); 
		double dy = Math.pow(metrics.heightPixels / metrics.ydpi, 2);
		double sz2 = Math.sqrt(dx+dy);
		mLst.add("SizeNormal(inch):"+sz2);
		mLst.add("Is Pad:"+((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE));
		mLst.add("screenlayout:"+(getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK));
		mLst.add("Metrics info -----------------");
		Field[] fs = metrics.getClass().getFields();
		for (Field f : fs) {
			if (f.getModifiers() == (Modifier.PUBLIC)) {
				String s = new String();
				try {
					s = String.valueOf(f.get(metrics));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					continue;
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					continue;
				}
				mLst.add(f.getName()+":"+s);
			}
		}
		mLst.add("Build info -----------------");
		fs = Build.class.getFields();
		for (Field f : fs) {
			if (f.getModifiers() == (Modifier.FINAL + Modifier.PUBLIC + Modifier.STATIC)) {
				String s = new String();
				try {
					s = String.valueOf(f.get(null));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					continue;
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					continue;
				}
				mLst.add(f.getName()+":"+s);
			}
		}

		mLst.add("User dirs -----------------");
		if (Build.VERSION.SDK_INT >= 8) {
		    File f = getActivity().getExternalCacheDir();
    		mLst.add("External Cache dir:"+(f != null?f.toString():"null"));
    		f = getActivity().getExternalFilesDir(null);
    		mLst.add("External File dir:"+(f != null?f.toString():"null"));
		}
        
        if (Build.VERSION.SDK_INT >= 11) { // above 3.0
            File f = getActivity().getObbDir();
            mLst.add("ObbDir dir:"+(f != null?f.toString():"null"));
        } 

        if (Build.VERSION.SDK_INT >= 19){ 
            File[] ffs = getActivity().getExternalCacheDirs();
            if (ffs != null)
                for (int i = 0; i < ffs.length; i++) {
                    File f = ffs[i];
                    mLst.add("External Cachedir "+i+":"+(f != null?f.toString():"null"));
                }
            ffs = getActivity().getExternalFilesDirs(null);
            if (ffs != null)
                for (int i = 0; i < ffs.length; i++) {
                    File f = ffs[i];
                    mLst.add("External Filesdir "+i+":"+(f != null?f.toString():"null"));
                }
        }		

		
		mLst.add("Environment info -----------------");
        Method[] ms = Environment.class.getMethods();
        for (Method m : ms) {
            if (m.getModifiers() == (Modifier.PUBLIC + Modifier.STATIC)) {
                String s = new String();
                try {
                    
                    Class<?>[] cs = m.getParameterTypes();
                    if (cs.length == 0) {
                        //没有参数的公开静态函数
                        Object ret = m.invoke(Environment.class);
                        if (ret != null)
                            s = ret.toString();
                        mLst.add(m.getName()+":"+s);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    continue;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue;
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    continue;
                }                
            }
        }
        if (Build.VERSION.SDK_INT >= 8) {
            fs = Environment.class.getFields();
            for (Field f : fs) {
                if (f.getModifiers() == (Modifier.PUBLIC + Modifier.STATIC)) {
                    String s = new String();
                    try {
                        s = (String)f.get(null);
                        mLst.add(f.getName()+":"+Environment.getExternalStoragePublicDirectory(s));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
            }            
        }
        
		mConnMgr = NetworkManager.GetInstance(getActivity());
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.list, container, false);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mList = (ListView) getView().findViewById(R.id.list);
		mList.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, mLst));
		
		final ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Activity.CLIPBOARD_SERVICE); 
		mList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String s = (String)mList.getAdapter().getItem(position);
				cm.setText(s);
				Toast.makeText(getActivity(), "add clipboard text:"+s, Toast.LENGTH_SHORT).show();
			}
		});
		Update();
	}
	
    @Override
    public void onSelected() {
        
    }

	void Update() {
		int nInfo = mConnMgr.GetActiveConnection();
		
		String s = new String();
		if(nInfo == NetworkManager.CMWAP) {
			s = "network CMWAP";
			try {
				String s1 = System.getProperty("net.gprs.http-proxy");
				if (s1 != null && s1.length() > 0) {
					s += "\nnet.gprs.http-proxy:" + s1;
				}			
				s1 = System.getProperty("http.proxyHost");
				if (s1 != null && s1.length() > 0) {
					s += "\nhttp.proxyHost:" + s1 + "\nhttp.proxyPort:" + System.getProperty("http.proxyPort");
				}
				s1 = Proxy.getDefaultHost();
				if (s1 != null && s1.length() > 0) {
					s += "\nProxy.getDefaultHost:" + s1 + "\nProxy.getDefaultPort:" + Proxy.getDefaultPort();
				}
				s1 = Settings.System.getString(getActivity().getContentResolver(), Settings.System.HTTP_PROXY);
				if (s1 != null && s1.length() > 0) {
					s += "\nSettings.System.HTTP_PROXY:" + s1;
				}
				else {
					s1 = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.HTTP_PROXY);
					if (s1 != null && s1.length() > 0) {
						s += "\nSettings.Secure.HTTP_PROXY:" + s1;
					}					
				}
			}
			catch (Exception e) {
				s += "\n" + e.toString();
			}
		}
		else if (nInfo == NetworkManager.CMNET) {
			s = "network CMNET";
		}
		else if (nInfo == NetworkManager.WIFI) {
			s = "network CMNET";
		}
		else if (nInfo == NetworkManager.NONE) {
			s = "network NONE";			
		}
		
		Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();
	}

}
