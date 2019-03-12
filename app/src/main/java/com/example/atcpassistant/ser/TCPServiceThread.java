package com.example.atcpassistant.ser;

import android.util.Log;

import com.example.atcpassistant.view.MainView;
import com.example.atcpassistant.view.SendView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author zjh
 * Created by kys_8 on 2018/8/6.
 */
public class TCPServiceThread extends Thread{
    private static final String TAG = "TCPServiceThread";
    private ServerSocket mServerSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Socket mSocket;
    private int port;
    private boolean isRuning;
    private MainView mView;

    public TCPServiceThread(MainView view) {
        mView = view;
        isRuning = true;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            mServerSocket = new ServerSocket(port);
            while (isRuning) {
                Log.e(TAG, "等待客户端连接isRuning=" + isRuning);
                mSocket = mServerSocket.accept();                     //开启监听
                if (mSocket != null) {
                    //启动接收线程
                    ReceiveThread receiveThread = new ReceiveThread(mSocket);    //当有客户端连接时，开启数据接收的线程
                    receiveThread.start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
//            send_Error();
        }
    }

    class ReceiveThread extends Thread{
        private Socket socket = null;

        public ReceiveThread(Socket socket) {
            this.socket = socket;
            getTargetAddress();
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
//                send_Error();
            }
        }

        @Override
        public void run() {
            super.run();
            while (isRuning) {
                try {
                    while (inputStream.available() == 0) {} // 阻塞式
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    final byte[] buf = new byte[1024];
                    final int len = inputStream.read(buf);
                    mView.onReceive(buf,len);
//                    Message message = mhandler.obtainMessage(SERVER_STATE_CORRECT_READ, len, 1, buf);
//                    mhandler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
//                    send_Error();
                }
            }
            try {
                if (inputStream != null)
                    inputStream.close();
                if (outputStream != null)
                    outputStream.close();
                if (socket != null) {
                    socket.close();
                    socket = null;
                }
                Log.e(TAG, "断开连接监听 释放资源");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void getTargetAddress() {
            InetAddress inetAddress = socket.getInetAddress();
            String ipAddress = inetAddress.getHostAddress();
            mView.onSuc(ipAddress);
            mView.setLinked();
//            Message message = mhandler.obtainMessage(SERVER_STATE_IOFO, strings);
//            mhandler.sendMessage(message);
        }

    }

    public void closeServerSocket() {
        try {
            if (mServerSocket != null)
                mServerSocket.close();
            if (mSocket != null)
                mSocket.close();
            mView.off();
            Log.e(TAG, "断开连接监听 关闭监听Socket");
         } catch (IOException e) {
            e.printStackTrace();
         }
    }

    /**
     * 数据写入函数
     *
     * @param buf
     */
    public void sendData(byte[] buf, SendView sendView) {
        try {
            if (outputStream != null) {
                outputStream.write(buf);
                sendView.onSendSuc(buf);
            } else {
//                send_Error();
                sendView.onSendErr();
            }
        } catch (IOException e) {
            e.printStackTrace();
            sendView.onSendErr();
//            send_Error();
        }
    }
//    public  void send_Error(){
//        Message er_message = mhandler.obtainMessage(SERVER_STATE_ERROR);
//        mhandler.sendMessage(er_message);
//    }
    public void setRuning(boolean isRuning) {
        this.isRuning = isRuning;
    }

}
