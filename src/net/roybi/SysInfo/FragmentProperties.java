package net.roybi.SysInfo;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import net.roybi.SysInfo.ui.PageFragment;
import android.app.Activity;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import net.roybi.SysInfo.R;

@SuppressWarnings("deprecation")
public class FragmentProperties extends PageFragment {
	ListView mList;
	ArrayList<String> mLst = new ArrayList<String>();
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Properties pps = System.getProperties();
		Enumeration<?> names = pps.propertyNames();

		if (names != null) {
			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
				mLst.add(name+':'+pps.getProperty(name));
			}
		}		
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
				Toast.makeText(getActivity(), getString(R.string.clipadd)+s, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
    @Override
    public void onSelected() {
        
    }	
}
