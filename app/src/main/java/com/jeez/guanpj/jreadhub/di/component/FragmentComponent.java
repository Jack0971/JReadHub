package com.jeez.guanpj.jreadhub.di.component;

import android.app.Activity;

import com.jeez.guanpj.jreadhub.di.module.FragmentModule;
import com.jeez.guanpj.jreadhub.di.scope.FragmentScope;
import com.jeez.guanpj.jreadhub.module.common.CommonListFragment;
import com.jeez.guanpj.jreadhub.module.settting.SettingFragment;
import com.jeez.guanpj.jreadhub.module.star.news.StarCommonListFragment;
import com.jeez.guanpj.jreadhub.module.star.search.SearchFragment;
import com.jeez.guanpj.jreadhub.module.star.topic.StarTopicFragment;
import com.jeez.guanpj.jreadhub.module.topic.TopicFragment;
import com.jeez.guanpj.jreadhub.module.topic.detail.TopicDetailFragment;
import com.jeez.guanpj.jreadhub.module.topic.instant.InstantReadFragment;
import com.jeez.guanpj.jreadhub.module.web.WebViewFragment;

import dagger.Component;

@FragmentScope
@Component(dependencies = AppComponent.class, modules = FragmentModule.class)
public interface FragmentComponent {
    Activity getActivity();

    void inject(TopicFragment fragment);

    void inject(InstantReadFragment fragment);

    void inject(TopicDetailFragment fragment);

    void inject(CommonListFragment fragment);

    void inject(SettingFragment fragment);

    void inject(StarTopicFragment fragment);

    void inject(StarCommonListFragment fragment);

    void inject(WebViewFragment fragment);

    void inject(SearchFragment fragment);
}
