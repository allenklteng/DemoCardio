package com.vitalsigns.democardio;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
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
}
