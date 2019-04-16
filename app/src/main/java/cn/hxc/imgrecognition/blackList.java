package cn.hxc.imgrecognition;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.hxc.imgrecognitionSRI_OCR.R;

/**
 * Created by Administrator on 2019/4/11.
 */

public class blackList extends Activity {
    public ImageView dPic;

    String ImageID = "38934230-fe1e-41e1-b794-398038b7565b";
    String ImageUrl = "http://119.23.33.12/PaisService.asmx/GetImageUrlByImageid?";
    String DownLoadImageUrl = "";
    String finalID = "";
    String temp;

    Bitmap bitmap;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blacklist);//软件activity的布局

        dPic = (ImageView)findViewById(R.id.dpic);
    }

    public void dloadpic(View v){
        finalID = "Imageid=" + URLEncoder.encode(ImageID);
        Callable<String> callable = new Callable<String>() {
            public String call() throws Exception {
                DownLoadImageUrl = GETUtils(ImageUrl,finalID);
                return DownLoadImageUrl;
            }
        };
        FutureTask<String> future = new FutureTask<String>(callable);
        new Thread(future).start();
        try {
            System.out.println(future.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        temp = getContext(DownLoadImageUrl);
        temp = temp.substring(1,temp.length()-1);

        Callable<String> callable2 = new Callable<String>() {
            public String call() throws Exception {
                bitmap = getImageBitmap(temp);
                return "ok";
            }
        };
        FutureTask<String> future2 = new FutureTask<String>(callable2);
        new Thread(future2).start();
        try {
            System.out.println(future2.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        dPic.setImageBitmap(bitmap);
    }


    public Bitmap getImageBitmap(String url) {
        URL imgUrl = null;
        Bitmap bitmap = null;
        try {
            imgUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imgUrl
                    .openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    public static String getContext(String html) {
        List resultList = new ArrayList();
        Pattern p = Pattern.compile(">([^<]+)<");//正则表达式
        Matcher m = p.matcher(html);
        while (m.find()) {
            resultList.add(m.group(1));
        }
        return resultList.toString();
    }

    public String GETUtils(String urlString,String inputLine){
        String msg="";
        int code = 0;
        String codeString;
        try{
            String WholeString=urlString+inputLine;
            URL url = new URL(WholeString);
            //Toast.makeText(this,WholeString,Toast.LENGTH_LONG).show();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //设置请求方式
            conn.setRequestMethod("GET");
            //设置运行输入,输出:
            conn.setDoOutput(false);
            conn.setDoInput(true);
            //Post方式不能缓存,需手动设置为false
            conn.setUseCaches(true);
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(3000);
            conn.connect();
            code = conn.getResponseCode();

            if (code==200)
            {
                BufferedReader read=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line=null;
                while ((line=read.readLine())!=null)
                {
                    msg+=line;
                }
                read.close();
            }

            conn.disconnect();

            //Toast.makeText(this,msg,Toast.LENGTH_LONG).show();

        }catch (Exception e){
            e.printStackTrace();
        }

        codeString = Integer.toString(code);
        return msg;
    }

    public void SelectPicFromPhone(View v){
        Intent intent = new Intent(this, blackListFromPhone.class);
        startActivity(intent);
    }

    public void SelectPicFromDataBase(View v){
        Intent intent = new Intent(this, blackListFromDB.class);
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
        Intent intent = new Intent(this, takePhoto.class);
        startActivity(intent);
    }
}
