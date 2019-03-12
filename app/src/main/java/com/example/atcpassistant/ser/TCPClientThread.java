package com.example.atcpassistant.ser;

import android.util.Log;

import com.example.atcpassistant.utils.LogUtil;
import com.example.atcpassistant.view.CView;
import com.example.atcpassistant.view.MainView;
import com.example.atcpassistant.view.SendView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @author zjh
 * Created by kys_8 on 2018/8/10.
 */
public class TCPClientThread extends Thread{

    private static final String TAG = "TCPClientThread";
    private Socket mSocket;
    private boolean isRuning;
    private InputStream inStream;
    private OutputStream outStream;
    private InetAddress inetAddress; // IP地址
    private int port; // 端口号
    private MainView mView;

    public TCPClientThread(MainView view) {
        this.mView=view;       //传递handler对象用于与主线程的信息交互
        isRuning=true;
    }
    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        if (mSocket == null) {
            try {
                Log.e(TAG, "（创建）启动连接线程");
                mSocket = new Socket(inetAddress, port);
                getTargetAddress();
                while (isRuning) {
                    inStream = mSocket.getInputStream();
                    while (inStream.available() == 0) {}
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    final byte[] buf = new byte[1024];//创建接收缓冲区

                    final int len = inStream.read(buf);//数据读出来，并且数据的长度
                     mView.onReceive(buf,len);
//                    mHandler.sendMessage(mHandler.
//                            obtainMessage(CLIENT_STATE_CORRECT_READ, len, -1, buffer));
                }
            } catch (IOException e) {

                // 加一个超时异常处理。哈
//                mView.onErr();
                if (e.getMessage() == null)
                    return;
                LogUtil.e(TAG,"e->"+e.getMessage());
                if (e.getMessage().contains("(No route to host)"))
                    mView.onErr("请检查主机地址");
                else if (e.getMessage().contains("(Connection timed out)"))
                    mView.onErr("连接超时");
                else if (e.getMessage().contains("(Connection refused)"))
                    mView.onErr("连接拒绝");
                else
                    mView.onErr("1"); // Socket is closed

            }finally {
                if (mSocket != null) {
                    try {
                        mSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (inStream != null) {
                        inStream.close();
                    }
                    if (outStream != null) {
                        outStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.e(TAG, "关闭连接，释放资源");
            }
        }
    }

    private void getTargetAddress(){
        String  targetIp = mSocket.getInetAddress().getHostAddress();
        mView.onSuc(targetIp);
        mView.setLinked();
    }

    public void sendData(byte[] buf, SendView sendView){
       try {
           outStream =mSocket.getOutputStream();
//           mHandler.sendMessage(mHandler.
//                   obtainMessage(CLIENT_STATE_CORRECT_WRITE,-1,-1,message));
           outStream.write(buf);
           sendView.onSendSuc(buf);

       } catch (IOException e) {
           e.printStackTrace();
           sendView.onSendErr();
       }
   }

   public void close() {
        if (mSocket != null) {
            try {
                mSocket.close();
                mSocket = null;
                isRuning = false;
            } catch (IOException e) {
                LogUtil.e(TAG,"关闭异常 "+e.getMessage());
            }
        } else {
            Log.e(TAG, "未建立连接或断开");
        }
    }


}
