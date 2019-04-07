package com.example.administrator.dongnaosqlite;

import com.example.administrator.dongnaosqlite.db.annotion.DbFiled;
import com.example.administrator.dongnaosqlite.db.annotion.DbTable;

/**
 * Created by Administrator on 2017/1/9 0009.
 */
@DbTable("tb_down")
public class DownFile {
    public DownFile(String time, String path) {
        this.time = time;
        this.path = path;
    }

    public DownFile( ) {

    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     *
     */
    @DbFiled("tb_time")
    public String time;

    @DbFiled("tb_path")
    public String path;
}
