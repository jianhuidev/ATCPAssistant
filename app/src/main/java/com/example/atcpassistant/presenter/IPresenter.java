package com.example.atcpassistant.presenter;


import com.example.atcpassistant.view.IView;

public interface IPresenter {

    void submitInfo(String ip,String port,int mode);

    void submitInfo(String ip,String remotePort,String nativePort,int mode);

    void createThread(IView v);

    void onDestroy();

//    void attachView(IView v); //定义了一个attachView方法，用于绑定我们定义的View。
}
