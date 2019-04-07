package com.example.administrator.dongnaosqlite.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.example.administrator.dongnaosqlite.db.annotion.DbFiled;
import com.example.administrator.dongnaosqlite.db.annotion.DbTable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2017/1/9 0009.
 */

public abstract class BaseDao<T> implements  IBaseDao<T> {
    /**]
     * 持有数据库操作类的引用
     */
    private SQLiteDatabase database;
    /**
     * 保证实例化一次
     */
    private boolean isInit=false;
    /**
     * 持有操作数据库表所对应的java类型
     * User
     */
    private Class<T> entityClass;
    /**
     * 维护这表名与成员变量名的映射关系（之后用来生成表名和实体成员变量值得关系，在getValues方法）
     * key---》表名
     * value --》Field
     * class  methoFiled
     * {
     *     String  数据表列名
     *     Filed  fild 实体类字段
     * }Field的 字段注解或者filed的变量名就是表列名，Field在getvalues有用
     */
    private HashMap<String,Field> cacheMap;

    private String tableName;
    /**
     * @param entity
     * @param sqLiteDatabase
     * @return
     * 实例化一次
     */
    protected synchronized boolean init(Class<T> entity, SQLiteDatabase sqLiteDatabase)
    {
        if(!isInit)
        {
            entityClass=entity;
            database=sqLiteDatabase;
            if (entity.getAnnotation(DbTable.class)==null)
            {
               tableName=entity.getClass().getSimpleName();
            }else
            {
                tableName=entity.getAnnotation(DbTable.class).value();
            }
              if(!database.isOpen())
              {
                  return  false;
              }
                if(!TextUtils.isEmpty(createTable()))
                {
                    //把创建数据表的操作放在了这里
                    database.execSQL(createTable());
                }
              cacheMap=new HashMap<>();
              initCacheMap();

            isInit=true;
        }
        return  isInit;
    }

    /**
     * 维护映射关系
     */
    private void initCacheMap() {
        /*
        第一条数据  查0个数据
         */
        String sql="select * from "+this.tableName+" limit 1 , 0";
        Cursor cursor=null;
        try {
            cursor=database.rawQuery(sql,null);
            /**
             * 表的列名数组
             */
            String[] columnNames=cursor.getColumnNames();
            /**
             * 拿到Filed数组
             */
            Field[] colmunFields=entityClass.getFields();
            for(Field filed:colmunFields)
            {
                 filed.setAccessible(true);
            }
            /**
             * 开始找对应关系
             */
            for(String colmunName:columnNames)
            {
                /**
                 * 如果找到对应的Filed就赋值给他
                 * User
                 */
                 Field colmunFiled=null;
                for (Field field:colmunFields)
                {
                    String fieldName=null;
                    if(field.getAnnotation(DbFiled.class)!=null)
                    {
                         fieldName=field.getAnnotation(DbFiled.class).value();
                    }else
                    {
                        fieldName =field.getName();
                    }
                    /**
                     * 如果表的列名 等于了  成员变量的注解名字
                     */
                    if(colmunName.equals(fieldName))
                    {
                        colmunFiled= field;
                        break;
                    }
                }
                //找到了对应关系
                if(colmunFiled!=null)
                {
                    cacheMap.put(colmunName,colmunFiled);
                }
            }

        }catch (Exception e)
        {

        }finally {
            cursor.close();
        }

    }

    @Override
    public int delete(T where) {
        Map map=getValues(where);

        Condition condition=new Condition(map);
        /**
         * id=1 数据
         * id=?      new String[]{String.value(1)}
         */
        int reslut=database.delete(tableName,condition.getWhereClause(),condition.getWhereArgs());
        return reslut;
    }

    @Override
    public List<T> query(T where) {
        return query(where,null,null,null);
    }

    @Override
    public List<T> query(T where, String orderBy, Integer startIndex, Integer limit) {
        Map map=getValues(where);

        String limitString=null;
        if(startIndex!=null&&limit!=null)
        {
            limitString=startIndex+" , "+limit;
        }

        Condition condition=new Condition(map);
        Cursor cursor=database.query(tableName,null,condition.getWhereClause()
                ,condition.getWhereArgs(),null,null,orderBy,limitString);
        List<T> result=getResult(cursor,where);
        cursor.close();
        return result;
    }

