package com.vitalsigns.democardio.Calibration;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.vitalsigns.democardio.GlobalData;
import com.vitalsigns.democardio.R;
import com.vitalsigns.democardio.database.ResultData;
import com.vitalsigns.sdk.dsp.bp.Calibrate;
import com.vitalsigns.sdk.dsp.bp.Constant;
import com.vitalsigns.sdk.utility.Utility;

/**
 * Created by coge on 2018/5/15.
 */

public class Calibration extends IntentService
{
  private static final String LOG_TAG = "Calibration:";
  public Calibration()
  {
    super(LOG_TAG);
  }

  @Override
  protected void onHandleIntent(Intent intent)
  {
    int nIdx;
    Calibrate calibrate;
    ResultData[] arrPttResult;

    calibrate = new Calibrate((Activity)GlobalData.mContext,
                              GlobalData.mContext.getExternalFilesDir(null).getPath(),
                              GlobalData.mContext.getString(R.string.package_identity),
                              GlobalData.bSelectChina);

    arrPttResult = GlobalData.DATABASE.GetResultTraining(Constant.MINIMUM_TRAINING_SBP,
                                                         Constant.MINIMUM_TRAINING_DBP,
                                                         Constant.MINIMUM_TRAINING_HR);
    if(arrPttResult == null)
    {
      Log.d(LOG_TAG, "(TrainThread) arrPttResult == null");
      return;
    }
    if(arrPttResult.length <= 0)
    {
      Log.d(LOG_TAG, "(TrainThread) arrPttResult.length <= 0");
      return;
    }

    calibrate.Initialize(arrPttResult.length);
    calibrate.SetInitLinearData(GlobalData.FSbpGain,
                                GlobalData.FSbpOffset,
                                GlobalData.FDbpGain,
                                GlobalData.FDbpOffset);

    nIdx = arrPttResult.length - 1;
    while(nIdx >= 0)
    {
      calibrate.SetData((float)arrPttResult[nIdx].NMeasHR,
                        (float)arrPttResult[nIdx].NMeasPtt,
                        25f,                                            ///< [CC] : Ambient temperature ; 05/15/2018
                        (float)(Utility.GetYear() - 1982),                  ///< [CC] : User birth year ; 05/15/2018
                        (float)arrPttResult[nIdx].NHeight,
                        (float)arrPttResult[nIdx].NWeight,
                        true,                                            ///< [CC] : true = MALE, false = FEMALE ; 05/15/2018
                        (float)arrPttResult[nIdx].GetMeasHour(),
                        true,                                           ///< [CC] : true = left hand, false = right hand ; 05/15/2018
                        (byte)arrPttResult[nIdx].NPosition,
                        false,                                          ///< [CC] : true if user has diabetes ; 05/15/2018
                        false,                                          ///< [CC] : true if user has hypertension ; 05/15/2018
                        false,                                          ///< [CC] : true if user has peripheral vascular diseases ; 05/15/2018
                        false,                                          ///< [CC] : true if user has stroke ; 05/15/2018
                        false,                                          ///< [CC] : true if user has arrhythmia ; 05/15/2018
                        false,                                          ///< [CC] : true if user has heart failure ; 05/15/2018
                        false,                                          ///< [CC] : true if user has chronic kidney disease ; 05/15/2018
                        false,                                         ///< [CC] : true if user has high cholesterol ; 05/15/2018
                        false,                                         ///< [CC] : true if user does smokes ; 05/15/2018
                        false,                                         ///< [CC] : true if user does drink ; 05/15/2018
                        (float)arrPttResult[nIdx].NRealSbp,
                        (float)arrPttResult[nIdx].NRealDbp);
      nIdx --;
    }

    calibrate.Execute();

    GlobalData.FSbpGain      = calibrate.GetSbpGain();
    GlobalData.FSbpOffset    = calibrate.GetSbpOffset();
    GlobalData.FDbpGain      = calibrate.GetDbpGain();
    GlobalData.FDbpOffset    = calibrate.GetDbpOffset();
    GlobalData.FSbpWeighting = calibrate.GetSbpWeighting();
    GlobalData.FDbpWeighting = calibrate.GetDbpWeighting();
    GlobalData.NPttMax       = GlobalData.DATABASE.GetResultMaxPtt();
    GlobalData.NPttMin       = GlobalData.DATABASE.GetResultMinPtt();
    Log.d(LOG_TAG, "(TrainThread) Training thread is done");
  }
}
