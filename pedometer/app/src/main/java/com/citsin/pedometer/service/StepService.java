package com.citsin.pedometer.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.citsin.pedometer.MainActivity;
import com.citsin.pedometer.util.PedometerSettings;
import com.citsin.pedometer.R;
import com.citsin.pedometer.util.StepDetector;
import com.citsin.pedometer.util.StepListener;
import com.citsin.pedometer.db.SportRecordHelper;
import com.citsin.pedometer.util.Utils;

public class StepService extends Service {

    private final static String LOG_TAG = "StepService";
    public final static int RUNNING_TYPE_NONE = -1;
    public final static int RUNNING_TYPE_STOP = 0;
    public final static int RUNNING_TYPE_RUNNING = 1;
    public final static int RUNNING_TYPE_PAUSE = 2;
    private static double METRIC_RUNNING_FACTOR = 1.02784823;
    private static double METRIC_WALKING_FACTOR = 0.708;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private StepDetector mStepDetector;
    private int mStepCount = 0;            //总步数
    private float distance;//距离
    private float calories;//卡路里
    private long duration;//运动时长
    private float speed;//运动速度
    private long mRunningTime = 0;        //总时间
    private PowerManager.WakeLock mWakeLock;
    private long mStartTime = 0;//开始运动时的时间
    private Notification mNotification;
    private RemoteViews mNotificationView;
    private int mRunningType = RUNNING_TYPE_STOP;
    private int mCallbackNumber = 0;
    private final RemoteCallbackList<IPedometerCallback> mCallbacks
            = new RemoteCallbackList<IPedometerCallback>();
    private SharedPreferences mSettings;
    private PedometerSettings mPedometerSettings;
    public StepService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }



    private boolean mIsTimeUpdate = false;

    private IPedometer.Stub mBinder = new IPedometer.Stub() {
        @Override
        public void registerCallback(IPedometerCallback cb) throws RemoteException {
            Log.v(LOG_TAG, "registerCallback" + cb);
            if (cb == null)return;
            synchronized (StepService.this){
                mCallbacks.register(cb);
                ++mCallbackNumber;
                if (!mIsTimeUpdate){//注册时间更新
                    mIsTimeUpdate = true;
                    mTimeChangeHandler.sendEmptyMessageDelayed(0, 1000);
                }
            }
            if (mRunningType == RUNNING_TYPE_RUNNING){
                broadcastValueChange();
            }
        }
        @Override
        public void unregisterCallback(IPedometerCallback cb) throws RemoteException {
            Log.v(LOG_TAG, "unregisterCallback" + cb);
            if (cb == null)return;
            synchronized (StepService.this){
                //解除注册
                mCallbacks.unregister(cb);
                //回调数减一
                --mCallbackNumber;
                if (mCallbackNumber == 0)mIsTimeUpdate = false;
            }
        }

        /**
         * 开启运动记录
         * @throws RemoteException
         */
        @Override
        public synchronized void start() throws RemoteException {
            //如果已经是运行状态，则直接返回
            if (mRunningTime == RUNNING_TYPE_RUNNING)return;
            //读取配置文件信息
            mSettings = PreferenceManager.getDefaultSharedPreferences(StepService.this);
            mPedometerSettings = new PedometerSettings(mSettings);
            //设置开启前台进程模式，以防止服务被结束
            setStartFroground();
            //更新步数为0
            mStepCount = 0;//设置步数为0
            calories = 0;
            distance = 0;
            speed = 0;
            duration = 0;
            //注册使用传感器
            registerDetector();
            //保持CPU在屏幕锁定是唤醒状态
            acquireWakeLock();
            //更新运行状态
            mRunningType = RUNNING_TYPE_RUNNING;//运行状态
            mStartTime = System.currentTimeMillis();//运动开始时间
            if (!mIsTimeUpdate) mTimeChangeHandler.sendEmptyMessageDelayed(0, 1000);

        }

        @Override
        public void pause() throws RemoteException {
            mRunningType = RUNNING_TYPE_PAUSE;
        }

        /**
         * 停止运动的记录
         * @throws RemoteException
         */
        @Override
        public synchronized void stop() throws RemoteException {
            Log.v(LOG_TAG, "stop");
            if (mRunningType == RUNNING_TYPE_STOP)return;
            mRunningType = RUNNING_TYPE_STOP;//停止状态
            unregisterDetector();//解除传感器使用
            releaseWakeLock();//释放设备唤醒状态
            stopForeground(true);//停止将服务升级为前台进程
            //将数据存入数据库
            SportRecordHelper.insert(StepService.this,mStepCount,duration,distance,speed,calories);
        }

        @Override
        public int getRunningType() throws RemoteException {
            return mRunningType;
        }
    };

    /**
     * 时间改变
     */
    private final Handler mTimeChangeHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            if (mRunningType == RUNNING_TYPE_RUNNING){
                mRunningTime = (System.currentTimeMillis() - mStartTime);
                broadcastTimeChange(mRunningTime);
                Log.v(LOG_TAG, "Time changed");
            }
            //需要更新时间
