package cn.hxc.imgrecognition;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.Toast;

import cn.hxc.imgrecognitionSRI_OCR.R;

/**
 * Created by 刘欢 on 2018/2/28.
 */

public class queryDBInfor extends Activity{

    private MyWebView wView;

    public ImageButton B_contrast;
    public ImageButton B_QueryLoc;
    public ImageButton B_QueryDB;
    public ImageButton B_set;
    public ImageButton B_blist;

    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        requestWindowFeature(Window.FEATURE_NO_TITLE);// ȥ������
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// ����ȫ��
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.query_information);

        wView = (MyWebView) findViewById(R.id.webView);
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager tm = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
            String phoneID = tm.getDeviceId();//获取智能设备唯一编号

            wView.loadUrl("http://119.23.33.12/SearchUserDatas.aspx?phoneid="+phoneID+"");
            wView.setWebViewClient(new WebViewClient() {
                //在webview里打开新链接
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });
        }
        else{
            Toast.makeText(this,"请开放手机通话权限！",Toast.LENGTH_LONG).show();
        }


        //比如这里做一个简单的判断，当页面发生滚动，显示那个Button

    }
    public void searchBack(View v){
        //wView.goBack();
        super.onBackPressed();
    }

    public void onBackPressed(View v){
        super.onBackPressed();
    }

    public void DBSignOut(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void botmSet(View v){
        Intent intent = new Intent(this, set.class);
        startActivity(intent);
    }
    public void botmContrast(View v){
        Intent intent = new Intent(this, takePhoto.class);
        startActivity(intent);
    }
    public void botmBlacklist(View v){
        Intent intent = new Intent(this, blackList.class);
        startActivity(intent);
    }
    public void botmQueryLoc(View v){
        Intent intent = new Intent(this, queryLocInfor.class);
        startActivity(intent);
    }
    public void botmQueryDB(View v){
        Intent intent = new Intent(this, queryDBInfor.class);
        startActivity(intent);
    }
    public void onBackPressed(){
        Intent intent = new Intent(queryDBInfor.this, takePhoto.class);
        startActivity(intent);
    }
}
