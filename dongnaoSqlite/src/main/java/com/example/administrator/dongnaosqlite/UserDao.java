package com.example.administrator.dongnaosqlite;

import com.example.administrator.dongnaosqlite.db.BaseDao;

import java.util.List;

/**
 * Created by Administrator on 2017/1/9 0009.
 */

public class UserDao extends BaseDao {
    @Override
    protected String createTable() {

        return "create table if not exists tb_user(user_Id int,sex int ,name varchar(20),password varchar(10))";
    }


    @Override
    public List query(String sql) {


        return null;
    }
}
