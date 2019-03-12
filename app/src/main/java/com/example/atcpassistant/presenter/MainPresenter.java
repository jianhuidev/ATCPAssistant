package com.example.atcpassistant.presenter;

import com.example.atcpassistant.ser.TCPServiceThread;
import com.example.atcpassistant.ser.UDPReceiveThread;
import com.example.atcpassistant.ser.UDPSendThread;
import com.example.atcpassistant.utils.DataSynUtil;
import com.example.atcpassistant.view.CView;
import com.example.atcpassistant.DataAnalysis;
import com.example.atcpassistant.view.CloseView;
import com.example.atcpassistant.view.IView;
import com.example.atcpassistant.ser.TCPClientThread;
import com.example.atcpassistant.view.MainView;
import com.example.atcpassistant.view.SendView;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author zjh
 * Created by kys_8 on 2018/8/10.
 */
public class MainPresenter implements IPresenter{


    private TCPClientThread mCThread;
    private TCPServiceThread mSThread;
    private UDPSendThread sendThread;
    private UDPReceiveThread udpReceiveThread;
    private CView cView;
    private MainView mView;
    private String mIp,mPort,udpNativePort;
    private int mMode;

    @Override
    public void submitInfo(String ip, String port,int mode) {
        mIp = ip;
        mPort = port; // mPort 充当tcp 端口
        mMode = mode;
    }

    @Override
    public void submitInfo(String ip, String remotePort, String nativePort, int mode) {
        mIp = ip;
        mPort = remotePort; // 充当udp 远程端口
        mMode = mode;
        udpNativePort = nativePort;
    }

    /**
     * 先提交信息，再创建线程
     *
     * @param v
     */
    @Override
    public void createThread(IView v) {
        switch (mMode) {
            case 1: // TCP 客户端
                mView = (MainView) v;
                if (mCThread == null) {
                    mCThread = new TCPClientThread(mView);
                    InetAddress ipAddress = null;
                    try {
                        ipAddress = InetAddress.getByName(mIp);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    mCThread.setInetAddress(ipAddress);
                    mCThread.setPort(Integer.valueOf(mPort));
                    mCThread.start();
                }
                break;
            case 2: // TCP 服务端
                mView = (MainView) v;
                if (mSThread == null){
                    mSThread = new TCPServiceThread(mView);
                    mSThread.setPort(Integer.valueOf(mPort));
                    mSThread.start();
                }
                break;
            case 3: // UDP
                mView = (MainView) v;
                if (udpReceiveThread == null) {
                    udpReceiveThread = new UDPReceiveThread(mView);
                    InetAddress ipAddress = null;
                    try {
                        ipAddress = InetAddress.getByName(mIp);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    udpReceiveThread.setInetAddress(ipAddress);
                    udpReceiveThread.setPort(Integer.valueOf(udpNativePort));
                    udpReceiveThread.start();
                }
                break;
        }

    }

    /**
     * 关闭socket ，释放资源
     */
    @Override
    public void onDestroy() {
        if (mCThread != null)
            mCThread.close();
        if (mSThread != null)
            mSThread.closeServerSocket();
    }

    /**
     * 根据模式返回相应线程是否创建的情况
     *
     * @param mode 模式
     * @return
     */
    public boolean isThreadNull(int mode) {
        if (mode == 1) {
            return mCThread == null;
        } else if (mode == 2) {
            return mSThread == null;
        } else {
            return udpReceiveThread == null;
        }
    }

    /**
     * 接收数据
     *
     * @param buf
     * @param len
     * @param isHexShow
     * @param isWordWrap
     * @param dataAnalysis
     */
    public void onReceive(byte[] buf,int len,boolean isHexShow,boolean isWordWrap,
                          DataAnalysis dataAnalysis){
        if (isHexShow){
            String readStr = ""+DataSynUtil.bytesToHexString(buf,len);
            dataAnalysis.onReceive(readStr);
        }else {
            String str = null;
            try {
                str = new String(buf, 0, len, "GBK");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            dataAnalysis.onReceive(str);
        }
    }

    /**
     * 在TCP 模式下发送数据
     *
     * @param send
     * @param mode
     * @param sendView
     */
    public void sendData(final byte[] send, int mode, final SendView sendView){
        switch (mode){
            case 1:
                if (mCThread == null){
                    sendView.noLinked();
                    return;
                }
                mCThread.sendData(send,sendView);
                break;
            case 2:
                if (mSThread == null){
                    sendView.noLinked();
                    return;
                }
                mSThread.sendData(send,sendView);
                break;
            case 3:
                InetAddress ipAddress = null;
                try {
                    ipAddress = InetAddress.getByName(mIp);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                new UDPSendThread(ipAddress,Integer.valueOf(mPort),send).start();
                break;
        }
    }

    /**
     * 关闭连接
     *
     * @param mode
     * @param closeView
     */
    public void closeLink(int mode, CloseView closeView){
        switch (mode){
            case 1:
                if (mCThread != null) {
                    mCThread.close();
                    mCThread = null;
                }
                closeView.close();
                break;
            case 2:
                mSThread.setRuning(false);
                if (mSThread != null) {
                    mSThread.closeServerSocket();
                    mSThread = null;
                }
                closeView.close();
                break;
            case 3:
                udpReceiveThread.setRunning(false);
                if (udpReceiveThread != null) {
                    udpReceiveThread.close();
                    udpReceiveThread = null;
                }
                closeView.close();
                break;
        }
    }
}
