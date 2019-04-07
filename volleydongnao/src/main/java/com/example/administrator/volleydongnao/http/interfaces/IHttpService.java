package com.example.administrator.volleydongnao.http.interfaces;

/**
 * Created by Administrator on 2017/1/13 0013.
 */

import java.util.Map;

/**
 *获取网络(此接口重要，真正访问网络的在这个地方，所以需要url，请求参数，回调HttpListener)
 * 注意：真正访问网络的地方如果不是实现了runnable接口，也是在runnable接口里调用的
 */
public interface IHttpService {
    /**
     * 设置url
     * @param url
     */
    void setUrl(String url);

    /**
     * 执行获取网络
     */
    void excute();

    /**
     * 设置处理接口
     * @param httpListener
     */
    void setHttpListener(IHttpListener httpListener);

    /**
     * 设置请求参数
     * String  1
     * byte[]  2
     *
     */
    void setRequestData(byte[] requestData);

    void pause();

    /**
     *
     * 以下的方法是 额外添加的
     * 获取请求头的map
     * @return
     */
     Map<String,String> getHttpHeadMap();

     boolean cancle();

     boolean isCancle();

     boolean isPause();


}
