package com.example.administrator.volleydongnao.http.download;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.administrator.volleydongnao.db.BaseDaoFactory;
import com.example.administrator.volleydongnao.http.HttpTask;
import com.example.administrator.volleydongnao.http.RequestHodler;
import com.example.administrator.volleydongnao.http.download.dao.DownLoadDao;
import com.example.administrator.volleydongnao.http.download.enums.DownloadStatus;
import com.example.administrator.volleydongnao.http.download.enums.DownloadStopMode;
import com.example.administrator.volleydongnao.http.download.enums.Priority;
import com.example.administrator.volleydongnao.http.download.interfaces.IDownloadCallable;
import com.example.administrator.volleydongnao.http.download.interfaces.IDownloadServiceCallable;
import com.example.administrator.volleydongnao.http.interfaces.IHttpListener;
import com.example.administrator.volleydongnao.http.interfaces.IHttpService;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Administrator on 2017/1/16 0016.
 */

public class DownFileManager implements IDownloadServiceCallable {
    private static final String TAG ="dongnao" ;
    //    private  static
    private byte[] lock=new byte[0];
    DownLoadDao downLoadDao= BaseDaoFactory.getInstance().
            getDataHelper(DownLoadDao.class,DownloadItemInfo.class);
    java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    /**
     * 观察者模式
     */
    private final List<IDownloadCallable> applisteners = new CopyOnWriteArrayList<IDownloadCallable>();

    /**
     * 正在下载的所有任务
     */
    private static List<DownloadItemInfo> downloadFileTaskList = new CopyOnWriteArrayList();

    Handler handler=new Handler(Looper.getMainLooper());


    public int download(String url)
    {
        String[] preFix=url.split("/");
        return this.download(url,Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+preFix[preFix.length-1]);
    }
    public int download(String url, String filePath )
    {
        String[] preFix=url.split("/");
        String displayName=preFix[preFix.length-1];
        return this.download(url,filePath,displayName);
    }
    public int download(String url, String filePath, String displayName)
    {
        return this.download(url,filePath,displayName,Priority.middle);
    }

