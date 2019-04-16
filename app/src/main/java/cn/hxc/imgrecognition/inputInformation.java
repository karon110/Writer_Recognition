package cn.hxc.imgrecognition;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.hxc.imgrecognitionSRI_OCR.R;

/**
 * Created by 刘欢 on 2018/2/1.
 */

public class inputInformation extends Activity implements Callable<String>{

    public static String sxname; //书写人名称
    public static String sxnametemp;//按照指定方式存的书写人
    public static String cjname; //采集人名称
    public String cjnametemp;//按照指定方式存的采集人
    public static String titlesxname;//txt文本标题中书写人的名字
    public static String titlenum;//txt文本标题中文本的序列
    public static String txtID;//每个文本唯一的ID
    public static String imageID;//每个图片唯一的ID
    public static String nowTime;//获取的系统时间
    public static String phoneID = "";//手机唯一的ID
    public static String SendString;//手机向webAPI发送的字符串
    public static String txtUrlWeb="http://119.23.33.12/PaisService.asmx/PaisUploadTxts?";//需要上传txt的URL
    public static String imageUrlWeb="http://119.23.33.12/PaisService.asmx/PaisUploadImages?";//需要上传jpg的URL
    public static String oracleUrl="http://119.23.33.12/PaisService.asmx/PaisInsertImages?";//需要上传数据库的URL
    public static String oracleString;
    public static String imageBackUrl;
    public static String txtBackUrl;//完整的url，包括上传的字符串
    static String featurePath=Environment.getExternalStorageDirectory()+ File.separator + "WR_LPAIS"+ File.separator + "txt"+ File.separator+"feature.txt";
    static String AnotherfeaturePath=Environment.getExternalStorageDirectory()+ File.separator + "WR_LPAIS"+ File.separator + "txt"+ File.separator+"Anotherfeature.txt";
    static String imgpath= Environment.getExternalStorageDirectory() + File.separator + "WR_LPAIS"+ File.separator + "NorTemp.jpg";  //手机的根目录下存细化图片的地址
    static Bitmap myimgae;  //手机根目录下存细化的图片
    static String showimgpath= Environment.getExternalStorageDirectory() + File.separator + "WR_LPAIS"+ File.separator + "NorTemp.jpg";  //手机的根目录下存细化图片的地址
    static String yuanTupath= Environment.getExternalStorageDirectory() + File.separator + "WR_LPAIS"+ File.separator + "originalPic.jpg";
    static Bitmap myshowimgae;  //手机根目录下存细化的图片
    static byte[] imageData;//图片的数据
    static public byte[] bytes;
    static String sendtemp;
    static String imageResult;//插入图片返回的结果
    static String txtResult;//插入文本返回结果
    static String databaseResult;//插入数据库返回的结果
    static String toastResult;//上传完成后最后弹出的结果
    CheckBox ckb_save;
    CheckBox ckb_send;
    CheckBox ckb_yuantu;
    EditText cjr_login;
    static String imageReturn = "";
    static String txtReturn = "";
    static String oracleReturn = "";

    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.input_information);
        WindowManager wm = (WindowManager) this
                .getSystemService(this.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);

        ckb_save = (CheckBox) findViewById(R.id.ckb_save);
        ckb_send = (CheckBox) findViewById(R.id.ckb_send);
        ckb_yuantu = (CheckBox) findViewById(R.id.ckb_yuantu);
        cjr_login = (EditText) findViewById(R.id.cjname);
        cjr_login.setText(MainActivity.userName);
    }

    /*点击确定按钮执行的操作，将图片及其信息保存在指定文件夹下
    */
    public void inforYes(View v){
        //int a = saveimage();
        //saveTxt(a);
        sxname= ((EditText) findViewById(R.id.sxname)).getText().toString();
        cjname= ((EditText) findViewById(R.id.cjname)).getText().toString();

        //获取系统时间，格式是_年_月_日_时_分_秒，并记录到相应的TXT文本里
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss");
        Date curDate =  new Date(System.currentTimeMillis());
        nowTime  =  formatter.format(curDate);

        if (checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager tm = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
            phoneID = tm.getDeviceId();//获取智能设备唯一编号
        }

        if (!sxname.isEmpty()&&!cjname.isEmpty())
        {
            if(ckb_save.isChecked()){
                String uuid = UUID.randomUUID().toString();
                saveimage(uuid);
                saveTxt(uuid);
                savePictureData(featurePath,uuid);
                savePictureData(AnotherfeaturePath,uuid);
            }
            if(ckb_send.isChecked()){
                String uuid = UUID.randomUUID().toString();
                sendOut(uuid);
                savePictureData(AnotherfeaturePath,uuid);
            }
            if(ckb_yuantu.isChecked()){
                String uuid = UUID.randomUUID().toString();
                saveYuanTu(uuid);
            }

            inputInformation.this.finish();
            Intent intent = new Intent(inputInformation.this, takePhoto.class);
            startActivity(intent);
        }
        else
            Toast.makeText(this,"请将带*的内容填完！",Toast.LENGTH_LONG).show();

    }

    //点击取消键，取消当前的操作
    public void inforNo(View v){
        inputInformation.this.finish();
        Intent intent = new Intent(this, takePhoto.class);
        startActivity(intent);
    }

    //点击发送键，发送到服务器
    public void sendOut(String uuid){

        SendString=uuid + "\r\n" + sxname + "\r\n" + cjname + "\r\n" +nowTime + "\r\n" + phoneID + "\r\n";
        String SendString64=Base64.encodeToString(SendString.getBytes(),0);
        txtBackUrl="base64string="+ URLEncoder.encode(SendString64)+"&orifilename="+URLEncoder.encode(uuid)+".txt";

        //上传图片的操作
        imageData=image2Bytes(showimgpath);
        sendtemp=Base64.encodeToString(imageData,0);
        imageBackUrl="base64string="+URLEncoder.encode(sendtemp)+"&orifilename="+URLEncoder.encode(uuid)+".jpg";

        //插入数据库的操作
        String oracleStringtemp="IMGMSG::" + uuid + "::" + sxname + "::" + cjname + "::" +nowTime + "::" + phoneID + "::END";
        oracleString="Picinfo="+URLEncoder.encode(oracleStringtemp);

        inputInformation ctt = new inputInformation();
        FutureTask<String> ft = new FutureTask<>(ctt);

        new Thread(ft,"有返回值的线程").start();

        String concatStr = " ";
        try
        {
            concatStr = ft.get();
            System.out.println("子线程的返回值："+ft.get());
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } catch (ExecutionException e)
        {
            e.printStackTrace();
        }
        Toast.makeText(this,"上传成功！",Toast.LENGTH_LONG).show();
        /*if(concatStr.equals("200[10200]200"))
            Toast.makeText(this,"上传成功！",Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this,"上传失败！",Toast.LENGTH_LONG).show();*/
            //Toast.makeText(this,concatStr,Toast.LENGTH_LONG).show();


        /*Thread sendThread=new Thread(new Runnable() {
            @Override
            public void run() {
                txtReturn = GETUtils(txtUrlWeb,txtBackUrl);  //上传TXT
                //GETUtils(imageUrlWeb,imageBackUrl);//上传图片
                imageReturn = GETUtilImgs(imageUrlWeb,imageBackUrl);
                oracleReturn = GETUtils(oracleUrl,oracleString);//插入数据库
            }
        });
        sendThread.start();*/
    }
    @Override
    public String call() throws Exception
    {
        txtReturn = GETUtils(txtUrlWeb,txtBackUrl);  //上传TXT
        //GETUtils(imageUrlWeb,imageBackUrl);//上传图片
        imageReturn = GETUtilImgs(imageUrlWeb,imageBackUrl);
        oracleReturn = GETUtils(oracleUrl,oracleString);//插入数据库
        String concatStr = txtReturn + imageReturn + oracleReturn;
        return concatStr;
    }

        public byte[] image2Bytes(String imgPath)
    {
        try{
            FileInputStream fin = new FileInputStream(new File(imgPath));
            //可能溢出,简单起见就不考虑太多,如果太大就要另外想办法，比如一次传入固定长度byte[]
            bytes = new byte[fin.available()];
            //将文件内容写入字节数组，提供测试的case
            fin.read(bytes);

            fin.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return bytes;
    }

    //Andriod访问WebAPI
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

            Toast.makeText(this,msg,Toast.LENGTH_LONG).show();

        }catch (Exception e){
            e.printStackTrace();
        }

        codeString = Integer.toString(code);
        return codeString;
    }

    //上传图片
    public String GETUtilImgs(String urlString,String imgstr){
        String result = "";
        try{
            result = HttpUtils.doPost(urlString,imgstr);
            result = getContext(result);
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
      }

    public static String getContext(String html) {
        List resultList = new ArrayList();
        Pattern p = Pattern.compile(">([^</]+)</");//正则表达式 commend by danielinbiti
        Matcher m = p.matcher(html );
        while (m.find()) {
            resultList.add(m.group(1));
        }
        return resultList.toString();
    }

    public void saveYuanTu(String uuid){
        Bitmap yuanTuImage = BitmapFactory.decodeFile(yuanTupath);
        File file = new File(
                Environment.getExternalStorageDirectory()
                        + File.separator + "WR_LPAIS"+ File.separator + "yuanTu");

        if (!file.exists()) {
            file.mkdirs();
        }

        FileOutputStream fos = null;
        FileOutputStream showfos = null;
        //按规定格式保存图片
        try {
            fos = new FileOutputStream(file + File.separator + uuid+".jpg");
            yuanTuImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            fos.flush();
            fos.close();
        } catch (IOException e) {
            Toast.makeText(this,"save faied!",Toast.LENGTH_LONG).show();
        }
    }

    /*
    NAME######.JPG
    NAME是中文名称，用6个字节表示，2个字节表示一个汉字，不足三个字用两个下划线表示。
    ######是6位数字编号，从000001开始计数。
    * */
    public int saveimage(String uuid)
    {
        titlesxname=sxname;
        myimgae = BitmapFactory.decodeFile(imgpath);
        myshowimgae = BitmapFactory.decodeFile(showimgpath);
//------------------将归一化后的图片保存下来--------------------
        File showfile = new File(
                Environment.getExternalStorageDirectory()
                        + File.separator + "WR_LPAIS"+ File.separator + "ShowImage");

        if (!showfile.exists()) {
            showfile.mkdirs();
        }
//------------------------将细化后的图片保存下来------------------
        File file = new File(
                Environment.getExternalStorageDirectory()
                        + File.separator + "WR_LPAIS"+ File.separator + "Image");

        if (!file.exists()) {
            file.mkdirs();
        }
        int newname = 0;
        FileOutputStream fos = null;
        FileOutputStream showfos = null;

/*        if(titlesxname.length()<3){
            int a=3-titlesxname.length();
            while(a!=0){
                titlesxname+="__";
                a--;
            }
        }
        //数字数目补足为6位
        titlenum=String.valueOf(i);
        while(titlenum.length()<6){
            StringBuilder SB=new StringBuilder(titlenum);
            SB.insert(0,'0');
            titlenum=SB.toString();
        }*/

        //按规定格式保存图片
        try {
            fos = new FileOutputStream(file + File.separator + uuid +".jpg");
            myimgae.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            showfos = new FileOutputStream(showfile + File.separator + uuid +".jpg");
            myshowimgae.compress(Bitmap.CompressFormat.JPEG, 100, showfos);

            fos.flush();
            fos.close();

            showfos.flush();
            showfos.close();
        } catch (IOException e) {
            Toast.makeText(this,"save faied!",Toast.LENGTH_LONG).show();
        }
        return newname;
    }

    /*保存txt文件，其中包括图片的信息
    * NAME######.txt
    NAME是中文名称，用6个字节表示，2个字节表示一个汉字，不足三个字用两个下划线表示。
    ######是6位数字编号，从000001开始计数。
     */
    public void saveTxt(String uuid){
            //final String snewname = String.valueOf(a);
            File txtfile = new File(
                    Environment.getExternalStorageDirectory()
                            + File.separator + "WR_LPAIS" + File.separator + "txt");

            if (!txtfile.exists()) {
                txtfile.mkdirs();
            }
////////-----------------------------保存图片的信息，如书写人、采集人、手机号等信息-------------------------------------------
            File sxf = new File(
                    Environment.getExternalStorageDirectory()
                            + File.separator + "WR_LPAIS" + File.separator + "txt" + File.separator + uuid +".txt");
            if(!sxf.exists()){
                try{
                    sxf.createNewFile();
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }

            FileWriter fs = null;
            try {
                fs = new FileWriter(sxf, true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            PrintWriter sxw = new PrintWriter(fs);
            sxw.print(sxname + "\r\n");  //将书写人的名字记录到相应的TXT文本里
            sxw.print(cjname + "\r\n");  //将采集人的名字记录到相应的TXT文本里

            sxw.print(nowTime + "\r\n");

            sxw.print(phoneID + "\r\n");

            sxw.flush();

            try {
                fs.flush();
                sxw.close();
                fs.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
/////////---------------------------------保存图像的特征数组--------------------------------------------------------
    }

    public void savePictureData(String Path,String uuid){
        File f = new File(Path);

        FileWriter fw = null;
        try {
            fw = new FileWriter(f, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PrintWriter pw = new PrintWriter(fw);
        pw.print(uuid + " ");
        pw.print(sxname + " ");
        for (int ii = 0; ii < 920; ii++) {
            String temp = String.format("%-6f", processActivity.ixyj[ii]);
            pw.print(temp + " ");
        }
        pw.print("\r\n");
        pw.flush();

        try {
            fw.flush();
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


