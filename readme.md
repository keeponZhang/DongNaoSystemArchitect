# BadgeTabLayout
### 一些特性
 *      tl:tl_underline_color="#1A000000"  //设置下划线颜色
 *      tl:tl_underline_gravity="BOTTOM"
 *      tl:tl_textsize="12sp"               //设置字体大小
 *      tl:tl_underline_height="1dp"
 *      tl:tl_badge_backgroundColor="@color/colorPrimary"  //角标背景
 *      tl:tl_badge_textsize="10sp"          //角标字体大小
 *      tl:tl_badge_draggable="false"          //角标是否可拖拽，默认false
 *      tl_tab_item_margin="15dp"            //设置tab item 的margin   //更多属性查看attrs文件

### 2. code
```java
  //设置数据，adapter自定义
  mAdapter = new MyPagerAdapter(getSupportFragmentmViewPager.setAdapter(mAdapter);
   //重要，设置或更新数据（更新数据时viewpager对应的数据源要改变）
  mBadgeTabLayout.setViewPager(mViewPager); //或者m
  //角标设置，对应位置设置num
   mBadgeTabLayout.showMsg(2, 1);
   //因此角标
   mBadgeTabLayout.hideMsg(0);
   //顶部tiltle tab点击监听
   mBadgeTabLayout.setOnTabSelectListener(this);
    
   //角标拖拽监听
   interface OnDragStateChangedListener {
           int STATE_START = 1;
           int STATE_DRAGGING = 2;
           int STATE_DRAGGING_OUT_OF_RANGE = 3;
           int STATE_CANCELED = 4;
           int STATE_SUCCEED = 5;
            
           void onDragStateChanged(int dragState, Badge badge, int positon);
   }
```   

# JShare 
### 1. ShareMsgObj
```java
   //分享类型  文字  图片    网页
       private              SharePlatform    sharePlatform;//分享平台类型
       private              int    shareType;//分享类型 必填
        private              String msgTitle;//标题
       private              String Text;//文本信息
       private              String url;// 网页地址
       private              String localImgUrl;//本地图片链接
       private              Bitmap bmp;//图片
       private              String Img_url;//图片链接
       private              Bitmap thunbBmp;//缩略图
       
       private              String msgDesription;//描述
       private              String shareAppName;//分享的客户端（默认）
    
     public static class  ShareType{
            public static final int SHARE_TEXT=1;//文字
            public static final int SHARE_IMAGE=2;//图片
            public static final int SHARE_WEB=3;//网页
   }
```

