package com.example.administrator.volleydongnao.http;

import com.example.administrator.volleydongnao.http.interfaces.IDataListener;
import com.example.administrator.volleydongnao.http.interfaces.IHttpListener;
import com.example.administrator.volleydongnao.http.interfaces.IHttpService;

import java.util.concurrent.FutureTask;

/**
 * Created by Administrator on 2017/1/13 0013.
 */

public class Volley {
    /**
     *
     * @param <T>  请求参数类型(例如User)
     * @param <M>  响应参数类型
     *           暴露给调用层
     */
    public static <T,M> void sendRequest(T  requestInfo, String url,
                                         Class<M> response, IDataListener dataListener)
    {
        RequestHodler<T> requestHodler=new RequestHodler<>();
        requestHodler.setUrl(url);
        IHttpService httpService=new JsonHttpService();
        IHttpListener httpListener=new JsonDealListener<>(response,dataListener);
        requestHodler.setHttpService(httpService);
        requestHodler.setHttpListener(httpListener);
        //将请求参数赋值
        requestHodler.setRequestInfo(requestInfo);

        HttpTask<T> httpTask=new HttpTask<>(requestHodler);
        try {
            //将任务加入ThreadPoolManager的阻塞队列taskQuene
            ThreadPoolManager.getInstance().execte(new FutureTask<Object>(httpTask,null));
        } catch (InterruptedException e) {
            dataListener.onErro();
        }
    }

}
