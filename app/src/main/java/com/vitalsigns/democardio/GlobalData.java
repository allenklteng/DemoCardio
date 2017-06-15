package com.vitalsigns.democardio;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.vitalsigns.sdk.ble.BleControl;

/**
 * Created by allen_teng on 24/10/2016.
 */

public class GlobalData extends Application
{
  public static final String LOG_TAG = "DEMO CARDIO";

  public static boolean Recording = false;

  private static Toast mToast = null;
  private static final Handler toastReleaseHandler = new Handler();
  public static void showToast(Context mContext, String message, int toastDuration)
  {
    if (mToast != null)
    {
      mToast.setText(message);
      mToast.setDuration(toastDuration);

      toastReleaseHandler.removeCallbacksAndMessages(null);
      toastReleaseHandler.postDelayed(new Runnable()
      {
        @Override
        public void run() {
          mToast = null;
        }
      }, toastDuration == Toast.LENGTH_LONG ? 3500 : 2000);
    }
    else
    {
      mToast = Toast.makeText(mContext, message, toastDuration);
      mToast.show();

      toastReleaseHandler.postDelayed(new Runnable()
      {
        @Override
        public void run() {
          mToast = null;
        }
      }, toastDuration == Toast.LENGTH_LONG ? 3500 : 2000);
    }
  }

  public static BluetoothDevice BleDevice;
  public static BleControl BleControl;

  public static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

  public static boolean requestPermissionForAndroidM(final Activity activity)
  {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
    {
      /// Android M Permission check?
      if(activity.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
      {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getResources().getString(R.string.request_permission_title));
        builder.setMessage(activity.getResources().getString(R.string.request_permission_content));
        builder.setPositiveButton(android.R.string.ok,
                                  new DialogInterface.OnClickListener()
                                  {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                      ActivityCompat.requestPermissions(activity,
                                                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                                                        PERMISSION_REQUEST_COARSE_LOCATION);
                                    }
                                  });
        builder.setNegativeButton(android.R.string.cancel,
                                  new DialogInterface.OnClickListener()
                                  {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                      activity.finish();
                                    }
                                  });
        builder.show();
        return (false);
      }
    }
    return (true);
  }
}
