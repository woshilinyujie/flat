package com.wl.wlflatproject.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.location.AMapLocation;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.wl.wlflatproject.Bean.CalendarParam;
import com.wl.wlflatproject.Bean.GDFutureWeatherBean;
import com.wl.wlflatproject.Bean.GDNowWeatherBean;
import com.wl.wlflatproject.Bean.MainMsgBean;
import com.wl.wlflatproject.Bean.SetMsgBean;
import com.wl.wlflatproject.Bean.StateBean;
import com.wl.wlflatproject.Bean.UpdataJsonBean;
import com.wl.wlflatproject.Bean.UpdateAppBean;
import com.wl.wlflatproject.MService.DataService;
import com.wl.wlflatproject.MUtils.CameraUtils;
import com.wl.wlflatproject.MUtils.DateUtils;
import com.wl.wlflatproject.MUtils.DeviceUtils;
import com.wl.wlflatproject.MUtils.DpUtils;
import com.wl.wlflatproject.MUtils.GsonUtils;
import com.wl.wlflatproject.MUtils.IntentUtil;
import com.wl.wlflatproject.MUtils.LocationUtils;
import com.wl.wlflatproject.MUtils.LunarUtils;
import com.wl.wlflatproject.MUtils.PreferencesUtils;
import com.wl.wlflatproject.MUtils.RbMqUtils;
import com.wl.wlflatproject.MUtils.SerialPortUtil;
import com.wl.wlflatproject.MUtils.VersionUtils;
import com.wl.wlflatproject.MView.CodeDialog;
import com.wl.wlflatproject.MView.WaitDialogTime;
import com.wl.wlflatproject.R;
import com.wl.wlflatproject.support.widget.FunVideoView;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    @BindView(R.id.bg)
    ImageView bg;
    @BindView(R.id.video_switch)
    TextView videoSwitch;
    @BindView(R.id.fun_view)
    FunVideoView funView;
    @BindView(R.id.view)
    View view;
    @BindView(R.id.time)
    TextView time;
    @BindView(R.id.rl)
    RelativeLayout rl;
    @BindView(R.id.lock_bt)
    ImageView lockBt;
    @BindView(R.id.video_iv)
    ImageView videoIv;
    @BindView(R.id.onoff_bt)
    ImageView onoffBt;
    @BindView(R.id.bufang_bt)
    ImageView bufangBt;
    @BindView(R.id.code_bt)
    ImageView codeBt;
    @BindView(R.id.location_tv)
    TextView locationTv;
    @BindView(R.id.today_weather_view)
    View todayWeatherView;
    @BindView(R.id.today_temp_tv)
    TextView todayTempTv;
    @BindView(R.id.today_weather_tv)
    TextView todayWeatherTv;
    @BindView(R.id.second_day_view)
    View secondDayView;
    @BindView(R.id.second_day_tv)
    TextView secondDayTv;
    @BindView(R.id.third_day_view)
    View thirdDayView;
    @BindView(R.id.third_day_tv)
    TextView thirdDayTv;
    @BindView(R.id.weather_ll)
    LinearLayout weatherLl;
    @BindView(R.id.date_tv)
    TextView dateTv;
    @BindView(R.id.week_cn_tv)
    TextView weekCnTv;
    @BindView(R.id.week_en_tv)
    TextView weekEnTv;
    @BindView(R.id.changkai)
    ImageView changKai;
    @BindView(R.id.calendar_cn_tv)
    TextView calendarCnTv;
    @BindView(R.id.calendar_ll)
    RelativeLayout calendarLl;
    @BindView(R.id.setting)
    TextView setting;
    @BindView(R.id.swtich)
    TextView switchMq;
    @BindView(R.id.today_extent_tv)
    TextView todayExtentTv;
    @BindView(R.id.second_weather_tv)
    TextView secondWeatherTv;
    @BindView(R.id.third_weather_tv)
    TextView thirdWeatherTv;
    private int version;
    /* 更新进度条 */
    private ProgressBar mProgress;
    private AlertDialog mDownloadDialog;
    private int cPostion = -1;
    private int fHeight = 0;
    private RbMqUtils rbmq;
    private SerialPortUtil serialPort;
    private StateBean bean = new StateBean();
    private WaitDialogTime dialogTime;
    private CodeDialog codeDialog;
    private int changkaiFlag = 3;
    private String openDegree = "--";//开门角度
    private String openDoorWaitTime = "--";//开门等待时间
    private String openDoorSpeed = "--";//开门速度
    private String closeDoorSpeed = "--";//关门速度
    private String leftDegreeRepair = "--";//右角度修复值
    private String rightDegreeRepair = "--";//左角度修复值
    static String lcddisplay = "/sys/gpio_test_attr/lcd_power";
    File file = new File(lcddisplay);
    Handler handler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (wifiManager == null)
                        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    String ssid = wifiInfo.getSSID();
                    if (!TextUtils.isEmpty(ssid) && !ssid.equals("<unknown ssid>")) {
                        rbmq.pushMsg(id + "#" + stateJson);
                    }
                    sendEmptyMessageDelayed(0, 60000);
                    break;
                case 1:
                    if (isFull)
                        setScreen();
                    cameraUtils.stopMedia();
                    Log.e("有人离开停止视频", "..");
                    break;
                case 2:
                    if (mDownloadDialog != null) {
                        mDownloadDialog.dismiss();
                        mDownloadDialog = null;
                    }
                    OkGo.getInstance().cancelTag(MainActivity.this);
                    requestPermission();
                    handler.sendEmptyMessageDelayed(2, 24 * 60 * 60 * 1000);
                    break;
                case 3:
                    writeFile(file, 1 + "");
                    handler.sendEmptyMessageDelayed(3, 1000 * 3 * 60);
                    break;
                case 4:
                    time.setText(dateUtils.dateFormat6(System.currentTimeMillis()));
                    handler.sendEmptyMessageDelayed(4, 1000);
                    break;
                case 5:
                    serialPort.sendDate("+DATATOPAD\r\n".getBytes());
                    break;
            }
        }
    };
    private String id;
    private String stateJson;
    private WifiManager wifiManager;
    private NetStatusReceiver receiver;
    private CameraUtils cameraUtils;
    private TimeReceiver mTimeReceiver;
    private AMapLocation mAMapLocation;
    private long mExitTime;
    private boolean isFull = false;
    private PowerManager.WakeLock wl;
    private FileOutputStream fout;
    private PrintWriter printWriter;
    private DateUtils dateUtils;
    private String msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initData();
        setMq();
        initSerialPort();
        initCalendar();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        wl.acquire();
    }

    private void initData() {
        EventBus.getDefault().register(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        DataService dataService = new DataService();
        registerReceiver(dataService, filter);


        dateUtils = DateUtils.getInstance();
        handler.removeMessages(2);
        handler.removeMessages(3);
        handler.removeMessages(4);
        if (dialogTime == null)
            dialogTime = new WaitDialogTime(this, android.R.style.Theme_Translucent_NoTitleBar);
        requestPermission();
        id = DeviceUtils.getSerialNumber(MainActivity.this);
        rbmq = new RbMqUtils();
        bean.setAck(0);
        bean.setCmd(0x46);
        bean.setDevType("WL025S1");
        bean.setDevId(id);
        bean.setSeqId(1);
        bean.setTime(System.currentTimeMillis());
        bean.setVendor("general");
        stateJson = GsonUtils.GsonString(bean);
        receiver = new NetStatusReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(receiver, intentFilter);
        registerTimeReceiver();
        cameraUtils = new CameraUtils(this, funView, bg, time);
        if (!TextUtils.isEmpty(PreferencesUtils.getString(this, "videoId" + id))) {
            cameraUtils.setVideoUuid(PreferencesUtils.getString(this, "videoId" + id));
        }
        handler.sendEmptyMessageDelayed(2, 24 * 60 * 60 * 1000);
        handler.sendEmptyMessageDelayed(3, 1000 * 3 * 60);
        handler.sendEmptyMessage(4);
    }

    @OnClick({R.id.swtich, R.id.changkai, R.id.setting, R.id.video_switch, R.id.lock_bt, R.id.onoff_bt,
            R.id.bufang_bt, R.id.code_bt, R.id.fun_view,
            R.id.weather_ll, R.id.calendar_ll, R.id.video_iv})
    public void onViewClicked(View view) {
        handler.removeMessages(3);
        handler.sendEmptyMessageDelayed(3, 1000 * 3 * 60);
        switch (view.getId()) {
            case R.id.setting:
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                intent.putExtra("openDegree", openDegree);
                intent.putExtra("openDoorWaitTime", openDoorWaitTime);
                intent.putExtra("openDoorSpeed", openDoorSpeed);
                intent.putExtra("closeDoorSpeed", closeDoorSpeed);
                intent.putExtra("leftDegreeRepair", leftDegreeRepair);
                intent.putExtra("rightDegreeRepair", rightDegreeRepair);
                startActivity(intent);
                break;
            case R.id.video_switch:
                cameraUtils.switchMediaStream(videoSwitch);
                break;
            case R.id.swtich:
                if (RbMqUtils.MQIP.equals("rmq.wonlycloud.com")) {
                    rbmq.mClose();
                    RbMqUtils.MQIP = "116.62.46.10";
                    setMq();
                    switchMq.setText("当前-->测试环境");
                } else {
                    rbmq.mClose();
                    RbMqUtils.MQIP = "rmq.wonlycloud.com";
                    setMq();
                    switchMq.setText("当前-->正式环境");
                }
                break;
            case R.id.fun_view:
                if (fHeight == 0) {
                    fHeight = rl.getHeight();
                }
                if (!isFull) {
                    setFullScreen();
                } else {
                    setScreen();
                }
                break;
            case R.id.lock_bt://开门
                dialogTime.show();
                serialPort.sendDate("+COPEN:1\r\n".getBytes());
                break;
            case R.id.video_iv:
                if (!funView.isPlaying()) {
                    handler.removeMessages(1);
                    handler.sendEmptyMessageDelayed(1, 60000);
                    if (cameraUtils.getVideoUuid() == null) {
                        Toast.makeText(MainActivity.this, "请通过王力智能客户端配置WIFI", Toast.LENGTH_SHORT).show();
                    } else {
                        cameraUtils.PlayCamera();
                    }

                } else {
                    cameraUtils.stopMedia();
                }

                break;
            case R.id.changkai:
                if (changkaiFlag == 1) {
                    dialogTime.show();
                    serialPort.sendDate("+ALWAYSOPEN\r\n".getBytes());
                } else if (changkaiFlag == 2) {
                    dialogTime.show();
                    serialPort.sendDate("+CLOSEALWAYSOPEN\r\n".getBytes());
                }
                break;
            case R.id.onoff_bt:
                break;
            case R.id.bufang_bt:
                break;
            case R.id.code_bt:
                if (codeDialog == null)
                    codeDialog = new CodeDialog(this, R.style.ActionSheetDialogStyle);
                codeDialog.show();
                break;
            case R.id.weather_ll:
                ConnectivityManager connectivityManager
                        = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = connectivityManager.getActiveNetworkInfo();
                if (info != null && info.isAvailable()) {
                    mLocationUtils.startLocation();
                } else {
                    Toast.makeText(this, "WiFi不可用或已断开", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.calendar_ll:
                //日历
                if (mAMapLocation == null) {
                    Toast.makeText(this, "数据获取中...", Toast.LENGTH_SHORT).show();
                    return;
                }
                CalendarParam calendarParam = new CalendarParam();
                calendarParam.temp = todayTempTv.getText().toString();
                calendarParam.weather = todayWeatherTv.getText().toString();
                String city = mAMapLocation.getCity();
                String district = mAMapLocation.getDistrict();
                calendarParam.location = district == null ? city : district;
                CalendarActivity.start(this, calendarParam);
                break;
            default:
        }
    }

    /**
     * 服务器socket
     */
    public void setMq() {
        //发送端
        rbmq.publishToAMPQ("");
        //接收端
        String s = DeviceUtils.getSerialNumber(this) + "_robot";
        rbmq.subscribe(s);
        rbmq.setUpConnectionFactory();
        rbmq.setRbMsgListener(new RbMqUtils.OnRbMsgListener() {
            @Override
            public void AcceptMsg(String msg) {//服务器返回数据
                String s = "+MIPLWRITE:" + msg.length() + "," + msg + "\r\n";
                Log.e("服务器发给平板---", s);
                serialPort.sendDate(s.getBytes());
            }
        });
        handler.removeMessages(0);

        handler.sendEmptyMessageDelayed(0, 10000);
    }

    /**
     * 串口socket
     */
    private void initSerialPort() {
        serialPort = SerialPortUtil.getInstance();
        handler.sendEmptyMessageDelayed(5, 2000);
        serialPort.readCode(new SerialPortUtil.DataListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
            @Override
            public void getData(String data) {//串口返回数据
                runOnUiThread(new Runnable() {

                    private SetMsgBean setMsgBean;

                    @Override
                    public void run() {
                        if (data.contains("AT+CDOOR=")) {
                            String[] split = data.split("=");
                            switch (split[1]) {
                                case "1"://表示故障1
                                    Toast.makeText(MainActivity.this, "故障1", Toast.LENGTH_SHORT).show();
                                    break;
                                case "2"://表示故障2
                                    Toast.makeText(MainActivity.this, "故障2", Toast.LENGTH_SHORT).show();
                                    break;
                                case "C"://表示故障3
                                    Toast.makeText(MainActivity.this, "故障3", Toast.LENGTH_SHORT).show();
                                    break;
                                case "D"://表示故障4
                                    Toast.makeText(MainActivity.this, "故障4", Toast.LENGTH_SHORT).show();
                                    break;
                                case "A"://表示报警1
                                    Toast.makeText(MainActivity.this, "报警1", Toast.LENGTH_SHORT).show();
                                    break;
                                case "B"://表示报警2
                                    Toast.makeText(MainActivity.this, "报警2", Toast.LENGTH_SHORT).show();
                                    break;
                                case "E"://表示假锁状态
                                    Toast.makeText(MainActivity.this, "假锁", Toast.LENGTH_SHORT).show();
                                    break;
                                case "8"://8表示开门成功
                                    Toast.makeText(MainActivity.this, "开门成功", Toast.LENGTH_SHORT).show();
                                    if (dialogTime != null & dialogTime.isShowing())
                                        dialogTime.dismiss();
                                    break;
                                case "9"://表示关门成功
                                    Toast.makeText(MainActivity.this, "关门成功", Toast.LENGTH_SHORT).show();
                                    if (dialogTime != null & dialogTime.isShowing())
                                        dialogTime.dismiss();
                            }
                        } else if (data.contains("AT+DEFAULT=")) {
                            String[] s = data.split("=");
                            String[] split = s[1].split(",");
                            switch (Integer.parseInt(split[0])) {
                                case 1://代表开门角度
                                    openDegree = split[1];
                                    break;
                                case 2://开门等待时间
                                    openDoorWaitTime = split[1];
                                    break;
                                case 3://开门速度
                                    openDoorSpeed = split[1];
                                    break;
                                case 4://关门速度
                                    closeDoorSpeed = split[1];
                                    break;
                                case 5://右角度修复值
                                    rightDegreeRepair = split[1];
                                    break;
                                case 6://左角度修复值
                                    leftDegreeRepair = split[1];
                                    break;
                                case 7://已经常开
                                    if (changkaiFlag != 2) {
                                        changkaiFlag = 2;
                                        changKai.setBackgroundResource(R.drawable.cancel_open);
                                    }
                                    break;
                                case 8://没有开启常开
                                    if (changkaiFlag != 1) {
                                        changKai.setBackgroundResource(R.drawable.changkai);
                                        changkaiFlag = 1;
                                    }
                                    break;
                            }
                        } else if (data.contains("AT+LEFTANGLEREPAIR=1")) {//左角度修复值
                            if (setMsgBean == null)
                                setMsgBean = new SetMsgBean();
                            setMsgBean.setFlag(1);
                            if (!TextUtils.isEmpty(msg))
                                leftDegreeRepair = msg;
                            EventBus.getDefault().post(setMsgBean);
                        } else if (data.contains("AT+RIGHTANGLEREPAIR=1")) {//右角度修复值
                            if (setMsgBean == null)
                                setMsgBean = new SetMsgBean();
                            if (!TextUtils.isEmpty(msg))
                                rightDegreeRepair = msg;
                            setMsgBean.setFlag(2);
                            EventBus.getDefault().post(setMsgBean);
                        } else if (data.contains("AT+OPENANGLE=1")) {//开门角度
                            if (setMsgBean == null)
                                setMsgBean = new SetMsgBean();
                            if (!TextUtils.isEmpty(msg))
                                openDegree = msg;
                            setMsgBean.setFlag(3);
                            EventBus.getDefault().post(setMsgBean);
                        } else if (data.contains("AT+OPENWAITTIME=1")) {//等待时间
                            if (setMsgBean == null)
                                setMsgBean = new SetMsgBean();
                            if (!TextUtils.isEmpty(msg))
                                openDoorWaitTime = msg;
                            setMsgBean.setFlag(4);
                            EventBus.getDefault().post(setMsgBean);
                        } else if (data.contains("AT+OPENSPEED=1")) {//开门速度
                            if (setMsgBean == null)
                                setMsgBean = new SetMsgBean();
                            if (!TextUtils.isEmpty(msg))
                                openDoorSpeed = msg;
                            setMsgBean.setFlag(5);
                            EventBus.getDefault().post(setMsgBean);
                        } else if (data.contains("AT+CLOSESPEED=1")) {//关门速度AT+ALWAYSOPEN=1
                            if (setMsgBean == null)
                                setMsgBean = new SetMsgBean();
                            if (!TextUtils.isEmpty(msg))
                                closeDoorSpeed = msg;
                            setMsgBean.setFlag(6);
                            EventBus.getDefault().post(setMsgBean);
                        } else if (data.contains("AT+ALWAYSOPEN=1")) {//常开
                            changkaiFlag = 2;
                            if (dialogTime != null & dialogTime.isShowing()) ;
                            dialogTime.dismiss();
                            changKai.setBackgroundResource(R.drawable.cancel_open);
                        } else if (data.contains("AT+CLOSEALWAYSOPEN=1")) {//取消常开
                            changkaiFlag = 1;
                            if (dialogTime != null & dialogTime.isShowing()) ;
                            dialogTime.dismiss();
                            changKai.setBackgroundResource(R.drawable.changkai);
                        } else if (data.contains("AT+CDWAKE=1")) {//有人   但是不打开视频
                            writeFile(file, 2 + "");//打开屏幕
                            handler.removeMessages(3);
                            handler.sendEmptyMessageDelayed(3, 1000 * 3 * 60);
                        } else if (data.contains("AT+CDECT=")) {
                            String[] split = data.split("=");
                            String[] split1 = split[1].split(",");
                            switch (split1[0]) {
                                case "0"://表示前板检测到遮挡  门外
                                    if (split1[1].equals("0")) {//人离开
                                        handler.sendEmptyMessageDelayed(1, 60000);
                                        Log.e("" +
                                                "", "..");
                                    } else {//人靠近
                                        handler.removeMessages(1);
                                        Log.e("检测有人", "..");
                                        writeFile(file, 2 + "");//打开屏幕
                                        handler.removeMessages(3);
                                        handler.sendEmptyMessageDelayed(3, 1000 * 3 * 60);
                                        if (cameraUtils.getVideoUuid() == null) {
                                            Toast.makeText(MainActivity.this, "请检查摄像头是否配置wifi", Toast.LENGTH_SHORT).show();
                                        } else {
                                            if (!funView.isPlaying())
                                                cameraUtils.PlayCamera();
                                        }
                                    }
                                    break;
                                case "1"://表示后板检测到遮挡 门内

                                    break;
                            }
                        } else if (data.contains("AT+MIPLNOTIFY=")) {//加密消息上报给服务器
                            String[] split = data.split(",");
                            String s = split[split.length - 1];
                            String[] split1 = s.split("\r\n");
                            String s1 = id + "#" + split1[0];
                            rbmq.pushMsg(s1);
                            handler.removeMessages(0);
                            handler.sendEmptyMessageAtTime(0, 60000);
                        } else if (data.contains("AT+CGSN=1")) {//上传id
                            serialPort.sendDate(("+CGSN:" + id + "\r\n").getBytes());
                        } else if (data.contains("AT+CGATT?")) {//服务器是否链接
                            serialPort.sendDate((rbmq.isConnection() + "\r\n").getBytes());
                        } else if (data.contains("AT+CCLK?")) {//上报时间
                            serialPort.sendDate(("+CCLK:" + System.currentTimeMillis() + "\r\n").getBytes());
                        } else if (data.contains("AT+VIDEOSN=")) {//设置摄像头id
                            String[] split = data.split("=");
                            if (split.length > 1) {
                                cameraUtils.setVideoUuid(split[1]);
                                PreferencesUtils.saveString(MainActivity.this, "videoId" + id, split[1]);
                            }
                        }
                    }
                });
            }
        });
    }


    //---------------------eventBus----------------
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MainMsgBean msgBean) {
        msg = msgBean.getMsg();
        int flag = msgBean.getFlag();
        switch (flag) {
            case 1://左角度修复值
                serialPort.sendDate(("+LEFTANGLEREPAIR:" + msg + "\r\n").getBytes());
                break;
            case 2://右角度修复值
                serialPort.sendDate(("+RIGHTANGLEREPAIR:" + msg + "\r\n").getBytes());
                break;
            case 3://开门角度
                serialPort.sendDate(("+OPENANGLE:" + msg + "\r\n").getBytes());
                break;
            case 4://等待时间
                serialPort.sendDate(("+OPENWAITTIME:" + msg + "\r\n").getBytes());
                break;
            case 5://开门速度
                serialPort.sendDate(("+OPENSPEED:" + msg + "\r\n").getBytes());
                break;
            case 6://关门速度
                serialPort.sendDate(("+CLOSESPEED:" + msg + "\r\n").getBytes());
                break;
        }
    }

    /**
     * 初始化日历
     */
    private void initCalendar() {
        DateUtils instance = DateUtils.getInstance();
        //日期
        String dayOrMonthOrYear = instance.getDayOrMonthOrYear(System.currentTimeMillis());
        dateTv.setText(dayOrMonthOrYear);
        //星期
        weekCnTv.setText(instance.getWeekday(System.currentTimeMillis(), true));
        weekEnTv.setText(instance.getWeekday(System.currentTimeMillis(), false));
        //农历
        //猪 贰零壹玖 润 六月 小廿八 己亥 辛未 戊辰
        Calendar calendar = Calendar.getInstance();
        String[] lunar = LunarUtils.getLunar(
                calendar.get(Calendar.YEAR) + "",
                (calendar.get(Calendar.MONTH) + 1) + "",
                calendar.get(Calendar.DAY_OF_MONTH) + "");
        calendarCnTv.setText(String.format("农历-%s月-%s", lunar[3], lunar[4]));
    }

    /**
     * 注册时间广播
     */
    private void registerTimeReceiver() {
        mTimeReceiver = new TimeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(mTimeReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocationUtils.startLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mLocationUtils.stopLocation();
    }

    @Override
    protected void onDestroy() {
        try {
            if (fout != null) {
                fout.close();
                fout = null;
            }
            if (printWriter != null) {
                printWriter.close();
                printWriter = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        rbmq.mClose();
        handler.removeMessages(0);
        handler.removeMessages(1);
        handler.removeMessages(2);
        handler.removeMessages(3);
        handler.removeMessages(4);
        serialPort.close();
        cameraUtils.stopMedia();
        cameraUtils.unRegister();
        mLocationUtils.destroyLocationClient();
        unregisterReceiver(receiver);
        unregisterReceiver(mTimeReceiver);
        wl.release();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }


    public class NetStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                if (cameraUtils != null) {
                    cameraUtils.setDeviceNull();
                }
                ConnectivityManager connectivityManager
                        = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = connectivityManager.getActiveNetworkInfo();
                if (info != null && info.isAvailable()) {
                    mLocationUtils.startLocation();
                    handler.removeMessages(0);
                    handler.sendEmptyMessageDelayed(0, 10000);
                    rbmq.clearQueue();
                    Log.d("hsl666", "onReceive: ==可用");
                    initCalendar();
                } else {
                    Toast.makeText(context, "WiFi不可用或已断开", Toast.LENGTH_SHORT).show();
                    Log.d("hsl666", "onReceive: ==不可用");
                }
//
            }
        }
    }

    /**
     * 时间更新的广播
     */
    public class TimeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                DateUtils dateUtils = DateUtils.getInstance();
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                if (hour == 0) {
                    //日期
                    String dayOrMonthOrYear = dateUtils.getDayOrMonthOrYear(System.currentTimeMillis());
                    dateTv.setText(dayOrMonthOrYear);
                    //星期
                    weekCnTv.setText(dateUtils.getWeekday(System.currentTimeMillis(), true));
                    weekEnTv.setText(dateUtils.getWeekday(System.currentTimeMillis(), false));
                    //农历
                    //农历
                    //猪 贰零壹玖 润 六月 小廿八 己亥 辛未 戊辰
                    String[] lunar = LunarUtils.getLunar(
                            calendar.get(Calendar.YEAR) + "",
                            (calendar.get(Calendar.MONTH) + 1) + "",
                            calendar.get(Calendar.DAY_OF_MONTH) + "");
                    calendarCnTv.setText(String.format("农历-%s月-%s", lunar[3], lunar[4]));
                }
            }
        }
    }

    /**
     * 定位工具类
     */
    private LocationUtils mLocationUtils = new LocationUtils() {

        @Override
        protected void onQueryNowWeatherResult(GDNowWeatherBean.LivesBean livesBean, AMapLocation aMapLocation) {
            mAMapLocation = aMapLocation;
            //地址
            StringBuffer locationBuffer = new StringBuffer();
            String city = aMapLocation.getCity();
            String district = aMapLocation.getDistrict();
            locationBuffer.append(city);
            if (district != null) {
                locationBuffer.append(district);
            }
            locationTv.setText(locationBuffer);
            //今日天气
            //ICON Text
            setWeatherIcon(todayWeatherView, livesBean.getWeather());
            //实时时间
            String dayOrMonthOrYear = DateUtils.getInstance().getDayOrMonthOrYear(System.currentTimeMillis());
            setWeatherText(todayWeatherTv, livesBean.getWeather(), dayOrMonthOrYear, false);
            //体感温度
            todayTempTv.setText(livesBean.getTemperature());
        }

        @Override
        protected void onQueryFutureWeatherResult(GDFutureWeatherBean.ForecastsBean forecastsBean, AMapLocation aMapLocation) {
            List<GDFutureWeatherBean.ForecastsBean.CastsBean> beanCasts = forecastsBean.getCasts();
            DateUtils dateUtils = DateUtils.getInstance();
            boolean night = dateUtils.isNight();
            //当前天气
            GDFutureWeatherBean.ForecastsBean.CastsBean todayWeather = beanCasts.get(0);
            todayExtentTv.setText(todayWeather.getDaytemp() + "°" + " /  " + todayWeather.getNighttemp() + "°");

            //后两天天气
            GDFutureWeatherBean.ForecastsBean.CastsBean secondWeather = beanCasts.get(1);
            setWeatherIcon(secondDayView, night ? secondWeather.getNightweather() : secondWeather.getDayweather());
            setWeatherText(secondDayTv,
                    secondWeatherTv,
                    "明天",
                    night ? secondWeather.getNightweather() : secondWeather.getDayweather(),
                    secondWeather.getDaytemp(),
                    secondWeather.getNighttemp());
            GDFutureWeatherBean.ForecastsBean.CastsBean thirdWeather = beanCasts.get(2);
            setWeatherIcon(thirdDayView, night ? thirdWeather.getNightweather() : thirdWeather.getDayweather());
            setWeatherText(thirdDayTv,
                    thirdWeatherTv,
                    "后天",
                    night ? thirdWeather.getNightweather() : thirdWeather.getDayweather(),
                    thirdWeather.getDaytemp(),
                    thirdWeather.getNighttemp());
        }
    };

    /**
     * 设置天气ICON
     *
     * @param view
     * @param weather
     */
    private void setWeatherIcon(View view, String weather) {
        String code = LocationUtils.weatherCode(weather);
        switch (code) {
            case "1":
                view.setBackgroundResource(R.drawable.sun_icon);
                break;
            case "2":
                view.setBackgroundResource(R.drawable.cloud_icon);
                break;
            case "3":
                view.setBackgroundResource(R.drawable.rain_icon);
                break;
            case "4":
                view.setBackgroundResource(R.drawable.snow_icon);
                break;
            default:
        }
    }

    /**
     * 设置天气文本
     *
     * @param tv
     * @param weather
     * @param showDate
     */
    private void setWeatherText(TextView tv, String weather, String date, boolean showDate) {
        String code = LocationUtils.weatherCode(weather);
        StringBuffer content = new StringBuffer();
        if (showDate) {
            String format11 = DateUtils.getInstance().dateFormat11(date);
            content.append(format11 + " ");
        }
        switch (code) {
            case "1":
                content.append("晴");
                break;
            case "2":
                content.append("阴");
                break;
            case "3":
                content.append("雨");
                break;
            case "4":
                content.append("雪");
                break;
            default:
        }
        tv.setText(content.toString());
    }

    /**
     * 设置天气文本
     *
     * @param tv
     * @param date
     * @param weather
     * @param dayTemp
     * @param nightTemp
     */
    private void setWeatherText(TextView tv, TextView tvW, String date, String weather, String dayTemp, String nightTemp) {
        String code = LocationUtils.weatherCode(weather);
        String w = "晴";
        switch (code) {
            case "1":
                w = "晴";
                break;
            case "2":
                w = "阴";
                break;
            case "3":
                w = "雨";
                break;
            case "4":
                w = "雪";
                break;
            default:
        }
        tvW.setText(date + " · " + w);
        tv.setText(dayTemp + "°" + " /  " + nightTemp + "°");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(MainActivity.this, "再按一次退出程序",
                        Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
        }

        return true;
    }


    public void setFullScreen() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rl.setLayoutParams(layoutParams);
        funView.setLayoutParams(layoutParams1);
        isFull = true;
    }

    public void setScreen() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, fHeight);
        RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, fHeight);
        layoutParams1.rightMargin = DpUtils.dip2px(this, 120);
        layoutParams1.leftMargin = DpUtils.dip2px(this, 120);
        rl.setLayoutParams(layoutParams);
        funView.setLayoutParams(layoutParams1);
        isFull = false;
    }


    private void requestPermission() {
        AndPermission.with(this)
                .runtime()
                .permission(
                        Manifest.permission.ACCESS_NOTIFICATION_POLICY,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                        checkUpdate();
                        if (mLocationUtils != null) {
                            mLocationUtils.startLocation();
                        }
                        initCalendar();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        checkUpdate();
                    }
                })
                .start();
    }

    protected void checkUpdate() {
        version = VersionUtils.getVersionCode(this);
        requestAppUpdate(version, new DataRequestListener<UpdateAppBean>() {
            @Override
            public void success(UpdateAppBean data) {
                downloadApp(data.getPUS().getBody().getUrl());
            }

            @Override
            public void fail(String msg) {

            }
        });
    }

    private void requestAppUpdate(int version, final DataRequestListener<UpdateAppBean> listener) {
        UpdataJsonBean updataJsonBean = new UpdataJsonBean();
        UpdataJsonBean.PUSBean pusBean = new UpdataJsonBean.PUSBean();
        UpdataJsonBean.PUSBean.BodyBean bodyBean = new UpdataJsonBean.PUSBean.BodyBean();
        UpdataJsonBean.PUSBean.HeaderBean headerBean = new UpdataJsonBean.PUSBean.HeaderBean();

        bodyBean.setToken("");
        bodyBean.setVendor_name("general");
        bodyBean.setPlatform("android");
        bodyBean.setEndpoint_type("WL025S1");
        bodyBean.setCurrent_version(version + "");

        headerBean.setApi_version("1.0");
        headerBean.setMessage_type("MSG_PRODUCT_UPGRADE_DOWN_REQ");
        headerBean.setSeq_id("1");

        pusBean.setBody(bodyBean);
        pusBean.setHeader(headerBean);
        updataJsonBean.setPUS(pusBean);

        String s = GsonUtils.GsonString(updataJsonBean);

        OkGo.<String>post("https://pus.wonlycloud.com:10400").upJson(s).execute(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                String s = response.body();
                Gson gson = new Gson();
                UpdateAppBean updateAppBean = gson.fromJson(s, UpdateAppBean.class);
                if (Integer.parseInt(updateAppBean.getPUS().getBody().getNew_version()) > version) {
                    listener.success(updateAppBean);
                }
            }

            @Override
            public void onError(Response<String> response) {
                listener.fail("服务器连接失败");
            }
        });
    }


    //下载apk文件并跳转(第二次请求，get)
    private void downloadApp(String apk_url) {
        OkGo.<File>get(apk_url).tag(this).execute(new FileCallback() {
            @Override
            public void onError(Response<File> response) {
                if (mDownloadDialog != null) {
                    mDownloadDialog.dismiss();
                    mDownloadDialog = null;
                }
            }

            @Override
            public void onSuccess(Response<File> response) {
                String filePath = response.body().getAbsolutePath();
                mDownloadDialog.dismiss();
                mDownloadDialog = null;
                Intent intent = IntentUtil.getInstallAppIntent(MainActivity.this, filePath);
//                测试过这里必须用startactivity，不能用stratactivityforresult
                startActivity(intent);
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        SilentInstall installHelper = new SilentInstall();
//                        final boolean result = installHelper.install(filePath);
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (result) {
//                                    Toast.makeText(MainActivity.this, "安装成功！", Toast.LENGTH_SHORT).show();
//                                } else {
//                                    Toast.makeText(MainActivity.this, "安装失败！", Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        });
//
//                    }
//                }).start();
            }

            @Override
            public void downloadProgress(Progress progress) {
                if (mDownloadDialog == null) {
                    // 构造软件下载对话框
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("正在更新");
                    // 给下载对话框增加进度条
                    final LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                    View v = inflater.inflate(R.layout.item_progress, null);
                    mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
                    builder.setView(v);
                    mDownloadDialog = builder.create();
                    mDownloadDialog.show();
                }
                mProgress.setProgress((int) (progress.fraction * 100));
            }
        });
    }

    public interface DataRequestListener<T> {
        //请求成功
        void success(T data);

        //请求失败
        void fail(String msg);
    }

    public void writeFile(File file, String mode) {
        try {
            if (fout == null) {
                fout = new FileOutputStream(file);
            }
            if (printWriter == null) {
                printWriter = new PrintWriter(fout);
            }
            printWriter.println(mode);
            printWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
