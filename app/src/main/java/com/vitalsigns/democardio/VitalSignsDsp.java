package com.vitalsigns.democardio;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

import com.vitalsigns.sdk.dsp.bp.Constant;
import com.vitalsigns.sdk.dsp.bp.Dsp;
import com.vitalsigns.sdk.utility.Utility;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static com.vitalsigns.democardio.GlobalData.LOG_TAG;
import static com.vitalsigns.sdk.dsp.bp.Constant.DEV_TYPE_BLE_WATCH;
import static com.vitalsigns.sdk.dsp.bp.Constant.UI_CODE_TYPE_STANDARD;

/**
 * Created by allen_teng on 25/10/2016.
 */

public class VitalSignsDsp
{
  private final int CAPTURE_BLE_INIT_TIME = 100;
  private final int UPDATE_VIEW_WINDOW = 6000;
  private final int UPDATE_VIEW_INTERVAL = 1000;
  private final int BLE_TIMEOUT_CHECK_INTERVAL = 2000;
  private final int STABLE_COUNT = 10;

  private BlockingQueue<float[]> queueBleData = new ArrayBlockingQueue<float[]>(64);
  private boolean Running = false;
  private boolean Executing = false;
  private boolean BleDataReceived = false;
  private int StableCnt = 0;
  private Dsp DSP = null;
  private HandlerThread DspThread;
  private Handler DspThreadHandler;
  private Handler UpdateResultHandler;
  private Handler BleTimeoutHandler;
  private Handler AutoStopHandler;
  private OnUpdateResult OnUpdateResultCallback;

  public VitalSignsDsp(Context context)
  {
    DSP = new Dsp((Activity)context,
                  context.getExternalFilesDir(null).getPath(),
                  context.getString(R.string.package_identity),
                  mOnSendBPInfoEvent);
    OnUpdateResultCallback = (OnUpdateResult)context;
  }

  public boolean Start()
  {
    /// [AT-PM] : Check DSP is available ; 10/25/2016
    if(!DSP.IsJniAvailable())
    {
      Log.d(LOG_TAG, "DSP.IsJniAvailable() == false");
      return (false);
    }

    /// [AT-PM] : Start DSP ; 10/25/2016
    if(!startDsp())
    {
      Log.d(LOG_TAG, "startDsp() == false");
      return (false);
    }

    queueBleData.clear();

    DspThread = new HandlerThread("DSP Thread", Process.THREAD_PRIORITY_BACKGROUND);
    DspThread.start();
    DspThreadHandler = null;
    DspThreadHandler = new Handler(DspThread.getLooper());
    DspThreadHandler.postDelayed(dspThreadRunnable, CAPTURE_BLE_INIT_TIME);
    UpdateResultHandler = null;
    UpdateResultHandler = new Handler();
    UpdateResultHandler.postDelayed(updateResultRunnable, UPDATE_VIEW_INTERVAL);
    BleTimeoutHandler = null;
    BleTimeoutHandler = new Handler();
    BleTimeoutHandler.postDelayed(bleTimeoutRunnable, BLE_TIMEOUT_CHECK_INTERVAL);
    Running = true;
    BleDataReceived = false;
    StableCnt = 0;

    /// [AT-PM] : Start BLE ; 10/25/2016
    GlobalData.BleControl.start();

    /// [AT-PM] : Callback to update information ; 10/25/2016
    OnUpdateResultCallback.onUpdateResult(-1.0f, -1.0f, -1.0f);
    Log.d(LOG_TAG, "START");
    return (true);
  }

  public void Stop()
  {
    UpdateResultHandler.removeCallbacksAndMessages(null);
    BleTimeoutHandler.removeCallbacksAndMessages(null);

    /// [AT-PM] : Stop the thread ; 10/25/2016
    Running = false;
    if(DspThread != null)
    {
      DspThreadHandler.removeCallbacksAndMessages(null);
      DspThread.quit();
      DspThread.interrupt();
      DspThread = null;
      while(Executing)
      {
        try
        {
          Thread.sleep(10);
        }
        catch (InterruptedException e)
        {
          e.printStackTrace();
        }
      }
    }

    /// [AT-PM] : Stop BLE ; 10/25/2016
    GlobalData.BleControl.stop();

    /// [AT-PM] : Stop DSP ; 10/25/2016
    DSP.Stop();
  }

  public void SetBleData(float [] floats)
  {
    queueBleData.offer(floats);
    BleDataReceived = true;
  }

  private final Runnable updateResultRunnable = new Runnable()
  {
    @Override
    public void run()
    {
      float fStartTime;
      float fEndTime;

      /// [AT-PM] : Update the view ; 10/25/2016
      fEndTime = DSP.GetEndTime();
      fStartTime = fEndTime > UPDATE_VIEW_WINDOW ? fEndTime - UPDATE_VIEW_WINDOW : fEndTime;
      Log.d(LOG_TAG, "UpdateView(" + Float.toString(fStartTime) + ", " + Float.toString(fEndTime) + ")");
      DSP.UpdateView(fStartTime, fEndTime, UI_CODE_TYPE_STANDARD);

      /// [AT-PM] : Check the measurement is stable ; 10/25/2016
      if(DSP.BPStable())
      {
        StableCnt ++;
        if(StableCnt >= STABLE_COUNT)
        {
          /// [AT-PM] : Auto stop the measurement if stable ; 10/25/2016
          AutoStopHandler = null;
          AutoStopHandler = new Handler();
          AutoStopHandler.postDelayed(autoStopProc, 0);
        }
      }
      else
      {
        StableCnt = 0;
      }
      Log.d(LOG_TAG, "Stable Count = " + Integer.toString(StableCnt));

      /// [AT-PM] : Callback to update information ; 10/25/2016
      OnUpdateResultCallback.onUpdateResult(DSP.GetSbp(), DSP.GetDbp(), DSP.GetHeartRate());

      /// [AT-PM] : Start next round ; 10/25/2016
      if(Running)
      {
        UpdateResultHandler.postDelayed(updateResultRunnable, UPDATE_VIEW_INTERVAL);
      }
    }
  };

