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
  public static VitalSignsBle BleControl;

  public static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
  public static final int PERMISSION_REQUEST_EXTERNAL_STORAGE = 2;
  public static final int PERMISSION_REQUEST_READ_PHONE_STATE = 3;

  private static boolean requestPermissionAccessCoarseLocation(final Activity activity)
  {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
    {
      if(activity.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
      {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getResources().getString(R.string.request_permission_access_coarse_location_title));
        builder.setMessage(activity.getResources().getString(R.string.request_permission_access_coarse_location_content));
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

  private static boolean requestPermissionExternalStorage(final Activity activity)
  {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
    {
      if((activity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
         (activity.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))
      {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getResources().getString(R.string.request_permission_external_storage_title));
        builder.setMessage(activity.getResources().getString(R.string.request_permission_external_storage_content));
        builder.setPositiveButton(android.R.string.ok,
                                  new DialogInterface.OnClickListener()
                                  {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                      ActivityCompat.requestPermissions(activity,
                                                                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                                                     android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                                                        PERMISSION_REQUEST_EXTERNAL_STORAGE);
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

  private static boolean requestPermissionReadPhoneState(final Activity activity)
  {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
    {
      if(activity.checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
      {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getResources().getString(R.string.request_permission_read_phone_state_title));
        builder.setMessage(activity.getResources().getString(R.string.request_permission_read_phone_state_content));
        builder.setPositiveButton(android.R.string.ok,
                                  new DialogInterface.OnClickListener()
                                  {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                      ActivityCompat.requestPermissions(activity,
                                                                        new String[]{Manifest.permission.READ_PHONE_STATE},
                                                                        PERMISSION_REQUEST_READ_PHONE_STATE);
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

  public static boolean requestPermissionForAndroidM(final Activity activity)
  {
    boolean granted = true;

    if(!requestPermissionAccessCoarseLocation(activity))
    {
      granted = false;
    }
    if(!requestPermissionExternalStorage(activity))
    {
      granted = false;
    }
    if(!requestPermissionReadPhoneState(activity))
    {
      granted = false;
    }
    return (granted);
  }
}