//            if (mIsTimeUpdate)
                mTimeChangeHandler.sendEmptyMessageDelayed(0, 1000);

            return true;
        }
    });
    private void broadcastTimeChange(long time){
        mNotificationView.setCharSequence(R.id.view_pedometer_notification_text2, "setText", Utils.getDuration(time/1000) );
        NotificationManagerCompat.from(this).notify(R.id.pedometer_notification, mNotification);
        // Broadcast to all clients the new value.
        final int N = mCallbacks.beginBroadcast();
        for (int i=0; i<N; i++) {
            try {
                mCallbacks.getBroadcastItem(i).timeChanged(time);
            } catch (RemoteException e) {
            }
        }
        mCallbacks.finishBroadcast();
    }


    private void broadcastValueChange(){
        //步长
        float stepLength = mPedometerSettings.getStepLength()/100;
        Log.v(LOG_TAG,"StepLength---->"+stepLength);
        double cf = mPedometerSettings.isRunning() ? METRIC_RUNNING_FACTOR : METRIC_WALKING_FACTOR;
        //计算距离(米)
         distance = (float)(mStepCount * stepLength);

        //计算卡路里消耗
         calories = (float)(mPedometerSettings.getBodyWeight()* mStepCount * cf * stepLength/100);
         duration = (System.currentTimeMillis()-mStartTime)/1000;
        //平均速度
        float speed = (float)(distance/duration);
        //更新通知
        mNotificationView.setCharSequence(R.id.view_pedometer_notification_text1, "setText", Integer.toString(mStepCount) + "步");
        NotificationManagerCompat.from(this).notify(R.id.pedometer_notification, mNotification);
        // Broadcast to all clients the new value.
        final int N = mCallbacks.beginBroadcast();
        for (int i=0; i<N; i++) {
            try {
                IPedometerCallback cb = mCallbacks.getBroadcastItem(i);
                cb.valueChanged(mStepCount,duration,distance,calories,speed);
            } catch (RemoteException e) {
            }
        }
        mCallbacks.finishBroadcast();
    }

    /**
     * 注册加速度传感器监听
     * 这里仅仅针对室内运动使用
     */
    private void registerDetector() {
        mStepDetector = new StepDetector();
        StepListener mStepListener = new StepListener() {
            @Override
            public void onStep() {
                mStepCount++;//步数
                broadcastValueChange();
            }
        };
        mStepDetector.addStepListener(mStepListener);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(mStepDetector,
                mSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    /**
     * 解除传感器监听
     * 这里针对室内运动
     */
    private void unregisterDetector() {
        mSensorManager.unregisterListener(mStepDetector);
    }

    private void setStartFroground() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        mNotificationView = new RemoteViews(getPackageName(), R.layout.view_pedometer_notification);
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
        mNotificationView.setOnClickPendingIntent(R.id.view_pedometer_notification_text1, pi);
        mNotificationView.setOnClickPendingIntent(R.id.view_pedometer_notification_text2, pi);
        builder.setContentIntent(pi);
        builder.setSmallIcon(R.drawable.icon);
        builder.setContentText("运动中...");
        builder.setContentTitle("计步器");
        builder.setContent(mNotificationView);
        builder.setAutoCancel(false);
        mNotification = builder.build();
        startForeground(R.id.pedometer_notification, mNotification);
    }

    private void acquireWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        int wakeFlags = PowerManager.PARTIAL_WAKE_LOCK;
        mWakeLock = pm.newWakeLock(wakeFlags, LOG_TAG);
        mWakeLock.acquire();
    }

    private void releaseWakeLock() {
        if (null != mWakeLock) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }



}
