package com.wl.wlflatproject.MUtils;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lib.FunSDK;
import com.lib.IFunSDKResult;
import com.lib.MsgContent;
import com.wl.wlflatproject.MView.WaitDialogTime1;
import com.wl.wlflatproject.R;
import com.wl.wlflatproject.support.FunSupport;
import com.wl.wlflatproject.support.OnFunDeviceListener;
import com.wl.wlflatproject.support.OnFunDeviceOptListener;
import com.wl.wlflatproject.support.models.FunDevType;
import com.wl.wlflatproject.support.models.FunDevice;
import com.wl.wlflatproject.support.models.FunStreamType;
import com.wl.wlflatproject.support.sdk.bean.JsonConfig;
import com.wl.wlflatproject.support.sdk.struct.H264_DVR_FILE_DATA;
import com.wl.wlflatproject.support.utils.TalkManager;
import com.wl.wlflatproject.support.widget.FunVideoView;

import java.util.List;

/**
 * 摄像头播放类
 */
public class CameraUtils implements IFunSDKResult, OnFunDeviceListener, OnFunDeviceOptListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener {
    private Context context;
    private WaitDialogTime1 mWaitDlg1;
    private String videoUuid;
    private FunVideoView mFunVideoView;
    private int nbStateCount = 0;
    public FunDevice funDevice;
    private int userId;
    private ImageView bg;
    private TextView time;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // FunSupport.getInstance().requestDeviceStatus(FunDevType.EE_DEV_NORMAL_MONITOR, videoUuid);
        }
    };
    private int hTalkHandle;
    private TalkManager mTalkManager;

    public CameraUtils(Context context, FunVideoView funVideoView, ImageView bg,TextView tiem) {
        this.bg = bg;
        this.context = context;
        this.time=tiem;
        mFunVideoView = funVideoView;
        FunSupport.getInstance().registerOnFunDeviceListener(this);
        FunSupport.getInstance().registerOnFunDeviceOptListener(this);
        FunSupport.getInstance().registerOnFunDeviceOptListener(this);

    }

    public void setVideoUuid(String videoUuid) {
        this.videoUuid = videoUuid;
    }

    public String getVideoUuid() {
        return videoUuid;
    }

    public void PlayCamera() {
        if (mWaitDlg1 == null)
            mWaitDlg1 = new WaitDialogTime1(context, android.R.style.Theme_Translucent_NoTitleBar, this);
        mWaitDlg1.setWaitText("连接设备中");
        if(!mFunVideoView.isPlaying()){
            mWaitDlg1.show();
        }
        if (funDevice != null) {
            initPlay(funDevice);
        } else {
            FunSupport.getInstance().requestLanDeviceList();
        }
//        getVideoState();
//        FunSupport.getInstance().requestDeviceStatus(FunDevType.EE_DEV_NORMAL_MONITOR, videoUuid);

    }

    public void getVideoState() {

        //请求雄迈设备是否已经上线
        if (!TextUtils.isEmpty(videoUuid)) {
            if (nbStateCount < 18) {//查询雄迈api18次  直到已经上线或18次了
                nbStateCount++;
                Log.e("查询摄像头次数-------", nbStateCount + "");
                handler.sendEmptyMessageDelayed(0, 3000);
            } else {
                if (mWaitDlg1 != null && mWaitDlg1.isShowing()) {
                    mWaitDlg1.dismiss();
                }
                Toast.makeText(context, "摄像头链接失败请稍后重试", Toast.LENGTH_SHORT).show();
                handler.removeCallbacksAndMessages(null);
                nbStateCount = 0;
            }
        }
    }

    public void initPlay(FunDevice funDevice) {
        hTalkHandle = FunSupport.getInstance().requestDeviceStartTalk(funDevice);
        mFunVideoView.setOnPreparedListener(this);
        mFunVideoView.setOnErrorListener(this);
        mFunVideoView.setOnInfoListener(this);
        mTalkManager = new TalkManager(funDevice);

        userId = FunSDK.GetId(userId, this);
        onFlip();
        playRealMedia();
    }

    @Override
    public void onDeviceListChanged() {

    }

    @Override
    public void onDeviceStatusChanged(FunDevice funDevice) {
        int satusId = funDevice.devStatus.getSatusId();
        Log.e("摄像头状态-----", satusId + "");
        if (satusId == 1) {
            if (mWaitDlg1 != null && mWaitDlg1.isShowing()) {
                mWaitDlg1.dismiss();
            }//在线
            handler.removeCallbacksAndMessages(null);
            FunSupport.getInstance().requestDeviceLogin(funDevice);
            this.funDevice = funDevice;
            initPlay(funDevice);
        } else {
            getVideoState();
        }
    }

    @Override
    public void onDeviceAddedSuccess() {
        Log.e("摄像头------", "onDeviceAddedSuccess");
    }

    @Override
    public void onDeviceAddedFailed(Integer errCode) {
        Log.e("摄像头------", "onDeviceAddedFailed");
    }

    @Override
    public void onDeviceRemovedSuccess() {
        Log.e("摄像头------", "onDeviceRemovedSuccess");
    }

    @Override
    public void onDeviceRemovedFailed(Integer errCode) {
        Log.e("摄像头------", "onDeviceRemovedFailed");
    }

    @Override
    public void onAPDeviceListChanged() {
        Log.e("摄像头------", "onAPDeviceListChanged");
    }

    @Override
    public void onLanDeviceListChanged() {
        List<FunDevice> lanDeviceList = FunSupport.getInstance().getLanDeviceList();
        if (lanDeviceList.size() > 0) {
            for (int x = 0; x < lanDeviceList.size(); x++) {
                if (videoUuid.equals(lanDeviceList.get(x).getDevSn())) {
                    funDevice = lanDeviceList.get(x);
                }
            }
            if (funDevice != null) {
                initPlay(funDevice);
            } else {
//                Toast.makeText(context, "该WIFI内未查询到设备", Toast.LENGTH_SHORT).show();
//                if (mWaitDlg1 != null && mWaitDlg1.isShowing()) {
//                    mWaitDlg1.dismiss();
//                }
                FunSupport.getInstance().requestDeviceStatus(FunDevType.EE_DEV_NORMAL_MONITOR, videoUuid);
            }
        } else {
//            Toast.makeText(context, "该WIFI内未查询到设备", Toast.LENGTH_SHORT).show();
//            if (mWaitDlg1 != null && mWaitDlg1.isShowing()) {
//                mWaitDlg1.dismiss();
//            }
            FunSupport.getInstance().requestDeviceStatus(FunDevType.EE_DEV_NORMAL_MONITOR, videoUuid);
        }
    }

    @Override
    public void onDeviceLoginSuccess(FunDevice funDevice) {
        Log.e("摄像头------", "onDeviceLoginSuccess");
    }

    @Override
    public void onDeviceLoginFailed(FunDevice funDevice, Integer errCode) {
        Log.e("摄像头------", "onDeviceLoginFailed");
    }

    @Override
    public void onDeviceGetConfigSuccess(FunDevice funDevice, String configName, int nSeq) {
        Log.e("摄像头------", "onDeviceGetConfigSuccess");
    }

    @Override
    public void onDeviceGetConfigFailed(FunDevice funDevice, Integer errCode) {
        Log.e("摄像头------", "onDeviceGetConfigFailed");
    }

    @Override
    public void onDeviceSetConfigSuccess(FunDevice funDevice, String configName) {
        Log.e("摄像头------", "onDeviceSetConfigSuccess");
    }

    @Override
    public void onDeviceSetConfigFailed(FunDevice funDevice, String configName, Integer errCode) {
        Log.e("摄像头------", "onDeviceSetConfigFailed");
    }

    @Override
    public void onDeviceChangeInfoSuccess(FunDevice funDevice) {
        Log.e("摄像头------", "onDeviceChangeInfoSuccess");
    }

    @Override
    public void onDeviceChangeInfoFailed(FunDevice funDevice, Integer errCode) {
        Log.e("摄像头------", "onDeviceChangeInfoFailed");
    }

    @Override
    public void onDeviceOptionSuccess(FunDevice funDevice, String option) {
        Log.e("摄像头------", "onDeviceOptionSuccess");
    }

    @Override
    public void onDeviceOptionFailed(FunDevice funDevice, String option, Integer errCode) {
        Toast.makeText(context, "请稍后重试", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeviceFileListChanged(FunDevice funDevice) {
        Log.e("摄像头------", "onDeviceFileListChanged");
    }

    @Override
    public void onDeviceFileListChanged(FunDevice funDevice, H264_DVR_FILE_DATA[] datas) {
        Log.e("摄像头------", "onDeviceFileListChanged");
    }

    @Override
    public void onDeviceFileListGetFailed(FunDevice funDevice) {

    }

    public void unRegister() {
        FunSupport.getInstance().removeOnFunDeviceListener(this);
        FunSupport.getInstance().removeOnFunDeviceOptListener(this);
        FunSupport.getInstance().removeOnFunDeviceOptListener(this);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mWaitDlg1 != null && mWaitDlg1.isShowing())
            mWaitDlg1.dismiss();
        mFunVideoView.setVisibility(View.VISIBLE);
        Log.e("摄像头------", "onPrepared");
        time.setVisibility(View.GONE);
        mFunVideoView.setMediaSound(true);
        mTalkManager.onStopTalk();
    }

    public void onFlip() {
        String devSn = funDevice.getDevSn();
        //首先要判断该设备是否支持翻转 通过能力集判断  SupportCorridorMode;//是否支持走廊模式，就是90度旋转
        FunSDK.DevGetConfigByJson(userId, funDevice.getDevSn(),
                JsonConfig.SYSTEM_FUNCTION, 8192, 0, 5000, 0);
    }

    @Override
    public int OnFunSDKResult(Message message, MsgContent msgContent) {
        return 0;
    }

    public void playRealMedia() {
        if (mWaitDlg1 == null)
            mWaitDlg1 = new WaitDialogTime1(context, android.R.style.Theme_Translucent_NoTitleBar, this);
        mWaitDlg1.setWaitText("正在打开视频...");
        if(!mFunVideoView.isPlaying())
            mWaitDlg1.show();
        //正在打开视频...
        if (funDevice.isRemote) {
            mFunVideoView.setRealDevice(funDevice.getDevSn(), funDevice.CurrChannel);
        } else {
            String deviceIp = FunSupport.getInstance().getDeviceWifiManager().getGatewayIp();
            mFunVideoView.setRealDevice(deviceIp, funDevice.CurrChannel);
        }

    }

    public void stopMedia() {
        if (null != mFunVideoView) {
            mFunVideoView.setVisibility(View.INVISIBLE);
            bg.setBackgroundResource(R.drawable.bg1);
            time.setVisibility(View.VISIBLE);
            mFunVideoView.stopPlayback();
            mFunVideoView.stopRecordVideo();
            nbStateCount = 0;
            if (funDevice != null)
                FunSupport.getInstance().requestDeviceLogout(funDevice);
            handler.removeCallbacksAndMessages(null);
        }
    }

    public void switchMediaStream(TextView view) {
        if (null != mFunVideoView) {
            if (FunStreamType.STREAM_MAIN == mFunVideoView.getStreamType()) {
                mFunVideoView.setStreamType(FunStreamType.STREAM_SECONDARY);
                view.setText("流畅");
            } else {
                mFunVideoView.setStreamType(FunStreamType.STREAM_MAIN);
                view.setText("高清");
            }

            // 重新播放
            mFunVideoView.stopPlayback();
            playRealMedia();
        }
    }


    public void setDeviceNull() {
        funDevice = null;
    }
}
