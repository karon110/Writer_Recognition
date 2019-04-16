package cn.hxc.imgrecognition;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.telecom.Call;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.hxc.imgrecognitionSRI_OCR.R;

/**
 * Created by Administrator on 2019/4/11.
 */

public class blackListFromDB extends Activity implements Callable<String> {

    public String listResult="";

    private List<DBItem> list = new ArrayList<DBItem>();
    private MyListView dbListView;
    private DBAdapter dbmAdapter;

    public String downloadTxtXml = "";

    String phoneTxt = Environment.getExternalStorageDirectory()+ File.separator + "WR_LPAIS" + File.separator  + "txt" +
            File.separator  + "feature.txt"; //手机里存本地txt（特征）的地址
    String serveTxt = Environment.getExternalStorageDirectory()+ File.separator + "WR_LPAIS" + File.separator  + "dataBaseTxt" +
            File.separator  + "serveTxt.txt"; // 手机里存数据库txt（特征）的地址

    public static String GetBlacklist="http://119.23.33.12/PaisService.asmx/GetBlacklist";//需要上传txt的URL
    public static String GetImagefeaturesByBlacklists="http://119.23.33.12/PaisService.asmx/GetImagefeaturesByBlacklists?";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blacklistfromdb);//软件activity的布局

        dbListView=(MyListView)findViewById(R.id.dblistview);
        dbmAdapter = new DBAdapter(this,R.layout.listdb,list);
        dbListView.setAdapter(dbmAdapter);

        blackListFromDB ctt = new blackListFromDB();
        FutureTask<String> ft = new FutureTask<>(ctt);
        new Thread(ft,"有返回值的线程").start();

        try
        {
            listResult = ft.get();
            System.out.println("子线程的返回值："+ft.get());
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } catch (ExecutionException e)
        {
            e.printStackTrace();
        }
        listResult = getContext(listResult);
        listResult = listResult.substring(1,listResult.length()-1);
        groupInfo[] groupinfo = string2NameAndId(listResult);
        for(int i = 0; i < groupinfo.length; i++){
            DBItem item=new DBItem(groupinfo[i].ListId, groupinfo[i].ListName,false);
            list.add(item);  //添加item
        }
    }

    @Override
    public String call() throws Exception
    {
        listResult = GETUtilImgs(GetBlacklist);  //上传TXT
        return listResult;
    }

    public String GETUtilImgs(String listUrl){
        String result = "";
        try{
            result = HttpUtils.doGet(listUrl);
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public void choose(View v){
        int index = 0;
        String onloadString = "";
        for(int i = 0; i < list.size(); i++){
            if(list.get(i).getIsCheck() == true){
                index++;
                onloadString += list.get(i).getListId() + "::";
            }
        }
        onloadString = "IMGMSG::" + MainActivity.userName + "::" +String.valueOf(index) + "::" + onloadString + "END";
        onloadString = "Msglistids=" + URLEncoder.encode(onloadString);

        final String finalOnloadString = onloadString;
        String result = "";
        Callable<String> callable = new Callable<String>() {
            public String call() throws Exception {
                downloadTxtXml = GETUtils(GetImagefeaturesByBlacklists,finalOnloadString);
                return downloadTxtXml;
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
        //String downloadTxtUrl = getContext(downloadTxtXml).substring(1,downloadTxtXml.length()-1);
        String downloadTxtUrl = getContext(downloadTxtXml);
        downloadTxtUrl = downloadTxtUrl.substring(1,downloadTxtUrl.length()-1);
        String rootPath = Environment.getExternalStorageDirectory()+ File.separator + "WR_LPAIS" + File.separator  + "dataBaseTxt";
        downLoad(downloadTxtUrl, rootPath);
    }
    /**
     * 从服务器下载文件
     * @param path 下载文件的地址
     * @param rootName 文件名字
     */
    public void downLoad(final String path, final String rootName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(path);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setReadTimeout(5000);
                    con.setConnectTimeout(5000);
                    con.setRequestProperty("Charset", "UTF-8");
                    con.setRequestMethod("GET");
                    if (con.getResponseCode() == 200) {
                        InputStream is = con.getInputStream();//获取输入流
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                        FileOutputStream fileOutputStream = null;//文件输出流
                        if (is != null) {
                            File rootFile = new File(rootName);
                            if(!rootFile.exists()){
                                rootFile.mkdirs();
                            }
                            //从下载txt的url中获取的txt文本的名字
                            String subFile = "serveTxt.txt";
                            String FileName = rootName + File.separator + subFile;
                            File file = new File(FileName);
                            if (!file.exists()) {
                                file.createNewFile();
                            }
                            fileOutputStream = new FileOutputStream(file,true);//指定文件保存路径，代码看下一步
                            byte[] buf = new byte[1024];
                            int ch;
                            String nowString;//将读到的byte数据转化成string
                            String serverStringID;//提出当前读到一行信息的ImageID

                            while (true) {
                                nowString = reader.readLine();
                                serverStringID = nowString.substring(0,nowString.indexOf(" "));
                                if(nowString!=null){
                                    if(findUnique(phoneTxt,serveTxt,serverStringID)){
                                        fileOutputStream.write((nowString+"\n").getBytes());//将获取到的流写入文件中
                                    }
                                }
                                else
                                    break;
                            }
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.flush();
                            fileOutputStream.close();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public boolean findUnique(String phoneTxt, String serveTxt, String ID){
        boolean flag = true;//设置是否唯一的标识符
        File phoneTxtFile = new File(phoneTxt);
        Vector<String> vector = new Vector<String>(); //读取手机里txt的所有的ID
        //手机里本地的txt文本存在就比较
        if (phoneTxtFile.exists()){
            vector = readLine(phoneTxt);
            for(int i = 0; i < vector.size(); i++){
                if(vector.get(i).equals(ID)){
                    flag = false;
                    break;
                }
            }
        }
        //服务器下载的txt文本存在就比较
        File serveTxtFile = new File(phoneTxt);
        if (phoneTxtFile.exists()) {
            vector = readLine(serveTxt);
            for(int i = 0; i < vector.size(); i++){
                if(vector.get(i).equals(ID))
                {
                    flag = false;
                    break;
                }
            }
        }
        return flag;
    }
    //读取txt文本里的ID
    public static Vector<String> readLine(String path){
        Vector<String> vector = new Vector<String>();
        try{
            FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);

            String nowLine = "";//将当前读到的一行转化成字符串
            String temp = null;
            while((nowLine = br.readLine()) != null) {
                nowLine = nowLine.substring(0,nowLine.indexOf(" "));
                vector.add(nowLine);
            }
            fr.close();
            br.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return vector;
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

    public static String getContext(String html) {
        List resultList = new ArrayList();
        Pattern p = Pattern.compile(">([^<]+)<");//正则表达式
        Matcher m = p.matcher(html);
        while (m.find()) {
            resultList.add(m.group(1));
        }
        return resultList.toString();
    }

    private static groupInfo[] string2NameAndId(String dbString){
        String[] stringArray = dbString.split("\\::");
        int arrayLength = stringArray.length;
        if(!stringArray[0].equals("IMGMGS")&&!stringArray[arrayLength - 1].equals("END")){
            return null;
        }
        int groupNum = Integer.valueOf(stringArray[1]);
        groupInfo[] groupinfo = new groupInfo[groupNum];
        int stringIndex = 2;
        for(int i = 0; i < groupNum; i++){
            groupinfo[i] = new groupInfo();
            groupinfo[i].ListId = stringArray[stringIndex];
            stringIndex ++;
            groupinfo[i].ListName = stringArray[stringIndex];
            stringIndex ++;
        }
        return groupinfo;
    }
    public static class groupInfo{
        String ListId;
        String ListName;
    }
}
