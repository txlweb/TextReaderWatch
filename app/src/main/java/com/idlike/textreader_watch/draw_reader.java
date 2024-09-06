package com.idlike.textreader_watch;


import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class draw_reader {
    private InputStream fp = null;
    private static int width = 600;
    private int height = 600;
    private int font_width = 10;
    private int font_height = 10;

    private int backgroundcolor =Color.WHITE;
    private int textcolor = Color.BLACK;
    private List<String> page = new ArrayList<>();
    private String title = "";
    public String getTitle() {
        return title;
    }
    public List<String> getPages() {
        return page;
    }


    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public boolean init(String file,int w,int h,int fw,int fh,int bg,int tx){
        //初始化 内部变量，文件打开,处理编码
        page.clear();
        this.title="";
        this.width = w;
        this.height = h;
        this.font_width = fw;
        this.font_height = fh;
        this.backgroundcolor = bg;
        this.textcolor = tx;
        BufferedReader br = null;
        try {
            fp = new FileInputStream(file);
            String encode = "UTF-8";
            encode = EncodingDetect.getJavaEncode(file);
            br = new BufferedReader( new InputStreamReader(fp, encode));
            String str;
            int ww = 0;
            int hh = 0;
            String tmp_page = "";
            while ((str = br.readLine()) != null){
                if(Objects.equals(title, "")){
                    title=str;
                }
                //处理一行
                for (int i = 0; i < str.length(); i++) {
                    ww+=this.font_width;
                    tmp_page = tmp_page + str.charAt(i);
                    if(ww >= this.width){//处理换行
                        hh+=this.font_height;//换行
                        tmp_page = tmp_page + "\r\n";
                        ww = 0;
                    }
                    if(hh >= this.height){//处理换页
                        hh = 0;
                        page.add(tmp_page);
                        tmp_page = "";
                    }
                }
            }
            if(tmp_page != ""){
                page.add(tmp_page);
                tmp_page = "";
            }
            br.close();
        } catch (IOException e) {
            System.out.println(e);
            return false;
        }
        //System.out.println(page);
        System.out.println(page.size());
        paint.setColor(textcolor); // 设置文本颜色
        paint.setTextSize(font_width); // 设置文本大小
        paint.setTextAlign(Paint.Align.LEFT); // 设置文本对齐方式
        return true;
    }
    public boolean reinit(String file){
        return this.init(file,this.width,this.height,this.font_width,this.font_height,this.backgroundcolor,this.textcolor);
    }
    public Bitmap draw(int index){
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(backgroundcolor);
        //异常处理: 为0长度,判断未完成加载
        if(page.size() == 0){
            canvas.drawText("[正在加载文件]", 0, font_height, paint);
            return bitmap;
        }
        //换行制作（
        String[] lines = page.get(index).split("\n");
        if(lines.length == 0){
            canvas.drawText(page.get(index), 0, font_height, paint);
            return bitmap;
        }
        int i = 1;
        for (String line : lines) {
            canvas.drawText(line, 0, i*font_height, paint);
            i++;
        }
        //System.out.println(page.get(index));
        return bitmap;
    }

}
