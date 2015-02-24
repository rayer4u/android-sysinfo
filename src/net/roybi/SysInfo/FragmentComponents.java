package net.roybi.SysInfo;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import net.roybi.SysInfo.ui.PageFragment;
import net.roybi.SysInfo.utils.PackageUtil;
import net.roybi.SysInfo.utils.ReflectUtil;
import android.app.AlertDialog;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class FragmentComponents extends PageFragment {
    ExpandableListView mList;
    PackageInfo info;
    
    ArrayList<Entry<? extends ComponentInfo, ArrayList<? extends IntentFilter>>> mLst = new ArrayList<Entry<? extends ComponentInfo, ArrayList<? extends IntentFilter>>>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        info = getArguments().getParcelable("info");
        if (info != null) {
            Object apackage = PackageUtil.parsePackage(info.applicationInfo.publicSourceDir);
            
            mLst.addAll(PackageUtil.getActivityIntentFilter(apackage));
            mLst.addAll(PackageUtil.getReceiverIntentFilter(apackage));
            mLst.addAll(PackageUtil.getServiceIntentFilter(apackage));
            mLst.addAll(PackageUtil.getProviderIntentFilter(apackage));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.list2, container, false);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mList = (ExpandableListView) getView().findViewById(android.R.id.list);
        mList.setAdapter(new MyArrayAdapter());
        
        mList.setOnChildClickListener(mChildClicker);
    }

    @Override
    public void onSelected() {
        
    }
    
    class MyArrayAdapter extends BaseExpandableListAdapter {

        public MyArrayAdapter() {
            
        }
        
        @Override
        public int getGroupCount() {
            return mLst.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mLst.get(groupPosition).getValue().size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mLst.get(groupPosition).getKey();
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mLst.get(groupPosition).getValue().get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {
            
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(
                        R.layout.listitem_button1, parent, false);
            } else {
            }
            
            ComponentInfo info = (ComponentInfo) getGroup(groupPosition);
            
            TextView txt1 = (TextView) convertView.findViewById(android.R.id.text1);
            txt1.setText(info.name);
            TextView txt2 = (TextView) convertView.findViewById(android.R.id.text2);
            txt2.setText(info.toString());
            ImageView image = (ImageView) convertView.findViewById(R.id.image);
            image.setVisibility(View.VISIBLE);
            if (getChildrenCount(groupPosition) > 0) {
                if (isExpanded) {
                    image.setImageResource(R.drawable.expander_ic_maximized);
                }
                else {
                    image.setImageResource(R.drawable.expander_ic_minimized);
                }
            }
            else {
                image.setVisibility(View.INVISIBLE);           
            }
            
            View but = convertView.findViewById(R.id.but1);
            but.setTag(groupPosition);
            but.setOnClickListener(mClicker);

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(
                        android.R.layout.simple_list_item_1, parent, false);
            } else {
            }
            
            IntentFilter info = (IntentFilter) getChild(groupPosition, childPosition);
            
            TextView txt = (TextView) convertView.findViewById(android.R.id.text1);
            txt.setText(info.toString());

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
    
    OnClickListener mClicker = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            int position = (Integer) v.getTag();

            Map<String, Object> map = ReflectUtil.GetClassFileds(mList.getExpandableListAdapter().getGroup(position));
            StringBuilder str = new StringBuilder();
            
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                str.append(entry.getKey()+":"+(entry.getValue()!=null?entry.getValue().toString():"null")+"\n");
            }

            new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.app_name))
                    .setMessage(str.toString()).create().show();
        }
    };
    
    OnChildClickListener mChildClicker = new OnChildClickListener() {
        
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                int childPosition, long id) {
            IntentFilter ift = (IntentFilter) mList.getExpandableListAdapter().getChild(
                    groupPosition, childPosition);
            if (ift != null) {
                StringBuilder str = new StringBuilder();

                int count = ift.countActions();
                if (count > 0) {
                    str.append("Actions:");
                    for (int i = 0; i < count; i++) {
                        str.append("\n"+ift.getAction(i) + ";");
                    }
                    str.append("\n\n");
                }

                count = ift.countCategories();
                if (count > 0) {
                    str.append("Categories:");
                    for (int i = 0; i < count; i++) {
                        str.append("\n"+ift.getCategory(i) + ";");
                    }
                    str.append("\n\n");
                }

                count = ift.countDataTypes();
                if (count > 0) {
                    str.append("DataTypes:");
                    for (int i = 0; i < count; i++) {
                        str.append("\n"+ift.getDataType(i) + ";");
                    }
                    str.append("\n\n");
                }

                count = ift.countDataPaths();
                if (count > 0) {
                    str.append("DataPaths:");
                    for (int i = 0; i < count; i++) {
                        str.append("\n"+ift.getDataPath(i) + ";");
                    }
                    str.append("\n\n");
                }

                count = ift.countDataSchemes();
                if (count > 0) {
                    str.append("DataSchemes:");
                    for (int i = 0; i < count; i++) {
                        str.append("\n"+ift.getDataScheme(i) + ";");
                    }
                    str.append("\n\n");
                }

                if (Build.VERSION.SDK_INT > 18) {
                    count = ift.countDataSchemeSpecificParts();
                    if (count > 0) {
                        str.append("DataSchemeSpecificParts:");
                        for (int i = 0; i < count; i++) {
                            str.append("\n"+ift.getDataSchemeSpecificPart(i) + ";");
                        }
                        str.append("\n\n");
                    }
                }

                count = ift.countDataAuthorities();
                if (count > 0) {
                    str.append("DataAuthorities:");
                    for (int i = 0; i < count; i++) {
                        str.append("\n"+ift.getDataAuthority(i) + ";");
                    }
                    str.append("\n\n");
                }

                new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.app_name))
                        .setMessage(str.toString()).create().show();
                return true;
            }
            return false;
        }
    };
}