    private List<T> getResult(Cursor cursor, T where) {
        ArrayList list=new ArrayList();

        Object item;
        while (cursor.moveToNext())
        {
            try {
                item=where.getClass().newInstance();
                /**
                 * 列名  name
                 * 成员变量名  Filed;
                 */
                Iterator iterator=cacheMap.entrySet().iterator();
                while (iterator.hasNext())
                {
                    Map.Entry entry= (Map.Entry) iterator.next();
                    /**
                     * 得到列名
                     */
                    String colomunName= (String) entry.getKey();
                    /**
                     * 然后以列名拿到  列名在游标的位子
                     */
                    Integer colmunIndex=cursor.getColumnIndex(colomunName);

                    Field field= (Field) entry.getValue();

                    Class type=field.getType();
                    if(colmunIndex!=-1)
                    {
                        if(type==String.class)
                        {
                            //反射方式赋值
                            field.set(item,cursor.getString(colmunIndex));
                        }else if(type==Double.class)
                        {
                            field.set(item,cursor.getDouble(colmunIndex));
                        }else  if(type==Integer.class)
                        {
                            field.set(item,cursor.getInt(colmunIndex));
                        }else if(type==Long.class)
                        {
                            field.set(item,cursor.getLong(colmunIndex));
                        }else  if(type==byte[].class)
                        {
                            field.set(item,cursor.getBlob(colmunIndex));
                            /*
                            不支持的类型
                             */
                        }else {
                            continue;
                        }
                    }

                }
                list.add(item);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        return list;
    }

    /**
     * 讲 map 转换成ContentValues
     * @param map
     * @return
     */
    private ContentValues getContentValues(Map<String, String> map) {
        ContentValues contentValues=new ContentValues();
        Set keys=map.keySet();
        Iterator<String> iterator=keys.iterator();
        while (iterator.hasNext())
        {
            String key=iterator.next();
            String value=map.get(key);
            if(value!=null)
            {
                contentValues.put(key,value);
            }
        }

       return contentValues;
    }

    /**
     * 注意：不直接转换成ContentValues，是因为这个方法还有其他地方用到
     * 将对象拥有的成员变量
     * 转换成  表的列名  ---》成员变量的值
     * 如  tb_name  ----> "张三"
     * 这样的map集合
     * User
     * name  "zhangsn"
     * @param entity
     * @return
     */
    private Map<String, String> getValues(T entity) {
        HashMap<String,String> result=new HashMap<>();
        Iterator<Field> filedsIterator=cacheMap.values().iterator();
        /**
         * 循环遍历 映射map的  Filed
         */
        while (filedsIterator.hasNext())
        {
            //实体类Field字段，Field字段注解有值，字段注解值是数据表列名，否则变量名就是数据表列名
            Field colmunToFiled=filedsIterator.next();
            String cacheKey=null;
            String cacheValue=null;
            if(colmunToFiled.getAnnotation(DbFiled.class)!=null)
            {
                cacheKey=colmunToFiled.getAnnotation(DbFiled.class).value();
            }else
            {
                cacheKey=colmunToFiled.getName();
            }
            try {
                //防止取值空指针
                if(null==colmunToFiled.get(entity))
                {
                    Log.e("TAG", "getValues colmunToFiled : "+colmunToFiled+ " ==null" );
                    continue;
                }
                cacheValue=colmunToFiled.get(entity).toString();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            result.put(cacheKey,cacheValue);
        }

        return result;
    }
    @Override
    public Long insert(T entity) {
        Map<String,String> map=getValues(entity);
        ContentValues values=getContentValues(map);
        //这里主要是tableName的获取和ContentValues的生成，表名可以通过实体类的注解值获得
        //直接反射实体类，拼接成ContentValues，一是性能低，每次都反射，而是实体类变量名不一定是数据库表列名
        Long result =database.insert(tableName,null,values);
        return result;
    }
    @Override
    public int update(T entity, T where) {
        int reslut=-1;
        Map values=getValues(entity);
        /**
         * 将条件对象 转换map
         */
        Map whereClause=getValues(where);

        Condition condition=new Condition(whereClause);
        ContentValues contentValues=getContentValues(values);
        //values 要更新的列（键值对）,查出来多条的话会全部改掉
        reslut=database.update(tableName,contentValues,condition.getWhereClause(),condition.getWhereArgs());
        return reslut;
    }

    /**
     * 封装修改语句
     *
     */
    class Condition
    {
        /**
         * 查询条件
         * name=? && password =?
         */
        private String whereClause;

        private  String[] whereArgs;
        public Condition(Map<String ,String> whereClause) {
            ArrayList list=new ArrayList();
            StringBuilder stringBuilder=new StringBuilder();

            stringBuilder.append(" 1=1 ");
            Set keys=whereClause.keySet();
            Iterator iterator=keys.iterator();
            while (iterator.hasNext())
            {
                String key= (String) iterator.next();
                String value=whereClause.get(key);

                if (value!=null)
                {
                    /*
                    拼接条件查询语句
                    1=1 and name =? and password=?
                     */
                    stringBuilder.append(" and "+key+" =?");
                    /**
                     * ？----》value
                     */
                    list.add(value);
                }
            }
            this.whereClause=stringBuilder.toString();
            this.whereArgs= (String[]) list.toArray(new String[list.size()]);

        }

        public String[] getWhereArgs() {
            return whereArgs;
        }

        public String getWhereClause() {
            return whereClause;
        }
    }
    /**
     * 创建表
     * @return
     */
    protected  abstract  String createTable();
}
