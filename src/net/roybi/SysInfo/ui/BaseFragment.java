package net.roybi.SysInfo.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

public class BaseFragment extends Fragment {

    
    @Override
    public void onPause() {
//        Log.e("", this.toString()+" onPause");
        super.onPause();
    }
    
    @Override
    public void onResume() {
//        Log.e("", this.toString()+" onResume");
        super.onResume();
    }
    
    @Override
    public void onStart() {
//        Log.e("", this.toString()+" onStart");
        super.onStart();
    }
    
    @Override
    public void onStop() {
//        Log.e("", this.toString()+" onStop");
        super.onStop();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
//        Log.e("", this.toString()+" onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
//        Log.e("", this.toString()+" setUserVisibleHint to "+isVisibleToUser);
    }
}
