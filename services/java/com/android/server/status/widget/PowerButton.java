package com.android.server.status.widget;

import com.android.internal.R;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff.Mode;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.LinearLayout;
import android.util.Log;
import android.provider.Settings;
import android.view.View;

import com.android.server.status.ExpandedView;

public abstract class PowerButton {
    public static final String TOGGLE_WIFI = "toggleWifi";
    public static final String TOGGLE_GPS = "toggleGPS";
    public static final String TOGGLE_BLUETOOTH = "toggleBluetooth";
    public static final String TOGGLE_BRIGHTNESS = "toggleBrightness";
    public static final String TOGGLE_SOUND = "toggleSound";
    public static final String TOGGLE_SYNC = "toggleSync";
    public static final String TOGGLE_WIFIAP = "toggleWifiAp";
    public static final String TOGGLE_SCREENTIMEOUT = "toggleScreenTimeout";
    public static final String TOGGLE_MOBILEDATA = "toggleMobileData";
    public static final String TOGGLE_LOCKSCREEN = "toggleLockScreen";
    public static final String TOGGLE_NETWORKMODE = "toggleNetworkMode";
    public static final String TOGGLE_AUTOROTATE = "toggleAutoRotate";
    public static final String TOGGLE_AIRPLANE = "toggleAirplane";
    public static final String TOGGLE_SLEEPMODE = "toggleSleepMode";

    private Mode expPDMode = Mode.SCREEN;
    public static final int STATE_ENABLED = 1;
    public static final int STATE_DISABLED = 2;
    public static final int STATE_TURNING_ON = 3;
    public static final int STATE_TURNING_OFF = 4;
    public static final int STATE_INTERMEDIATE = 5;
    public static final int STATE_UNKNOWN = 6;

    public int currentIcon;
    public int currentState;
    public int currentPosition;

    abstract void initButton(int position);
    abstract public void toggleState(Context context);
    public abstract void updateState(Context context);
    public abstract boolean launchActivity(Context context);

    public void setupButton(int position) {
        currentPosition = position;
    }

    public void updateView(Context context, ExpandedView views) {
        if(currentPosition > 0) {
             int buttonLayer = getLayoutID(currentPosition);
             int buttonIcon = getImageID(currentPosition);

             views.findViewById(buttonLayer).setVisibility(View.VISIBLE);

             updateImageView(views, buttonIcon, currentIcon);

        }
    }

    private void updateImageView(ExpandedView view, int id, int resId) {
        ImageView imageIcon = (ImageView)view.findViewById(id);
        imageIcon.setImageResource(resId);
    }
    private void updateImageView(ExpandedView view, int id, Drawable resDraw) {
        ImageView statusInd = (ImageView)view.findViewById(id);
        statusInd.setImageResource(com.android.internal.R.drawable.stat_bgon_custom);
        statusInd.setImageDrawable(resDraw);
    }

    public static int getLayoutID(int posi) {
        switch(posi) {
            case 1: return R.id.exp_power_stat_1;
            case 2: return R.id.exp_power_stat_2;
            case 3: return R.id.exp_power_stat_3;
            case 4: return R.id.exp_power_stat_4;
            case 5: return R.id.exp_power_stat_5;
            case 6: return R.id.exp_power_stat_6;
            case 7: return R.id.exp_power_stat_7;
            case 8: return R.id.exp_power_stat_8;
            case 9: return R.id.exp_power_stat_9;
            case 10: return R.id.exp_power_stat_10;
            case 11: return R.id.exp_power_stat_11;
            case 12: return R.id.exp_power_stat_12;
            case 13: return R.id.exp_power_stat_13;
            case 14: return R.id.exp_power_stat_14;
        }
        return 0;
    }

    private int getImageID(int posi) {
        switch(posi) {
            case 1: return R.id.exp_power_image_1;
            case 2: return R.id.exp_power_image_2;
            case 3: return R.id.exp_power_image_3;
            case 4: return R.id.exp_power_image_4;
            case 5: return R.id.exp_power_image_5;
            case 6: return R.id.exp_power_image_6;
            case 7: return R.id.exp_power_image_7;
            case 8: return R.id.exp_power_image_8;
            case 9: return R.id.exp_power_image_9;
            case 10: return R.id.exp_power_image_10;
            case 11: return R.id.exp_power_image_11;
            case 12: return R.id.exp_power_image_12;
            case 13: return R.id.exp_power_image_13;
            case 14: return R.id.exp_power_image_14;
        }
        return 0;
    }
}
