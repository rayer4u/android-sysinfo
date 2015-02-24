package net.roybi.SysInfo;

import java.lang.reflect.Array;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Map;

import net.roybi.SysInfo.ui.PageFragment;
import net.roybi.SysInfo.ui.ViewPagerCustom;
import net.roybi.SysInfo.utils.PackageUtil;
import net.roybi.SysInfo.utils.ReflectUtil;
import net.roybi.SysInfo.utils.SignatureGettter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.ClipboardManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityApp extends FragmentActivity {
    TabHost mHost;
    ViewPagerCustom mViewPager;
    TabsAdapter mTabsAdapter;
    
    private PackageInfo mInfo;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.app);

        PackageManager pm = getPackageManager();
        int flags = PackageManager.GET_RECEIVERS | PackageManager.GET_SERVICES
                | PackageManager.GET_PROVIDERS | PackageManager.GET_INSTRUMENTATION
                | PackageManager.GET_INTENT_FILTERS | PackageManager.GET_RESOLVED_FILTER
                | PackageManager.GET_META_DATA | PackageManager.GET_SHARED_LIBRARY_FILES
                | PackageManager.GET_ACTIVITIES | PackageManager.GET_SIGNATURES
                | PackageManager.GET_GIDS | PackageManager.GET_PERMISSIONS;
        try {
            mInfo = pm.getPackageInfo(getIntent().getStringExtra("info"), flags);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return;
        } 

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            getActionBar().setDisplayHomeAsUpEnabled(true);            
            getActionBar().setIcon(mInfo.applicationInfo.loadIcon(pm));
        }
        setTitle(mInfo.applicationInfo.loadLabel(pm));

        mHost = (TabHost) findViewById(android.R.id.tabhost);
        mHost.setup();

        mViewPager = (ViewPagerCustom) findViewById(R.id.pager);

        mTabsAdapter = new TabsAdapter(this, mHost, mViewPager);

        Bundle bd = new Bundle();
        bd.putParcelable("info", mInfo.applicationInfo);
        mTabsAdapter.addTab(mHost.newTabSpec("1").setIndicator(getString(R.string.fg_appinfo)),
                ListFragment.class, bd);
        bd = new Bundle();
        bd.putParcelable("info", mInfo);
        mTabsAdapter.addTab(mHost.newTabSpec("2").setIndicator(getString(R.string.fg_pkginfo)),
                ListFragment.class, bd);
        bd = new Bundle();
        bd.putParcelable("info", mInfo);
        mTabsAdapter.addTab(mHost.newTabSpec("3").setIndicator(getString(R.string.fg_componentsinfo)),
                FragmentComponents.class, bd);
        
        if (savedInstanceState != null) {
            mHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }

        Button but = (Button) findViewById(R.id.but1);
        but.setOnClickListener(mInfoListener);
        but = (Button) findViewById(R.id.but2);
        but.setOnClickListener(mCertListener);
        but = (Button) findViewById(R.id.but3);
        Intent intent = pm.getLaunchIntentForPackage(mInfo.packageName);
        if (intent != null) {
            but.setEnabled(true);
            but.setTag(intent);
            but.setOnClickListener(mRunListener);
            
        }
        else {
            but.setEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", mHost.getCurrentTabTag());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private OnClickListener mInfoListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= 9) { // above 2.3
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", mInfo.packageName, null);
                intent.setData(uri);
            } else { // below 2.3
                final String appPkgName = (Build.VERSION.SDK_INT == 8 ? "pkg"
                        : "com.android.settings.ApplicationPkgName");
                intent.setAction(Intent.ACTION_VIEW);
                intent.setClassName("com.android.settings",
                        "com.android.settings.InstalledAppDetails");
                intent.putExtra(appPkgName, mInfo.packageName);
            }
            startActivity(intent);
        }
    };
    
    private OnClickListener mCertListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Signature sig = mInfo.signatures[0];
            X509Certificate cert = SignatureGettter.parseSignature(sig);
            if (cert == null) {
                Toast.makeText(ActivityApp.this, getString(R.string.error_signature),
                        Toast.LENGTH_LONG).show();
                return;
            }
            StringBuilder str = new StringBuilder();
            str.append(getString(R.string.cert_sigalgname) + cert.getSigAlgName() + "\n");
            str.append(getString(R.string.cert_publickey) + cert.getPublicKey().toString()
                    + "\n");
            str.append(getString(R.string.cert_serialnumber)
                    + cert.getSerialNumber().toString() + "\n");
            str.append(getString(R.string.cert_subjectdn) + cert.getSubjectDN().toString()
                    + "\n");
            str.append(getString(R.string.cert_printfinger)
                    + SignatureGettter.getFingerprint(sig.toByteArray()) + "\n");
            // same as above
            // try {
            // str.append(getString(R.string.cert_printfinger) +
            // SignatureGettter.getFingerprint(cert.getEncoded()) + "\n");
            // } catch (CertificateEncodingException e) {
            // e.printStackTrace();
            // }
            new AlertDialog.Builder(ActivityApp.this)
                    .setTitle(getString(R.string.app_name))
                    .setMessage(str.toString())
                    .setNegativeButton(getString(android.R.string.cancel),
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //nothing
                                    dialog.cancel();
                                }
                            })
                    .setPositiveButton(getString(android.R.string.copy),
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    TextView tv = (TextView) ((AlertDialog)dialog).findViewById(android.R.id.message);
                                    if (tv == null)
                                        return;
                                    ClipboardManager cm = (ClipboardManager) getSystemService(Activity.CLIPBOARD_SERVICE); 
                                    cm.setText(tv.getText());
                                    Toast.makeText(ActivityApp.this, "add clipboard text:"+tv.getText(), Toast.LENGTH_SHORT).show();
                                }
                            })
                     .setCancelable(true).create().show();
        }
    };
    
    public static class TabsAdapter extends FragmentPagerAdapter implements
            TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
        private final Context mContext;
        private final TabHost mTabHost;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        static final class TabInfo {
            @SuppressWarnings("unused")
            private final String tag;
            private final Class<?> clss;
            private final Bundle args;

            TabInfo(String _tag, Class<?> _class, Bundle _args) {
                tag = _tag;
                clss = _class;
                args = _args;
            }
        }

        static class DummyTabFactory implements TabHost.TabContentFactory {
            private final Context mContext;

            public DummyTabFactory(Context context) {
                mContext = context;
            }

            @Override
            public View createTabContent(String tag) {
                View v = new View(mContext);
                v.setMinimumWidth(0);
                v.setMinimumHeight(0);
                return v;
            }
        }

        public TabsAdapter(FragmentActivity activity, TabHost tabHost, ViewPager pager) {
            super(activity.getSupportFragmentManager());
            mContext = activity;
            mTabHost = tabHost;
            mViewPager = pager;
            mTabHost.setOnTabChangedListener(this);
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
            tabSpec.setContent(new DummyTabFactory(mContext));
            String tag = tabSpec.getTag();

            TabInfo info = new TabInfo(tag, clss, args);
            mTabs.add(info);
            mTabHost.addTab(tabSpec);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            return Fragment.instantiate(mContext, info.clss.getName(), info.args);
        }

        @Override
        public void onTabChanged(String tabId) {
            int position = mTabHost.getCurrentTab();
            mViewPager.setCurrentItem(position);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            TabWidget widget = mTabHost.getTabWidget();
            int oldFocusability = widget.getDescendantFocusability();
            widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            mTabHost.setCurrentTab(position);
            widget.setDescendantFocusability(oldFocusability);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }

    private OnClickListener mRunListener = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            Intent intent = (Intent) v.getTag();
            startActivity(intent);
        }
    };
    
    public static class ListFragment extends PageFragment {

        public ListFragment() {
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

            Parcelable info = getArguments().getParcelable("info");

            Map<String, Object> map = ReflectUtil.GetClassFileds(info);

            ListView list = (ListView) getView().findViewById(R.id.list);
            InfoAdapter adapter = new InfoAdapter(map, getActivity());
            list.setAdapter(adapter);
            // list.setAdapter(new ArrayAdapter<String>(getActivity(),
            // android.R.layout.simple_list_item_1, array));
        }

        @Override
        public void onSelected() {
            // TODO Auto-generated method stub
            
        }
    }

    static class InfoAdapter extends BaseAdapter {
        ArrayList<Map.Entry<String, Object>> mList = new ArrayList<Map.Entry<String,Object>>();
        Context mContext;

        public InfoAdapter(Map<String, Object> map, Context context) {
            mList.addAll(map.entrySet());
            mContext = context;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(
                        android.R.layout.simple_list_item_1, parent, false);
            } else {
            }

            String text;
            Map.Entry<String, Object> entry = mList.get(position);
            if (entry.getValue() != null) {
                final Object value = entry.getValue();
                text = entry.getKey() + ":" + value.toString();
                if (value instanceof Integer) {
                    text += "(0x"+Integer.toHexString((Integer)value)+")";
                }

                convertView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (value.getClass().isArray()) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < Array.getLength(value); i++) {
                                sb.append(Array.get(value, i).toString() + "\n");
                            }
                            AlertDialog dlg = new AlertDialog.Builder(mContext).setMessage(
                                    sb.toString()).create();
                            dlg.setCanceledOnTouchOutside(true);
                            dlg.setCancelable(true);
                            dlg.show();
                        }
                    }
                });
            } else {
                text = entry.getKey() + ":null";
                convertView.setOnClickListener(null);
            }

            ((TextView) convertView).setText(text);
            return convertView;
        }

    }


    
}