package com.jeez.guanpj.jreadhub;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.support.multidex.MultiDex;
import android.text.TextUtils;

import com.jakewharton.threetenabp.AndroidThreeTen;
import com.jeez.guanpj.jreadhub.data.AppStatusTracker;
import com.jeez.guanpj.jreadhub.di.component.AppComponent;
import com.jeez.guanpj.jreadhub.di.component.DaggerAppComponent;
import com.jeez.guanpj.jreadhub.di.module.AppModule;
import com.jeez.guanpj.jreadhub.module.main.MainActivity;
import com.meituan.android.walle.WalleChannelReader;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.tinker.TinkerApplicationLike;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReadhubApplicationLike extends TinkerApplicationLike {
    private static Application sInstance;
    private static volatile AppComponent appComponent;

    public ReadhubApplicationLike(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent);
    }

    public static synchronized Application getInstance() {
        return sInstance;
    }

    public static synchronized AppComponent getAppComponent() {
        if (appComponent == null) {
            appComponent = DaggerAppComponent.builder()
                    .appModule(new AppModule(sInstance))
                    .build();
        }
        return appComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = getApplication();
        if (LeakCanary.isInAnalyzerProcess(getApplication())) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }

        initBugly();

        LeakCanary.install(getApplication());
        AppStatusTracker.init(getApplication());
        AndroidThreeTen.init(getApplication());
        //CrashHandler.getInstance().init(getApplicationContext());
    }

    private void initBugly() {
        String packageName = getApplication().getPackageName();
        String processName = getProcessName(android.os.Process.myPid());
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplication());
        strategy.setUploadProcess(processName == null || processName.equals(packageName));

        Beta.storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        Beta.autoCheckUpgrade = false;
        Beta.canShowUpgradeActs.add(MainActivity.class);
        String channel = WalleChannelReader.getChannel(getApplication());
        strategy.setAppChannel(channel);
        Bugly.init(getApplication(), "c16799f8bc", false, strategy);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);
        // you must install multiDex whatever tinker is installed!
        MultiDex.install(base);

        // 安装tinker
        Beta.installTinker(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Beta.unInit();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void registerActivityLifecycleCallback(
            Application.ActivityLifecycleCallbacks callbacks) {
        getApplication().registerActivityLifecycleCallbacks(callbacks);
    }

    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }
}
