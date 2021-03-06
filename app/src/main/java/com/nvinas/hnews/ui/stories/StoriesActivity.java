package com.nvinas.hnews.ui.stories;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.nvinas.hnews.R;
import com.nvinas.hnews.common.listener.ActivityIdlingResourceListener;
import com.nvinas.hnews.common.listener.RecyclerViewScrollListener;
import com.nvinas.hnews.common.listener.StoryItemClickListener;
import com.nvinas.hnews.common.util.ActivityUtil;
import com.nvinas.hnews.common.util.CommonUtil;
import com.nvinas.hnews.data.Story;
import com.nvinas.hnews.ui.comments.CommentsActivity;
import com.nvinas.hnews.ui.webview.WebViewActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.DaggerAppCompatActivity;
import timber.log.Timber;

public class StoriesActivity extends DaggerAppCompatActivity
        implements StoriesContract.View, StoryItemClickListener, ActivityIdlingResourceListener {

    @BindView(R.id.rv_stories)
    public RecyclerView rvStories;

    @BindView(R.id.swipe_refresh)
    public SwipeRefreshLayout swipeRefresh;

    @BindView(R.id.text_stories_unavailable)
    TextView textStoriesUnavailable;

    @Inject
    StoriesContract.Presenter presenter;

    private StoriesAdapter storiesAdapter;
    private Unbinder unbinder;
    private boolean isIdle = true;
    private boolean isLoadingMoreItems = false;

    private static final String KEY_CURRENT_PAGE = "key_current_page";
    private ArrayList<Story> stories = new ArrayList<>();
    private static final String KEY_STORIES = "key_stories";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stories);

        unbinder = ButterKnife.bind(this);

        initUi();

        presenter.takeView(this);
        if (savedInstanceState == null) {
            presenter.loadStories(true);
        } else {
            presenter.setNextPage(savedInstanceState.getInt(KEY_CURRENT_PAGE) + 1);
            showStories(savedInstanceState.getParcelableArrayList(KEY_STORIES));
        }
    }

    private void initUi() {
        storiesAdapter = new StoriesAdapter(this, new ArrayList<>());
        storiesAdapter.setItemClickListener(this);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        rvStories.setLayoutManager(lm);
        rvStories.addOnScrollListener(new RecyclerViewScrollListener(lm) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                Timber.d("is loading more items? %s", isLoadingMoreItems);
                if (!isLoadingMoreItems) {
                    storiesAdapter.showLoadingIndicator();
                    presenter.loadStories(false);
                    isLoadingMoreItems = true;
                }
            }
        });
        rvStories.setAdapter(storiesAdapter);
        swipeRefresh.setOnRefreshListener(() -> {
            presenter.refreshStories();
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList(KEY_STORIES, stories);
        outState.putInt(KEY_CURRENT_PAGE, stories.size() / CommonUtil.Constants.PAGE_STORY_SIZE);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void showStories(@Nullable List<Story> stories) {
        storiesAdapter.setShowProgressIndicator(false);
        isLoadingMoreItems = false;
        if (stories == null) {
            showStoriesUnavailableError();
            return;
        }
        this.stories = new ArrayList<>(stories);
        storiesAdapter.setStories(stories);
    }

    @Override
    public void showMoreStories(@Nullable List<Story> stories) {
        storiesAdapter.setShowProgressIndicator(false);
        isLoadingMoreItems = false;
        if (stories == null) {
            showStoriesUnavailableError();
            return;
        }
        this.stories.addAll(stories);
        storiesAdapter.addStories(stories);
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
        ActivityUtil.startActivity(this, CommentsActivity.class,
                new Intent().putExtra(CommonUtil.Constants.INTENT_KEY_STORY, story));
    }

    @Override
    public void showStoryWebView(@NonNull String url) {
        ActivityUtil.startActivity(this, WebViewActivity.class,
                new Intent().putExtra(CommonUtil.Constants.INTENT_KEY_URL, url));
    }

    @Override
    public void showErrorMessage(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
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
        return !isFinishing();
    }

    @Override
    public void onDestroy() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        presenter.dropView();
        super.onDestroy();
    }

    @Override
    public boolean isIdle() {
        return isIdle;
    }

    @Override
    public void setIdleStatus(boolean isIdle) {
        this.isIdle = isIdle;
    }
}
