package com.idlike.textreader_watch;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import com.idlike.textreader_watch.types.ReadSaved;

import java.util.ArrayList;
import java.util.List;

public class draw_setting {

    private int w = -1;
    private int fs = 0;
    private int bg = 0;
    private int tx = 0;
    private int h = -1;
    private int test_x = 0;
    private int test_y = 0;
    private String cfg = "";
    public void init(String fp,int width,int height){
        w=width;
        h=height;
        cfg=fp;
        ReadSaved s = new ReadSaved();
        s.read(fp);
        fs = s.GetFontSize();
        bg = s.GetBG_color();
        tx = s.GetTextColor();
    }
    public Bitmap draw(){
        Bitmap newBitmap = Bitmap.createBitmap(w,h , Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        draw_reader_main.paint_bk30.setColor(Color.RED);
        canvas.drawText("[<] 返回阅读",10,30,draw_reader_main.paint_bk30);
        canvas.drawText("[字号]   [-] [+]"+fs+" ",10,70,draw_reader_main.paint_bk30);
        canvas.drawText("[背景]   [-] [+]"+bg+" ",10,130,draw_reader_main.paint_bk30);
        canvas.drawText("[前景]   [-] [+]"+tx+" ",10,190,draw_reader_main.paint_bk30);
//        canvas.drawLine(test_x,0,test_x,0,draw_reader_main.paint_bk30);
//        canvas.drawLine(0,test_y,0,test_y,draw_reader_main.paint_bk30);
//        canvas.drawText("("+test_x+","+test_y+")",test_x,test_y,draw_reader_main.paint_bk30);
        draw_reader_main.paint_bk30.setColor(Color.BLACK);

        return newBitmap;
    }
    public void Onclick(int x,int y){
        test_x=x;
        test_y=y;
        if(x<180&&y<120&&x>100&&y>60){
            fs--;
        }
        if(x<250&&y<120&&x>180&&y>60){
            fs++;
        }
        if(x<180&&y<160&&x>100&&y>120){
            bg--;
        }
        if(x<250&&y<160&&x>180&&y>120){
            bg++;
        }
        if(x<180&&y<210&&x>100&&y>170){
            tx--;
        }
        if(x<250&&y<210&&x>180&&y>170){
            tx++;
        }
        ReadSaved s = new ReadSaved();
        s.create(cfg,bg+"|"+tx+"|"+fs+"|");
    }
}
