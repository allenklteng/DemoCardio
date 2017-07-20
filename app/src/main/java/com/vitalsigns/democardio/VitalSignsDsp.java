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

import static com.vitalsigns.sdk.dsp.bp.Constant.DEV_TYPE_BLE_WATCH;
import static com.vitalsigns.sdk.dsp.bp.Constant.UI_CODE_TYPE_STANDARD;

/**
 * Created by allen_teng on 25/10/2016.
 */

public class VitalSignsDsp
{
  private static final String LOG_TAG = "VitalSignsDsp";

  private final int CAPTURE_BLE_INIT_TIME = 100;
  private final int UPDATE_VIEW_WINDOW = 6000;
  private final int UPDATE_VIEW_INTERVAL = 1000;
  private final int STABLE_COUNT = 10;

  private boolean Running = false;
  private boolean Executing = false;
  private int StableCnt = 0;
  private Dsp DSP = null;
  private HandlerThread DspThread;
  private Handler DspThreadHandler;
  private Handler UpdateResultHandler;
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

    DspThread = new HandlerThread("DSP Thread", Process.THREAD_PRIORITY_BACKGROUND);
    DspThread.start();
    DspThreadHandler = null;
    DspThreadHandler = new Handler(DspThread.getLooper());
    DspThreadHandler.postDelayed(dspThreadRunnable, CAPTURE_BLE_INIT_TIME);
    UpdateResultHandler = null;
    UpdateResultHandler = new Handler();
    UpdateResultHandler.postDelayed(updateResultRunnable, UPDATE_VIEW_INTERVAL);
    Running = true;
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
    if(UpdateResultHandler != null)
    {
      UpdateResultHandler.removeCallbacksAndMessages(null);
    }

    /// [AT-PM] : Stop the thread ; 10/25/2016
    Running = false;
    if(DspThread != null)
    {
      if(DspThreadHandler != null)
      {
        DspThreadHandler.removeCallbacksAndMessages(null);
      }
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
      updateView(fStartTime, fEndTime);

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
      int [] nBleDatas;
      BlockingQueue<Integer> queueData = new ArrayBlockingQueue<Integer>(256);
      float [] fEcgDatas;
      float [] fPpgDatas;
      int nSize;
      int nIdx;

      queueData.clear();

      while(Running)
      {
        nBleDatas = null;
        if(!GlobalData.mBleIntDataQueue.isEmpty())
        {
          /// [AT-PM] : Get data from BLE queue ; 10/25/2016
          nBleDatas = GlobalData.mBleIntDataQueue.poll();
          for(int nBleData : nBleDatas)
          {
            queueData.offer(nBleData);
          }
          Log.d(LOG_TAG, "Get " + Integer.toString(nBleDatas.length) + " Data");

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

  /**
   * Get measurement end time
   * @return ms
   */
  public float getEndTime()
  {
    return (DSP.GetEndTime());
  }

  /**
   * Update the view for fetching data from DSP
   * @param start start time in ms
   * @param end end time in ms
   * @return data count for ECG/PPG waveform
   */
  public int updateView(float start, float end)
  {
    return (DSP.UpdateView(start, end, UI_CODE_TYPE_STANDARD));
  }

  /**
   * Get ECG waveform point
   * @param idx index of the point
   * @return (time, adc code) array
   */
  public float [] getEcg(int idx)
  {
    return (new float [] {DSP.GetEcgX(idx), DSP.GetEcgY(idx)});
  }

  /**
   * Get PPG waveform point
   * @param idx index of the point
   * @return (time, adc code) array
   */
  public float [] getPpg(int idx)
  {
    return (new float [] {DSP.GetPpgX(idx), DSP.GetPpgY(idx)});
  }
}
