package com.vitalsigns.democardio;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

import com.vitalsigns.democardio.database.SqlDBHelper;
import com.vitalsigns.sdk.utility.RequestPermission;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by allen_teng on 24/10/2016.
 */

public class GlobalData extends Application
{
  public static boolean Recording = false;
  public static SqlDBHelper DATABASE = null;
  public static VitalSignsBle BleControl;
  private static final int BLE_DATA_QUEUE_SIZE = 128;
  public static BlockingQueue<int []> mBleIntDataQueue = new ArrayBlockingQueue<>(BLE_DATA_QUEUE_SIZE);

  public static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
  public static final int PERMISSION_REQUEST_EXTERNAL_STORAGE = 2;
  public static final int PERMISSION_REQUEST_READ_PHONE_STATE = 3;
  public static float FSbpGain = 0;
  public static float FSbpOffset = 0;
  public static float FDbpGain = 0;
  public static float FDbpOffset = 0;
  public static int   NPttMax = -1;
  public static int   NPttMin = -1;
  public static float FSbpWeighting = 0.5f;
  public static float FDbpWeighting = 0.5f;
  public static Context mContext = null;
  public static boolean bSelectChina = false;

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

  public static boolean requestPermissionForAndroidM(Activity activity)
  {
    boolean granted = true;
    
    if(!(RequestPermission.accessCoarseLocation(activity)))
    {
      granted = false;
    }
    if(!(RequestPermission.accessExternalStorage(activity)))
    {
      granted = false;
    }
    if(!RequestPermission.accessReadPhoneState(activity))
    {
      granted = false;
    }
    return (granted);
  }

  /**
   * @brief setContext
   *
   * Set activity context
   *
   * @param context
   */
  public static void setContext(Context context)
  {
    mContext = context;
  }

  /**
   * @brief initDatabase
   *
   * Initialize database
   *
   * return true if initial success
   */
  public static boolean initDatabase(Activity activity)
  {
    if(activity.getExternalFilesDir(null) != null)
    {
      DATABASE = new SqlDBHelper(activity, activity.getExternalFilesDir(null).getPath() + "/Database.db");
      return (true);
    }
    return (false);
  }

  public static int GetYear()
  {
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy", Locale.US);
    return Integer.parseInt(simpleDateFormat.format(calendar.getTime()));
  }

  public static int GetMonth() {
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM", Locale.US);
    return Integer.parseInt(simpleDateFormat.format(calendar.getTime()));
  }

  public static int GetDay() {
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd", Locale.US);
    return Integer.parseInt(simpleDateFormat.format(calendar.getTime()));
  }

  /**
   * @brief readString
   *
   * Read a string from the file
   * @return string read from the file
   * @throws IOException
   */
  public static String readString(File fPath, String sFilename) throws IOException
  {
    String state = Environment.getExternalStorageState();
    if((!Environment.MEDIA_MOUNTED.equals(state)) &&
       (!Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)))
    {
      return (null);
    }

    File fFile = new File(fPath, sFilename);
    if(!fFile.isFile())
    {
      return (null);
    }

    byte[] bBuf = new byte[(int)fFile.length()];
    FileInputStream objStream = new FileInputStream(fFile);
    try
    {
      objStream.read(bBuf);
    }
    finally
    {
      objStream.close();
    }
    return (new String(bBuf));
  }

  /**
   * brief netFileChkSum
   *
   * Check the NET file is download finish
   *
   * return lChkSum value
   */
  public static int netFileChkSum(Context context, String strFilename)
  {
    int lChkSum = 0;
    byte[] bInputBuff = null;

    String InputFilename = context.getExternalFilesDir(null).getPath();
    InputFilename += "/";
    InputFilename += strFilename;

    File fFile =  new File(InputFilename);
    try
    {
      FileInputStream inFile = new FileInputStream(InputFilename);
      try
      {
        bInputBuff = new byte[(int)fFile.length()];
        inFile.read(bInputBuff);
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }

      for(int i = 0; i < (int)fFile.length(); i++)
      {
        if(bInputBuff[i] > 0)
        {
          lChkSum += bInputBuff[i];
        }
        else
        {
          lChkSum += (bInputBuff[i] & 0xff);
        }
      }

      try
      {
        inFile.close();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
    catch(FileNotFoundException e)
    {
      e.printStackTrace();
    }
    return lChkSum;
  }

  /**
   * @brief delete
   *
   * Delete the file
   *
   * @return true if success
   */
  public static boolean delete(File fPath, String sFilename) {
    File fFile = new File(fPath, sFilename);
    return (fFile.delete());
  }

  /**
   * @brief writeString
   *
   * Write a string to the file
   *
   * @param sData string to be written
   * @throws IOException
   */
  public static void writeString(File fPath, String sFilename, String sData) throws IOException {
    String state = Environment.getExternalStorageState();
    if(!Environment.MEDIA_MOUNTED.equals(state)) {
      return;
    }

    File fFile = new File(fPath, sFilename);
    if(!fFile.exists()) {
      fFile.createNewFile();
    }
    FileOutputStream objStream = new FileOutputStream(fFile, true);
    try {
      objStream.write(sData.getBytes());
    } finally {
      objStream.close();
    }
  }
}