  private final Runnable bleTimeoutRunnable = new Runnable()
  {
    @Override
    public void run()
    {
      if(BleDataReceived == false)
      {
        /// [AT-PM] : BLE transmit timeout ; 10/25/2016
        OnUpdateResultCallback.onInterrupt();
      }
      BleDataReceived = false;

      /// [AT-PM] : Restart the thread ; 10/25/2016
      if(Running)
      {
        BleTimeoutHandler.postDelayed(bleTimeoutRunnable, BLE_TIMEOUT_CHECK_INTERVAL);
      }
    }
  };

  private final Runnable autoStopProc = new Runnable()
  {
    @Override
    public void run()
    {
      Log.d(LOG_TAG, "Auto stop is triggered");
      OnUpdateResultCallback.onInterrupt();
    }
  };

  private final Runnable dspThreadRunnable = new Runnable()
  {
    @Override
    public void run()
    {
      float [] fBleDatas;
      BlockingQueue<Float> queueData = new ArrayBlockingQueue<Float>(256);
      float [] fEcgDatas;
      float [] fPpgDatas;
      int nSize;
      int nIdx;

      queueData.clear();

      while(Running)
      {
        fBleDatas = null;
        if(!queueBleData.isEmpty())
        {
          /// [AT-PM] : Get data from BLE queue ; 10/25/2016
          fBleDatas = queueBleData.poll();
          for(float fBleData : fBleDatas)
          {
            queueData.offer(fBleData);
          }
          Log.d(LOG_TAG, "Get " + Integer.toString(fBleDatas.length) + " Data");

          /// [AT-PM] : Execute DSP ; 10/25/2016
          nSize = queueData.size();
          if(nSize >= 2)
          {
            Executing = true;

            fEcgDatas = null;
            fEcgDatas = new float[nSize / 2];
            fPpgDatas = null;
            fPpgDatas = new float[nSize / 2];
            nIdx = 0;
            while(nIdx < (nSize / 2))
            {
              fEcgDatas[nIdx] = queueData.poll();
              fPpgDatas[nIdx] = queueData.poll() * (-1);
              nIdx ++;
            }

            DSP.Execute(fEcgDatas, fPpgDatas);
            Log.d(LOG_TAG, "Run " + Integer.toString(fEcgDatas.length) + " ECG and " + Integer.toString(fPpgDatas.length) + " PPG");

            Executing = false;
          }
        }
      }
    }
  };

  private boolean startDsp()
  {
    boolean bRtn;

    DSP.ResetDspInfo();

    /// [AT-PM] : Set basic user information ; 10/25/2016
    DSP.SetUserInfo((float)(Utility.GetYear() - 1982),      ///< [AT-PM] : User birth year ; 10/25/2016
                    true,                                   ///< [AT-PM] : true = MALE, false = FEMALE ; 10/25/2016
                    173.0f,                                 ///< [AT-PM] : User height in cm ; 10/25/2016
                    76.0f,                                  ///< [AT-PM] : User weight in kg ; 10/25/2016
                    false,                                  ///< [AT-PM] : true if user has diabetes ; 10/25/2016
                    false,                                  ///< [AT-PM] : true if user has hypertension ; 10/25/2016
                    false,                                  ///< [AT-PM] : true if user has peripheral vascular diseases ; 10/25/2016
                    false,                                  ///< [AT-PM] : true if user has stroke ; 10/25/2016
                    false,                                  ///< [AT-PM] : true if user has arrhythmia ; 10/25/2016
                    false,                                  ///< [AT-PM] : true if user has heart failure ; 10/25/2016
                    false,                                  ///< [AT-PM] : true if user has chronic kidney disease ; 10/25/2016
                    false,                                  ///< [AT-PM] : true if user has high cholesterol ; 10/25/2016
                    false,                                  ///< [AT-PM] : true if user does smokes ; 10/25/2016
                    false                                   ///< [AT-PM] : true if user does drink ; 10/25/2016
                    );

    /// [AT-PM] : Set measurement information ; 10/25/2016
    DSP.SetMeasInfo((float)Utility.GetHour(),     ///< [AT-PM] : Hour of a day when measurement executed ; 10/25/2016
                    25.0f,                        ///< [AT-PM] : Ambient temperature in oC when measurement executed ; 10/25/2016
                    true,                         ///< [AT-PM] : true if using left wristband is at left hand ; 10/25/2016
                    Constant.MEAS_POSITION_SIT    ///< [AT-PM] : Position during measurement ; 10/25/2016
                    );

    /// [AT-PM] : Set initial value ; 10/25/2016
    DSP.SetInitValue(-1.0f, -1.0f, -1.0f, -1.0f);

    /// [AT-PM] : Start the DSP ; 10/25/2016
    bRtn = DSP.Start(GlobalData.BleControl.sampleRate(), DEV_TYPE_BLE_WATCH);
    return (bRtn);
  }

  interface OnUpdateResult
  {
    void onUpdateResult(float fSbp, float fDbp, float fHR);

    void onInterrupt();
  }

  private Dsp.OnSendBPInfoEvent mOnSendBPInfoEvent = new Dsp.OnSendBPInfoEvent()
  {
    @Override
    public void OnSendBPInfo(float v, float v1, float v2, float v3, float v4)
    {
    }
  };
}
