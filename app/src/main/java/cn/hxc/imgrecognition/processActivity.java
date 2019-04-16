package cn.hxc.imgrecognition;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics. Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import cn.hxc.imgrecognitionSRI_OCR.R;

public class processActivity extends Activity {
    private TextView tv_photo;
    private ImageView iv_photo1;
    private ImageView iv_photo2;
    private Matrix matrix = new Matrix();
    private int[] newpixels;
    private int[] pixels;
    private int[] ThinNorpixels;
    private int width;
    private int height;
    private int NeedHeight=80;
    private int NeedWidth;
    private PreferencesService service;
    private String content;
    private Bitmap greyBitmap;
    private Bitmap screenBitmap;
    private int[] by;
    private int[] ThinNorBy;
    private int[] step;

    int phoneLineNum = 0;//手机里存的图片特征的行数
    int serveLineNum = 0;//手机里下载的服务器特征的行数
    int datanum = 0; //phoneLineNum + serveLineNum


    // ????????
    private float mScreenWidth;
    private float mScreenHeight;

    private SharedPreferences preferences;


    public static float[] ixyj;

    static {
        System.loadLibrary("processActivity");
    }

    public native String callint(int[] by1, int w, int h, String num,
                                 String win2, String whi2, String model, int flag);


    //创建一个Item格式的List
    private List<Item> list = new ArrayList<Item>();
    private MyListView listView;
    private ItemAdapter itemAdapter;
    //private TextView printText;

