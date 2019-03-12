package com.example.atcpassistant.view;

/**
 * @author zjh
 * Created by kys_8 on 2018/8/10.
 */
public interface CView extends IView{
    void onSuc(String targetIp);
    void onErr();
}
