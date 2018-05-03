/*
 * Tencent is pleased to support the open source community by making  XiaoweiSDK Demo Codes available.
 *
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.tencent.aiaudio.alarm;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.xutils.DbManager;
import org.xutils.DbManager.DbOpenListener;
import org.xutils.DbManager.DbUpgradeListener;
import org.xutils.DbManager.TableCreateListener;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.db.table.TableEntity;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class AlarmDbManager implements DbUpgradeListener, DbOpenListener, TableCreateListener {
    private static final String TAG = AlarmDbManager.class.getSimpleName();

    private static final int DEF_DB_VERSION = 1;
    private static final String DB_NAME = "alarm_db";

    public static final String ALARM_ADD_ACTION = "ALARM_ADD_ACTION";
    public static final String ALARM_DEL_ACTION = "ALARM_DEL_ACTION";
    public static final String ALARM_UPDATE_ACTION = "ALARM_UPDATE_ACTION";

    private DbManager mDbManager;
    private volatile static AlarmDbManager INSTANCE;

    public static AlarmDbManager instance() {
        if (INSTANCE == null) {
            synchronized (AlarmDbManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AlarmDbManager();
                }
            }
        }

        return INSTANCE;
    }

    public AlarmDbManager() {
        DbManager.DaoConfig config = new DbManager.DaoConfig();
        config.setDbName(DB_NAME);
        config.setDbVersion(DEF_DB_VERSION);
        config.setAllowTransaction(true);
        config.setDbUpgradeListener(this);
        config.setDbOpenListener(this);
        config.setTableCreateListener(this);

        mDbManager = x.getDb(config);
    }

    public void release() {
        if (mDbManager == null) {
            Log.d(TAG, "release() mDbManager == null.");
            return;
        }

        DbManager.DaoConfig config = mDbManager.getDaoConfig();
        if (config != null) {
            config.setDbUpgradeListener(null);
            config.setDbOpenListener(null);
            config.setTableCreateListener(null);
        }

        try {
            mDbManager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addAlarmItem(SkillAlarmBean bean, OnOperationFinishListener listener) {
        if (bean == null) {
            Log.d(TAG, "addAlarmItem() bean == null.");
            return;
        }

        if (TextUtils.isEmpty(bean.getKey())) {
            Log.d(TAG, "TextUtils.isEmpty(bean.getKey()).");
            return;
        }

        Log.d(TAG, String.format("addAlarmItem() bean:%s", bean.toString()));

        AddAlarmAsyncTask addAlarmAsyncTask = new AddAlarmAsyncTask(listener);
        addAlarmAsyncTask.execute(bean);
    }

    public void deleteAlarmItem(SkillAlarmBean bean, OnOperationFinishListener listener) {
        if (bean == null) {
            Log.d(TAG, "deleteAlarmItem() bean == null.");
            return;
        }

        Log.d(TAG, String.format("deleteAlarmItem() bean:%s", bean.toString(), new Exception()));

        DeleteAlarmAsyncTask deleteAlarmAsyncTask = new DeleteAlarmAsyncTask(listener);
        deleteAlarmAsyncTask.execute(bean);
    }

    public void updateAlarmItem(SkillAlarmBean bean, OnOperationFinishListener listener) {
        if (bean == null) {
            Log.d(TAG, "updateAlarmItem() bean == null.");
            return;
        }

        Log.d(TAG, String.format("updateAlarmItem() bean:%s", bean.toString()));

        UpdateAlarmAsyncTask updateAlarmAsyncTask = new UpdateAlarmAsyncTask(listener);
        updateAlarmAsyncTask.execute(bean);
    }

    public void queryAllAlarmItem(OnQueryAllAlarmListener listener) {
        QueryAllAlarmAsyncTask queryAllAlarmAsyncTask = new QueryAllAlarmAsyncTask(listener);
        queryAllAlarmAsyncTask.execute();
    }

    @Override
    public void onUpgrade(DbManager dbManager, int i, int i1) {
        Log.d(TAG, String.format("onUpgrade(i=%s,i1=%s).", i, i1));
    }

    @Override
    public void onDbOpened(DbManager dbManager) {
        Log.d(TAG, "onDbOpened().");
    }

    @Override
    public void onTableCreated(DbManager dbManager, TableEntity<?> tableEntity) {
        Log.d(TAG, "onTableCreated().");
    }

    private class QueryAllAlarmAsyncTask extends AsyncTask<Void, Void, List<SkillAlarmBean>> {
        private OnQueryAllAlarmListener mOnQueryAllAlarmListener;

        public QueryAllAlarmAsyncTask(OnQueryAllAlarmListener listener) {
            this.mOnQueryAllAlarmListener = listener;
        }

        @Override
        protected List<SkillAlarmBean> doInBackground(Void... params) {
            if (mDbManager == null) {
                Log.d(TAG, "AddAlarmAsyncTask mDbManager == null.");
                return null;
            }

            try {
                return mDbManager.findAll(SkillAlarmBean.class);
            } catch (DbException e) {
                e.printStackTrace();
            }

            return new ArrayList<>();
        }

        @Override
        protected void onPostExecute(List<SkillAlarmBean> skillAlarmBeen) {
            if (mOnQueryAllAlarmListener == null) {
                Log.d(TAG, "mOnQueryAllAlarmListener == null.");
            } else {
                mOnQueryAllAlarmListener.onQueryAllAlarm(skillAlarmBeen);
            }
        }
    }

    private class AddAlarmAsyncTask extends AsyncTask<SkillAlarmBean, Void, SkillAlarmBean> {
        private OnOperationFinishListener mOnOperationFinishListener;

        public AddAlarmAsyncTask(OnOperationFinishListener listener) {
            this.mOnOperationFinishListener = listener;
        }

        @Override
        protected SkillAlarmBean doInBackground(SkillAlarmBean... beans) {
            if (mDbManager == null) {
                Log.d(TAG, "AddAlarmAsyncTask mDbManager == null.");
                return null;
            }

            if (beans == null || beans.length == 0) {
                Log.d(TAG, "AddAlarmAsyncTask beans == null || beans.length == 0.");
                return null;
            }

            SkillAlarmBean bean = beans[0];
            try {
                mDbManager.saveOrUpdate(bean);
            } catch (DbException e) {
                e.printStackTrace();
            }

            return bean;
        }

        @Override
        protected void onPostExecute(SkillAlarmBean bean) {
            if (mOnOperationFinishListener == null) {
                //Log.d(TAG, "mOnOperationFinishListener == null.");
            } else {
                mOnOperationFinishListener.onOperationFinish(bean, ALARM_ADD_ACTION);
            }
        }
    }

    private class DeleteAlarmAsyncTask extends AsyncTask<SkillAlarmBean, Void, SkillAlarmBean> {
        private OnOperationFinishListener mOnOperationFinishListener;

        public DeleteAlarmAsyncTask(OnOperationFinishListener listener) {
            this.mOnOperationFinishListener = listener;
        }

        @Override
        protected SkillAlarmBean doInBackground(SkillAlarmBean... beans) {
            if (mDbManager == null) {
                Log.d(TAG, "DeleteAlarmAsyncTask mDbManager == null.");
                return null;
            }

            if (beans == null || beans.length == 0) {
                Log.d(TAG, "DeleteAlarmAsyncTask beans == null || beans.length == 0.");
                return null;
            }

            SkillAlarmBean bean = beans[0];
            try {
                mDbManager.delete(SkillAlarmBean.class, WhereBuilder.b("key", "=", bean.getKey()));
            } catch (DbException e) {
                e.printStackTrace();
            }

            return bean;
        }

        @Override
        protected void onPostExecute(SkillAlarmBean bean) {
            if (mOnOperationFinishListener == null) {
                //Log.d(TAG, "mOnOperationFinishListener == null.");
            } else {
                mOnOperationFinishListener.onOperationFinish(bean, ALARM_DEL_ACTION);
            }
        }
    }

    private class UpdateAlarmAsyncTask extends AsyncTask<SkillAlarmBean, Void, SkillAlarmBean> {
        private OnOperationFinishListener mOnOperationFinishListener;

        public UpdateAlarmAsyncTask(OnOperationFinishListener listener) {
            this.mOnOperationFinishListener = listener;
        }

        @Override
        protected SkillAlarmBean doInBackground(SkillAlarmBean... beans) {
            if (mDbManager == null) {
                Log.d(TAG, "DeleteAlarmAsyncTask mDbManager == null.");
                return null;
            }

            if (beans == null || beans.length == 0) {
                Log.d(TAG, "DeleteAlarmAsyncTask beans == null || beans.length == 0.");
                return null;
            }

            SkillAlarmBean bean = beans[0];

            try {
                mDbManager.update(bean);
            } catch (DbException e) {
                e.printStackTrace();
            }

            return bean;
        }

        @Override
        protected void onPostExecute(SkillAlarmBean bean) {
            if (mOnOperationFinishListener == null) {
                //Log.d(TAG, "mOnOperationFinishListener == null.");
            } else {
                mOnOperationFinishListener.onOperationFinish(bean, ALARM_UPDATE_ACTION);
            }
        }
    }
}
