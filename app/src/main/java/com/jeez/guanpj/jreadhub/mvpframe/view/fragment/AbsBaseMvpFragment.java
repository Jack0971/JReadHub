package com.jeez.guanpj.jreadhub.mvpframe.view.fragment;

import android.os.Bundle;

import com.jeez.guanpj.jreadhub.base.BaseFragment;
import com.jeez.guanpj.jreadhub.mvpframe.presenter.AbsBasePresenter;
import com.jeez.guanpj.jreadhub.mvpframe.view.IBaseView;

import javax.inject.Inject;

/**
 * Created by Jie on 2016-11-2.
 */

public abstract class AbsBaseMvpFragment<P extends AbsBasePresenter> extends BaseFragment implements IBaseView {

    @Inject
    public P mPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        performInject();
        if (null != mPresenter) {
            mPresenter.onAttatch(this);
        }
    }

    protected abstract void performInject();

    @Override
    public void onRequestError(String msg) {

    }

    @Override
    public void onInternetError() {

    }

    @Override
    public void onDestroy() {
        if (mPresenter != null) {
            mPresenter.onDetach();
        }
        super.onDestroy();
    }
}