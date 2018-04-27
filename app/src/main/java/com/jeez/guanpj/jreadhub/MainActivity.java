package com.jeez.guanpj.jreadhub;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;

import com.jeez.guanpj.jreadhub.base.BaseActivity;
import com.jeez.guanpj.jreadhub.constant.AppStatus;
import com.jeez.guanpj.jreadhub.ui.adpter.FragmentAdapter;
import com.jeez.guanpj.jreadhub.util.PermissionsChecker;
import com.jeez.guanpj.jreadhub.util.UncaughtException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.dl_main)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.nv_main)
    NavigationView mNavigationView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tabs)
    TabLayout mTabLayout;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;
    /*@BindView(R.id.fab)
    FloatingActionButton mFloatingActionButton;*/

    private List<Fragment> fragments;
    private FragmentAdapter fragmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission();
        } else {
            initErrorLogDetactor();
        }
        ButterKnife.bind(this);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {
        initToolBar();
    }

    @Override
    public void initDataAndEvent() {
        List<String> mPageTitles = new ArrayList<>();
        mPageTitles.add("热门话题");
        mPageTitles.add("科技动态");
        mPageTitles.add("开发者资讯");
        mPageTitles.add("区块链快讯");
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager(), mPageTitles);
        mViewPager.setAdapter(adapter);

        mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        for (int i = 0; i < mPageTitles.size(); i++) {
            mTabLayout.addTab(mTabLayout.newTab().setText(mPageTitles.get(i)));
        }
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);

        mNavigationView.setNavigationItemSelectedListener(this);
    }

    private void initToolBar() {
        setSupportActionBar(mToolbar);

        mToolbar.inflateMenu(R.menu.menu_main);
        Resources.Theme theme = getTheme();

        TypedValue navIcon = new TypedValue();
        TypedValue overFlowIcon = new TypedValue();

        theme.resolveAttribute(R.attr.navIcon, navIcon, true);
        theme.resolveAttribute(R.attr.overFlowIcon, overFlowIcon, true);

        mToolbar.setNavigationIcon(navIcon.resourceId);
        mToolbar.setOverflowIcon(ContextCompat.getDrawable(this, overFlowIcon.resourceId));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int action = intent.getIntExtra(AppStatus.KEY_HOME_ACTION, AppStatus.ACTION_BACK_TO_HOME);
        switch (action) {
            case AppStatus.ACTION_KICK_OUT:
                break;
            case AppStatus.ACTION_LOGOUT:
                break;
            case AppStatus.ACTION_RESTART_APP:
                protectApp();
                break;
            case AppStatus.ACTION_BACK_TO_HOME:
                break;
            default:
        }
    }

    @Override
    protected void protectApp() {
        startActivity(new Intent(this, SplashActivity.class));
        finish();
    }

    private void requestPermission() {
        if (PermissionsChecker.checkPermissions(this, PermissionsChecker.storagePermissions)) {
            initErrorLogDetactor();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionsChecker.REQUEST_STORAGE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initErrorLogDetactor();
            } else {
                showShortToast("权限获取失败");
            }
        }
    }

    private void initErrorLogDetactor() {
        UncaughtException mUncaughtException = UncaughtException.getInstance();
        mUncaughtException.init(this, getString(R.string.app_name));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}
