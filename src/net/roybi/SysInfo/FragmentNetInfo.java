package net.roybi.SysInfo;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import net.roybi.SysInfo.ui.PageFragment;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.ClipboardManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import net.roybi.SysInfo.R;

@SuppressWarnings("deprecation")
public class FragmentNetInfo extends PageFragment {
	ListView mList;
    TextView mText;

    ConnectivityManager mConnMgr;
	ArrayList<String> mLst = new ArrayList<String>();
	private String mFilePath;
	boolean bExport = false;
	AsyncTask<Void, Void, Void>  mTask;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		mConnMgr = (ConnectivityManager)getActivity().getSystemService(Activity.CONNECTIVITY_SERVICE);
//		NetworkInfo[] infos = mConnMgr.getAllNetworkInfo();
//		if (infos != null) {
//			for (NetworkInfo info : infos) {
//				mLst.add(info.toString());
//			}
//		}
		List<NetInfo> infos = getNetworkInterfaceInfo();
		for (NetInfo info : infos) {
		    StringBuilder sb = new StringBuilder();
		    sb.append(info.name+" mac:"+info.mac);
		    if (info.ips != null) {
		        sb.append(" ip:");
		        for (String ip:info.ips) {
		            sb.append(ip+',');
		        }
		    }
		    mLst.add(sb.toString()); 
		}
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.nets, container, false);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mList = (ListView) getView().findViewById(R.id.list);
		mList.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, mLst));
		
	    Button but = (Button) getView().findViewById(R.id.but1);
        but.setOnClickListener(mSelect);
        but = (Button) getView().findViewById(R.id.but2);
        but.setOnClickListener(mExport);

        if (mFilePath == null)
            mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/netCapture.pcap";
        mText = (TextView) getView().findViewById(R.id.text);
        mText.setText(mFilePath);
	        
		final ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Activity.CLIPBOARD_SERVICE); 
		mList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String s = (String)mList.getAdapter().getItem(position);
				cm.setText(s);
				Toast.makeText(getActivity(), getString(R.string.clipadd)+s, Toast.LENGTH_SHORT).show();
			}
		});	
	}	
	
    @Override
    public void onSelected() {
        
    }   
    
    private OnClickListener mSelect = new OnClickListener() {

        @Override
        public void onClick(View v) {
//            SelectFile();
        }
    };

    private OnClickListener mExport = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (bExport) {
                if (mTask != null && !mTask.isCancelled()) {
                    mTask.cancel(true);
                    getActivity().setProgressBarIndeterminateVisibility(false);
                    bExport = false;
                }
                return;
            }
            mTask = new ExportIconTask().execute((Void)null);
        }
    };

    private class ExportIconTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            bExport = true;
            getActivity().setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Process p = null;
            DataOutputStream dos = null;
            BufferedReader  dis = null;
            String cmd = "tcpdump -i any -p -s 0 -w " + mFilePath + "\n";
            int ret = 1;
            try {
                p = Runtime.getRuntime().exec("su -c "+cmd);
//                dos = new DataOutputStream(p.getOutputStream());
                dis = new BufferedReader(new InputStreamReader(p.getErrorStream()));              
//                dos.writeBytes(cmd);
//                dos.flush();
                
                String line = null;  
                while ((line = dis.readLine()) != null) {  
                    Log.w("error result:", line);  
                }
                
                ret = p.waitFor();
//                if (ret != 0) {
//                    Log.e("", "no root");
//                }

                Log.i("", "exec "+cmd+" return "+ret);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (dos != null) {  
                    try {  
                        dos.close();  
                    } catch (IOException e) {  
                        e.printStackTrace();  
                    }  
                }  
                if (dis != null) {  
                    try {  
                        dis.close();  
                    } catch (IOException e) {  
                        e.printStackTrace();  
                    }  
                }  
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            getActivity().setProgressBarIndeterminateVisibility(false);
            bExport = false;
        }

    }
    
    public static class NetInfo {
        String name;
        String mac;
        List<String> ips;
    }
    
    public static List<NetInfo> getNetworkInterfaceInfo() {
        List<NetInfo> infos = new ArrayList<NetInfo>();
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                NetInfo info = new NetInfo();
                //name 
                info.name = intf.getDisplayName();
                
                //ip
                
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toString();
                        if (info.ips == null) {
                            info.ips = new ArrayList<String>();
                        }
                        info.ips.add(sAddr);
//                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr); 
//                        if (useIPv4) {
//                            if (isIPv4) 
//                                return sAddr;
//                        } else {
//                            if (!isIPv4) {
//                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
//                                return delim<0 ? sAddr : sAddr.substring(0, delim);
//                            }
//                        }
                    }
                }
                
                //mac
                byte[] mac = intf.getHardwareAddress();
                if (mac!=null) {
                    StringBuilder buf = new StringBuilder();
                    for (int idx=0; idx<mac.length; idx++)
                        buf.append(String.format("%02X:", mac[idx]));       
                    if (buf.length()>0) buf.deleteCharAt(buf.length()-1);
                    info.mac = buf.toString();
                }

                infos.add(info);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } // for now eat exceptions
        return infos;
    }
    
    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac==null) return "";
                StringBuilder buf = new StringBuilder();
                for (int idx=0; idx<mac.length; idx++)
                    buf.append(String.format("%02X:", mac[idx]));       
                if (buf.length()>0) buf.deleteCharAt(buf.length()-1);
                return buf.toString();
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
        /*try {
            // this is so Linux hack
            return loadFileAsString("/sys/class/net/" +interfaceName + "/address").toUpperCase().trim();
        } catch (IOException ex) {
            return null;
        }*/
    }
    
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr); 
                        if (useIPv4) {
                            if (isIPv4) 
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                return delim<0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }
}
