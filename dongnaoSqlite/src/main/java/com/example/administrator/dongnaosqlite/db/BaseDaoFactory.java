package com.example.administrator.dongnaosqlite.db;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

/**
 * Created by Administrator on 2017/1/9 0009.
 */

public class BaseDaoFactory {
    private String sqliteDatabasePath;

    private SQLiteDatabase sqLiteDatabase;

    private static  BaseDaoFactory instance=new BaseDaoFactory();
    public BaseDaoFactory()
    {
        sqliteDatabasePath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/teacher.db";
        openDatabase();
    }
        public  synchronized  <T extends  BaseDao<M>,M> T
        getDataHelper(Class<T> clazz,Class<M> entityClass)
        {
            BaseDao baseDao=null;
            try {
                baseDao=clazz.newInstance();
                //跟数据库底层打交道需要sqLiteDatabase
                baseDao.init(entityClass,sqLiteDatabase);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            return (T) baseDao;
        }
        //在外部存储卡创建一个db文件，所以不用使用SQLiteOpenHelper
    private void openDatabase() {
        this.sqLiteDatabase=SQLiteDatabase.openOrCreateDatabase(sqliteDatabasePath,null);
    }

    public  static  BaseDaoFactory getInstance()
    {
        return instance;
    }
}
