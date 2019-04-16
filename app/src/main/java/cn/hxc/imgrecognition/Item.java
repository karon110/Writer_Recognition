package cn.hxc.imgrecognition;

import android.graphics.Bitmap;

/**
 * Created by 刘欢 on 2018/4/21.
 */

public class Item {
    private String txtContent;  //显示的文本内容
    private Bitmap bitmap;     //要显示的图片的地址

    //构造函数
    public Item(String txtContent, Bitmap bitmap) {
        this.txtContent = txtContent;
        this.bitmap = bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setTxtContent(String txtContent) {
        this.txtContent = txtContent;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public String getTxtContent() {
        return txtContent;
    }
}
