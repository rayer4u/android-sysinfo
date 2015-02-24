package net.roybi.SysInfo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public abstract class PageFragment extends BaseFragment {

    private BaseFragment mParent;
    private Boolean mCreated = false;
    private Boolean mOnSelect = false;
    
    public abstract void onSelected();

    public PageFragment() {

    }

    public PageFragment(BaseFragment parent) {
        mParent = parent;
    }

//    @Override
//    public void setLeftBack() {
//        if (mParent != null)
//            mParent.setLeftBack();
//        else
//            super.setLeftBack();
//    }
//
//    @Override
//    public void setLeftNavNone() {
//        if (mParent != null)
//            mParent.setLeftNavNone();
//        else
//            super.setLeftNavNone();
//    }
//
//    @Override
//    public void setLeftNaviImg(int resid) {
//        if (mParent != null)
//            mParent.setLeftNaviImg(resid);
//        else
//            super.setLeftNaviImg(resid);
//    }
//
//    @Override
//    public void setLeftOnClick(OnClickListener listener) {
//        if (mParent != null)
//            mParent.setLeftOnClick(listener);
//        else
//            super.setLeftOnClick(listener);
//    }
//
//    @Override
//    public void setRightNaviImg(int resid) {
//        if (mParent != null)
//            mParent.setRightNaviImg(resid);
//        else
//            super.setRightNaviImg(resid);
//    }
//
//    @Override
//    public void setRightNaviNone() {
//        if (mParent != null)
//            mParent.setRightNaviNone();
//        else
//            super.setRightNaviNone();
//    }
//
//    @Override
//    public void setRightOnClick(OnClickListener listener) {
//        if (mParent != null)
//            mParent.setRightOnClick(listener);
//        else
//            super.setRightOnClick(listener);
//    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        Log.e("", this.toString()+" onActivityCreated "+getActivity());
        mCreated = true;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (mOnSelect) {
            onSelected();
            mOnSelect = false;
        }
    }
    
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
//            Log.e("", this.toString()+" onSelected in onResume. getActivity "+getActivity());
            mOnSelect = true;
            if (mCreated) {
                onSelected();
                mOnSelect = false;
            }
        }
    }

}
