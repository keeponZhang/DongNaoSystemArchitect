package com.example.administrator.volleydongnao.http;

import com.alibaba.fastjson.JSON;
import com.example.administrator.volleydongnao.http.interfaces.IHttpListener;
import com.example.administrator.volleydongnao.http.interfaces.IHttpService;

import java.util.concurrent.FutureTask;

/**
 * Created by Administrator on 2017/1/13 0013.
 */

/**
 * 网络请求可以看哪个类实现了runnable，run方法是个入口，run方法不是真正发生网络请求的地方，也是个发起点，一般会持有真正访问网络的引用，这里是IHttpService
 * @param <T>
 */
//T 请求参数实体类型（例如User)
public class HttpTask<T> implements Runnable {
    private IHttpService httpService;
    private FutureTask futureTask;
    public HttpTask(RequestHodler<T> requestHodler)
    {
        httpService=requestHodler.getHttpService();
        //从requestHolder拿到getHttpListener，url给httpService设置进去
        httpService.setHttpListener(requestHodler.getHttpListener());
        httpService.setUrl(requestHodler.getUrl());
        //增加方法
        IHttpListener httpListener=requestHodler.getHttpListener();

        //设置断点下载信息，httpListener中的DownloadItemInfo中获得，DownloadItemInfo初始化在HttpTask初始化之前，但是这里只是设置到了httpService的map里面，在execute方法中还要拼接
        //之所以在这里设置，因为HttpTask是同一的，下载跟普通网络请求都走这里
        httpListener.addHttpHeader(httpService.getHttpHeadMap());
        try {
            T request=requestHodler.getRequestInfo();
            if(request!=null)
            {
            	//把实体类型转换成jsonString
                String requestInfo= JSON.toJSONString(request);
                httpService.setRequestData(requestInfo.getBytes("UTF-8"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        httpService.excute();
    }
    /**
     * 新增方法
     */
    public void start()
    {
        futureTask=new FutureTask(this,null);
        try {
            ThreadPoolManager.getInstance().execte(futureTask);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * 新增方法
     */
    public  void pause()
    {
        httpService.pause();
        if(futureTask!=null)
        {
            ThreadPoolManager.getInstance().removeTask(futureTask);
        }

    }
}
