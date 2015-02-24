package net.roybi.SysInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import net.roybi.SysInfo.ui.PageFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import net.roybi.SysInfo.R;

@SuppressWarnings("unchecked")
public class FragmentLogs extends PageFragment {
	private static final String TAG = "LogsFragment";
	private static final String LINE_SEPARATOR = "\n";
	public static final String EXTRA_FILTER_SPECS = "cn.roybi.NetInfo.extra.FILTER_SPECS";//$NON-NLS-1$
	public static final String EXTRA_FORMAT = "cn.roybi.NetInfo.extra.FORMAT";//$NON-NLS-1$
	public static final String EXTRA_BUFFER = "cn.roybi.NetInfo.extra.BUFFER";//$NON-NLS-1$
	
	private CollectLogTask mCollectLogTask;
	@SuppressWarnings("unused")
	private CollectLogTask mClearLogTask;
	private String[] mFilterSpecs;
	private String mFormat;
	private String mBuffer;
	
	private TextView mText;
	private Button   mButClear;
	private Button   mButFilter;
	 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bd = getArguments();
		if (bd != null) {
			mFilterSpecs = bd.getStringArray(EXTRA_FILTER_SPECS);
			mFormat = bd.getString(EXTRA_FORMAT);
			mBuffer = bd.getString(EXTRA_BUFFER);
		}

	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.logs, container, false);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mText = (TextView) getView().findViewById(R.id.text);
		mText.setMovementMethod(ScrollingMovementMethod.getInstance());
		
		mButClear = (Button) getView().findViewById(R.id.but1);
		mButClear.setOnClickListener(mDoClear);
		mButFilter = (Button) getView().findViewById(R.id.but2);
		mButFilter.setOnClickListener(mDoFilter);
	}
	
    @Override
    public void onSelected() {
        
    }   
    
	private OnClickListener mDoClear = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			ArrayList<String> list = new ArrayList<String>();

			list.add("-c");

			mClearLogTask = (CollectLogTask) new CollectLogTask().execute(list);
		}
	};
	
	private OnClickListener mDoFilter = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			ArrayList<String> list = new ArrayList<String>();

			if (mFormat != null) {
				list.add("-v");
				list.add(mFormat);
			}

			if (mBuffer != null) {
				list.add("-b");
				list.add(mBuffer);
			}

			if (mFilterSpecs != null) {
				for (String filterSpec : mFilterSpecs) {
					list.add(filterSpec);
				}
			}
			
			mCollectLogTask = (CollectLogTask) new CollectLogTask().execute(list);
		}
	};	
	
	void cancellCollectTask() {
		if (mCollectLogTask != null
				&& mCollectLogTask.getStatus() == AsyncTask.Status.RUNNING) {
			mCollectLogTask.cancel(true);
			mCollectLogTask = null;
		}
	}
	 
	private class CollectLogTask extends
			AsyncTask<ArrayList<String>, Void, StringBuilder> {
		@Override
		protected void onPreExecute() {
			getActivity().setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected StringBuilder doInBackground(ArrayList<String>... params) {
			final StringBuilder log = new StringBuilder();
			try {
				ArrayList<String> commandLine = new ArrayList<String>();
				commandLine.add("logcat");//$NON-NLS-1$
				commandLine.add("-d");//$NON-NLS-1$
				ArrayList<String> arguments = ((params != null) && (params.length > 0)) ? params[0]
						: null;
				if (null != arguments) {
					commandLine.addAll(arguments);
				}

				Process process = Runtime.getRuntime().exec(
						commandLine.toArray(new String[0]));
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(process.getInputStream()));

				String line;
				while ((line = bufferedReader.readLine()) != null) {
					log.append(line);
					log.append(LINE_SEPARATOR);
				}
			} catch (IOException e) {
				Log.e(TAG, "CollectLogTask.doInBackground failed", e);//$NON-NLS-1$
			}

			return log;
		}

		@Override
		protected void onPostExecute(StringBuilder log) {
			getActivity().setProgressBarIndeterminateVisibility(false);
			
			if (null != log) {
				mText.scrollBy(0, 0);
				
				mText.setText(log.toString());
				
				Layout layout = mText.getLayout();
				if (mText.getLineCount() > 0) {
					int scrollDelta = layout.getLineBottom(mText.getLineCount() - 1) - mText.getScrollY() - mText.getHeight();
					if (scrollDelta > 0) 
						mText.scrollBy(0, scrollDelta);
				}
				else 
					mText.scrollBy(0, 0);

			}
		}
	}
}
