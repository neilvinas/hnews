package com.nvinas.hnews.ui.comments;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.nvinas.hnews.common.base.BasePresenter;
import com.nvinas.hnews.common.base.BaseView;
import com.nvinas.hnews.data.Comment;
import com.nvinas.hnews.data.Story;

import java.util.List;

/**
 * Created by nvinas on 10/02/2018.
 */

public interface CommentsContract {

    interface View extends BaseView {

        void showStoryInfo(Story story);

        void showStoryWebview(@NonNull String url);

        void showComment(@NonNull Comment comment, int pos);

        boolean isActive();
    }

    interface Presenter extends BasePresenter<View> {

        void refreshComments();

        void loadComments(List<Integer> ids);
    }
}
