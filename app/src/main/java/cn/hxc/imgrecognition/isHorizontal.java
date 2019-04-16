package cn.hxc.imgrecognition;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class isHorizontal {

    public static boolean check(Bitmap image_bitmap){
        if(image_bitmap == null)
            return false;
        Mat img = new Mat();
        Utils.bitmapToMat(image_bitmap,img);
        Log.i("test",img.height()+" "+img.width());
        while(img.height()*img.width()>540*960+10)
            Imgproc.resize(img,img,new Size((int)(img.width()/2),(int)(img.height()/2)));
        //Imgproc.resize(img,img,new Size(540,960));
        Mat gray = new Mat();
        //转灰度图
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGRA2GRAY);
        Mat binary = new Mat();
        //二值化
        Imgproc.adaptiveThreshold(gray,binary,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY_INV,45  ,5);
        //Imgproc.threshold(gray,binary,120,255,Imgproc.THRESH_BINARY);
        List<MatOfPoint> contours = new ArrayList<>();
        //hierarchy的结构[Next, previous, Child, parent]
        Mat hierarchy = new Mat();
        //寻找轮廓
        Imgproc.findContours(binary,contours,hierarchy,Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);
        int sum = 0;
        List<Rect>result = new ArrayList<>();

        for(int i=0;i<contours.size();i++){
            //判断是否有包含轮廓
            if(((int)hierarchy.get(0,i)[2]) == -1)
                continue;
            int index = (int)hierarchy.get(0,i)[2];
            if(((int)hierarchy.get(0,index)[2]) == -1)
                continue;
            int index3 = (int)hierarchy.get(0,index)[2];
            int index2 = -1;
            //最外层轮廓的外接矩形
            Rect rect1 = Imgproc.boundingRect(contours.get(i));
            //次外层
            Rect rect2 = Imgproc.boundingRect(contours.get(index));
            double w1 = rect1.width;
            double h1 = rect1.height;
            double w2 = rect2.width;
            double h2 = rect2.height;
            //判断是否为正方形
            if(Math.abs(w1/h1-1)>0.1 || Math.abs(w2/h2-1)>0.1)
                continue;
            while(index3 != -1 ){
                Rect rect_temp = Imgproc.boundingRect(contours.get(index3));
                double w3 = rect_temp.width;
                double h3 = rect_temp.height;
                if(Math.abs(w1/w3-2)<1.0 && Math.abs(w3/h3-1)<=0.1){
                    if(hierarchy.get(0,index3)[2] == -1){
                        index2 = index3;
                        break;
                    }
                }
                index3 = (int)hierarchy.get(0,index3)[0];
            }
            if(index2 == -1)
                continue;
            result.add(rect1);
        }
        Log.i("test",result.size()+" ");
        if(result.size()!=3)
            return false;
        List<Point>points = new ArrayList<>();
        List<Double>dist = new ArrayList<>();
        for(int i=0;i<3;i++){
            Point temp = new Point(result.get(i).x+result.get(i).width/2.0,
                    result.get(i).y+result.get(i).height/2.0);

            points.add(temp);
        }

        //判断左右分别几个点
        points.sort(new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                return o1.x<o2.x?-1:1;
            }
        });
        if(Math.abs(points.get(0).x-points.get(1).x)>Math.abs(points.get(1).x-points.get(2).x))
            return false;
        Log.i("test_","左右ok");
        //判断上下分别几个点
        points.sort(new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                return o1.y<o2.y?-1:1;
            }
        });
        if(Math.abs(points.get(0).y-points.get(1).y)<Math.abs(points.get(1).y-points.get(2).y))
            return false;
        Log.i("test_","上下ok");
        for(int i=0;i<3;i++){
            Double dist_temp = Math.pow((points.get(i).x-points.get((i+1)%3).x),2)
                    + Math.pow((points.get(i).y-points.get((i+1)%3).y),2);
            dist_temp =Math.pow(dist_temp,0.5);
            dist.add(dist_temp);
        }
        for(int i=0;i<3;i++){
            Point temp = points.get(i);
            Log.i("test_point",temp.x+" "+temp.y);
        }

        double k = (points.get(2).y-points.get(1).y)/(points.get(2).x-points.get(1).x+0.00001);
        Log.i("test_angle",k+" ");
        if(Math.abs(k)>0.09)
            return false;
        Collections.sort(dist);
        Log.i("test",dist.get(0)+" "+dist.get(1));
        double t = (dist.get(0)*dist.get(0) + dist.get(1)*dist.get(1)-dist.get(2)*dist.get(2))
                /(2*dist.get(0)*dist.get(1));
        double angle = Math.acos(t)*180/Math.PI;
        Log.i("test",angle+" ");
        if(Math.abs(angle-90)<5.0)
            return true;
        else
            return false;
    }

    public static ArrayList<Point> getP(Bitmap image_bitmap){
        if(image_bitmap == null)
            return null;
        Mat img = new Mat();
        Utils.bitmapToMat(image_bitmap,img);
        Log.i("test-",img.height()+" "+img.width());
        //while(img.height()*img.width()>540*960+10)
        //Imgproc.resize(img,img,new Size((int)(img.width()/2),(int)(img.height()/2)));
        //Imgproc.resize(img,img,new Size(540,960));
        Mat gray = new Mat();
        //转灰度图
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGRA2GRAY);
        Mat binary = new Mat();
        //二值化
        Imgproc.adaptiveThreshold(gray,binary,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY_INV,65  ,5);
        //Imgproc.threshold(gray,binary,120,255,Imgproc.THRESH_BINARY);
        List<MatOfPoint> contours = new ArrayList<>();
        //hierarchy的结构[Next, previous, Child, parent]
        Mat hierarchy = new Mat();
        //寻找轮廓
        Imgproc.findContours(binary,contours,hierarchy,Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);
        int sum = 0;
        List<Rect>result = new ArrayList<>();

        for(int i=0;i<contours.size();i++){
            //判断是否有包含轮廓
            if(((int)hierarchy.get(0,i)[2]) == -1)
                continue;
            int index = (int)hierarchy.get(0,i)[2];
            if(((int)hierarchy.get(0,index)[2]) == -1)
                continue;
            int index3 = (int)hierarchy.get(0,index)[2];
            int index2 = -1;
            //最外层轮廓的外接矩形
            Rect rect1 = Imgproc.boundingRect(contours.get(i));
            //次外层
            Rect rect2 = Imgproc.boundingRect(contours.get(index));
            double w1 = rect1.width;
            double h1 = rect1.height;
            double w2 = rect2.width;
            double h2 = rect2.height;
            //判断是否为正方形
            if(Math.abs(w1/h1-1)>0.1 || Math.abs(w2/h2-1)>0.1)
                continue;
            while(index3 != -1 ){
                Rect rect_temp = Imgproc.boundingRect(contours.get(index3));
                double w3 = rect_temp.width;
                double h3 = rect_temp.height;
                if(Math.abs(w1/w3-2)<1.0 && Math.abs(w3/h3-1)<=0.1){
                    if(hierarchy.get(0,index3)[2] == -1){
                        index2 = index3;
                        break;
                    }
                }
                index3 = (int)hierarchy.get(0,index3)[0];
            }
            if(index2 == -1)
                continue;
            result.add(rect1);
        }
        Log.i("test*",result.size()+" ");
        if(result.size()!=3)
            return null;
        List<Point>points = new ArrayList<>();
        List<Double>dist = new ArrayList<>();
        for(int i=0;i<3;i++){
            Point temp = new Point(result.get(i).x+result.get(i).width/2.0,
                    result.get(i).y+result.get(i).height/2.0);

            points.add(temp);
        }
        ArrayList<Point>res = new ArrayList<>();
        //判断上下分别几个点
        points.sort(new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                return o1.y<o2.y?-1:1;
            }
        });
        if(Math.abs(points.get(0).y-points.get(1).y)<Math.abs(points.get(1).y-points.get(2).y))
            return null;
        Log.i("test_","上下ok");
        res.add(points.get(0));
        //判断左右分别几个点
        points.sort(new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                return o1.x<o2.x?-1:1;
            }
        });
        if(Math.abs(points.get(0).x-points.get(1).x)>Math.abs(points.get(1).x-points.get(2).x))
            return null;
        Log.i("test_","左右ok");
        for(int i=0;i<3;i++){
            if(points.get(i).y == res.get(0).y)
                continue;
            res.add(points.get(i));
        }
        double x = (res.get(2).x-res.get(1).x)+res.get(0).x;
        double y = res.get(2).y-(res.get(1).y-res.get(0).y);
        res.add(new Point(x,y));
        return res;
    }

}
