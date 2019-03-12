package com.example.atcpassistant.ser;

import android.util.Log;

import com.example.atcpassistant.utils.LogUtil;
import com.example.atcpassistant.view.MainView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * @author zjh
 * Created by kys_8 on 2018/8/16.
 */
public class UDPReceiveThread extends Thread {

    private static final String TAG = "UDPReceiveThread";

    private DatagramSocket mSocket;
    private DatagramPacket mDp;
    private InetAddress inetAddress;
    private int port;

    private Boolean isRunning = false;
    private MainView mView;

    public UDPReceiveThread(MainView view) {
        mView = view;
        isRunning = true;
    }

    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        byte[] buf = new byte[1024]; // 创建接收缓冲区
        try {
            mSocket = new DatagramSocket(port); // 设置监听端口
            mView.onSuc("");
            mView.setLinked();
            mDp = new DatagramPacket(buf, buf.length);
            while (isRunning) {
                if (mSocket != null)
                    mSocket.receive(mDp);
//                String str = new String(mDp.getData(), 0, mDp.getLength());
                mView.onReceive(mDp.getData(),mDp.getLength());
            }
            mView.off();
        } catch (IOException e) {
            mView.off();
//            mView.onErr();
            e.printStackTrace();
            LogUtil.e(TAG,e.getMessage());
        }finally {
            mView.off();
            if (mSocket != null)
                mSocket.close();
        }
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public void close() {
        if (mSocket != null) {
            mSocket.close();
            mSocket = null;
            isRunning = false;
        } else {
            Log.e(TAG, "未建立连接或断开");
        }
    }

}
