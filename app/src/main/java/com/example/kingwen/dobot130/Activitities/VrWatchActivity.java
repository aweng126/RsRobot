package com.example.kingwen.dobot130.Activitities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.kingwen.dobot130.Application.MyApplication;
import com.example.kingwen.dobot130.R;
import com.example.kingwen.dobot130.widgets.Dialog;
import com.example.kingwen.dobot130.widgets.Zeemote;

/**
 * 本类用于暴风魔镜进行监控。通过相同网络下的adision板的ip地址来进行视频流的获取。
 */
public class VrWatchActivity extends AppCompatActivity {


    private WebView webView;

    private static String weburl="192.168.42.1:8081";

    private static final String TAG="VrWatchActivity";

    private Handler mHandler;

    private MyApplication myApplication;

    private Zeemote zeemote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //设置全屏显示
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_vrwatch_layout);

        myApplication= (MyApplication) getApplication();
        mHandler=myApplication.getmHandler();

        initViews();

        initListeners();

    }

    private void initViews() {


        webView= (WebView) findViewById(R.id.wv_viedo);

    }

    private void initListeners() {

        WebSettings ws = webView.getSettings();
        ws.setBuiltInZoomControls(true);// 隐藏缩放按钮
        // ws.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);// 排版适应屏幕

        ws.setUseWideViewPort(true);// 可任意比例缩放
        ws.setLoadWithOverviewMode(true);// setUseWideViewPort方法设置webview推荐使用的窗口。setLoadWithOverviewMode方法是设置webview加载的页面的模式。

        ws.setSavePassword(true);
        ws.setSaveFormData(true);// 保存表单数据
        ws.setJavaScriptEnabled(true);
        ws.setGeolocationEnabled(true);// 启用地理定位
        ws.setGeolocationDatabasePath("/data/data/org.itri.html5webview/databases/");// 设置定位的数据库路径
        ws.setDomStorageEnabled(true);
        ws.setSupportMultipleWindows(true);// 新加

        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(weburl);

        Toast.makeText(VrWatchActivity.this, "ip地址不可用，请更换视频流ip", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onResume() {
        //每次重新建立界面的时候要进行判断，如果当前界面不是横向的，则更改属性。
        if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        webView.resumeTimers();
        webView.onResume();

        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.vrwatch_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.changeip:
                //开启一个弹出框然后更新我们的视频地址
                Dialog mDialog =new Dialog(VrWatchActivity.this, "请输入连接地址", new Dialog.OnMyDialogListener() {
                    @Override
                    public void back(String address) {

                        VrWatchActivity.weburl=address;

                        Log.d("Main 回传", weburl);
                        initListeners();

                    }
                });
                mDialog.show();
                return true;

            case R.id.zeemote:

                zeemote =new Zeemote(VrWatchActivity.this,mHandler);

                //zeemote.connect();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