### 2. 使用
#### 弹框分享 
```java
new JShareDialog.Builder(this).setShareMsgObj(shareMsgObj).setJShareCallBack(new JShareCallBack() {
	@Override
	public void onShareSucc(SharePlatform sharePlatform) {
		Log.e("TAG", "onShareSucc: "+sharePlatform.name());
	}

	@Override
	public void onShareFailure(SharePlatform sharePlatform) {
		Log.e("TAG", "onShareFailure: "+sharePlatform.name());
	}

	@Override
	public void onShareCancel(SharePlatform sharePlatform) {
		Log.e("TAG", "onShareCancel: "+sharePlatform.name());
	}
}).hideShareQQ().//隐藏某个分享，调用hide方法
setCopyLinkString("https://blog.csdn.net/qiy6010/article/details/77155661").
setExpolerOpenString("https://blog.csdn.net/qiy6010/article/details/77155661").build().show();
```
#### 无界面分享
```java
//微信分享
WxShareHelper.getInstance().shareToWeChat(context,shareMsgObj,new WxShareListener() {
	@Override
	public void onWxShareSucc(String var) {
	}

	@Override
	public void onWxShareFailure(int var, String var1) {
	}

	@Override
	public boolean onWxShareCancel() {
		return true;
	}
});
//qq分享
QQShareHelper.getInstance().shareToQQ(context,
		 shareMsgObj, new QQShareListener() {
	@Override
	public void onQQShareSucc(Object var) {
	}

	@Override
	public void onQQShareFailure(UiError var1) {
	}

	@Override
	public boolean onQQShareCancel() {
		return false;
	}
});
//微信分享
WeiBoShareHelper.getInstance().shareToWeiBo(context,shareMsgObj, new WbShareCallback() {
	@Override
	public void onWbShareSuccess() {
	}

	@Override
	public void onWbShareCancel() {
	}

	@Override
	public void onWbShareFail() {
	}
});
```
### 3.注意点
#### 微博分享和QQ分享需要在onActivityResult调
```java
//可选中统一回调
 JShare.getInstance().onActivityResultData(requestCode,resultCode,data);
 
 //或者单独回调
 //qq
 if (requestCode == Constants.REQUEST_QQ_SHARE || requestCode == Constants.REQUEST_QZONE_SHARE) {
 	QQShareHelper.getInstance().onActivityResultData(requestCode, resultCode, data);
 }
 //微博
 WeiBoShareHelper.getInstance().onActivityResultData(data);
```
#### activity销毁需要释放，在onDestroy
```java
//可选中统一释放，
 JShare.getInstance().release();
 
//或者单独释放
QQShareHelper.getInstance().release();
WxShareHelper.getInstance().release();
WeiBoShareHelper.getInstance().release();
```
#### 分享网络图片需要设置一个下载器
```java
  JShare.getInstance().setImageDownlad(new MyImageDownload());
```
#### 清单文件配置
```java
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
  <activity
  	android:name="com.tencent.tauth.AuthActivity"
  	android:noHistory="true"
  	android:launchMode="singleTask" >
  	<intent-filter>
  		<action android:name="android.intent.action.VIEW" />
  		<category android:name="android.intent.category.DEFAULT" />
  		<category android:name="android.intent.category.BROWSABLE" />
  		<data android:scheme="tencent+你的appid" />
  	</intent-filter>
 </activity>
 
 <activity android:name="com.tencent.connect.common.AssistActivity"
  	          android:theme="@android:style/Theme.Translucent.NoTitleBar"
  	          android:configChanges="orientation|keyboardHidden|screenSize"
  />
  
 <activity
  android:name=".wxapi.WXEntryActivity"
  android:exported="true"
  android:launchMode="singleTop"
  android:theme="@android:style/Theme.Translucent" />
```
#### 使用微信分享时
```java
public class WXEntryActivity extends Activity implements IWXAPIEventHandler
{
    private WxShareHelper mShareHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mShareHelper=WxShareHelper.getInstance();
        if(null!=getIntent()||null!=mShareHelper&&mShareHelper.getmShareIWXAPI()!=null){
            mShareHelper.getmShareIWXAPI().handleIntent(getIntent(),this);
        }
    }
    @Override
    protected void onNewIntent(Intent intent)
    {
        setIntent(intent);
        if(mShareHelper!=null&&mShareHelper.getmShareIWXAPI()!=null){
            mShareHelper.getmShareIWXAPI().handleIntent(getIntent(),this);
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        if(mShareHelper!=null){
            mShareHelper.onResumeResultData();
        }
    }

    @Override
    public void onReq(BaseReq baseReq)
    {
        if(mShareHelper!=null){
            mShareHelper.dealWithWeiXin0nRep(baseReq);
        }
    }

    @Override
    public void onResp(BaseResp baseResp)
    {
        if(mShareHelper!=null){
            mShareHelper.dealWithWeixinOnResp(baseResp);
            mShareHelper =null;
        }
        finish();
    }
}
```
#### 代码混淆
```java
#qq
-keep class com.tencent.**{*;}
#微信
-keep class com.tencent.mm.opensdk.** {
    *;
}
-keep class com.tencent.wxop.** {
    *;
}
-keep class com.tencent.mm.sdk.** {
    *;
}
#微博分享
-keep class com.sina.weibo.sdk.** { *; }
```
# login 
### 1. 调隐私提示弹框
```java
//方式1：
@Autowired()
IShowPrivacyTipsSerice mIShowPrivacyTipsSerice;
mIShowPrivacyTipsSerice.showTipsDialog(context);

//方式2：
IShowPrivacyTipsSerice iShowPrivacyTipsSerice = ARouter.getInstance().navigation(IShowPrivacyTipsSerice.class);
iShowPrivacyTipsSerice.showTipsDialog(context);
```
### 1. 调推送弹框
```java
String mImgUrl = "http://img3.cache.netease.com/photo/0005/2013-03-07/8PBKS8G400BV0005.jpg";
 IPushMsgDialogService service = ARouter.getInstance().navigation(IPushMsgDialogService.class);
 service.showPushMsgDialog(this, "直播推送", "精彩资讯来袭,下面正在直播“金十早报”，欢迎前往收看节目。", mImgUrl, new View.OnClickListener() {
   @Override
   public void onClick(View v) {
      Log.e("TAG", "onClick: ");
   }
});
```


