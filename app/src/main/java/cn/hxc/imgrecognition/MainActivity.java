
package cn.hxc.imgrecognition;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Matrix;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.hxc.imgrecognitionSRI_OCR.R;

public class MainActivity extends Activity{

      private static String TAG = "Opencv";
	  public static String userName;   //用户名
	  public String passWord;   //密码
	  public String phoneSerialNumber;
	  public AlertDialog diag;
	  public String webLoginPath="http://119.23.33.12/PaisService.asmx/PaisLogin?";
	  public String backLoginPath;
	  inputInformation infor;
	  public String result;//输入用户名和密码之后返回的结果代码
	  public EditText Edit_username;
	  public EditText Edit_password;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);//软件activity的布局
		initData();
	}

	public void initData(){
		Edit_username = (EditText)findViewById(R.id.Edit_username);
		Edit_password = (EditText)findViewById(R.id.Edit_password);
		Edit_username.getText().clear();
		Edit_password.getText().clear();
	}
	//点击登录确定按钮执行的操作
	public void login_Yes(View v){

		TelephonyManager tm = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
		//phoneSerialNumber = tm.getDeviceId();//获取智能设备唯一编号

		TelephonyManager tmm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

		//String IMEI =android.os.SystemProperties.get(android.telephony.TelephonyProperties.PROPERTY_IMEI);

		infor=new inputInformation();
		userName=((EditText)findViewById(R.id.Edit_username)).getText().toString();
		passWord=((EditText)findViewById(R.id.Edit_password)).getText().toString();
		if(!userName.isEmpty()&&!passWord.isEmpty()){
			if(userName.equals("admin") && passWord.equals("123456")){
				Intent intent = new Intent(this, takePhoto.class);
				startActivity(intent);
			}
			else{
				String tip="用户名或者密码错误！";
				Toast.makeText(this,tip,Toast.LENGTH_LONG).show();
			}
		}
		else{
			String tip="请将括号里的内容填完！";
			Toast.makeText(this,tip,Toast.LENGTH_LONG).show();
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			//Toast.makeText(getApplicationContext(), "用户名或密码错误！", Toast.LENGTH_LONG).show();
			showDialog();

		}
	};

	private void showDialog(){
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setTitle("温馨提示");
		builder.setIcon(R.drawable.alert);
		builder.setMessage("用户名或密码错误，有问题请联系2294011886@qq.com");
		builder.setPositiveButton("我知道了",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {

					}
				});
		AlertDialog dialog=builder.create();
		dialog.show();

	}


	//正则表达式，获得返回结果中的有用信息
	public static String getContext(String html) {
		List resultList = new ArrayList();
		Pattern p = Pattern.compile(">([^</]+)</");//正则表达式 commend by danielinbiti
		Matcher m = p.matcher(html );
		while (m.find()) {
			resultList.add(m.group(1));
		}
		return resultList.toString();
	}

	//判断返回值是不是正确结果
	public void judge(String all){
		if(all.equals("[10200]")){
			/*Edit_username=(EditText)findViewById(R.id.Edit_username);
			Edit_password=(EditText)findViewById(R.id.Edit_password);
			Edit_username.setText("12");
			Edit_password.setText("12");*/
			Intent intent = new Intent(this, takePhoto.class);
			startActivity(intent);
			//this.finish();
		}
		else
			handler.sendEmptyMessage(0);
	}

	//点击忘记密码执行的操作
	public void forgetPSW(View v){
		Intent intent = new Intent(MainActivity.this, forgetPassword.class);
		startActivity(intent);
	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS: {
					Log.i(TAG, "OpenCV loaded successfully");
				}
				break;
				default: {
					super.onManagerConnected(status);
				}
				break;
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		if (!OpenCVLoader.initDebug()) {
			Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
		} else {
			Log.d(TAG, "OpenCV library found inside package. Using it!");
			mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
		}
	}
}


