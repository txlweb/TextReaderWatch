package com.idlike.textreader_watch;

import static com.idlike.textreader_watch.libFILE.getFileHash256;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import androidx.core.content.FileProvider;

import com.idlike.textreader_watch.types.ReadSaved;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class draw_reader_main {
    private boolean menued = false;
    private draw_reader reader = new draw_reader();
    private draw_list lister = new draw_list();
    private draw_setting cfger = new draw_setting();
    private String now_reading = "";
    private int w = 600;
    private int fontsize = 20;
    private int h = 600;
    private int bg = -1;
    private int tx = -16777216;
    private int rerading_index = 0;
    private List<String> chapters = new ArrayList<>();
    private int rerading_page = 0;
    private String tmp = "/";
    private ReadSaved config = new ReadSaved();

    public int getNow_page() {
        return now_page;
    }

    private int now_page = 0;//0 阅读 1 目录 2 设置 -1销毁




    //这里设置部分用到的常量,别渲染一针就处理一次
    public static Paint paint_bk10 = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static Paint paint_bk30 = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static Paint paint_wt = new Paint(Paint.ANTI_ALIAS_FLAG);
    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
    private Context ct = null;
    private String rt = "";



    public void reinit(){
        init(rt,ct);
    }
    public boolean init(String read_txt, Context context){
        ct=context;
        rt=read_txt;
        //初始化常量
        paint_bk10.setColor(Color.BLACK);
        paint_bk10.setTextSize(10);
        paint_bk10.setTextAlign(Paint.Align.LEFT);
        paint_bk30.setColor(Color.BLACK);
        paint_bk30.setTextSize(30);
        paint_bk30.setTextAlign(Paint.Align.LEFT);
        paint_wt.setColor(Color.WHITE);
        //处理为分章节的小说文件
        now_reading = read_txt;
        if(!new File(now_reading).isFile()) return false;

        //获取文件的hash
        String hash = getFileHash256(read_txt);
        tmp = context.getApplicationContext().getFilesDir().getAbsolutePath() + "/" + hash;
        //获取全局配置文件
        if(!config.read(context.getApplicationContext().getFilesDir().getAbsolutePath()+"/config.cfg")) config.create(context.getApplicationContext().getFilesDir().getAbsolutePath()+"/config.cfg","-1|-16777216|20|");
        bg = config.GetBG_color();
        tx = config.GetTextColor();
        fontsize = config.GetFontSize();
        //case: 没有已经处理过的，现处理。
        if(!new File(tmp).isDirectory()){
            if(!new File(tmp).mkdir()) return false;
            //匹配方法：章节
            Pattern compile = Pattern.compile(".*第.*章.*");
            //开始txt处理
            String encode = "UTF-8";
            encode = EncodingDetect.getJavaEncode(read_txt);
            try {
                FileInputStream in = new FileInputStream(read_txt);
                BufferedReader br = new BufferedReader(new InputStreamReader(in, encode));
                String str;
                int index = 0;
                FileOutputStream of = new FileOutputStream(tmp+"/"+index+".txt");
                while ((str = br.readLine()) != null) {
                    if (compile.matcher(str).matches()) {
                        index++;
                        of.close();
                        of = new FileOutputStream(tmp+"/"+index+".txt");
                    }
                    of.write(str.getBytes());//后写，为了标题在同一个文件
                }
                if(of.getChannel().isOpen()){
                    of.close();
                }
                in.close();
                br.close();
            } catch (IOException e) {
                System.out.println(e);
                return false;
            }
        }
        new File(tmp+"/0.txt").delete();
        //遍历目录，获取txt列表
        File[] files = new File(tmp).listFiles();
        for (File file : files) {
            if(file.getName().contains(".txt")) chapters.add(file.getPath());
        }
        //获取屏幕宽高，准备渲染资源
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        w = displayMetrics.widthPixels;
        h = displayMetrics.heightPixels;
        //w=600;
        //h=600;
        //初始化reader文本渲染器
        rerading_index=0;
        rerading_page=0;
        ReadSaved cfw = new ReadSaved();
        if(!cfw.read(tmp+"/read.cfg")) cfw.create(tmp+"/read.cfg","0|");
        cfw.read(tmp+"/read.cfg");
        rerading_index = cfw.GetNowReadPage();
        //预留60（30+30的info显示空间）
        reader.init(chapters.get(rerading_index),w,h-60,fontsize,fontsize,bg,tx);
        cfger.init(context.getApplicationContext().getFilesDir().getAbsolutePath()+"/config.cfg",w,h);
        //回收垃圾
        System.gc();
        return true;
    }

    public void OnClick(int x,int y){
        System.out.println("[DEBUG CLICK]:"+x+","+y);
        if(now_page == 0) {
            if (menued) {
                //math w,h
                int tool_h = h / 100 * 30;
                int back_h = h / 100 * 10;
                //thing: back
                if (x > 0 && y > 0 && x < back_h && y < back_h) {
                    System.out.println("CLICKED: BACK");
                    now_page = -1;
                }
                //thing: list
                if (x > 0 && y > h - tool_h && x < w / 2 && y < h) {
                    System.out.println("CLICKED: LIST");
                    List<String> t = new ArrayList<>();
                    try {
                        for (int i = 0; i < chapters.size(); i++) {
                            FileInputStream in = new FileInputStream(chapters.get(i));
                            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

                            String cfg = br.readLine();
                            if (cfg.length() >= 10) {
                                t.add(cfg.substring(0, 10));
                            } else {
                                t.add(cfg);
                            }
                            in.close();
                            br.close();
                        }
                    }catch (IOException e){
                        System.out.println(e);
                    }
                    lister.init(t,rerading_index,w,h);
                    now_page = 1;
                }
                //thing: setting
                if (x > w / 2 && y > h - tool_h && x < w && y < h) {
                    System.out.println("CLICKED: SETTING");
                    now_page = 2;
                }
                //thing: backread
                if (y > back_h && y < h - tool_h) {
                    menued = false;
                    System.out.println("CLICKED: BACK_READ");
                    now_page = 0;
                }
            } else {
                //thing: pageLeft
                if (x > 0 && x < w / 2 - 100) {
                    System.out.println("CLICKED: PAGE_LEFT");
                    left();
                }
                //thing: pageRight
                if (x > w / 2 + 100 && x < w) {
                    System.out.println("CLICKED: PAGE_RIGHT");
                    right();
                }
                if (x > w / 2 - 100 && x < w / 2 + 100) {
                    System.out.println("CLICKED: MENU");
                    menued = true;
                }
            }
        }
        if(now_page == 1){
            lister.Onclick(x,y);
            if(y<40){
                now_page = 0;
                if (lister.getSelected() > 0 && lister.getSelected() < chapters.size()) rerading_index = lister.getSelected();
                reinit_and_save();
            }
        }
        if(now_page == 2){
            cfger.Onclick(x,y);
            if(y<40){
                now_page = 0;
                reinit();
            }
        }
    }

    private void reinit_and_save() {
        reader.reinit(chapters.get(rerading_index));
        rerading_page=0;
        ReadSaved cfw = new ReadSaved();
        cfw.create(tmp+"/read.cfg",rerading_index+"|");
    }

    public void left(){
        if(rerading_page>0){
            rerading_page-=1;
        }else{
            if(rerading_index>0){
                rerading_index-=1;
                reinit_and_save();
            }
        }
    }
    public void right(){
        if(reader.getPages().size()-1>rerading_page){
            rerading_page+=1;
        }else{
            if(rerading_index<chapters.size()-1){
                rerading_index+=1;
                reinit_and_save();
            }
        }
    }

    public Bitmap draw(){
        //System.out.println("drawed");
        //先让文本渲染器渲染完成
        //layout reader
        if(now_page == 0) {
            Bitmap basemap = reader.draw(rerading_page);
            //上下留60,创建新画布
            Bitmap newBitmap = Bitmap.createBitmap(basemap.getWidth(), basemap.getHeight() + 60, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(newBitmap);
            canvas.drawColor(Color.WHITE);
            //渲染器结果贴上
            canvas.drawBitmap(basemap, 0, 30, null);
            if (menued) {
                int tool_h = h / 100 * 30;
                int back_h = h / 100 * 10;
                //绘制功能区背景
                canvas.drawRect(0, 0, w, back_h, paint_wt);
                canvas.drawRect(0, h - tool_h / 2, w, h, paint_wt);
                //绘制顶部返回按钮
                canvas.drawLine(10, back_h / 2, back_h - 10, 10, paint_bk30);
                canvas.drawLine(10, back_h / 2, back_h - 10, back_h - 10, paint_bk30);
                //绘制章节列表
                canvas.drawLine(10, h - back_h + back_h / 5 * 1, back_h - 10, h - back_h + back_h / 5 * 1, paint_bk30);
                canvas.drawLine(10, h - back_h + back_h / 5 * 2, back_h - 10, h - back_h + back_h / 5 * 2, paint_bk30);
                canvas.drawLine(10, h - back_h + back_h / 5 * 3, back_h - 10, h - back_h + back_h / 5 * 3, paint_bk30);
                canvas.drawText("[章节列表]", back_h / 2 - 25, h - back_h + back_h / 5 * 4 + 5, paint_bk10);
                //绘制设置按钮
                canvas.drawText("[设置]", w - 30 * 3, h - back_h + back_h / 2, paint_bk30);
            } else {
                //绘制时间(00:00)格式
                Date now = new Date();
                canvas.drawText(formatter.format(now), 10, 30, paint_bk30);
                //绘制标题 绘制第一行前10个字
                if (reader.getTitle().length() >= 10) {
                    canvas.drawText(reader.getTitle().substring(0, 10), w - 10 * 30, 30, paint_bk30);
                } else {
                    canvas.drawText(reader.getTitle(), w - 10 * 30, 30, paint_bk30);
                }
                //绘制章节页数阅读进度
                canvas.drawText("第" + (rerading_index + 1) + "/" + chapters.size() + "章      " + (rerading_page + 1) + "/" + reader.getPages().size(), 10, h, paint_bk30);

            }
            return newBitmap;
        }
        //layout lister
        if (now_page == 1){
            return lister.draw();
        }
        //layout setting
        if(now_page == 2){
            return cfger.draw();

        }
        return null;
    }
}
