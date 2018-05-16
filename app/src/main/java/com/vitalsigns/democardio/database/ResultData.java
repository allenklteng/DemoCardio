package com.vitalsigns.democardio.database;

import android.database.Cursor;

import com.vitalsigns.sdk.dsp.bp.Constant;

/**
 * Created by coge on 2018/5/11.
 */

public class ResultData
{
  public static final String TABLE_NAME = "ptt_result";

  public static final String COL_ID          = "idx";
  public static final String COL_MEAS_PTT    = "m_ptt";
  public static final String COL_MEAS_PW     = "m_pw";
  public static final String COL_MEAS_SBP    = "m_sbp";
  public static final String COL_MEAS_DBP    = "m_dbp";
  public static final String COL_MEAS_HR     = "m_hr";
  public static final String COL_DATE        = "date";
  public static final String COL_REAL_SBP    = "r_sbp";
  public static final String COL_REAL_DBP    = "r_dbp";
  public static final String COL_REAL_HR     = "r_hr";

  public static final String TYPE_ID          = "INTEGER PRIMARY KEY";
  public static final String TYPE_MEAS_PTT    = "INTEGER";
  public static final String TYPE_MEAS_PW     = "INTEGER";
  public static final String TYPE_MEAS_SBP    = "INTEGER";
  public static final String TYPE_MEAS_DBP    = "INTEGER";
  public static final String TYPE_MEAS_HR     = "INTEGER";
  public static final String TYPE_REAL_SBP    = "INTEGER";
  public static final String TYPE_REAL_DBP    = "INTEGER";
  public static final String TYPE_REAL_HR     = "INTEGER";
  public static final String TYPE_TRAINING    = "INTEGER";
  public static final String TYPE_VSB_FILE    = "VARCHAR(256)";
  public static final String TYPE_DATE        = "DATETIME";

  public static final String INIT_DATE = "1970-01-01 00:00:00";
  public static final int POSITION_SIT = Constant.MEAS_POSITION_SIT;
  public static final int DEFAULT_HEIGHT = 173;
  public static final int DEFAULT_WEIGHT = 76;
  public static final int DEFAULT_TRAINING_DISABLE = 0;

  public int NIdx = 0;
  public int NMeasPtt = -1;
  public int NMeasPW = -1;
  public int NMeasSbp = -1;
  public int NMeasDbp = -1;
  public int NMeasHR = -1;
  public int NHeight = DEFAULT_HEIGHT;
  public int NWeight = DEFAULT_WEIGHT;
  public String StrDate = null;
  public int NPosition = POSITION_SIT;
  public int NRealSbp = -1;
  public int NRealDbp = -1;
  public int NRealHR = -1;

  /**
   * @brief ResultData
   *
   * Constructor
   *
   * @return NULL
   */
  public ResultData()
  {
    StrDate = new String(INIT_DATE);
  }

  /**
   * @brief GetMeasHour
   *
   * Get the measurement hour
   *
   * @return hour in 24hour format
   */
  public int GetMeasHour()
  {
    String [] arrString = StrDate.split(" ");
    if(arrString.length > 1)
    {
      String [] arrStringTime = arrString[1].split(":");
      if(arrStringTime.length > 0)
      {
        return (Integer.parseInt(arrStringTime[0]));
      }
    }
    return (0);
  }

  /**
   * @brief ResultData
   *
   * Constructor
   *
   * @return NULL
   */
  public ResultData(Cursor objCursor)
  {
    this.NIdx          = objCursor.getInt(objCursor.getColumnIndex(ResultData.COL_ID));
    this.StrDate       = objCursor.getString(objCursor.getColumnIndex(ResultData.COL_DATE));
    this.NMeasSbp      = objCursor.getInt(objCursor.getColumnIndex(ResultData.COL_MEAS_SBP));
    this.NMeasDbp      = objCursor.getInt(objCursor.getColumnIndex(ResultData.COL_MEAS_DBP));
    this.NMeasHR       = objCursor.getInt(objCursor.getColumnIndex(ResultData.COL_MEAS_HR));
    this.NMeasPtt      = objCursor.getInt(objCursor.getColumnIndex(ResultData.COL_MEAS_PTT));
    this.NMeasPW       = objCursor.getInt(objCursor.getColumnIndex(ResultData.COL_MEAS_PW));
    this.NRealSbp      = objCursor.getInt(objCursor.getColumnIndex(ResultData.COL_REAL_SBP));
    this.NRealDbp      = objCursor.getInt(objCursor.getColumnIndex(ResultData.COL_REAL_DBP));
    this.NRealHR       = objCursor.getInt(objCursor.getColumnIndex(ResultData.COL_REAL_HR));
  }
}
