package com.example.atcpassistant.view;

/**
 * @author zjh
 * Created by kys_8 on 2018/8/10.
 */
public interface MainView extends IView{
    void onSuc(String targetIp);
    void onReceive(byte[] buf,int len);
    void setLinked();
    void off();
    void onErr(String s);
}
