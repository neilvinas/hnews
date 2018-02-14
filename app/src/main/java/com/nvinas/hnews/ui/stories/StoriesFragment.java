package com.nvinas.hnews.ui.stories;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.nvinas.hnews.R;
import com.nvinas.hnews.ui.comments.CommentsActivity;
import com.nvinas.hnews.common.listener.StoryItemClickListener;
import com.nvinas.hnews.common.listener.RecyclerViewScrollListener;
import com.nvinas.hnews.common.util.ActivityUtil;
import com.nvinas.hnews.common.util.CommonUtil;
import com.nvinas.hnews.data.Story;
import com.nvinas.hnews.ui.webview.WebViewActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.DaggerFragment;
import timber.log.Timber;

/**
 * Created by nvinas on 09/02/2018.
 */

public class StoriesFragment extends DaggerFragment implements StoriesContract.View, StoryItemClickListener {

    @BindView(R.id.rv_stories)
    RecyclerView rvStories;

    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;

    @BindView(R.id.text_stories_unavailable)
    TextView textStoriesUnavailable;

    @Inject
    StoriesContract.Presenter presenter;

    private StoriesAdapter storiesAdapter;
    private Unbinder unbinder;

    @Inject
    public StoriesFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stories, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        initUi();
        return rootView;
    }

    private void initUi() {
        Context context = getContext();
        if (context != null) {
            storiesAdapter = new StoriesAdapter(context, new ArrayList<>());
            storiesAdapter.setItemClickListener(this);
            LinearLayoutManager lm = new LinearLayoutManager(getContext());
            rvStories.setLayoutManager(lm);
            rvStories.addOnScrollListener(new RecyclerViewScrollListener(lm) {
                @Override
                public void onLoadMore(int page, int totalItemsCount) {
                    storiesAdapter.showLoadingIndicator();
                    presenter.loadStoriesInfo();
                }
            });
            rvStories.setAdapter(storiesAdapter);

            swipeRefresh.setOnRefreshListener(() -> presenter.loadStories());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.takeView(this);
    }

    @Override
    public void showStories(@Nullable List<Story> stories) {
        storiesAdapter.setShowProgressIndicator(false);
        if (stories == null) {
            showStoriesUnavailableError();
            return;
        }
        storiesAdapter.setStories(stories);
    }

    @Override
    public void onItemClick(int pos, Story story) {
        if (story == null) {
            Timber.d("Empty story");
            return;
        }
        showStory(story);
    }

    @Override
    public void onOpenStoryUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            Timber.d("Empty url");
            return;
        }
        showStoryWebView(url);
    }

    @Override
    public void onShareStory(Story story) {
    }

    @Override
    public void showStory(@NonNull Story story) {
        ActivityUtil.startActivity(getActivity(), CommentsActivity.class,
                new Intent().putExtra(CommonUtil.Constants.INTENT_KEY_STORY, story));
    }

    @Override
    public void showStoryWebView(@NonNull String url) {
        ActivityUtil.startActivity(getActivity(), WebViewActivity.class,
                new Intent().putExtra(CommonUtil.Constants.INTENT_KEY_URL, url));
    }

    @Override
    public void showErrorMessage(String errorMessage) {
        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showStoriesUnavailableError() {
        textStoriesUnavailable.setVisibility(View.VISIBLE);
    }

    @Override
    public void setProgressIndicator(boolean progress) {
        swipeRefresh.setRefreshing(progress);
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void onDestroy() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        presenter.dropView();
        super.onDestroy();
    }

    public static StoriesFragment newInstance() {
        return new StoriesFragment();
    }
}
