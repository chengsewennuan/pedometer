package com.citsin.pedometer.fragment;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.citsin.pedometer.util.PedometerSettings;
import com.citsin.pedometer.R;
import com.citsin.pedometer.service.IPedometer;
import com.citsin.pedometer.service.IPedometerCallback;
import com.citsin.pedometer.service.StepService;
import com.citsin.pedometer.util.Utils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PedometerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PedometerFragment extends Fragment {


    private final static String LOG_TAG = PedometerFragment.class.getSimpleName();

    @Bind(R.id.distance)
    TextView distanceView;
    @Bind(R.id.time)
    TextView timeView;
    @Bind(R.id.speed)
    TextView speedView;
    @Bind(R.id.handle)
    ImageButton mHandleView;
    @Bind(R.id.step)
    TextView stepView;
    private SharedPreferences mSettings;
    private PedometerSettings mPedometerSettings;

    public PedometerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PedometerFragment.
     */
    public static PedometerFragment newInstance() {
        PedometerFragment fragment = new PedometerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mPedometerSettings = new PedometerSettings(mSettings);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pedometer, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //加载字体库
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "LCDN.TTF");
        //设置控件应用字体
        distanceView.setTypeface(typeface);
        timeView.setTypeface(typeface);
        speedView.setTypeface(typeface);
        stepView.setTypeface(typeface);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTimeUpdateHandler.sendEmptyMessage(0);
        mStepUpdateHandler.sendEmptyMessage(0);
        mSpeedUpdateHandler.sendEmptyMessage(0);
        mDistanceUpdateHandler.sendEmptyMessage(0);
    }

    private IPedometer mPedometer;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPedometer = IPedometer.Stub.asInterface(service);
            try {
                mPedometer.registerCallback(mPedometerCallback);
                int runType = mPedometer.getRunningType();
                Log.v(LOG_TAG, "onServiceConnected---->" + runType);
                if (runType == StepService.RUNNING_TYPE_RUNNING) {//运行状态
                    mHandleView.setImageResource(R.mipmap.ic_pedometer_stop);
                } else if (runType == StepService.RUNNING_TYPE_STOP) {//停止状态
                    mHandleView.setImageResource(R.mipmap.ic_pedometer_start);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPedometer = null;
        }
    };

    private IPedometerCallback mPedometerCallback = new IPedometerCallback.Stub() {

        @Override
        public void timeChanged(long time) throws RemoteException {
            int second = (int) (time / 1000);
            Log.v(LOG_TAG, "time----->" + second);
            mTimeUpdateHandler.sendEmptyMessage(second);
        }

        @Override
        public void valueChanged(int step, long time, float distance, float calories, float speed) throws RemoteException {
            Log.v(LOG_TAG, "step->" + step+"time->" + time+"distance->" + distance+"calories->" + calories+"speed->" + speed);
            //更新步数
            mStepUpdateHandler.sendEmptyMessage(step);
            mDistanceUpdateHandler.sendEmptyMessage((int) (distance * 100));
            mSpeedUpdateHandler.sendEmptyMessage((int) (speed * 100));
        }
    };

    @OnClick({R.id.handle})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.handle:
                try {
                    if (mPedometer == null) return;
                    int runType = mPedometer.getRunningType();
                    if (runType == StepService.RUNNING_TYPE_RUNNING) {//运行状态---->暂停状态
                        mPedometer.stop();
                        mHandleView.setImageResource(R.mipmap.ic_pedometer_start);
                    } else if (runType == StepService.RUNNING_TYPE_STOP) {
                        mPedometer.start();
                        mHandleView.setImageResource(R.mipmap.ic_pedometer_stop);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(getActivity(), StepService.class);
        getActivity().startService(intent);
        getActivity().bindService(intent, mServiceConnection, Context.BIND_IMPORTANT);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            if (mPedometer != null) {//
                mPedometer.unregisterCallback(mPedometerCallback);
                int runType = mPedometer.getRunningType();
                if (runType == StepService.RUNNING_TYPE_STOP || runType == StepService.RUNNING_TYPE_NONE) {
                    getActivity().stopService(new Intent(getActivity(), StepService.class));
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //解除服务绑定
        getActivity().unbindService(mServiceConnection);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


    /**
     * 速度更新显示
     */
    private final Handler mStepUpdateHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int step = msg.what;
            stepView.setText(Html.fromHtml(getString(R.string.pedometer_step, step)));
            return true;
        }
    });

    private final Handler mTimeUpdateHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            //显示时间
            int houur = msg.what / 3600;
            int minute = msg.what / 60 % 60;
            int second = msg.what % 60;
            timeView.setText(Html.fromHtml(getString(R.string.pedometer_time, Utils.getFormatIntegerString(houur, 2),
                    Utils.getFormatIntegerString(minute, 2), Utils.getFormatIntegerString(second, 2))));
            return true;
        }
    });
    /**
     * 距离更新
     */
    private final Handler mDistanceUpdateHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            float distance = (float) msg.what / 100f;
            if (distance > 1000) {
                distanceView.setText(Html.fromHtml(getString(R.string.pedometer_distance_kilometer, Utils.getFormatDoubleString(distance / 1000, 2))));
            } else {
                distanceView.setText(Html.fromHtml(getString(R.string.pedometer_distance_meter, Utils.getFormatDoubleString(distance, 2))));
            }
            return true;
        }
    });
    /**
     * 速度更新显示
     */
    private final Handler mSpeedUpdateHandler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    float speed = (float) msg.what / 100f;
                    speedView.setText(Html.fromHtml(getString(R.string.pedometer_speed, Utils.getFormatDoubleString(speed, 2))));
                    return true;
                }
            });
}
