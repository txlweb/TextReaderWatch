package com.idlike.textreader_watch;

import static com.idlike.textreader_watch.draw_select.paint_bk30;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    float oncreater_x = 0;
    float oncreater_y = 0;
    boolean can_page = true;
    long no_click_time = 0;
    int float_fps = 30;
    String save_name = "normal.txt";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("导入TXT");
        final EditText input = new EditText(this);
        input.setHint("请输入导入后存储的名字");
        input.setText("normal.txt");
        builder.setView(input);
        builder.setPositiveButton("导入TXT(自动加后缀)", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 在这里获取EditText的内容
                String userInput = input.getText().toString();
                save_name = userInput + ".txt";
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("text/plain"); // 或者使用 "*/*" 来允许用户选择任何类型的文件
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                try {
                    startActivityForResult(intent, 1001); // REQUEST_CODE_PICK_FILE 是你定义的用于处理结果的请求码
                } catch (android.content.ActivityNotFoundException e) {
                    // 如果没有找到任何可以处理这个Intent的应用，比如没有文件管理器
                    Toast.makeText(MainActivity.this, "没有找到文件管理器", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNeutralButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        //builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();


        //draw out demo
        ImageView imageView = findViewById(R.id.imageView);
//        draw_reader dd = new draw_reader();
//        dd.init(getApplicationContext().getFilesDir().getAbsolutePath()+"/dw_txt.txt",600,600,60,60);
        //activity demo
        //cfgs: 字号.字体颜色,背景颜色
        draw_select dd = new draw_select();
        dd.init(this);



        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long startTime = System.nanoTime();
                Bitmap basemap = dd.draw();
                Bitmap newBitmap = Bitmap.createBitmap(basemap.getWidth(), basemap.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(newBitmap);
                canvas.drawBitmap(basemap, 0, 0, null);
                long endTime = System.nanoTime();
                int fps = (int)(1 / (double) ((endTime - startTime)/ 1e9));
                no_click_time+=1000/float_fps;
                if(no_click_time<1000){
                    canvas.drawText(fps+"/"+float_fps+" FPS ",10,50,paint_bk30);
                } else if (no_click_time<500) {
                    canvas.drawText("<20 FPS [*节能中 R1]",10,50,paint_bk30);
                }  else if (no_click_time<5000) {
                    canvas.drawText("<10 FPS [*节能中 R1]",10,50,paint_bk30);
                } else if(no_click_time<10000){
                    canvas.drawText("< 5 FPS [*节能中 R2]",10,50,paint_bk30);
                }else{
                    canvas.drawText("-- FPS [*Draw已暂停]",10,50,paint_bk30);
                }
                if(no_click_time<500) float_fps = 30;
                if(no_click_time>=500) float_fps = 20;
                if(no_click_time>=1000) float_fps = 10;
                if(no_click_time>=5000) float_fps = 5;
                if(no_click_time>=10000) float_fps = 1;

                imageView.setImageBitmap(newBitmap);
                //超出10秒不再绘制,需要等待OnClick后立刻绘制
                if(no_click_time<10000) handler.postDelayed(this, 1000/float_fps);
                //}
                // 再次调用postDelayed以重复执行
                 // 1000毫秒后再次执行

            }
        };
        handler.postDelayed(runnable, 0); // 首次执行延迟1000毫秒

        imageView.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        float x = event.getX();
                        float y = event.getY();
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    dd.OnClick((int) x, (int) y);
                                }
                            }).start();

                        }
                        if(no_click_time>10000){
                            handler.postDelayed(runnable, 100);
                        }
                        no_click_time=0;
                        return true;
                    }
                });
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        SensorEventListener lsn = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                //System.out.println("Sensor:x="+x+",y="+y+",z="+z);
                //右侧x-上y-翻面z-
                if(oncreater_x==0)oncreater_x=x;
                if(oncreater_y==0)oncreater_y=y;
                if(x-oncreater_x>2){//左
                    if(can_page) dd.left();
                    can_page=false;
                    if(no_click_time>10000){
                        handler.postDelayed(runnable, 100);
                    }
                    no_click_time=0;
                }
                if(x-oncreater_x<-2){//右
                    if(can_page) dd.right();
                    can_page=false;
                    if(no_click_time>10000){
                        handler.postDelayed(runnable, 100);
                    }
                    no_click_time=0;
                }
                //保证一次旋转只会触发一次翻页
                if(x-oncreater_x>-2&&x-oncreater_x<2){
                    //can_page = true;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        if (accelerometer != null) {
            sensorManager.registerListener(lsn, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();

            // 使用ContentResolver来读取文件内容
            try (InputStream inputStream = getContentResolver().openInputStream(uri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                String line;
                StringBuilder stringBuilder = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append('\n');
                }

                // 现在，stringBuilder包含了文件的内容
                // 你可以将其写入应用的内部存储
                writeToFileInternalStorage(stringBuilder.toString());
                finish();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "读取文件失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 写入文件到内部存储
    private void writeToFileInternalStorage(String data) {
        FileOutputStream outputStream = null;

        try {

            outputStream = openFileOutput(save_name, Context.MODE_PRIVATE);
            outputStream.write(data.getBytes());
            Toast.makeText(this, "文件已保存到内部存储,应用需要重启来获取文件.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}