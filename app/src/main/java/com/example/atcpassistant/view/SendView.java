package com.example.atcpassistant.view;

/**
 * @author zjh
 *         Created by kys_8 on 2018/8/12.
 */
public interface SendView {
    void noLinked();
    void onSendSuc(byte[] buf);
    void onSendErr();
}
