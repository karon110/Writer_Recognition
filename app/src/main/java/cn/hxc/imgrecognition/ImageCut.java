package cn.hxc.imgrecognition;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import cn.hxc.imgrecognitionSRI_OCR.R;

import com.edmodo.cropper.CropImageView;

import org.opencv.core.Point;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import static cn.hxc.imgrecognition.processActivity.getOption;

/**
 * Created by 刘欢 on 2018/12/8.
 */

public class ImageCut extends Activity {
    public ImageView BigImage;
    Button cropButton;
    Button quitCut;
    takePhoto tp = new takePhoto();
    CropImageView CropImageCamera;
    String fullBmpPath = Environment.getExternalStorageDirectory() + File.separator + "WR_LPAIS" + File.separator + "originalPic.jpg";
    Bitmap fullBmp;

    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        requestWindowFeature(Window.FEATURE_NO_TITLE);// ȥ������
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// ����ȫ��
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.image_cut);

        CropImageCamera = (CropImageView) findViewById(R.id.CropImageCamera);
        cropButton = (Button) findViewById(R.id.cutPic);
        fullBmp = getBmpFromPhone(fullBmpPath);
        CropImageCamera.setImageBitmap(fullBmp);

        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                final Bitmap croppedImage = CropImageCamera.getCroppedImage();
                if(savePicToPhone(croppedImage)==1){
                    Intent intent = new Intent(ImageCut.this, processActivity.class);
                    startActivity(intent);
                }
                else{
                    Intent intent = new Intent(ImageCut.this, takePhoto.class);
                    startActivity(intent);
                }
            }
        });
    }

    public int savePicToPhone(Bitmap bitmap){
        try {
            ArrayList<Point> points = new ArrayList<Point>();

            points = isHorizontal.getP(bitmap);

            if(points==null){
                Toast.makeText(this,"图片不合格，请重拍！",Toast.LENGTH_LONG).show();
                return -1;
            }

            int height = (int)(points.get(1).y - points.get(0).y);
            int width = (int)(points.get(2).x - points.get(1).x);

            int textTop = (int)(points.get(0).y + height/4);
            int textBot = (int)(points.get(1).y - height/4.8);
            int textLeft = (int)(points.get(1).x + width/17.2);
            int textRight = (int)(points.get(2).x - width/15.5);

            Bitmap rotaBitmap = Bitmap.createBitmap(bitmap, textLeft, textTop, textRight - textLeft, textBot - textTop);

            File file1 = new File(Environment.getExternalStorageDirectory() + File.separator + "WR_LPAIS");
            FileOutputStream fos1;
            fos1 = new FileOutputStream(file1 + File.separator + "shotCamera.jpg");
            rotaBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos1);
            fos1.flush();
            fos1.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    public Bitmap getBmpFromPhone(String path){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.Options opts = getOption(options);
        Bitmap screenBitmap = BitmapFactory.decodeFile(path, opts);  //取到的手机中切割未被处理像素的的图片
        return screenBitmap;
    }

    public void nophoto(View v){
        Intent intent = new Intent(this, takePhoto.class);
        startActivity(intent);
    }
}
