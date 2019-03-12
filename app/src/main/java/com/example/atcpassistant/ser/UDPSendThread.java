package com.example.atcpassistant.ser;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * @author zjh
 * Created by kys_8 on 2018/8/16.
 */
public class UDPSendThread extends Thread{

    private DatagramSocket mSocket;
    private DatagramPacket mDp;
    private InetAddress inetAddress;
    private int port;
    private byte[] buf;


    public UDPSendThread(InetAddress inetAddress,int port,byte[] buf){
        this.inetAddress = inetAddress;
        this.port = port;
        this.buf = buf;
    }

    @Override
    public void run() {
        try {
            mSocket = new DatagramSocket();
            mDp = new DatagramPacket(buf, buf.length, inetAddress, port);
            if (mSocket != null)
                mSocket.send(mDp);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (mSocket != null)
                mSocket.close();
        }
    }
}
