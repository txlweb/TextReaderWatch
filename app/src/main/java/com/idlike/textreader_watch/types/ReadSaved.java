package com.idlike.textreader_watch.types;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ReadSaved {
    private String cfg = "";//Nw|
    public boolean read(String fp){
        if(!new File(fp).isFile()) return false;
        try {
            FileInputStream in = new FileInputStream(fp);
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            cfg = br.readLine();
            in.close();
            br.close();
        } catch (IOException e) {
            System.out.println(e);
            return false;
        }
        return true;
    }


    public boolean create(String fp,String saved) {
        try {
            new File(fp).delete();
            new File(fp).createNewFile();
            FileOutputStream ot = new FileOutputStream(fp);
            ot.write(saved.getBytes());
            ot.close();
        } catch (IOException e) {
            System.out.println(e);
            return false;
        }
        return true;
    }

    public int GetNowReadPage(){
        String[] s = cfg.split("\\|");
        System.out.println(s[0]);
        try{
            Integer.parseInt(s[0]);
        } catch (NumberFormatException e) {
            return 0;
        }
        if(s.length>0) return Integer.parseInt(s[0]);
        return 0;
    }
    public int GetBG_color(){
        String[] s = cfg.split("\\|");
        try{
            Integer.parseInt(s[0]);
        } catch (NumberFormatException e) {
            return -1;
        }
        if(s.length>0) return Integer.parseInt(s[0]);
        return -1;//白色
    }
    public int GetTextColor(){
        String[] s = cfg.split("\\|");
        if(s.length>1) return Integer.parseInt(s[1]);
        return -16777216;//黑色
    }
    public int GetFontSize(){
        String[] s = cfg.split("\\|");
        if(s.length>2) return Integer.parseInt(s[2]);
        return 20;//20号
    }
}
