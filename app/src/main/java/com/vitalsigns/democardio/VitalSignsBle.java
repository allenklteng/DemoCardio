package com.vitalsigns.democardio;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import com.vitalsigns.sdk.ble.BleCmdService;
import com.vitalsigns.sdk.ble.BleService;
import com.vitalsigns.sdk.ble.BleStatus;
import com.vitalsigns.sdk.utility.Utility;

import org.jetbrains.annotations.NotNull;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * Created by AllenTeng on 6/15/2017.
 */

class VitalSignsBle implements BleCmdService.OnErrorListener
{
  private static final String LOG_TAG = "VitalSignsBle";
  private static final int CHECK_CONNECT_STATUS_INTERVAL = 100;

  private HandlerThread mServiceThread = null;
  private Handler mHandlerConnect = null;
  private Handler mHandlerCheckStop = null;

  @Override
  public void bleConnectionLost(String s)
  {
    Log.d(LOG_TAG, "bleConnectionLost:" + s);
    aboutCheckConnected();
    mBleEvent.onDisconnect();
  }

  @Override
  public void bleGattState()
  {
    Log.d(LOG_TAG, "bleGattState");
    aboutCheckConnected();
    mBleEvent.onDisconnect();
  }

  @Override
  public void bleTransmitTimeout()
  {
    Log.d(LOG_TAG, "bleTransmitTimeout");
    aboutCheckConnected();
    mBleEvent.onDisconnect();
  }

  @Override
  public void bleAckError(String s)
  {
    Log.d(LOG_TAG, "bleAckError:" + s);
    aboutCheckConnected();
    mBleEvent.onDisconnect();
  }

  interface BleEvent
  {
    void onDisconnect();
    void onConnect();
  }

  interface BleStop
  {
    void onStop();
  }

  private BleEvent              mBleEvent        = null;
  private BleService            mBleService      = null;
  private boolean               mBleServiceBind  = false;
  private Context               mContext         = null;

  VitalSignsBle(@NotNull Context context, @NotNull BleEvent event)
  {
    mContext = context;
    mBleEvent = event;

    Intent intent = new Intent(context, BleService.class);
    mBleServiceBind = context.bindService(intent, mBleServiceConnection, BIND_AUTO_CREATE);

    mServiceThread = new HandlerThread("BLE Service Thread", THREAD_PRIORITY_BACKGROUND);
    mServiceThread.start();
  }

  private ServiceConnection mBleServiceConnection = new ServiceConnection()
  {
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder)
    {
      mBleService = ((BleService.LocalBinder)iBinder).getService();
      mBleService.Initialize(GlobalData.mBleIntDataQueue, BleCmdService.HW_TYPE.CARDIO);
      mBleService.RegisterClient(null, VitalSignsBle.this, null, null, null);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName)
    {
    }
  };

  /**
   * Destroy the VitalSignsBle object
   */
  void destroy()
  {
    if(mBleServiceBind)
    {
      mContext.unbindService(mBleServiceConnection);
    }
  }

  /**
   * Connect to the device
   */
  void connect(String mac)
  {
    if(mBleService != null)
    {
      mBleService.SetBleDevice(BluetoothAdapter.getDefaultAdapter()
                                               .getRemoteDevice(mac));
      mBleService.Connect();

      /// [AT-PM] : Start a handler to check connected event ; 08/24/2017
      startCheckConnected();
    }
  }

  /**
   * Disconnect the device
   */
  void disconnect()
  {
    if(mBleService != null)
    {
      mBleService.Disconnect();
    }
  }

  /**
   * Start the measurement
   */
  void start()
  {
    if(mBleService != null)
    {
      mBleService.CmdPreStart();
    }
  }

  /**
   * Stop the measurement
   * @param event BleStop interface
   */
  void stop(@NotNull final BleStop event)
  {
    if(mBleService == null)
    {
      return;
    }
    mBleService.CmdStop();

    /// [AT-PM] : Start a runnable to wait STOP event ; 08/24/2017
    if(mHandlerCheckStop != null)
    {
      mHandlerCheckStop.removeCallbacksAndMessages(null);
      mHandlerCheckStop = null;
    }
    mHandlerCheckStop = new Handler(mServiceThread.getLooper());
    mHandlerCheckStop.post(new Runnable()
    {
      @Override
      public void run()
      {
        /// [AT-PM] : Wait for the STOP action finished ; 08/24/2017
        int timeout = 100;
        while(!mBleService.CheckBleStatus(BleStatus.STATUS.BLE_ACK_STOP))
        {
          Log.d(LOG_TAG, String.format("Wait for %d", timeout));
          Utility.SleepSomeTime(100);
          timeout --;
          if(timeout == 0)
          {
            break;
          }
        }
        event.onStop();
      }
    });
  }

  /**
   * Get device sample rate
   * @return sample rate
   */
  int sampleRate()
  {
    return ((mBleService != null) ? mBleService.GetSampleRate() : 0);
  }

  /**
   * Get device is connected or not
   * @return true if connected
   */
  boolean isConnect()
  {
    return ((mBleService != null) && mBleService.IsBleConnected());
  }

  /**
   * Start to check the BLE connection is ready
   */
  private void startCheckConnected()
  {
    if(mHandlerConnect != null)
    {
      mHandlerConnect.removeCallbacksAndMessages(null);
    }
    mHandlerConnect = null;
    mHandlerConnect = new Handler(mServiceThread.getLooper());
    mHandlerConnect.postDelayed(new Runnable()
    {
      @Override
      public void run()
      {
        if(mBleService.CheckBleStatus(BleStatus.STATUS.BLE_READY_TO_GET_DATA))
        {
          mBleEvent.onConnect();
        }
        else
        {
          /// [AT-PM] : Start next round ; 08/24/2017
          mHandlerConnect.postDelayed(this, CHECK_CONNECT_STATUS_INTERVAL);
        }
      }
    }, CHECK_CONNECT_STATUS_INTERVAL);
  }

  /**
   * About the check BLE connected process
   */
  private void aboutCheckConnected()
  {
    if(mHandlerConnect != null)
    {
      mHandlerConnect.removeCallbacksAndMessages(null);
    }
    mHandlerConnect = null;
  }

  /**
   * Check ECG is ready
   * @return true if ready
   */
  public boolean isEcgReady()
  {
    return (mBleService.IsEcgReady());
  }
}
