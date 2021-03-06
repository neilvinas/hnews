package com.nvinas.hnews.data.source;

import android.support.annotation.NonNull;

import com.nvinas.hnews.common.util.RxUtil;
import com.nvinas.hnews.data.Comment;
import com.nvinas.hnews.data.Story;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;

/**
 * Created by nvinas on 10/02/2018.
 */
@Singleton
public class StoryRepository implements StoryDataSource {

    @NonNull
    private final StoryDataSource remoteDataSource;

    private Observable<List<Integer>> cachedStoryIds;

    private boolean cacheIsDirty = false;

    @Inject
    public StoryRepository(@NonNull StoryDataSource remoteDataSource) {
        this.remoteDataSource = remoteDataSource;
    }

    @Override
    public Observable<List<Integer>> getTopStoryIds() {
        if (!cacheIsDirty && cachedStoryIds != null) {
            return cachedStoryIds;
        }
        cachedStoryIds = remoteDataSource.getTopStoryIds()
                .compose(RxUtil.applySchedulers());
        return cachedStoryIds;
    }

    @Override
    public Observable<Story> getStory(int id) {
        return remoteDataSource.getStory(id)
                .compose(RxUtil.applySchedulers());
    }

    @Override
    public Observable<Comment> getComment(int id) {
        return remoteDataSource.getComment(id)
                .compose(RxUtil.applySchedulers());
    }

    @Override
    public void refreshStoryIds() {
        cacheIsDirty = true;
    }
}
