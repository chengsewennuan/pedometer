/**
 *
 */
package com.citsin.pedometer.util;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.PathInterpolator;
import android.view.inputmethod.InputMethodManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Citsin
 */
public class Utils {

    /**
     * 获取设备ID
     *
     * @param context
     * @return String
     */
    public static String getDeviceId(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    /**
     * 获取设备名称
     *
     * @return String
     */
    public static String getDeviceName() {
        return Build.MODEL;
    }

    /**
     * 价格数字格式化
     *
     * @return 格式化后的价格字符串
     */
    public static String getPriceFormat(double data) {
        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);
        return format.format(data);
    }

    /**
     * 整形数据格式化
     * @param data
     * @param length
     * @return
     */
    public static String getFormatIntegerString(int data, int length) {
        NumberFormat format = NumberFormat.getInstance();
        format.setMinimumIntegerDigits(length);
        format.setMaximumIntegerDigits(length);
        return format.format(data);
    }

    /**
     * 浮点数格式化
     *
     * @param data   需要格式化的浮点数
     * @param length 保留的小数点位数
     * @return
     */
    public static String getFormatDoubleString(double data, int length) {
        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(length);
        format.setMinimumFractionDigits(length);
        return format.format(data);
    }

    /**
     * 时间格式化
     *
     * @param datetime
     * @return 标准格式的24小时制时间
     */
    @SuppressLint("SimpleDateFormat")
    public static String getDateTimeFormat(long datetime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(datetime);
    }


    /**
     * @param datetime
     * @return 返回yyyy年MM月dd日  HH:mm样式数据
     */
    public static String getChineseDateTimeFormat(long datetime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日  HH:mm");
        return sdf.format(datetime);
    }

    /**
     * @param datetime
     * @return 返回yyyy年MM月dd日 样式数据
     */
    public static String getChineseDateFormat(long datetime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        return sdf.format(datetime);
    }

    /**
     * 验证手机号码
     *
     * @param mobileNumber
     * @return
     */
    public static boolean isMobileNumberValid(String mobileNumber) {
        Pattern pattern = Pattern.compile("1[3|5|7|8|][0-9]{9}");
        Matcher matcher = pattern.matcher(mobileNumber);
        return matcher.matches();
    }

    /**
     * 获取应用版本
     *
     * @param context
     * @return
     */
    public static int getVersion(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取应用版本名称
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
    public final static int COLOR_ANIMATION_DURATION = 1000;
    public final static int DEFAULT_DELAY = 0;

    /**
     * Change the color of a view with an animation
     *
     * @param v the view to change the color
     * @param startColor the color to start animation
     * @param endColor the color to end the animation
     */
    public static void animateViewColor (View v, int startColor, int endColor) {

        ObjectAnimator animator = ObjectAnimator.ofObject(v, "backgroundColor",
                new ArgbEvaluator(), startColor, endColor);
//        animator.setInterpolator(new PathInterpolator(0.4f,0f,1f,1f));
        animator.setInterpolator(new TimeInterpolator() {
            @Override
            public float getInterpolation(float input) {
                return input;
            }
        });
        animator.setDuration(COLOR_ANIMATION_DURATION);
        animator.start();
    }

    /**
     * Scale and set the pivot when the animation will start from
     *
     * @param v the view to set the pivot
     */
    public static void configureHideYView(View v) {

        v.setScaleY(0);
        v.setPivotY(0);
    }

    /**
     * Reduces the X & Y from a view
     *
     * @param v the view to be scaled
     *
     * @return the ViewPropertyAnimation to manage the animation
     */
    public static ViewPropertyAnimator hideViewByScaleXY(View v) {

        return hideViewByScale(v, DEFAULT_DELAY, 0, 0);
    }

    /**
     * Reduces the Y from a view
     *
     * @param v the view to be scaled
     *
     * @return the ViewPropertyAnimation to manage the animation
     */
    public static ViewPropertyAnimator hideViewByScaleY(View v) {

        return hideViewByScale(v, DEFAULT_DELAY, 1, 0);
    }

    /**
     * Reduces the X from a view
     *
     * @param v the view to be scaled
     *
     * @return the ViewPropertyAnimation to manage the animation
     */
    public static ViewPropertyAnimator hideViewByScalyInX(View v) {

        return hideViewByScale(v, DEFAULT_DELAY, 0, 1);
    }

    /**
     * Reduces the X & Y
     *
     * @param v the view to be scaled
     * @param delay to start the animation
     * @param x integer to scale
     * @param y integer to scale
     *
     * @return the ViewPropertyAnimation to manage the animation
     */
    private static ViewPropertyAnimator hideViewByScale (View v, int delay, int x, int y) {

        ViewPropertyAnimator propertyAnimator = v.animate().setStartDelay(delay)
                .scaleX(x).scaleY(y);

        return propertyAnimator;
    }

    /**
     * Shows a view by scaling
     *
     * @param v the view to be scaled
     *
     * @return the ViewPropertyAnimation to manage the animation
     */
    public static ViewPropertyAnimator showViewByScale (View v) {

        ViewPropertyAnimator propertyAnimator = v.animate().setStartDelay(DEFAULT_DELAY)
                .scaleX(1).scaleY(1);

        return propertyAnimator;
    }

    /**
     * 从字符串获取时间
     *
     * @param date
     * @return
     */
    public static Date getDateFromString(String date) {
        try {
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return format1.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 修改输入法状态，隐藏或显示
     *
     * @param context
     */
    public static void toggleInputMethod(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 80, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 300) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            options -= 10;// 每次都减少10
            baos.reset();// 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
        }
        byte[] data = baos.toByteArray();// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        return bitmap;
    }


    /**
     * @param bmp
     * @param needRecycle
     * @return
     */
    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }
        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 获得格式化时长
     * @param second
     * @return
     */
    public static String getDuration(long second){
        int houur = (int)(second / 3600);
        int minute = (int)(second / 60 % 60);
        StringBuilder sb = new StringBuilder(getFormatIntegerString(houur,2));
        sb.append(":");
        sb.append(getFormatIntegerString(minute,2));
        sb.append(":");
        sb.append(getFormatIntegerString((int)(second% 60),2));
        return  new String(sb);
    }

    /**
     * 发送Post网络请求
     *
     * @param path
     * @param params
     * @return
     */
    public static String sendPostRequest(String path, HashMap<String, String> params) {
        StringBuilder sb = new StringBuilder();
        if (params != null) {
            for (HashMap.Entry<String, String> entry : params.entrySet()) {
                sb.append(entry.getKey()).append('=').append(entry.getValue())
                        .append('&');
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        byte[] entitydata = sb.toString().getBytes();
        // 网络请求
        HttpURLConnection conn = null;
        OutputStream os = null;
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        try {
            URL url = new URL(path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(30 * 1000);
            conn.setReadTimeout(30 * 1000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 向输入流写入数据
            os = conn.getOutputStream();
            os.write(entitydata);
            os.close();
            // 读取返回数据
            is = conn.getInputStream();
            if (conn.getResponseCode() == 200) {
                bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    bos.write(buffer, 0, len);
                }
                // 请求数据成功
                return new String(bos.toByteArray());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null)
                    bos.close();
                if (is != null)
                    is.close();
                if (os != null)
                    os.close();
                if (conn != null)
                    conn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * @param context
     * @param name
     * @return
     */
    public static String readAssets(Context context, String name) {
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        try {
            is = context.getAssets().open(name);
            byte[] buf = new byte[1024];
            bos = new ByteArrayOutputStream();
            int len = 0;
            while ((len = is.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }
            return new String(bos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (is != null) {
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


}