# downloader 
#### 注意
```java
//区别用户，必须调
DownloadManager.getInstance().changeUser("keepon");

```
#### 下载状态 DownloadStatus  
```java

// 未下载
public final static int STATUS_NORMAL = 0;
/**
 * 连接服务器
 */
public final static int STATUS_LOADING = 1;
/**
 * 下载中
 */
public final static int STATUS_DOWNLOADING = 2;
/**
 * 下载完成
 */
public final static int STATUS_COMPLETED = 3;

/**
 * 暂停下载
 */
public final static int STATUS_PAUSE = 4;
/**
 * 下载失败
 */
public final static int STATUS_FAILED = 5;
/**
 * 删除下载
 */
public final static int STATUS_DELETE = 6;
```
#### 下载信息 DownLoadInfo
```java
/**
 * 下载地址
 */
private              String url;
/**
 * 下载进度
 */
private              int    progress;
/**
 * 下载状态
 */
private              int    status;
/**
 * 保存地址
 */
private              String savePath;
/**
 * 文件的字节数
 */
private              long   length;
```
#### 下载状态监听
```java
@Subscribe(threadMode = ThreadMode.MAIN)
   public void onMessageEvent(DownLoadInfo downLoadInfo) {
}
```
#### 开始暂停下载
```java
//开始下载 savePath为空会存到默认路径
DownloadManager.getInstance().startDownload(url);
DownloadManager.getInstance().startDownload(url,savePath);

//暂停下载 savePath为空会暂停默认路径的下载
DownloadManager.getInstance().pauseTask(url);
DownloadManager.getInstance().pauseTask(url,savePath);


////删除下载,savePath为空会删除默认路径的下载信息
DownloadManager.getInstance().deleteTask(String url) 
DownloadManager.getInstance().deleteTask(String url,savePath) 

//如果确定已经下载完成，可以使用下面删除方法
DownloadManager.getInstance().deleteDataByUrl(url);
DownloadManager.getInstance().deleteDataByUrl(url,savePath);

//获取单个下载信息
DownLoadInfo downloadInfo =DownloadManager.getInstance().getDownloadInfoByUrl(url);
DownloadManager.getInstance().getDownloadInfoByUrlAndSavePath(url,savePath);

//获取所有下载信息
DownloadManager.getInstance().getAllDownloadInfoList();
//获取已经下载完成信息
DownloadManager.getInstance().getCompleteDownloadInfoList();
//获取未下载完成信息
DownloadManager.getInstance().getUnCompleteDownloadInfoList();

```
#  verfliplayout
#### 1.使用FlipScrollHelper结合recyclerView用
```java
//结合recyclerView用LinearLayoutManager，LinearLayoutManager方向为vertical为上下翻页，LinearLayoutManager方向为HORIZONTAL为左右翻页
FlipScrollHelper scrollHelper = new FlipScrollHelper();
scrollHelper.setUpRecycleView(recyclerView);
scrollHelper.setOnPageChangeListener(new FlipScrollHelper.onPageChangeListener() {
  @Override
  public void onPageChange(int index) {

  }
});
```
####2 .VerFlipLayout容器放置view
```java
<com.jin10.lgd.verfliplayout.VerFlipLayout xmlns:android="http://schemas.android.com/apk/res/android"
                                                xmlns:tools="http://schemas.android.com/tools"
                                                android:id="@+id/id_main_ly"
                                                android:layout_width="match_parent"
                                                android:layout_height="match_parent"
                                                android:orientation="vertical"
                                                android:background="#fff" >
    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@mipmap/ic_launcher" >
        <Button
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="hello1" />
    </RelativeLayout>

    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@mipmap/ic_launcher" >
        <Button
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="hello2" />
    </RelativeLayout>

</com.jin10.lgd.verfliplayout.VerFlipLayout>
```