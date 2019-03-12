package com.example.atcpassistant.ui;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.atcpassistant.presenter.MainPresenter;
import com.example.atcpassistant.R;
import com.example.atcpassistant.utils.DataSynUtil;
import com.example.atcpassistant.utils.LogUtil;
import com.example.atcpassistant.utils.ToastUtil;
import com.example.atcpassistant.view.CView;
import com.example.atcpassistant.DataAnalysis;
import com.example.atcpassistant.view.CloseView;
import com.example.atcpassistant.view.MainView;
import com.example.atcpassistant.view.SendView;

import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Toolbar mToolbar;
    FloatingActionButton mFab;
    CardView mCard;
    FrameLayout mSend,mClear;
    MainPresenter mMainPresenter;
    ToggleButton toggleButton;
    EditText mIpEt,mRemotePortEt,mNativePortEt,mSendEt,mPeriod_Et;
    RadioGroup mRg;
    RadioButton tcpClientRb,tcpServerRb,udpRb;
    ProgressBar mProgressBar;
    TextView targetIpTv,mReceiveTv,mReceiveCountTv,mSendCountTv,mConfirm,receiveTvCard,sendTvCard;
    TextView nativeIpTvCard,linkStateTvCard,targetIpTvCard,clearReceiveTv;
    CheckBox mHexShowCb,mWordWrapCb,mHexSendCb,mAutoSendCb;
    Timer mTimer;

    /**
     * 默认为不显示，true 为显示状态
     */
    private boolean isVisible = false;

    /**
     * flag 为true 才可以退出软件
     */
    private boolean flag = false;

    private Animator mAnimator;
    private int mWidth;// 屏幕宽度（像素）
    private int mHeight;// 屏幕高度（像素）

    private String mIp,mPort;
    private int mMode;
    private boolean isHexShow,isWordWrap,isHexSend,isAutoSend;

    //计数用
    private int  countReceive = 0;
    private  int countSend = 0;

    private boolean isLinked = false; // 用于发送时的判断


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GetPiexels();
        mMainPresenter = new MainPresenter();
        initViews();
        initListeners();
        radioGroupListener();
        toggleButtonListener();
        checkBoxListeners();
    }

    private void initViews(){
        mToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(mToolbar);
//        mToolbar.setTitle("TCP 调试助手");
        mFab = (FloatingActionButton)findViewById(R.id.fab_main);
        mCard = (CardView)findViewById(R.id.card_main);
        mConfirm = (TextView)findViewById(R.id.confirm_card);
        toggleButton = (ToggleButton)findViewById(R.id.toggle_btn);
        mIpEt = (EditText)findViewById(R.id.ip_et);
        mRemotePortEt = (EditText)findViewById(R.id.remote_port_et);
        mNativePortEt = (EditText)findViewById(R.id.native_port_et);
        mIpEt.setEnabled(true);
        mRemotePortEt.setEnabled(true);
        mNativePortEt.setEnabled(true);
        mRg = (RadioGroup) findViewById(R.id.rg_card);
        tcpClientRb = (RadioButton)findViewById(R.id.tcp_client_card);
        tcpServerRb = (RadioButton)findViewById(R.id.tcp_server_card);
        udpRb = (RadioButton)findViewById(R.id.udp_card);
        mProgressBar = (ProgressBar)findViewById(R.id.progress_bar_card);
        targetIpTv = (TextView)findViewById(R.id.target_ip_tv_card);
        mHexShowCb = (CheckBox)findViewById(R.id.hex_show_cb);
        mWordWrapCb = (CheckBox)findViewById(R.id.word_wrap_cb);
        mHexSendCb = (CheckBox)findViewById(R.id.hex_send_cb);
        mAutoSendCb = (CheckBox)findViewById(R.id.auto_send_cb);
        mReceiveTv = (TextView)findViewById(R.id.receive_tv);
        mReceiveTv.setMovementMethod(ScrollingMovementMethod
                .getInstance()); // 使TextView接收区可以滚动
        mReceiveCountTv = (TextView)findViewById(R.id.receive_count_tv);
        mSend = (FrameLayout)findViewById(R.id.send_layout);
        mClear = (FrameLayout)findViewById(R.id.clear_layout);
        mSendEt = (EditText)findViewById(R.id.send_et);
        mSendCountTv = (TextView)findViewById(R.id.send_count_tv);
        receiveTvCard = (TextView)findViewById(R.id.receive_tv_card);
        sendTvCard = (TextView)findViewById(R.id.send_tv_card);
        nativeIpTvCard = (TextView)findViewById(R.id.native_ip_tv_card);
        linkStateTvCard = (TextView)findViewById(R.id.link_state_tv_card);
        targetIpTvCard = (TextView)findViewById(R.id.target_ip_tv_card);
        nativeIpTvCard.setText(getLocalIpAddress()); // 先填本地，这是默认的，获取本地IP 地址显示
        clearReceiveTv = (TextView)findViewById(R.id.clear_receive_tv);
        mPeriod_Et = (EditText)findViewById(R.id.period_et);
    }

    private void initListeners(){
        mFab.setOnClickListener(this);
        mConfirm.setOnClickListener(this);
        mSend.setOnClickListener(this);
        mClear.setOnClickListener(this);
        clearReceiveTv.setOnClickListener(this);
    }

    /**
     * RadioGroup，RadioButton 监听
     */
    private void radioGroupListener(){
        mRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) { // 更新模式
                    case R.id.tcp_client_card:
                        mMode = 1;
                        mToolbar.setTitle("TCP Client");
                        toggleButton.setTextOff("链接网络");
                        toggleButton.setTextOn("断开网络");
                        toggleButton.setChecked(false);
                        mIpEt.setEnabled(true);
                        mRemotePortEt.setEnabled(true);
                        mNativePortEt.setEnabled(false);
                        break;
                    case R.id.tcp_server_card:
                        mMode = 2;
                        mToolbar.setTitle("TCP Server");
                        toggleButton.setTextOff("开始监听");
                        toggleButton.setTextOn("停止监听");
                        toggleButton.setChecked(false);
                        mIpEt.setEnabled(false);
                        mRemotePortEt.setEnabled(false);
                        mNativePortEt.setEnabled(true);
                        break;
                    case R.id.udp_card:
                        mMode = 3;
                        mToolbar.setTitle("UDP");
                        toggleButton.setTextOff("开启UDP");
                        toggleButton.setTextOn("关闭UDP");
                        toggleButton.setChecked(false);
                        mIpEt.setEnabled(true);
                        mRemotePortEt.setEnabled(true);
                        mNativePortEt.setEnabled(true);
                        break;
                }
            }
        });
    }

    /**
     * ToggleButton 监听
     */
    private void toggleButtonListener(){
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    switch (mMode){
                        case 0:
                            toggleButton.setChecked(false);
                            ToastUtil.showToast(MainActivity.this, "请先选择模式");
                            break;
                        case 1: // TCP Client
                            tcpClientRb.setEnabled(true);
                            tcpServerRb.setEnabled(false);
                            udpRb.setEnabled(false);
                            if (TextUtils.isEmpty(mIpEt.getText().toString().trim())) {
                                ToastUtil.showToast(MainActivity.this, "请填写远程主机地址");
                                return;
                            }
                            if (TextUtils.isEmpty(mRemotePortEt.getText().toString().trim())){
                                ToastUtil.showToast(MainActivity.this, "请填写远程主机端口");
                                return;
                            }
                            if (!mMainPresenter.isThreadNull(mMode)){
                                toggleButton.setChecked(true);
                                return; // 不是null 说明已创建是哪个键链接线程，所以结束
                            }
                            pBOpen();
                            //先提交信息，再创建线程
                            mMainPresenter.submitInfo(mIpEt.getText().toString().trim(),
                                    mRemotePortEt.getText().toString().trim(),mMode);
                            mMainPresenter.createThread(new MainView() {
                                @Override
                                public void onSuc(final String targetIp) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            pBClose();
                                            ToastUtil.showToast(MainActivity.this, "连接" + targetIp + "成功");
                                            targetIpTvCard.setVisibility(View.VISIBLE);
                                            linkStateTvCard.setText("连接到");
                                            targetIpTvCard.setText(targetIp);
                                        }
                                    });
                                }

                                @Override
                                public void onReceive(byte[] buf, int len) {
                                    mMainPresenter.onReceive(buf, len, isHexShow, isWordWrap, new DataAnalysis() {
                                        @Override
                                        public void onReceive(final String str) {
                                            updateCountTv(str);
                                        }
                                    });
                                }

                                @Override
                                public void setLinked() {
                                    isLinked = true;
                                }

                                @Override
                                public void off() {
                                    isLinked = false;
                                }

                                @Override
                                public void onErr(final String s) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            toggleButton.setChecked(false);
                                            pBClose();
                                            switch (s) {
                                                case "请检查主机地址":
                                                    new AlertDialog.Builder(MainActivity.this).setMessage("请检查主机地址")
                                                            .setPositiveButton("确定", null).show();
                                                    break;
                                                case "连接超时":
                                                    new AlertDialog.Builder(MainActivity.this).setMessage("连接超时")
                                                            .setPositiveButton("确定", null).show();
                                                    break;
                                                case "连接拒绝":
                                                    new AlertDialog.Builder(MainActivity.this).setMessage("连接拒绝")
                                                            .setPositiveButton("确定", null).show();
                                                    break;
                                            }
                                        }
                                    });
                                }
                            });
                            break;

                        case 2: // TCP Server
                            tcpClientRb.setEnabled(false);
                            tcpServerRb.setEnabled(true);
                            udpRb.setEnabled(false);
                            if (TextUtils.isEmpty(mNativePortEt.getText().toString().trim())) {
                                ToastUtil.showToast(MainActivity.this, "请填写本地监听端口");
                                return;
                            }
                            if (!mMainPresenter.isThreadNull(mMode)){
                                toggleButton.setChecked(true);
                                return; // 不是null 说明已创建，所以不用再创建，所以结束
                            }
                            pBOpen();

                            //先提交信息，再创建线程
                            mMainPresenter.submitInfo("",
                                    mNativePortEt.getText().toString().trim(),mMode);
                            mMainPresenter.createThread(new MainView() {
                                @Override
                                public void onSuc(final String targetIp) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            pBClose();
                                            ToastUtil.showToast(MainActivity.this, "链接" + targetIp + "成功");
                                        }
                                    });
                                }

                                @Override
                                public void onReceive(byte[] buf, int len) {
                                    mMainPresenter.onReceive(buf, len,isHexShow,isWordWrap, new DataAnalysis() {
                                        @Override
                                        public void onReceive(final String str) {
                                            updateCountTv(str);
                                        }
                                    });
                                }

                                @Override
                                public void setLinked() {
                                    isLinked = true;
                                }

                                @Override
                                public void off() {
                                    isLinked = false;
                                }

                                @Override
                                public void onErr(String s) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            isLinked = false;
                                            toggleButton.setChecked(false);
                                            pBClose();
                                            ToastUtil.showToast(MainActivity.this, "出现问题，请检查设置");
                                        }
                                    });
                                }
                            });
                            break;

                        case 3: // UDP
                            tcpClientRb.setEnabled(false);
                            tcpServerRb.setEnabled(false);
                            udpRb.setEnabled(true);
                            if (TextUtils.isEmpty(mIpEt.getText().toString().trim())){
                                ToastUtil.showToast(MainActivity.this, "请填写远程主机地址");
                                return;
                            }
                            if (TextUtils.isEmpty(mRemotePortEt.getText().toString().trim())){
                                ToastUtil.showToast(MainActivity.this, "请填写远程主机端口");
                                return;
                            }
                            if (TextUtils.isEmpty(mNativePortEt.getText().toString().trim())){
                                ToastUtil.showToast(MainActivity.this, "请填写本地监听端口");
                                return;
                            }
                            if (!mMainPresenter.isThreadNull(mMode)){
                                toggleButton.setChecked(true);
                                return; // 不是null 说明已创建，所以不用再创建，所以结束
                            }
                            pBOpen();

                            //先提交信息，再创建线程
                            mMainPresenter.submitInfo(mIpEt.getText().toString().trim(),
                                    mRemotePortEt.getText().toString().trim(),
                                    mNativePortEt.getText().toString().trim(),mMode);
                            mMainPresenter.createThread(new MainView() {
                                @Override
                                public void onSuc(String targetIp) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            pBClose();
                                            ToastUtil.showToast(MainActivity.this, "开启UDP");
                                        }
                                    });
                                }

                                @Override
                                public void onReceive(byte[] buf, int len) {
                                    mMainPresenter.onReceive(buf, len, isHexShow, isWordWrap, new DataAnalysis() {
                                        @Override
                                        public void onReceive(final String str) {
                                            updateCountTv(str);
                                        }
                                    });
                                }

                                @Override
                                public void setLinked() {
                                    isLinked = true;
                                }

                                @Override
                                public void off() {
                                    isLinked = false;
                                }

                                @Override
                                public void onErr(String s) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            isLinked = false;
                                            toggleButton.setChecked(false);
                                            pBClose();
                                            ToastUtil.showToast(MainActivity.this, "出现问题，请检查设置");
                                        }
                                    });
                                }
                            });
                            break;
                    }
                }else {
                    // 执行断开
                    offLink();
                }
            }
        });
    }

    /**
     * 十六进制显示，自动换行，
     * 十六进制发送，自动发送CheckBox 点击事件的监听
     */
    private void checkBoxListeners(){
        mHexShowCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    ToastUtil.showToast(MainActivity.this,"十六进制显示");
                    isHexShow = true;
                } else
                    isHexShow = false;
            }
        });
        mWordWrapCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    ToastUtil.showToast(MainActivity.this,"自动换行");
                    isWordWrap = true;
                } else
                    isWordWrap = false;
            }
        });
        mHexSendCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    ToastUtil.showToast(MainActivity.this,"十六进制发送");
                    isHexSend = true;
                } else
                    isHexSend = false;
            }
        });
        mAutoSendCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (TextUtils.isEmpty(mPeriod_Et.getText().toString().trim())){
                    mAutoSendCb.setChecked(false);
                    ToastUtil.showToast(MainActivity.this,"请填写自动发送周期");
                    return;
                }
                if (TextUtils.isEmpty(mSendEt.getText().toString().trim())) {
                    mAutoSendCb.setChecked(false);
                    ToastUtil.showToast(MainActivity.this, "请填写要发送的数据");
                    return;
                }
                final long t = Long.parseLong(mPeriod_Et.getText().toString().trim());
                if (isChecked) {
                    if (isLinked){
                        mTimer = new Timer();
                        mTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                sendData(mSendEt.getText().toString().trim());
                            }
                        },0,t);
                    }else
                        ToastUtil.showToast(MainActivity.this, "连接未建立");
                } else{
                    if (mTimer != null)
                        mTimer.cancel();
                }
            }
        });
    }

    /**
     * 获取wifi 本地IP
     *
     * @return
     */
    @SuppressLint("DefaultLocale")
    private String getLocalIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager != null ? wifiManager.getConnectionInfo() : null;
        // 获取32位整型IP地址
        int ipAddress = wifiInfo != null ? wifiInfo.getIpAddress() : 0;

        //返回整型地址转换成“*.*.*.*”地址
        return String.format("%d.%d.%d.%d",
                (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }

    /**
     * 接收个数的更新
     *
     * @param str
     */
    private void updateCountTv(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mReceiveTv.append(str);
                if (isWordWrap)
                    mReceiveTv.append("\n");
                if (isHexShow)
                    countReceive += str.length() / 2; // 接收计数
                else
                    countReceive += str.length();
                mReceiveCountTv.setText("" + countReceive);
                receiveTvCard.setText(""+countReceive);
            }
        });
    }

    /**
     * 断开链接
     */
    private void offLink(){
        mMainPresenter.closeLink(mMode, new CloseView() {
            @Override
            public void close() {
                pBClose();
                switch (mMode){
                    case 1:
                        tcpClientRb.setChecked(true);
                        tcpServerRb.setChecked(false);
                        udpRb.setChecked(false);

                        mIpEt.setEnabled(true);
                        mRemotePortEt.setEnabled(true);
                        mNativePortEt.setEnabled(false);
                        break;
                    case 2:
                        tcpClientRb.setChecked(false);
                        tcpServerRb.setChecked(true);
                        udpRb.setChecked(false);

                        mIpEt.setEnabled(false);
                        mRemotePortEt.setEnabled(false);
                        mNativePortEt.setEnabled(true);
                        break;
                    case 3:
                        tcpClientRb.setChecked(false);
                        tcpServerRb.setChecked(false);
                        udpRb.setChecked(true);

                        mIpEt.setEnabled(true);
                        mRemotePortEt.setEnabled(true);
                        mNativePortEt.setEnabled(true);
                        break;
                }
                tcpClientRb.setEnabled(true);
                tcpServerRb.setEnabled(true);
                udpRb.setEnabled(true);
                targetIpTvCard.setVisibility(View.GONE);
                linkStateTvCard.setText("无连接");
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fab_main: // 悬浮按钮的点击处理
                mFab.hide();
                if (!isVisible){
                    mAnimator = ViewAnimationUtils.createCircularReveal(mCard, // 动画
                            mWidth/2,
                            mHeight/2,
                            0,
                            mHeight);
                    isVisible = true; // 动画完，让卡纸显示，所以设置成true
                }
                setRevealAnimListener();
                mAnimator.setDuration(500);
                mAnimator.start();
//                receiveTvCard.setText(""+countReceive);
//                sendTvCard.setText(""+countSend);
                break;
            case R.id.confirm_card:
                if (isVisible){
                    pickUpRevealAnim();
                }
                mFab.show();
                break;

            case R.id.send_layout:
                if (TextUtils.isEmpty(mSendEt.getText().toString().trim())) {
                    ToastUtil.showToast(MainActivity.this, "请填写要发送的数据");
                    return;
                }
                if (isLinked)
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            sendData(mSendEt.getText().toString().trim());
                        }
                    }).start();
                else
                    ToastUtil.showToast(MainActivity.this, "连接未建立");
                break;

            case R.id.clear_receive_tv:
                mReceiveTv.setText("");
                break;
            case R.id.clear_layout:
                mSendEt.setText("");
                break;

        }
    }

    /**
     * 发送数据
     *
     * @param data
     */
    public void sendData(String data){
        if (isHexSend){ // 十六进制发送
            String regex="^[A-Fa-f0-9]+$";
            if(!data.matches(regex)){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(MainActivity.this).setMessage("十六进制字符串有误，请仔细检查！")
                                .setPositiveButton("确定", null).show();
                    }
                });
                return;
            }
            byte[] send = DataSynUtil.hexStr2Bytes(data);
            if (mMode != 0) {
                sendDataInPresenter(send);
            }
        }else { // 未选择十六进制发送
            byte[] send = new byte[0];
            try {
                send = data.getBytes("GBK");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (mMode != 0) {
                sendDataInPresenter(send);
            }
        }
    }

    /**
     * 将发送数据的处理逻辑转移到Presenter 处理
     *
     * @param send
     */
    private void sendDataInPresenter(byte[] send) {
        mMainPresenter.sendData(send, mMode, new SendView() {
            @Override
            public void noLinked() {
                ToastUtil.showToast(MainActivity.this, "请检查网络");
            }

            @Override
            public void onSendSuc(byte[] buf) {
                sendDataSuc(buf);
            }

            @Override
            public void onSendErr() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showToast(MainActivity.this, "连接异常");
                    }
                });
            }
        });
    }

    /**
     * 发送数据成功后的处理
     *
     * @param buf
     */
    private void sendDataSuc(byte[] buf){
        if (isHexSend){
            String message = DataSynUtil.Bytes2HexString(buf);
            countSend += message.length() / 2;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSendCountTv.setText(countSend+"");
                    sendTvCard.setText(""+countSend);
                }
            });
        } else {
            String message = null;
            try {
                message = new String(buf, "GBK");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            countSend += message != null ? message.length() : 0;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSendCountTv.setText(countSend+"");
                    sendTvCard.setText(""+countSend);
                }
            });
        }
    }

    private void setRevealAnimListener(){
        mAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (isVisible) {
                            mCard.setVisibility(View.VISIBLE);
                        }
                    }
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!isVisible) {
                            mCard.setVisibility(View.INVISIBLE);
                        }
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) {}
                    @Override
                    public void onAnimationRepeat(Animator animation) {}
                });
    }

    private void pBOpen(){
        mProgressBar.setVisibility(View.VISIBLE);
    }
    private void pBClose(){
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    /**
     * 收起动画设置
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void pickUpRevealAnim() {
        mAnimator = ViewAnimationUtils.createCircularReveal(
                mCard,
                0,
                0,
                mHeight,
                0);
        isVisible = false;
        setRevealAnimListener();
        mAnimator.setDuration(500);
        mAnimator.start();
    }

    /**
     * 获取屏幕宽高
     */
    private void GetPiexels() {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        mWidth = metric.widthPixels;
        mHeight = metric.heightPixels;
    }


    /**
     * 返回键
     */
    @Override
    public void onBackPressed() {
        if (isVisible) {
            pickUpRevealAnim();
            mFab.show();
        } else {
            if (flag){
                super.onBackPressed();
            }else {
                flag = true;
                ToastUtil.showToast(this,"再按一次将退出程序");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        flag = false;
                    }
                },2000);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTimer != null)
            mTimer.cancel();
        mMainPresenter.onDestroy();
    }
}