    public int download(String url, String filePath,
                        String displayName , Priority priority ) {

        if(priority==null)
        {
            priority=Priority.low;
        }
        File file=new File(filePath);
        DownloadItemInfo downloadItemInfo=null;

        downloadItemInfo=downLoadDao.findRecord(url,filePath);
        //没下载
        if(downloadItemInfo==null)
        {
            /**
             * 根据文件路径查找(这里不严谨，有可能filePath已经被文件占据了)
             */
            List<DownloadItemInfo> samesFile=downLoadDao.findRecord(filePath);
            /**
             * 大于0  表示下载
             */
            if(samesFile.size()>0)
            {
                DownloadItemInfo sameDown=samesFile.get(0);
                if(sameDown.getCurrentLen()==sameDown.getTotalLen())
                {
                    synchronized (applisteners)
                    {
                        for (IDownloadCallable downloadCallable:applisteners)
                        {
                            downloadCallable.onDownloadError(sameDown.getId(),2,"文件已经下载了");
                        }
                        return -1;
                    }

                }
            }
            /**---------------------------------------------
             * 插入数据库
             * 可能插入失败
             * 因为filePath  和id是独一无二的  在数据库建表时已经确定了
             */
           int recrodId=downLoadDao.addRecrod(url,filePath,displayName,priority.getValue());
            if(recrodId!=-1)
            {
                synchronized (applisteners)
                {
                    //新添加
                    downloadItemInfo=downLoadDao.findRecord(url,filePath);
                    for (IDownloadCallable downloadCallable:applisteners)
                    {
                        //通知应用层  数据库被添加了  （这里有问题，downloadItemInfo为null，其实不会走到这里recrodId!=-1，表明数据库有该条数据，上面就应该查出downloadItemInfo了）
                        downloadCallable.onDownloadInfoAdd(downloadItemInfo.getId());
                    }
                }
            }
            //插入失败时，再次进行查找，确保能查得到
            else
            {
                //插入   -1表示数据库存在该条数据
                downloadItemInfo=downLoadDao.findRecord(url,filePath);
            }
        }
        /**-----------------------------------------------
         * 括号写错了  放在外面
         *
         * 是否正在下载`
         */
        if(isDowning(file.getAbsolutePath()))
        {
            synchronized (applisteners)
            {
                for (IDownloadCallable downloadCallable:applisteners)
                {
                    downloadCallable.onDownloadError(downloadItemInfo.getId(),4,"正在下载，请不要重复添加");
                }
            }
            return downloadItemInfo.getId();
        }

        if(downloadItemInfo!=null)
        {
            downloadItemInfo.setPriority(priority.getValue());
            //添加----------------------------------------------------
            downloadItemInfo.setStopMode(DownloadStopMode.auto.getValue());

            //判断数据库存的 状态是否是完成
            if(downloadItemInfo.getStatus()!= DownloadStatus.finish.getValue())
            {
                if(downloadItemInfo.getTotalLen()==0L||file.length()==0L)
                {
                    Log.i(TAG,"还未开始下载");
                    //----------------------删除--------------------
                    downloadItemInfo.setStatus(DownloadStatus.failed.getValue());
                }
                //判断数据库中 总长度是否等于文件长度
                if(downloadItemInfo.getTotalLen()==file.length()&&downloadItemInfo.getTotalLen()!=0)
                {
                    downloadItemInfo.setStatus(DownloadStatus.finish.getValue());
                    synchronized (applisteners)
                    {
                        for (IDownloadCallable downloadCallable:applisteners)
                        {
                            try {
                                downloadCallable.onDownloadError(downloadItemInfo.getId(),4,"已经下载了");
                            }catch (Exception e)
                            {
                            }
                        }
                    }
                }
            }
            //------------------添加--------
            else
            {
                if(!file.exists()||(downloadItemInfo.getTotalLen()!=downloadItemInfo.getCurrentLen()))
                {
                    downloadItemInfo.setStatus(DownloadStatus.failed.getValue());
                }
            }
            /**
             *
             * 更新
             */
            downLoadDao.updateRecord(downloadItemInfo);
            //移到括号里面来----------------------------------------------------
            /**
             * 判断是否已经下载完成
             */
            if(downloadItemInfo.getStatus()==DownloadStatus.finish.getValue())
            {
                Log.i(TAG,"已经下载完成  回调应用层");
                final int downId=downloadItemInfo.getId();
                synchronized (applisteners)
                {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            for (IDownloadCallable downloadCallable:applisteners)
                            {
                                downloadCallable.onDownloadStatusChanged(downId,DownloadStatus.finish);
                            }
                        }
                    });
                }
                downLoadDao.removeRecordFromMemery(downId);
                return downloadItemInfo.getId();
            }//之前的下载 状态为暂停状态
            List<DownloadItemInfo> allDowning=downloadFileTaskList;
            //当前下载不是最高级  则先退出下载（这里有个问题，暂停的下载没法自动重新开启）
            if(priority!=Priority.high)
            {

                for(DownloadItemInfo downling:allDowning)
                {
                    //从下载表中  获取到全部正在下载的任务（这里感觉也有问题，downling为啥重新赋值）
                    downling=downLoadDao.findSigleRecord(downling.getFilePath());

                    if(downling!=null&&downling.getPriority()==Priority.high.getValue())
                    {

                        /**
                         *     更改---------
                         *     当前下载级别不是最高级 传进来的是middle    但是在数据库中查到路径一模一样 的记录   所以他也是最高级------------------------------
                         *     比如 第一次下载是用最高级下载，app闪退后，没有下载完成，第二次传的是默认级别，这样就应该是最高级别下载

                         */
                        if (downling.getFilePath().equals(downloadItemInfo.getFilePath()))
                        {
                            break;
                        }
                        else
                        {
                            return downloadItemInfo.getId();
                        }
//                        if(downloadItemInfo.getFilePath().equals(downling.getFilePath()))
//                        {
//                            return downloadItemInfo.getId();
//                        }
                    }
                }
            }
            //
            reallyDown(downloadItemInfo);
            //当前优先级高，暂停其他下载
            if(priority==Priority.high||priority== Priority.middle)
            {
                synchronized (allDowning)
                {
                    for (DownloadItemInfo downloadItemInfo1:allDowning)
                    {
                        if(!downloadItemInfo.getFilePath().equals(downloadItemInfo1.getFilePath()))
                        {
                            DownloadItemInfo downingInfo=downLoadDao.findSigleRecord(downloadItemInfo1.getFilePath());
                            if(downingInfo!=null)
                            {
                                pause(downloadItemInfo.getId(),DownloadStopMode.auto);
                            }
                        }
                    }
                }
                return downloadItemInfo.getId();
            }








        }



      return  -1;
    }


    /**
     * 停止
     * @param downloadId
     * @param mode
     */
    public void pause(int downloadId, DownloadStopMode mode)
    {
        if (mode == null)
        {
            mode = DownloadStopMode.auto;
        }
        final DownloadItemInfo downloadInfo =downLoadDao.findRecordById(downloadId);
        if (downloadInfo != null)
        {
            // 更新停止状态
            if (downloadInfo != null)
            {
                downloadInfo.setStopMode(mode.getValue());
                downloadInfo.setStatus(DownloadStatus.pause.getValue());
                downLoadDao.updateRecord(downloadInfo);
            }
            for (DownloadItemInfo downing:downloadFileTaskList)
            {
                if(downloadId==downing.getId())
                {
                    downing.getHttpTask().pause();
                }
            }
        }
    }

    /**
     * 判断当前是否正在下载
     *
     * @param absolutePath
     * @return
     */
    private boolean isDowning(String absolutePath) {
        for (DownloadItemInfo downloadItemInfo:downloadFileTaskList)
        {
            if(downloadItemInfo.getFilePath().equals(absolutePath))
            {
                return true;
            }
        }
        return false;
    }



    /**
     * 添加观察者
     * @param downloadCallable
     */
    public void setDownCallable(IDownloadCallable downloadCallable)
    {
        synchronized (applisteners)
        {
             applisteners.add(downloadCallable);
        }

    }

        /**
         * 下载
         * @param url
         * MainAcitivity
         * 1
         *
         * 2
         *
         */
    /**
     * 下载，downloadItemInfo，下载的信息，已经下载了的，会有长度，progress等信息
     */
    public DownloadItemInfo reallyDown(DownloadItemInfo downloadItemInfo)
    {
        synchronized (lock)
        {
            //这里封装网络请求，跟volley的sendRequest一样
            //实例化DownloadItem
            RequestHodler requestHodler=new RequestHodler();
            //设置请求下载的策略
            IHttpService httpService=new FileDownHttpService();
            //得到请求头的参数 map
            Map<String,String> map=httpService.getHttpHeadMap();
            /**
             * 处理结果的策略
             */
            IHttpListener httpListener=new DownLoadLitener(downloadItemInfo,this,httpService);

            requestHodler.setHttpListener(httpListener);
            requestHodler.setHttpService(httpService);
            /**
             *  bug  url
             */
            requestHodler.setUrl(downloadItemInfo.getUrl());

            HttpTask httpTask=new HttpTask(requestHodler);
            downloadItemInfo.setHttpTask(httpTask);

            /**
             * 添加
             */
            downloadFileTaskList.add(downloadItemInfo);
            httpTask.start();

        }

        return downloadItemInfo;

    }






    @Override
    public void onDownloadStatusChanged(DownloadItemInfo downloadItemInfo) {

    }

    @Override
    public void onTotalLengthReceived(DownloadItemInfo downloadItemInfo) {

    }

    @Override
    public void onCurrentSizeChanged(DownloadItemInfo downloadItemInfo, double downLenth, long speed) {
        Log.e("TAG", "onCurrentSizeChanged : "+downloadItemInfo+ " "+(downloadItemInfo.getHttpTask()==null));
        Log.i(TAG,"下载速度："+ speed/1000 +"k/s");
        Log.i(TAG,"-----路径  "+ downloadItemInfo.getFilePath()+"  下载长度  "+downLenth+"   速度  "+speed);
    }

    @Override
    public void onDownloadSuccess(DownloadItemInfo downloadItemInfo) {
        Log.i(TAG,"下载成功    路劲  "+ downloadItemInfo.getFilePath()+"  url "+ downloadItemInfo.getUrl());
        DownloadItemInfo downloadInfo = downLoadDao.findSigleRecord(downloadItemInfo.getFilePath());
        if(null != downloadInfo){
            downloadInfo.setCurrentLen(new File(downloadItemInfo.getFilePath()).length());
            downloadInfo.setFinishTime(dateFormat.format(new Date()));
            downloadInfo.setStopMode(DownloadStopMode.hand.getValue());
            downloadInfo.setStatus(DownloadStatus.finish.getValue());
            downLoadDao.updateRecord(downloadInfo);

            synchronized (applisteners){
                for(IDownloadCallable downloadCallable:applisteners){
                    downloadCallable.onDownloadSuccess(downloadItemInfo.getId());
                }
            }
            //新加，下载成功应该移除downloadDao的内存缓存
            downLoadDao.removeRecordFromMemery(downloadItemInfo.getId());
            resumeAutoCancelItem();
        }


    }
    private void resumeAutoCancelItem(){
        List<DownloadItemInfo> allAutoCancelList = downLoadDao.findAllAutoCancelRecords();
        List<DownloadItemInfo> notDownloadingList = new ArrayList<>();
        for(DownloadItemInfo downloadItemInfo:allAutoCancelList){
            if(!isDowning(downloadItemInfo.getFilePath())){
                notDownloadingList.add(downloadItemInfo);
            }
        }

        for(DownloadItemInfo downloadItemInfo:notDownloadingList){
            if(downloadItemInfo.getPriority() == Priority.high.getValue()){
                resumeItem(downloadItemInfo.getId(), Priority.high);
                return;
            }else if(downloadItemInfo.getPriority() == Priority.middle.getValue()){
                resumeItem(downloadItemInfo.getId(), Priority.middle);
            }
        }
    }

    public void resumeItem(final int downloadId, Priority priority){
        DownloadItemInfo downloadItemInfo = downLoadDao.findRecordById(downloadId);
        if(null == downloadItemInfo){
            return;
        }

        if(null == priority){
            priority = Priority.getInstance(downloadItemInfo.getPriority() == null?
                    Priority.low.getValue():downloadItemInfo.getPriority());
        }

        File file = new File(downloadItemInfo.getFilePath());
        downloadItemInfo.setStopMode(DownloadStopMode.auto.getValue());
        downLoadDao.updateRecord(downloadItemInfo);
        download(downloadItemInfo.getUrl(), file.getAbsolutePath(), null, priority);

    }

    @Override
    public void onDownloadPause(DownloadItemInfo downloadItemInfo) {



    }

    @Override
    public void onDownloadError(DownloadItemInfo downloadItemInfo, int var2, String var3) {

    }
}
