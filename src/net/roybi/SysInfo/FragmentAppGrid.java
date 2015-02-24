package net.roybi.SysInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.roybi.SysInfo.ui.PageFragment;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lamerman.FileDialog;
import com.lamerman.SelectionMode;

public class FragmentAppGrid extends PageFragment {
	public static final int PICK_REQUEST_CODE = 1;

	GridView mGrid;
	TextView mText;

	class AppInfo {  //for cache
	    PackageInfo  info;
	    ApplicationInfo app;
	    Drawable     image;
	    CharSequence label;
	}
	private List<AppInfo> mApps = new ArrayList<AppInfo>();
	private String mDir;
	private PackageManager mPM;

	boolean bExport = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    mPM = getActivity().getPackageManager();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.apps, container, false);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mGrid = (GridView) getView().findViewById(R.id.myGrid);
		mGrid.setAdapter(new AppsAdapter());
		mGrid.setOnItemClickListener(mShowInfo);

		Button but = (Button) getView().findViewById(R.id.but1);
		but.setOnClickListener(mSelect);
		but = (Button) getView().findViewById(R.id.but2);
		but.setOnClickListener(mExport);

		if (mDir == null)
			mDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/icons";
		mText = (TextView) getView().findViewById(R.id.text);
		mText.setText(mDir);

        new LoadAppTask().execute((Void)null);
	}

    @Override
    public void onSelected() {
    }   
    
    @Override
    public void onDestroyView() {
        mGrid.setAdapter(null);
        super.onDestroyView();
    }
    
	private OnClickListener mSelect = new OnClickListener() {

		@Override
		public void onClick(View v) {
			SelectFile();
		}
	};

	private OnClickListener mExport = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (bExport)
				return;
			new ExportIconTask().execute((Void)null);
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
			File dir = new File(mDir);
			if (!dir.exists()) {
				dir.mkdirs();
			}

			for (AppInfo info : mApps) {
				Drawable drawable = info.image;
				if (drawable != null) {
    				Bitmap bmp = drawableToBitmap(drawable);
    
    				String s = info.info.packageName + ".png";
    				File file = new File(mDir, s);
    				FileOutputStream outStream;
    				try {
    					outStream = new FileOutputStream(file);
    					bmp.compress(Bitmap.CompressFormat.PNG, 100, outStream);
    					outStream.flush();
    					outStream.close();
    				} catch (FileNotFoundException e) {
    					e.printStackTrace();
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
	
    private class LoadAppTask extends AsyncTask<Void, Void, List<AppInfo>> {
        @Override
        protected void onPreExecute() {
            getActivity().setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected List<AppInfo> doInBackground(Void... params) {
            
            return loadApps();
        }

        @Override
        protected void onPostExecute(List<AppInfo> result) {
            if (getActivity() == null) return;
            getActivity().setProgressBarIndeterminateVisibility(false);
            if (result.size() == 0) {
                Toast.makeText(getActivity(), R.string.error_getpackages, Toast.LENGTH_LONG).show();
            }
            mApps = result;
            AppsAdapter adapter = (AppsAdapter)mGrid.getAdapter();
            if (adapter != null)
                adapter.notifyDataSetChanged();
        }
    }	

	private OnItemClickListener mShowInfo = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			AppInfo info = mApps.get(position);

			Intent intent = new Intent(getActivity(), ActivityApp.class);
			intent.putExtra("info", info.info.packageName);
			startActivity(intent);
		}
	};

	private void SelectFile() {
		Intent intent = new Intent(getActivity(), FileDialog.class);

		intent.putExtra(FileDialog.START_PATH, Environment.getExternalStorageDirectory().getAbsolutePath());

		// can user select directories or not
		intent.putExtra(FileDialog.CAN_SELECT_DIR, true);

		// alternatively you can set file filter
		intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { "onlyfolder,no file needed" });

		intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_CREATE);

		startActivityForResult(intent, PICK_REQUEST_CODE);
	}

	private List<AppInfo> loadApps() {
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		List<AppInfo> newapps = new ArrayList<AppInfo>();
		List<PackageInfo> apps = null;
		AppInfo info = null;
        try {
            apps = mPM.getInstalledPackages(0);
            
            for (PackageInfo app:apps) {
                info = new AppInfo();
                info.info = app;
                info.app = app.applicationInfo;
                if (info.app != null) {
                    info.image = app.applicationInfo.loadIcon(mPM);
                    info.label = app.applicationInfo.loadLabel(mPM);
                }
                else {
                    info.image = null;
                    info.label = "aaa";
                }
                newapps.add(info);
            }            
        } catch (Exception e) {
            e.printStackTrace();
            if (info != null) {
                Log.e(getClass().getName(), info.info.toString());
            }
        }


        
		return newapps;
	}

	public class AppsAdapter extends BaseAdapter {
		public AppsAdapter() {
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			if (convertView == null) {
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.griditem, parent, false);
				holder = new ViewHolder();
				holder.image = (ImageView) convertView.findViewById(R.id.image);
				holder.text = (TextView) convertView.findViewById(R.id.text);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			AppInfo info = mApps.get(position);
			holder.image.setImageDrawable(info.image);
			holder.text.setText(info.label);

			return convertView;
		}

		public final int getCount() {
	        return mApps.size();
		}

		public final Object getItem(int position) {
			return mApps.get(position);
		}

		public final long getItemId(int position) {
			return position;
		}
	}

	static class ViewHolder {
		ImageView image;
		TextView text;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICK_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				mDir = data.getStringExtra(FileDialog.RESULT_PATH);
				mText.setText(mDir);
			} else {

			}

		}
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}
}
