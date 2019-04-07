package com.example.administrator.volleydongnao.http.download.interfaces;

import com.example.administrator.volleydongnao.http.interfaces.IHttpListener;
import com.example.administrator.volleydongnao.http.interfaces.IHttpService;

/**
 * Created by Administrator on 2017/1/16 0016.
 * 下载监听比较多，这里对IHttpListener进行拓展
 */
public interface IDownLitener  extends IHttpListener{

    void setHttpServive(IHttpService  httpServive);


    void  setCancleCalle();


    void  setPuaseCallble();

}
