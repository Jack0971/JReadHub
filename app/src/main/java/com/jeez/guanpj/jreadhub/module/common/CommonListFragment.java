package com.jeez.guanpj.jreadhub.module.common;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.jeez.guanpj.jreadhub.R;
import com.jeez.guanpj.jreadhub.bean.DataListBean;
import com.jeez.guanpj.jreadhub.bean.NewsBean;
import com.jeez.guanpj.jreadhub.event.OpenWebSiteEvent;
import com.jeez.guanpj.jreadhub.module.adpter.NewsListAdapterWithThirdLib;
import com.jeez.guanpj.jreadhub.mvpframe.rx.RxBus;
import com.jeez.guanpj.jreadhub.mvpframe.view.lce.animator.EmptyEffect;
import com.jeez.guanpj.jreadhub.mvpframe.view.lce.fragment.AbsBaseMvpLceFragment;
import com.jeez.guanpj.jreadhub.util.Constants;
import com.jeez.guanpj.jreadhub.util.ResourceUtil;
import com.jeez.guanpj.jreadhub.widget.custom.CustomLoadMoreView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;


public class CommonListFragment extends AbsBaseMvpLceFragment<DataListBean<NewsBean>, CommonPresenter> implements CommonContract.View<DataListBean<NewsBean>>, SwipeRefreshLayout.OnRefreshListener, BaseQuickAdapter.OnItemClickListener {

    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout mRefreshLayout;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private NewsListAdapterWithThirdLib mAdapter;
    private @NewsBean.Type String mNewsType;
    private boolean isPullToRefresh;

    public static CommonListFragment newInstance(@NewsBean.Type String type) {
        CommonListFragment fragment = new CommonListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.BUNDLE_NEWS_TYPE, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (null != bundle && !TextUtils.isEmpty(bundle.getString(Constants.BUNDLE_NEWS_TYPE))) {
            mNewsType = bundle.getString(Constants.BUNDLE_NEWS_TYPE);
        }
    }

    @Override
    protected void performInject() {
        getFragmentComponent().inject(this);
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_common;
    }

    @Override
    public void initView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new NewsListAdapterWithThirdLib();
        mAdapter.isFirstOnly(false);
        mAdapter.setNotDoAnimationCount(3);
        mAdapter.openLoadAnimation(BaseQuickAdapter.SCALEIN);
        mAdapter.setLoadMoreView(new CustomLoadMoreView());
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        mRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(),
                ResourceUtil.getResource(getActivity(), R.attr.readhubTheme)));
    }

    @Override
    public void initDataAndEvent() {
        mAdapter.setOnLoadMoreListener(() -> doLoadMore(), mRecyclerView);
        mRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setLceSwitchEffect(EmptyEffect.getInstance());
        loadData(false);
    }

    @Override
    public void loadData(boolean isPullToRefresh) {
        mPresenter.doRefresh(mNewsType, isPullToRefresh);
    }

    @Override
    public void onRefresh() {
        isPullToRefresh = true;
        mRefreshLayout.setRefreshing(true);
        mAdapter.setEnableLoadMore(false);
        mPresenter.doRefresh(mNewsType, true);
    }

    public void doLoadMore() {
        isPullToRefresh = false;
        mPresenter.doLoadMore(mNewsType, mAdapter.getItem(mAdapter.getItemCount() - 2).getPublishDate().toInstant().toEpochMilli());
    }

    @Override
    public void bindData(DataListBean<NewsBean> data) {
        if (null != data) {
            if (null != data.getData() && !data.getData().isEmpty()) {
                List<NewsBean> dataList = data.getData();
                if (isPullToRefresh) {
                    mRefreshLayout.setRefreshing(false);
                    //mAdapter.setNewData(dataList);
                    mPresenter.getDiffResult(mAdapter.getData(), dataList);
                } else {
                    mAdapter.addData(dataList);
                    mAdapter.loadMoreComplete();
                    mAdapter.setEnableLoadMore(true);
                }
            } else {
                if (isPullToRefresh) {
                } else {
                    mAdapter.loadMoreEnd(false);
                }
            }
        } else {
            if (isPullToRefresh) {
            } else {
                mAdapter.loadMoreFail();
            }
        }
    }

    @Override
    public void onDiffResult(DiffUtil.DiffResult diffResult, List<NewsBean> newData) {
        diffResult.dispatchUpdatesTo(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                List<NewsBean> changeList = new ArrayList<>();
                for (int i = position; i < position + count; i++) {
                    changeList.add(newData.get(i));
                }
                mAdapter.addData(position, changeList);
            }

            @Override
            public void onRemoved(int position, int count) {
                if (position < Constants.TOPIC_PAGE_SIZE) {
                    mAdapter.notifyItemRangeRemoved(position, count);
                }
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                mAdapter.notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onChanged(int position, int count, Object payload) {
                mAdapter.notifyItemRangeChanged(position, count, payload);
            }
        });
    }

    @Override
    public void onFabClick() {
        mRecyclerView.scrollToPosition(0);
        if (!mRefreshLayout.isRefreshing()) {
            mRefreshLayout.setRefreshing(true);
            onRefresh();
        }
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        if (null != adapter.getData() && null != adapter.getData().get(position)) {
            NewsBean newsBean = (NewsBean) adapter.getData().get(position);
            String url = null;
            if (!TextUtils.isEmpty(newsBean.getMobileUrl())) {
                url = newsBean.getMobileUrl();
            } else {
                url = newsBean.getUrl();
            }
            if (!TextUtils.isEmpty(url)) {
                RxBus.getInstance().post(new OpenWebSiteEvent(url, newsBean.getTitle()));
            }
        }
    }
}
