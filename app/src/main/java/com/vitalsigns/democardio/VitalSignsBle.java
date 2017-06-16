package com.vitalsigns.democardio;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.vitalsigns.sdk.ble.BleCmdService;
import com.vitalsigns.sdk.ble.BlePedometerData;
import com.vitalsigns.sdk.ble.BleService;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by AllenTeng on 6/15/2017.
 */

public class VitalSignsBle implements BleCmdService.OnServiceListener
{
  private static final String LOG_TAG = "VitalSignsBle";

  @Override
  public void chartNumberConfig(int i, int i1, int[] ints)
  {
  }

  @Override
  public void pedometerData(ArrayList<BlePedometerData> arrayList)
  {
  }

  @Override
  public void bleConnectionLost(String s)
  {
    Log.d(LOG_TAG, "bleConnectionLost:" + s);
    mBleEvent.onDisconnect();
  }

  @Override
  public void bleReadyToGetData()
  {
    mBleEvent.onConnect();
  }

  @Override
  public void bleGattState()
  {
    Log.d(LOG_TAG, "bleGattState");
    mBleEvent.onDisconnect();
  }

  @Override
  public void bleOtaAck()
  {
  }

  @Override
  public void bleTransmitTimeout()
  {
    Log.d(LOG_TAG, "bleTransmitTimeout");
    mBleEvent.onDisconnect();
  }

  @Override
  public void bleAckError(String s)
  {
    Log.d(LOG_TAG, "bleAckError:" + s);
    mBleEvent.onDisconnect();
  }

  @Override
  public void ackReceived(byte[] bytes)
  {
  }

  @Override
  public void ackBleWatchSyncTime(boolean b)
  {
  }

  public interface BleEvent
  {
    void onDisconnect();
    void onConnect();
  }

  private BleEvent              mBleEvent        = null;
  private BleService            mBleService      = null;
  private boolean               mBleServiceBind  = false;
  private Context               mContext         = null;

  public VitalSignsBle(@NotNull Context context, @NotNull BleEvent event)
  {
    mContext = context;
    mBleEvent = event;

    Intent intent = new Intent(context, BleService.class);
    mBleServiceBind = context.bindService(intent, mBleServiceConnection, BIND_AUTO_CREATE);
  }

  private ServiceConnection mBleServiceConnection = new ServiceConnection()
  {
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder)
    {
      mBleService = ((BleService.LocalBinder)iBinder).getService();
      mBleService.Initialize(GlobalData.mBleIntDataQueue, BleCmdService.HW_TYPE.CARDIO);
      mBleService.RegisterClient(VitalSignsBle.this);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName)
    {
    }
  };

  /**
   * Destroy the VitalSignsBle object
   */
  public void destroy()
  {
    if(mBleServiceBind)
    {
      mContext.unbindService(mBleServiceConnection);
    }
  }

  /**
   * Connect to the device
   */
  public void connect(String mac)
  {
    if(mBleService != null)
    {
      mBleService.SetBleDevice(BluetoothAdapter.getDefaultAdapter()
                                               .getRemoteDevice(mac));
      mBleService.Connect();
    }
  }

  /**
   * Disconnect the device
   */
  public void disconnect()
  {
    if(mBleService != null)
    {
      mBleService.Disconnect();
    }
  }

  /**
   * Start the measurement
   */
  public void start()
  {
    if(mBleService != null)
    {
      mBleService.CmdStart();
    }
  }

  /**
   * Stop the measurement
   */
  public void stop()
  {
    if(mBleService != null)
    {
      mBleService.CmdStop();
    }
  }

  /**
   * Get device sample rate
   * @return sample rate
   */
  public int sampleRate()
  {
    return ((mBleService != null) ? mBleService.GetSampleRate() : 0);
  }

  /**
   * Get device is connected or not
   * @return true if connected
   */
  public boolean isConnect()
  {
    return ((mBleService != null) ? mBleService.IsBleConnected() : false);
  }
}
