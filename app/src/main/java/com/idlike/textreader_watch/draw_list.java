package com.idlike.textreader_watch;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

public class draw_list {
    private List<String> chapters = new ArrayList<>();
    private int page = 0;
    private int pagein = -1;

    public int getSelected() {
        return selected;
    }

    private int selected = -1;
    private int w = -1;
    private int h = -1;
    private int endwith = -1;
    public void init(List<String> cs,int nw,int width,int height){
        w=width;
        h=height;
        chapters = cs;
        pagein = (h-80)/50;//计算每页能放下的
        if(pagein<=0){
            pagein = 1;
        }
        page = nw/pagein;
        endwith = h-40;
    }
    public Bitmap draw(){
        Bitmap newBitmap = Bitmap.createBitmap(w,h , Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        draw_reader_main.paint_bk30.setColor(Color.RED);
        canvas.drawText("[<] 返回阅读",10,30,draw_reader_main.paint_bk30);
        canvas.drawText("[<]",10,h-40,draw_reader_main.paint_bk30);
        canvas.drawText("[>]",w-60,h-40,draw_reader_main.paint_bk30);
        draw_reader_main.paint_bk30.setColor(Color.BLACK);
        for (int i = 0; i < pagein; i++) {
            if(chapters.size()>page*pagein+i){
                canvas.drawLine(0,i*40+42,w,i*40+42,draw_reader_main.paint_bk30);
                if(selected == page*pagein+i){
                    draw_reader_main.paint_bk30.setColor(Color.RED);
                }else {
                    draw_reader_main.paint_bk30.setColor(Color.BLACK);
                }
                canvas.drawText(chapters.get(page*pagein+i),10,i*40+80,draw_reader_main.paint_bk30);
            }

        }
        return newBitmap;
    }
    public void Onclick(int x,int y){
        if(y<h-40&&y>40){
            selected = page*pagein+(y-40)/40;
            System.out.println(selected);
        }
        if(y>h-40){
            if(x<w/2){
                if(page>0) page-=1;
            }else{
                if((page+1)*pagein<chapters.size()) page+=1;
            }
        }
    }
}
