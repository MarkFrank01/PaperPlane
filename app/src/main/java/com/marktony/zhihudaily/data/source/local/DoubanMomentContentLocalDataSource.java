package com.marktony.zhihudaily.data.source.local;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.marktony.zhihudaily.data.DoubanMomentContent;
import com.marktony.zhihudaily.data.source.datasource.DoubanMomentContentDataSource;
import com.marktony.zhihudaily.database.AppDatabase;
import com.marktony.zhihudaily.database.DatabaseCreator;

/**
 * Created by lizhaotailang on 2017/5/25.
 */

public class DoubanMomentContentLocalDataSource implements DoubanMomentContentDataSource {

    @Nullable
    private static DoubanMomentContentLocalDataSource INSTANCE = null;

    @Nullable
    private AppDatabase mDb = null;

    private DoubanMomentContentLocalDataSource(@NonNull Context context) {
        DatabaseCreator creator = DatabaseCreator.getInstance();
        if(!creator.isDatabaseCreated()) {
            creator.createDb(context);
        }
        mDb = creator.getDatabase();
    }

    public static DoubanMomentContentLocalDataSource getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new DoubanMomentContentLocalDataSource(context);
        }
        return INSTANCE;
    }

    @Override
    public void getDoubanMomentContent(int id, @NonNull LoadDoubanMomentContentCallback callback) {
        if (mDb == null) {
            callback.onDataNotAvailable();
            return;
        }

        new AsyncTask<Void, Void, DoubanMomentContent>() {

            @Override
            protected DoubanMomentContent doInBackground(Void... voids) {
                return mDb.doubanMomentContentDao().queryContentById(id);
            }

            @Override
            protected void onPostExecute(DoubanMomentContent content) {
                super.onPostExecute(content);
                if (content == null) {
                    callback.onDataNotAvailable();
                } else {
                    callback.onContentLoaded(content);
                }
            }
        }.execute();
    }

    @Override
    public void favorite(int id, boolean favorite) {
        if (mDb == null) {
            mDb = DatabaseCreator.getInstance().getDatabase();
        }

        if (mDb != null) {
            new Thread(() -> {
                DoubanMomentContent tmp = mDb.doubanMomentContentDao().queryContentById(id);
                tmp.setFavorite(favorite);
                mDb.doubanMomentContentDao().update(tmp);
            }).start();
        }
    }

    @Override
    public void saveContent(@NonNull DoubanMomentContent content) {
        if (mDb != null) {
            new Thread(() -> {
                mDb.beginTransaction();
                try {
                    mDb.doubanMomentContentDao().insert(content);
                    mDb.setTransactionSuccessful();
                } finally {
                    mDb.endTransaction();
                }
            }).start();
        }
    }
}