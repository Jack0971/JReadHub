package com.jeez.guanpj.jreadhub.ui.topic;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.jeez.guanpj.jreadhub.R;
import com.jeez.guanpj.jreadhub.bean.DataListBean;
import com.jeez.guanpj.jreadhub.bean.TopicBean;
import com.jeez.guanpj.jreadhub.mvpframe.view.fragment.AbsBaseMvpFragment;
import com.jeez.guanpj.jreadhub.ui.adpter.AnimatorAdapter;
import com.jeez.guanpj.jreadhub.ui.adpter.TopicListAdapter;
import com.jeez.guanpj.jreadhub.widget.LoadMoreFooter;
import com.takwolf.android.hfrecyclerview.HeaderAndFooterRecyclerView;

import butterknife.BindView;

public class TopicFragment extends AbsBaseMvpFragment<TopicPresenter> implements TopicContract.View, SwipeRefreshLayout.OnRefreshListener, LoadMoreFooter.OnLoadMoreListener {

    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.recycler_view)
    HeaderAndFooterRecyclerView recyclerView;

    private LoadMoreFooter loadMoreFooter;
    private TopicListAdapter listAdapter;
    private AnimatorAdapter animatorAdapter;

    public static TopicFragment newInstance() {
        Bundle args = new Bundle();
        TopicFragment fragment = new TopicFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_common;
    }

    @Override
    protected void performInject() {
        getFragmentComponent().inject(this);
    }

    @Override
    public void initView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //recyclerView.addItemDecoration(new GapItemDecoration(getActivity()));
        //recyclerView.addOnScrollListener(new FloatingTipButtonBehaviorListener.ForRecyclerView(btnBackToTopAndRefresh));

        loadMoreFooter = new LoadMoreFooter(getContext(), recyclerView);
        animatorAdapter = new AnimatorAdapter(getContext());
        animatorAdapter.isFirstOnly(false);
        animatorAdapter.setNotDoAnimationCount(3);
        animatorAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_LEFT);
        recyclerView.setAdapter(animatorAdapter);
        /*listAdapter = new TopicListAdapter(getContext());
        recyclerView.setAdapter(listAdapter);*/
    }

    @Override
    public void initDataAndEvent() {
        refreshLayout.setOnRefreshListener(this);
        loadMoreFooter.setOnLoadMoreListener(this);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setRefreshing(true);
        onRefresh();

    }

    @Override
    public void onRefresh() {
        mPresenter.doRefresh();
    }

    @Override
    public void onLoadMore() {
        mPresenter.doLoadMore(animatorAdapter.getItem(animatorAdapter.getItemCount() - 1).getOrder());
    }

    @Override
    public void onRequestStart() {

    }

    @Override
    public void onRequestEnd(DataListBean<TopicBean> data, boolean isPull2Refresh) {
        /*if (isPull2Refresh) {
            listAdapter.clear();
            listAdapter.clearExpandStates();
            listAdapter.addItems(data.getData());
            refreshLayout.setRefreshing(false);
            loadMoreFooter.setState(data.getData().isEmpty() ? LoadMoreFooter.STATE_DISABLED : LoadMoreFooter.STATE_ENDLESS);
        } else {
            listAdapter.addItems(data.getData());
            loadMoreFooter.setState(data.getData().isEmpty() ? LoadMoreFooter.STATE_FINISHED : LoadMoreFooter.STATE_ENDLESS);
        }*/
        if (isPull2Refresh) {
            animatorAdapter.replaceData(data.getData());
            refreshLayout.setRefreshing(false);
        } else {
            animatorAdapter.addData(data.getData());
        }
        loadMoreFooter.setState(data.getData().isEmpty() ? LoadMoreFooter.STATE_FINISHED : LoadMoreFooter.STATE_ENDLESS);
    }

    @Override
    public void onRequestError(boolean isPull2Refresh) {
        if (isPull2Refresh) {
            refreshLayout.setRefreshing(false);
        } else {
            loadMoreFooter.setState(LoadMoreFooter.STATE_FAILED);
        }
    }

    @Override
    public void onFabClick() {
        recyclerView.scrollToPosition(0);
        if (!refreshLayout.isRefreshing()) {
            refreshLayout.setRefreshing(true);
            onRefresh();
        }
    }
}
