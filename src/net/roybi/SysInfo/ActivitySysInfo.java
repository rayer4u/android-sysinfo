package net.roybi.SysInfo;

import java.util.ArrayList;

import net.roybi.SysInfo.ui.ViewPagerCustom;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabWidget;
import net.roybi.SysInfo.R;

public class ActivitySysInfo extends FragmentActivity {
	TabHost mHost;
	ViewPagerCustom  mViewPager;
    TabsAdapter mTabsAdapter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.main);
        
        setProgressBarIndeterminateVisibility(false);
        
        mHost = (TabHost) findViewById(android.R.id.tabhost);
        mHost.setup();
        
        mViewPager = (ViewPagerCustom)findViewById(R.id.pager);

        mTabsAdapter = new TabsAdapter(this, mHost, mViewPager);

        mTabsAdapter.addTab(mHost.newTabSpec("1").setIndicator(getString(R.string.fg_sys)),
                FragmentSysInfo.class, null);
        mTabsAdapter.addTab(mHost.newTabSpec("2").setIndicator(getString(R.string.fg_net)),
                FragmentNetInfo.class, null);
        mTabsAdapter.addTab(mHost.newTabSpec("3").setIndicator(getString(R.string.fg_prop)),
                FragmentProperties.class, null);
        mTabsAdapter.addTab(mHost.newTabSpec("4").setIndicator(getString(R.string.fg_log)),
                FragmentLogs.class, null);        
        mTabsAdapter.addTab(mHost.newTabSpec("5").setIndicator(getString(R.string.fg_sensor)),
        		FragmentSensor.class, null);     
        mTabsAdapter.addTab(mHost.newTabSpec("6").setIndicator(getString(R.string.fg_app)),
        		FragmentAppGrid.class, null);          
//        mTabsAdapter.addTab(mHost.newTabSpec("7").setIndicator(getString(R.string.fg_account)),
//                AccountFragment.class, null);          

        if (savedInstanceState != null) {
        	mHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }        
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
    
    public static class TabsAdapter extends FragmentPagerAdapter
    implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
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
}