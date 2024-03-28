package com.course.filetransfer;

import java.io.Serializable;
import java.util.List;

public class HistoryItem implements Serializable{
    private String displayText;
    private String ip;
    private String port;
    private String username;
    private String password;

    HistoryItem(String ip,String port,String username,String password){
        this.ip=ip;
        this.port=port;
        this.username=username;
        this.password=password;
        displayText=this.ip+" "+this.port+" "+this.username;
    }


    @Override
    public boolean equals(Object obj){
        if(this==obj){
            return true;
        }
        if(obj==null||getClass()!=obj.getClass()){
            return false;
        }
        if(obj.getClass()==getClass()){
            HistoryItem item = (HistoryItem) obj;
            boolean result = item.getIp().equals(this.getIp());
            result&=item.getPort().equals(this.getPort());
            result&=item.getUser().equals(this.getUser());
            result&=item.getPass().equals(this.getPass());
            return result;
        }
        return false;
    }
    @Override
    public String toString(){
        return displayText;
    }

    public String getIp(){
        return this.ip;
    }
    public String getPort(){
        return this.port;
    }
    public String getUser(){
        return this.username;
    }
    public String getPass(){
        return this.password;
    }
}
