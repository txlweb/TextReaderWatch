package com.idlike.textreader_watch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class draw_select {
    private boolean inited = false;
    private boolean ready = false;
    public static Paint paint_bk30 = new Paint(Paint.ANTI_ALIAS_FLAG);

    private List<String> texts = new ArrayList<>();
    private draw_reader_main readerMain = new draw_reader_main();
    private int w = -1;
    private int h = -1;
    private Context ct = null;
    private int page = 0;
    private int selected = -1;
    private int pagein = -1;
    private String tmp = "";

    public void init(Context context){
        paint_bk30.setColor(Color.BLACK);
        paint_bk30.setTextSize(30);
        paint_bk30.setTextAlign(Paint.Align.LEFT);
        ct = context;
        tmp = context.getApplicationContext().getFilesDir().getAbsolutePath();
        File[] files = new File(tmp).listFiles();
        for (File file : files) {
            if(file.getName().contains(".txt")) texts.add(file.getName());
        }
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        w = displayMetrics.widthPixels;
        h = displayMetrics.heightPixels;

        pagein = (h-80)/50;//计算每页能放下的
        if(pagein<=0){
            pagein = 1;
        }
        page = 0;
    }
    public Bitmap draw(){
        if(ready){
            Bitmap newBitmap = Bitmap.createBitmap(w,h , Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(newBitmap);
            canvas.drawColor(Color.RED);
            paint_bk30.setTextSize(100);
            paint_bk30.setColor(Color.BLACK);
            //canvas.drawText("[TextReader Watch Edition]  By.IDlike Vre.1.0.179b",10,30,paint_bk30);
            canvas.drawText("[正在加载文件]",50,100,paint_bk30);
            paint_bk30.setTextSize(30);
            return newBitmap;
        }
        if(inited){
            return readerMain.draw();
        }
        Bitmap newBitmap = Bitmap.createBitmap(w,h , Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        paint_bk30.setColor(Color.RED);
        canvas.drawText("[TextReader Watch Edition]  By.IDlike Vre.1.0.179b",10,30,paint_bk30);
        paint_bk30.setColor(Color.BLACK);
        paint_bk30.setColor(Color.RED);
        canvas.drawText("[<]",10,h-40,paint_bk30);
        canvas.drawText("[>]",w-60,h-40,paint_bk30);
        draw_reader_main.paint_bk30.setColor(Color.BLACK);
        for (int i = 0; i < pagein; i++) {
            if(texts.size()>page*pagein+i){
                canvas.drawLine(0,i*40+42,w,i*40+42,paint_bk30);
                if(selected == page*pagein+i){
                    paint_bk30.setColor(Color.RED);
                }else {
                    paint_bk30.setColor(Color.BLACK);
                }
                canvas.drawText(texts.get(page*pagein+i),10,i*40+80,paint_bk30);
            }

        }



        return newBitmap;
    }
    public void left(){
        if(inited) readerMain.left();
    }
    public void right(){
        if(inited) readerMain.right();
    }
    public void OnClick(int x,int y){
        if(inited){

            readerMain.OnClick(x,y);
            if(readerMain.getNow_page() == -1){
                inited = false;
                readerMain = new draw_reader_main();
                System.gc();
            }
            return;
        }
        if(y<h-40&&y>40){
            if(selected == page*pagein+(y-40)/40){
                //inital main view
                if(selected>=0 && selected<texts.size()) {
                    ready = true;
                    readerMain.init(tmp + "/" + texts.get(selected), ct);
                    inited = true;
                    ready = false;
                }

            }
            selected = page*pagein+(y-40)/40;
            System.out.println(selected);
        }
        if(y>h-40){
            if(x<w/2){
                if(page>0) page-=1;
            }else{
                if((page+1)*pagein<texts.size()) page+=1;
            }
        }
    }
}
