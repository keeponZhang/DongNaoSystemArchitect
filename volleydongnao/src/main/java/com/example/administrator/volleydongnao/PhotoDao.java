package com.example.administrator.volleydongnao;

import com.example.administrator.volleydongnao.db.BaseDao;

import java.util.List;

/**
 * Created by david on 20/1/2017.
 */

public class PhotoDao  extends BaseDao<Photo>{

    @Override
    public List<Photo> query(String sql) {
        return null;
    }

    @Override
    public String createTable() {
        return "create table if not exists tb_photo(\n" +
                "                time TEXT,\n" +
                "                path TEXT,\n" +
                "                to_user TEXT\n" +
                "                )";
    }
}