    //获取图片的下载地址的网址
    public String ImageUrl = "http://119.23.33.12/PaisService.asmx/GetImageUrlByImageid?";
    public String finalID = "";//最后转成Unicode合成的ID
    public String DownLoadImageXML = "";//包含图片的下载地址的XML
    public String DownLoadImageUrl = "";//下载图片的地址
    public Bitmap bitmapFromServer = null; //从数据库上下载的图片

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.process);

        listView=(MyListView)findViewById(R.id.listview);
        itemAdapter=new ItemAdapter(this,R.layout.listprocess,list);
        listView.setAdapter(itemAdapter);
        //printText = (TextView)findViewById(R.id.item_text);

        //label = (TextView) findViewById(R.id.label);
        WindowManager wm = (WindowManager) this
                .getSystemService(this.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        mScreenWidth = outMetrics.widthPixels;
        mScreenHeight = outMetrics.heightPixels;


        Rect frame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        // ???????
        int statusBarHeight = frame.top;
        View v = getWindow().findViewById(Window.ID_ANDROID_CONTENT);
        int contentTop = v.getTop();
        // statusBarHeight???????????????????
        int titleBarHeight = contentTop - statusBarHeight;
        imageProcess.noequl("statusBarHeight=", statusBarHeight);
        imageProcess.noequl("titleBarHeight=", titleBarHeight);

        iv_photo1 = (ImageView) findViewById(R.id.imgView1);//原图
        iv_photo2 = (ImageView) findViewById(R.id.imgView2);//归一化后的图像

        tv_photo= (TextView) findViewById(R.id.tv_photo);

        preferences = getSharedPreferences("set", MODE_PRIVATE);
        content = preferences.getString("content", "");
        try {
            greyScreen();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
     }

    public static BitmapFactory.Options getOption(BitmapFactory.Options opts) {
        opts.inPreferredConfig = Bitmap.Config.RGB_565;  //图片解码时使用的颜色格式
        opts.inPurgeable = true;
        opts.inInputShareable = true;
        opts.inJustDecodeBounds = false;
        return opts;
    }

    static class IImage{
        String ID;
        String name;
        String deviceFlag; //phone 来自手机  server来自服务器  判断是否来自于服务器从而决定是否要下载图片
        float distanse;
    }

    public void greyScreen() throws IOException {
        //long Starttime=System.currentTimeMillis();
        String path = Environment.getExternalStorageDirectory()
                + File.separator + "WR_LPAIS" + File.separator
                + "shotCamera.jpg";

        //取出手机中存贮的的已经被切割好的没有进行二值化的图像，以便于下一步的对图像的操作
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(path, options);  //加载手机存储卡中的图片

        BitmapFactory.Options opts = getOption(options);
        screenBitmap = BitmapFactory.decodeFile(path, opts);  //取到的手机中切割未被处理像素的的图片

        imageProcess imageprocess = new imageProcess();
        greyBitmap = imageprocess.greyToArray(screenBitmap);  //灰度化之后的图片
        //iv_photo.setImageBitmap(screenBitmap);//ImageView
        width = greyBitmap.getWidth();
        height = greyBitmap.getHeight();

        pixels = new int[width * height];
        greyBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        by = new int[width * height];
        step = new int[width * height];
        // int[] by1 = new int[width * height];
        for (int i = 0; i < width * height; i++) {
            by[i] = (0xff & pixels[i]);
        }

        SharedPreferences preferences = getSharedPreferences("set",
                MODE_PRIVATE);
        Editor editor = preferences.edit();
        int count = preferences.getInt("count", 1) + 1;
        editor.putInt("count", count);
        editor.commit();
        {
            int threshold = otsu(by, width, height, 0, 0, width, height);
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (by[i + j * width] < threshold) {
                        by[i + j * width] = 0;
                    } else {
                        by[i + j * width] = 255;
                    }
                }
            }
            int BinaryPixels[] = new int[width * height];
            for (int p = 0; p < height; p++) {
                for (int q = 0; q < width; q++) {
                    int gray = by[p * width + q];
                    int newcolor = (gray << 16) | (gray << 8) | (gray);
                    BinaryPixels[p * width + q] = newcolor;
                }
            }
            Bitmap binaryBitmap = Bitmap.createBitmap(BinaryPixels, 0, width,
                    width, height, Bitmap.Config.RGB_565);

            FileOutputStream fos1;
//begin归一化-------------------------------------------------------------------------------------------------------------------
            //归一化begin
            Projection pro1 = new Projection();
            //创建一个数组用于进行归一化
            int[] Nor;

            int begin_y = pro1.ProExtractBegin(pro1.ProjectionY(by, height, width), height);//y投影的第一个行
            int end_y = pro1.ProExtractEnd(pro1.ProjectionY(by, height, width), height);//y投影的最后一行
            int begin_x = pro1.ProExtractBegin(pro1.ProjectionX(by, height, width), width);//x投影的第一列
            int end_x = pro1.ProExtractEnd(pro1.ProjectionX(by, height, width), width);//x投影的最后一列

            int NorHeight = end_y - begin_y + 1;//归一化之前的高度
            int NorWidth = end_x - begin_x + 1;//归一化之前的宽度

            //提取切割后的图像
            Nor = new int[NorHeight * NorWidth];
            for (int i = begin_y; i <= end_y; i++) {
                for (int j = begin_x; j <= end_x; j++) {
                    Nor[(i - begin_y) * NorWidth + (j - begin_x)] = by[i * width + j];
                }
            }

            float YScale;
            float XScale;

            YScale = ((float) NeedHeight / NorHeight);

            if (NorWidth * YScale > width) {
                NorWidth = (int) (width / YScale);
            }

            NeedWidth = (int) (NorWidth * YScale);

            matrix.postScale(YScale, YScale); //长和宽放大缩小的比例

            Bitmap NorBitmap = Bitmap.createBitmap(binaryBitmap, begin_x, begin_y, NorWidth, NorHeight);
            //Bitmap a=new Bitmap();
            Bitmap resizeNorBitmap = Bitmap.createBitmap(NorBitmap, 0, 0, NorWidth, NorHeight, matrix, true);
//End归一化-------------------------------------------------------------------------------------------------------------------

            by = new int[height * width];

            for (int i = 0; i < height * width; i++) {
                by[i] = (0xff & BinaryPixels[i]);
            }
            ixyj = xyj(width, height, 1);

            //tv_photo.setText("细化+归一化后的图像");
            iv_photo1.setImageBitmap(screenBitmap);//原图
            iv_photo2.setImageBitmap(resizeNorBitmap);//归一化
            //iv_photo3.setImageBitmap(resizeNorThinBitmap);//细化
            //long Endtime=System.currentTimeMillis();

//End细化+归一化-------------------------------------------------------------------------------------------------------------

//Begin存储归一化后的图像和二值化图像-------------------------------------------------------------------------------------------------------

            try {
                File file1 = new File(
                        Environment.getExternalStorageDirectory()
                                + File.separator + "WR_LPAIS");

                //xyj
                fos1 = new FileOutputStream(file1 + File.separator
                        + "NorTemp.jpg");
                resizeNorBitmap.compress(CompressFormat.JPEG, 100, fos1);
                fos1.flush();
                fos1.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                File file1 = new File(
                        Environment.getExternalStorageDirectory()
                                + File.separator + "WR_LPAIS");

                //xyj
                fos1 = new FileOutputStream(file1 + File.separator
                        + "binaryTemp.jpg");
                binaryBitmap.compress(CompressFormat.JPEG, 100, fos1);
                fos1.flush();
                fos1.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
//存储图片结束-------------------------------------------------------------------------------------------------------------------

            File txtfile = new File(
                    Environment.getExternalStorageDirectory()
                            + File.separator + "WR_LPAIS" + File.separator + "txt");

            if (!txtfile.exists()) {
                txtfile.mkdirs();
            }

            File f = new File(
                    Environment.getExternalStorageDirectory()
                            + File.separator + "WR_LPAIS" + File.separator + "txt" + File.separator + "feature.txt");

            if (f.exists()) {
                phoneLineNum = getTextLines(f.toString());
            }

            File servef = new File(
                    Environment.getExternalStorageDirectory()
                            + File.separator + "WR_LPAIS" + File.separator + "dataBaseTxt" + File.separator + "serveTxt.txt");

            if (f.exists()) {
                serveLineNum = getTextLines(servef.toString());
            }

            datanum = phoneLineNum + serveLineNum;

            if (datanum < 3) {
                Toast.makeText(this, "数据库中只有" + datanum + "幅图像，数量过少（至少需要三幅图像）！", Toast.LENGTH_LONG).show();
                return;
            }

            int Threshold = 3;
            File tf = new File(
                    Environment.getExternalStorageDirectory()
                            + File.separator + "WR_LPAIS" + File.separator + "txt" + File.separator + "threshold.txt");
            if (!tf.exists()) {
                Threshold = 3;
            } else {
                FileReader fr = new FileReader(tf.toString());   //这里定义一个字符流的输入流的节点流，用于读取文件（一个字符一个字符的读取）
                BufferedReader br = new BufferedReader(fr);  // 在定义好的流基础上套接一个处理流，用于更加效率的读取文件（一行一行的读取）
                String stemp = null;
                stemp = br.readLine();
                if (stemp == null) {
                    Threshold = 3;
                    fr.close();
                    br.close();
                } else {
                    try {

                        Threshold = Integer.parseInt(stemp);

                    } catch (NumberFormatException e) {
                        Threshold = 3;
                    }
                    fr.close();
                    br.close();
                }
            }
            float[] yuzhi = {0.00001f, 0.00005f, 0.0001f, 1f};
            IImage[] iamgeID = new IImage[datanum];
            for (int i = 0; i < datanum; i++) {
                iamgeID[i] = new IImage();
            }
            float[][] base = new float[datanum][920];

            if (datanum > 0) {
                if(f.exists()){
                    getALLLines(f.toString(), 0, phoneLineNum, base, iamgeID);
                }
                if(servef.exists()){
                    getALLLines(servef.toString(), phoneLineNum, datanum, base, iamgeID);
                }
            }

            for (int i = 0; i < datanum; i++) {
                iamgeID[i].distanse = distance_xyj(ixyj, base[i]);
            }

            Arrays.sort(iamgeID, new MyComprator());

            int flag = 0;
            int NOcandidate = 3;
            File cf = new File(
                    Environment.getExternalStorageDirectory()
                            + File.separator + "WR_LPAIS" + File.separator + "txt" + File.separator + "NO_candidate.txt");
            if (!cf.exists()) {
                NOcandidate = 3;
            } else {
                FileReader fr = new FileReader(cf.toString());   //这里定义一个字符流的输入流的节点流，用于读取文件（一个字符一个字符的读取）
                BufferedReader br = new BufferedReader(fr);  // 在定义好的流基础上套接一个处理流，用于更加效率的读取文件（一行一行的读取）
                String stemp = null;
                stemp = br.readLine();
                if (stemp == null) {
                    NOcandidate = 3;
                    fr.close();
                    br.close();
                } else {
                    try {

                        NOcandidate = Integer.parseInt(stemp);

                    } catch (NumberFormatException e) {
                        NOcandidate = 3;
                    }
                    fr.close();
                    br.close();
                }
            }
            String name;
            String imgID;

            int trueIndex = 0;
            int tankuang = 1;
            for (int i = 0; i < NOcandidate && i < datanum && trueIndex < datanum; i++) {
                if (iamgeID[trueIndex].distanse > yuzhi[Threshold]) {
                    break;
                }
                //将书写人名字和序号改写成规定格式
                name = iamgeID[trueIndex].name;
                imgID = iamgeID[trueIndex].ID;

                if(iamgeID[0].name.equals("印刷体")){
                    //printText.setTextSize(TypedValue.COMPLEX_UNIT_SP,30);
                    //String tempStr="这是一幅印刷体~" + "相似距离："+iamgeID[0].distanse;
                    String tempStr="无法识别！";
                    Item item=new Item(tempStr,null); //picPath[i]为第i张图片的地址
                    list.add(item);
                    tankuang ++;
                    break;
                }

                if(iamgeID[trueIndex].name.equals("印刷体")){
                    //printText.setTextSize(TypedValue.COMPLEX_UNIT_SP,30);
                    i--;
                    trueIndex++;
                    continue;
                }
                    String temp;
                    String picPath=Environment.getExternalStorageDirectory() + File.separator + "WR_LPAIS" + File.separator + "ShowImage" + File.separator + imgID +".jpg";

                    Bitmap showBitmap = null;
                    if(iamgeID[trueIndex].deviceFlag.equals("phone")){
                        showBitmap = BitmapFactory.decodeFile(picPath);
                    }else if(iamgeID[trueIndex].deviceFlag.equals("server")){
                        showBitmap = getBmpFromServerThread(iamgeID[trueIndex].ID);
                    }
                    temp="第"+(i+1)+"候选人姓名："+iamgeID[trueIndex].name+"  相似距离："+iamgeID[trueIndex].distanse;
                    Item item=new Item(temp,showBitmap); //picPath[i]为第i张图片的地址
                    list.add(item);  //添加item
                    flag++;
                    trueIndex++;
            }
            if (flag == 0 && tankuang == 1) {
                Toast.makeText(this, "没有在数据库找到对应的书写人！", Toast.LENGTH_LONG).show();
            }
            takePhoto tp = new takePhoto();
        }
    }
    public Bitmap getBmpFromServerThread(String ImageID){
        finalID = "Imageid=" + URLEncoder.encode(ImageID);
        Callable<String> callable = new Callable<String>() {
            public String call() throws Exception {
                DownLoadImageXML = GETUtils(ImageUrl,finalID);
                return DownLoadImageXML;
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
        DownLoadImageUrl = getContext(DownLoadImageXML);
        DownLoadImageUrl = DownLoadImageUrl.substring(1,DownLoadImageUrl.length()-1);

        Callable<String> callable2 = new Callable<String>() {
            public String call() throws Exception {
                bitmapFromServer = getBmpFromServer(DownLoadImageUrl);
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
        return bitmapFromServer;
    }

    public Bitmap getBmpFromServer(String url) {
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
        }catch (Exception e){
            e.printStackTrace();
        }
        return msg;
    }

    static class MyComprator implements Comparator {
        public int compare(Object arg0, Object arg1) {
            IImage t1=(IImage)arg0;
            IImage t2=(IImage)arg1;
            return t1.distanse>t2.distanse? 1:-1;
        }
    }

    public static int getTextLines(String path) throws IOException {

        FileReader fr = new FileReader(path);   //这里定义一个字符流的输入流的节点流，用于读取文件（一个字符一个字符的读取）
        BufferedReader br = new BufferedReader(fr);  // 在定义好的流基础上套接一个处理流，用于更加效率的读取文件（一行一行的读取）
        int x = 0;   // 用于统计行数，从0开始
        while(br.readLine() != null) { //  readLine()方法是按行读的，返回值是这行的内容
            String string = br.toString();
            x++;   // 每读一行，则变量x累加1
        }
        fr.close();
        br.close();
        return x;  //返回总的行数
    }

    public static int getALLLines(String path, int begin, int num, float[][] base, IImage[] label) throws IOException {

        FileReader fr = new FileReader(path);   //这里定义一个字符流的输入流的节点流，用于读取文件（一个字符一个字符的读取）
        BufferedReader br = new BufferedReader(fr);  // 在定义好的流基础上套接一个处理流，用于更加效率的读取文件（一行一行的读取）
        int index = 0;

        String[][] rows = new String[num][930];
        String temp = null;
        while((temp=br.readLine()) != null) { //  readLine()方法是按行读的，返回值是这行的内容
            String string = br.toString();
            rows[index] = temp.split("( )+");
            String[] string2 = rows[index];
            index++;
        }
        String deviceFlag = "";
        if(Pattern.matches(".*feature.*", path)){
            deviceFlag = "phone";
        }else if(Pattern.matches(".*serveTxt.*", path)){
            deviceFlag = "server";
        }
        for(int i = begin; i<num;i++)
        {
            label[i].ID = (rows[i - begin][0]);
            label[i].name = (rows[i - begin][1]);
            label[i].deviceFlag = deviceFlag;
            for(int j = 0;j <920; j++)
            {
                 base[i][j] =  Float.parseFloat(rows[i - begin][j+2]);
            }
        }
        fr.close();
        br.close();
        return 1;  //返回总的行数
    }

    static float distance_xyj(float[] feature1, float[] feature2) {
        if (feature1.length != feature2.length) {
            return -1;
        }
        float dis = 0;
        for (int i = 0; i < feature1.length; i++) {
            dis = (feature1[i] - feature2[i]) * (feature1[i] - feature2[i]) + dis;
        }
        return dis;

    }

    float[] xyj(int width, int height, int flag) {
        int i, j;
        int[] mfeature1 = new int[8];
        int[] mfeature2 = new int[16];
        int[] mfeature3 = new int[24];
        int[] mfeature4 = new int[32];
//		int[] mfeature5 = new int[40];
//		int[] mfeature6 = new int[48];
//		int[] mfeature7 = new int[56];

        int pixel;
        float[] feature1 = new float[28];
        float[] feature2 = new float[120];
        float[] feature3 = new float[276];
        float[] feature4 = new float[496];
        if (height < 9 || width < 9) {
            return new float[1];
        }
        for (i = 4; i < height - 4; i++) {
            for (j = 4; j < width - 4; j++) {
                pixel = j + i * width;
                if (by[pixel] == 0) {
                    mfeature1[0] = by[pixel + 1];
                    mfeature1[1] = by[pixel - width - 1];
                    mfeature1[2] = by[pixel - width];
                    mfeature1[3] = by[pixel - width - 1];
                    mfeature1[4] = by[pixel - 1];
                    mfeature1[5] = by[pixel + width - 1];
                    mfeature1[6] = by[pixel + width];
                    mfeature1[7] = by[pixel + width + 1];

                    mfeature2[0] = by[pixel + 2];
                    mfeature2[1] = by[pixel - 1 * width + 2];
                    mfeature2[2] = by[pixel - 2 * width + 2];
                    mfeature2[3] = by[pixel - 2 * width + 1];
                    mfeature2[4] = by[pixel - 2 * width];
                    mfeature2[5] = by[pixel - 2 * width - 1];
                    mfeature2[6] = by[pixel - 2 * width - 2];
                    mfeature2[7] = by[pixel - 1 * width - 2];
                    mfeature2[8] = by[pixel - 2];
                    mfeature2[9] = by[pixel + 1 * width - 2];
                    mfeature2[10] = by[pixel + 2 * width - 2];
                    mfeature2[11] = by[pixel + 2 * width - 1];
                    mfeature2[12] = by[pixel + 2 * width];
                    mfeature2[13] = by[pixel + 2 * width + 1];
                    mfeature2[14] = by[pixel + 2 * width + 2];
                    mfeature2[15] = by[pixel + 1 * width + 2];

                    mfeature3[0] = by[pixel + 3];
                    mfeature3[1] = by[pixel - 1 * width + 3];
                    mfeature3[2] = by[pixel - 2 * width + 3];
                    mfeature3[3] = by[pixel - 3 * width + 3];
                    mfeature3[4] = by[pixel - 3 * width + 2];
                    mfeature3[5] = by[pixel - 3 * width + 1];
                    mfeature3[6] = by[pixel - 3 * width + 0];
                    mfeature3[7] = by[pixel - 3 * width - 1];
                    mfeature3[8] = by[pixel - 3 * width - 2];
                    mfeature3[9] = by[pixel - 3 * width - 3];
                    mfeature3[10] = by[pixel - 2 * width - 3];
                    mfeature3[11] = by[pixel - 1 * width - 3];
                    mfeature3[12] = by[pixel - 0 * width - 3];
                    mfeature3[13] = by[pixel + 1 * width - 3];
                    mfeature3[14] = by[pixel + 2 * width - 3];
                    mfeature3[15] = by[pixel + 3 * width - 3];
                    mfeature3[16] = by[pixel + 3 * width - 2];
                    mfeature3[17] = by[pixel + 3 * width - 1];
                    mfeature3[18] = by[pixel + 3 * width - 0];
                    mfeature3[19] = by[pixel + 3 * width + 1];
                    mfeature3[20] = by[pixel + 3 * width + 2];
                    mfeature3[21] = by[pixel + 3 * width + 3];
                    mfeature3[22] = by[pixel + 2 * width + 3];
                    mfeature3[23] = by[pixel + 1 * width + 3];

                    mfeature4[0] = by[pixel + 4];
                    mfeature4[1] = by[pixel - 1 * width + 4];
                    mfeature4[2] = by[pixel - 2 * width + 4];
                    mfeature4[3] = by[pixel - 3 * width + 4];
                    mfeature4[4] = by[pixel - 4 * width + 4];
                    mfeature4[5] = by[pixel - 4 * width + 3];
                    mfeature4[6] = by[pixel - 4 * width + 2];
                    mfeature4[7] = by[pixel - 4 * width + 1];
                    mfeature4[8] = by[pixel - 4 * width + 0];
                    mfeature4[9] = by[pixel - 4 * width - 1];
                    mfeature4[10] = by[pixel - 4 * width - 2];
                    mfeature4[11] = by[pixel - 4 * width - 3];
                    mfeature4[12] = by[pixel - 4 * width - 4];
                    mfeature4[13] = by[pixel - 3 * width - 4];
                    mfeature4[14] = by[pixel - 2 * width - 4];
                    mfeature4[15] = by[pixel - 1 * width - 4];
                    mfeature4[16] = by[pixel - 0 * width - 4];
                    mfeature4[17] = by[pixel + 1 * width - 4];
                    mfeature4[18] = by[pixel + 2 * width - 4];
                    mfeature4[19] = by[pixel + 3 * width - 4];
                    mfeature4[20] = by[pixel + 4 * width - 4];
                    mfeature4[21] = by[pixel + 4 * width - 3];
                    mfeature4[22] = by[pixel + 4 * width - 2];
                    mfeature4[23] = by[pixel + 4 * width - 1];
                    mfeature4[24] = by[pixel + 4 * width - 0];
                    mfeature4[25] = by[pixel + 4 * width + 1];
                    mfeature4[26] = by[pixel + 4 * width + 2];
                    mfeature4[27] = by[pixel + 4 * width + 3];
                    mfeature4[28] = by[pixel + 4 * width + 4];
                    mfeature4[29] = by[pixel + 3 * width + 4];
                    mfeature4[30] = by[pixel + 2 * width + 4];
                    mfeature4[31] = by[pixel + 1 * width + 4];

                    int a, b, c, d;
                    int flag2 = 0;
                    int flag3 = 0;
                    int flag41 = 0;
                    int flag42 = 0;

                    for (a = 0; a < 8; a++) {
                        if (mfeature1[a] == 0) {
                            for (b = a + 1; b < 8; b++) {
                                if (mfeature1[b] == 0) {
                                    feature1[a * (15 - a) / 2 + b - a - 1] = feature1[a * (15 - a) / 2 + b - a - 1] + 1;
                                    a = b;
                                    if (flag == 1) {
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    for (a = 0; a < 16; a++) {
                        if (mfeature2[a] == 0) {
                            for (b = a + 1; b < 16; b++) {
                                if (mfeature2[b] == 0) {
                                    for (c = 0; c < 8; c++) {
                                        if (a == 2 * c) {
                                            for (d = 0; d < 8; d++) {
                                                if (b == 2 * d) {
                                                    feature1[c * (15 - c) / 2 + d - c - 1] = feature1[c * (15 - c) / 2 + d - c - 1] + 1;
                                                    a = b;
                                                    flag2 = 1;
                                                    if (flag == 1) {
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (flag2 == 0) {
                                        feature2[a * (31 - a) / 2 + b - a - 1] = feature2[a * (31 - a) / 2 + b - a - 1] + 1;
                                        a = b;
                                        if (flag == 1) {
                                            break;
                                        }
                                    }
                                    flag2 = 0;
                                }
                            }
                        }
                    }

                    for (a = 0; a < 24; a++) {
                        if (mfeature3[a] == 0) {
                            for (b = a + 1; b < 24; b++) {
                                if (mfeature3[b] == 0) {
                                    for (c = 0; c < 8; c++) {
                                        if (a == 3 * c) {
                                            for (d = 0; d < 8; d++) {
                                                if (b == 3 * d) {
                                                    feature1[c * (15 - c) / 2 + d - c - 1] = feature1[c * (15 - c) / 2 + d - c - 1] + 1;
                                                    a = b;
                                                    flag3 = 1;
                                                    if (flag == 1) {
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (flag3 == 0) {
                                        feature3[a * (47 - a) / 2 + b - a - 1] = feature3[a * (47 - a) / 2 + b - a - 1] + 1;
                                        a = b;
                                        if (flag == 1) {
                                            break;
                                        }
                                    }
                                    flag3 = 0;
                                }
                            }
                        }
                    }

                    for (a = 0; a < 32; a++) {
                        if (mfeature4[a] == 0) {
                            for (b = a + 1; b < 32; b++) {
                                if (mfeature4[b] == 0) {
                                    for (c = 0; c < 8; c++) {
                                        if (a == 4 * c) {
                                            for (d = 0; d < 8; d++) {
                                                if (b == 4 * d) {
                                                    feature1[c * (15 - c) / 2 + d - c - 1] = feature1[c * (15 - c) / 2 + d - c - 1] + 1;
                                                    a = b;
                                                    flag41 = 1;
                                                    if (flag == 1) {
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    for (c = 0; c < 16; c++) {
                                        if (a == 2 * c) {
                                            for (d = 0; d < 16; d++) {
                                                if (b == 2 * d) {
                                                    if (flag41 == 0) {
                                                        feature2[c * (31 - c) / 2 + d - c - 1] = feature2[c * (31 - c) / 2 + d - c - 1] + 1;
                                                        a = b;
                                                        flag42 = 1;
                                                        if (flag == 1) {
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (flag41 == 0 && flag42 == 0) {
                                        feature4[a * (63 - a) / 2 + b - a - 1] = feature4[a * (63 - a) / 2 + b - a - 1] + 1;
                                        a = b;
                                        if (flag == 1) {
                                            break;
                                        }
                                    }
                                    flag41 = 0;
                                    flag42 = 0;
                                }
                            }
                        }
                    }
                }
            }
        }

        int a = 0;
        float sum = 0;
        for (a = 0; a < 28; a++) {
            sum = sum + feature1[a];
        }
        for (a = 0; a < 120; a++) {
            sum = sum + feature2[a];
        }
        for (a = 0; a < 276; a++) {
            sum = sum + feature3[a];
        }
        for (a = 0; a < 496; a++) {
            sum = sum + feature4[a];
        }

        float[] probability = new float[920];

        for (a = 0; a < 28; a++) {
            //para[a]=feature1[a]/sum;
            probability[a] = feature1[a] / sum;
        }
        for (a = 0; a < 120; a++) {
            //para[a]=feature1[a]/sum;
            probability[a + 28] = feature2[a] / sum;
        }

        for (a = 0; a < 276; a++) {
            //para[a]=feature1[a]/sum;
            probability[a + 28 + 120] = feature3[a] / sum;
        }
        for (a = 0; a < 496; a++) {
            //para[a]=feature1[a]/sum;
            probability[a + 28 + 120 + 276] = feature4[a] / sum;
        }

        return probability;
    }

    public Bitmap createBitmap(Bitmap bitmap, int count, int num) {
        int width = bitmap.getWidth() / count;
        int hight = bitmap.getHeight();
        return Bitmap.createBitmap(bitmap, num * width, 0, width, hight);
    }

    private void assetsDataToSD(String filePath, String fileName)
            throws IOException {
        InputStream myInput;
        myInput = this.getAssets().open(fileName);

        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        // ?��?sd????????
        if (sdCardExist) {
            sdDir = new File(filePath);// ???????
        } else {
//			Toast.makeText(this, "??????sd??", 1).show();
        }
        if (!sdDir.exists()) {
            sdDir.mkdirs();
        }
        // File dir_file=new
        // File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),fileName);
        OutputStream myOutput = new FileOutputStream(sdDir + "//" + fileName);

        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }
        myOutput.flush();
        myInput.close();
        myOutput.close();
    }

    public void saveResultToSD(String num, String filePath) throws IOException {
        File file = new File(filePath);
        FileWriter myOutput = new FileWriter(file, true);
        if (!file.exists()) {
            file.mkdirs();
        }
        myOutput.flush();
        myOutput.close();
    }

    /*
     * ?????
     */
    public static int otsu(int[] by, int w, int h, int x0, int y0, int dx, int dy) {
        // unsigned char *np; // ??????
        int thresholdValue = 1; // ???
        int ihist[] = new int[256]; // ?????????256????
        int k; // various counters
        int n, n1, n2;
        double m1, m2, sum, csum, fmax, sb;
        for (int j = y0; j < y0 + dy; j++)
            for (int i = x0; i < x0 + dx; i++) {
                {
                    try {
                        int np = by[j * w + i];
                        ihist[np]++;
                    } catch (Exception e) {
                        System.out.println("yuejie .........." + i + "  " + j);
                    }
                }
            }

        sum = csum = 0.0;
        n = 0;
        for (k = 0; k <= 255; k++) {
            sum += (double) k * (double) ihist[k]; // x*f(x) ??????
            n += ihist[k]; // f(x) ????
        }
        if (n == 0) {
            return (160);
        }
        fmax = -1.0;
        n1 = 0;
        for (k = 0; k <= 255; k++) {
            n1 += ihist[k];
            if (n1 == 0) {
                continue;
            }
            n2 = n - n1;
            if (n2 == 0) {
                break;
            }
            csum += (double) k * ihist[k];
            m1 = csum / n1;
            m2 = (sum - csum) / n2;
            sb = (double) n1 * (double) n2 * (m1 - m2) * (m1 - m2);

            if (sb > fmax) {
                fmax = sb;
                thresholdValue = k;
            }
        }
        return thresholdValue;// (thresholdValue);
    }

    public void onBackPressed(){
        destoryView();
        Intent intent = new Intent(processActivity.this, takePhoto.class);
        startActivity(intent);
    }
    public void savePic(View v)
    {
        //processActivity.this.finish();
        Intent intent = new Intent(this, inputInformation.class);
        startActivity(intent);
        //inputTitleDialog();
    }

    public void ProPicSignOut(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    void destoryView() {
        if (iv_photo1 != null) {
            iv_photo1 = null;
        }
        if (iv_photo2 != null) {
            iv_photo2 = null;
        }
        if (greyBitmap != null) {
            greyBitmap.recycle();
            greyBitmap = null;
        }
        if (screenBitmap != null) {
            screenBitmap.recycle();
            screenBitmap = null;
        }
  }
}
