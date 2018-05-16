package com.vitalsigns.democardio.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by coge on 2018/5/11.
 */

public class SqlDBHelper extends SQLiteOpenHelper
{
  private static final int DB_VERSION = 1;
  private static final String LOG_TAG = "[SDH]:";
  private static final String SQL_CREATE_TABLE = "CREATE TABLE ";
  private static final String SQL_INSERT_INTO = "INSERT INTO ";
  private static final String SQL_SELECT = "SELECT ";
  private static final String SQL_DELETE = "DELETE FROM ";
  private static final int LIMIT_TRAINING_CNT = 32;

  public SqlDBHelper(Context context, String name)
  {
    super(context, name, null, DB_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db)
  {
    createTablePttResult(db);
    createTableNetTable(db);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
  }

  /**
   * brief createTablePttResult
   *
   * Create PttResult table
   *
   * param db SQLiteDatabase
   * return NULL
   */
  private void createTablePttResult(SQLiteDatabase db)
  {
    String strSql = SQL_CREATE_TABLE + ResultData.TABLE_NAME;

    strSql += " (";
    strSql += ResultData.COL_ID +          " " + ResultData.TYPE_ID + ",";
    strSql += ResultData.COL_DATE +        " " + ResultData.TYPE_DATE + ",";
    strSql += ResultData.COL_MEAS_SBP +    " " + ResultData.TYPE_MEAS_SBP + ",";
    strSql += ResultData.COL_MEAS_DBP +    " " + ResultData.TYPE_MEAS_DBP + ",";
    strSql += ResultData.COL_MEAS_HR +     " " + ResultData.TYPE_MEAS_HR + ",";
    strSql += ResultData.COL_MEAS_PTT +    " " + ResultData.TYPE_MEAS_PTT + ",";
    strSql += ResultData.COL_MEAS_PW +     " " + ResultData.TYPE_MEAS_PW + ",";
    strSql += ResultData.COL_REAL_SBP +    " " + ResultData.TYPE_REAL_SBP + ",";
    strSql += ResultData.COL_REAL_DBP +    " " + ResultData.TYPE_REAL_DBP + ",";
    strSql += ResultData.COL_REAL_HR +     " " + ResultData.TYPE_REAL_HR;
    strSql += ");";
    db.execSQL(strSql);
  }

  /**
   * brief createTableNetTable
   *
   * Create NetTable table
   *
   * param db SQLiteDatabase
   * return NULL
   */
  private void createTableNetTable(SQLiteDatabase db)
  {
    String strSql = null;

    strSql = SQL_CREATE_TABLE + NetTableData.TABLE_NAME;
    strSql += "(";
    strSql += NetTableData.COL_ID +         " " + NetTableData.TYPE_ID + ",";
    strSql += NetTableData.COL_DATE +       " " + NetTableData.TYPE_DATE + ",";
    strSql += NetTableData.COL_PTT_MAX +    " " + NetTableData.TYPE_PTT_MAX + ",";
    strSql += NetTableData.COL_PTT_MIN +    " " + NetTableData.TYPE_PTT_MIN + ",";
    strSql += NetTableData.COL_SBP_GAIN +   " " + NetTableData.TYPE_SBP_GAIN + ",";
    strSql += NetTableData.COL_SBP_OFFSET + " " + NetTableData.TYPE_SBP_OFFSET + ",";
    strSql += NetTableData.COL_DBP_GAIN +   " " + NetTableData.TYPE_DBP_GAIN + ",";
    strSql += NetTableData.COL_DBP_OFFSET + " " + NetTableData.TYPE_DBP_OFFSET + ",";
    strSql += NetTableData.COL_ACTIVE +     " " + NetTableData.TYPE_ACTIVE + ",";
    strSql += NetTableData.COL_CHECKSUM +   " " + NetTableData.TYPE_CHECKSUM;
    strSql += ")";

    db.execSQL(strSql);
  }

  /**
   * brief DeleteNetTableAll
   *
   * Delete all data in net_table
   *
   * return NULL
   */
  public void DeleteNetTableAll()
  {
    SQLiteDatabase db = this.getWritableDatabase();
    String strSql = null;

    strSql = SQL_DELETE + NetTableData.TABLE_NAME;

    db.execSQL(strSql);
  }

  /**
   * brief AddNetTable
   *
   * Add NetTableData to local database
   *
   * param netTableData NetTableData
   * return null
   */
  public void AddNetTable(NetTableData netTableData)
  {
    SQLiteDatabase db = this.getWritableDatabase();
    String strSql = null;

    strSql = SQL_INSERT_INTO + NetTableData.TABLE_NAME;
    strSql += " (";
    strSql += NetTableData.COL_ID + ",";
    strSql += NetTableData.COL_DATE + ",";
    strSql += NetTableData.COL_PTT_MAX + ",";
    strSql += NetTableData.COL_PTT_MIN + ",";
    strSql += NetTableData.COL_SBP_GAIN + ",";
    strSql += NetTableData.COL_SBP_OFFSET + ",";
    strSql += NetTableData.COL_DBP_GAIN + ",";
    strSql += NetTableData.COL_DBP_OFFSET + ",";
    strSql += NetTableData.COL_ACTIVE + ",";
    strSql += NetTableData.COL_CHECKSUM;
    strSql += ") VALUES (";
    strSql += Integer.toString(netTableData.NIdx) + ",";
    strSql += "'" + netTableData.StrDate + "',";
    strSql += Integer.toString(netTableData.NPttMax) + ",";
    strSql += Integer.toString(netTableData.NPttMin) + ",";
    strSql += Float.toString(netTableData.FSbpGain) + ",";
    strSql += Float.toString(netTableData.FSbpOffset) + ",";
    strSql += Float.toString(netTableData.FDbpGain) + ",";
    strSql += Float.toString(netTableData.FDbpOffset) + ",";
    strSql += Integer.toString(netTableData.NActive) + ",";
    strSql += Integer.toString(netTableData.NCheckSum);
    strSql += ");";

    db.execSQL(strSql);
  }

  /**
   * brief GetNetTable
   *
   * Get NetTableData from database
   *
   * return NetTableData
   */
  public NetTableData GetNetTable()
  {
    String strSql;
    Cursor objCursor;
    SQLiteDatabase objDB = this.getReadableDatabase();
    NetTableData netTableData;

    strSql  = SQL_SELECT + "* FROM " + NetTableData.TABLE_NAME;
    strSql += " WHERE " + NetTableData.COL_ACTIVE + " = " + Integer.toString(NetTableData.ACTIVE);
    objCursor = objDB.rawQuery(strSql, null);
    if(objCursor == null) {
      return (null);
    }
    if(!objCursor.moveToFirst()) {
      objCursor.close();
      return (null);
    }

    netTableData = new NetTableData();
    netTableData.NIdx       =        objCursor.getInt(   objCursor.getColumnIndex(NetTableData.COL_ID));
    netTableData.StrDate    =        objCursor.getString(objCursor.getColumnIndex(NetTableData.COL_DATE));
    netTableData.NPttMax    =        objCursor.getInt(   objCursor.getColumnIndex(NetTableData.COL_PTT_MAX));
    netTableData.NPttMin    =        objCursor.getInt(   objCursor.getColumnIndex(NetTableData.COL_PTT_MIN));
    netTableData.FSbpGain   = (float)objCursor.getDouble(objCursor.getColumnIndex(NetTableData.COL_SBP_GAIN));
    netTableData.FSbpOffset = (float)objCursor.getDouble(objCursor.getColumnIndex(NetTableData.COL_SBP_OFFSET));
    netTableData.FDbpGain   = (float)objCursor.getDouble(objCursor.getColumnIndex(NetTableData.COL_DBP_GAIN));
    netTableData.FDbpOffset = (float)objCursor.getDouble(objCursor.getColumnIndex(NetTableData.COL_DBP_OFFSET));
    netTableData.NActive    =        objCursor.getInt(   objCursor.getColumnIndex(NetTableData.COL_ACTIVE));
    netTableData.NCheckSum  =        objCursor.getInt(   objCursor.getColumnIndex(NetTableData.COL_CHECKSUM));
    objCursor.close();
    return (netTableData);
  }

  /**
   * brief GetLatestResult
   *
   * Get latest result of SBP, DBP, HR and PTT
   *
   * return ResultData object
   */
  public ResultData GetLatestResult()
  {
    ResultData resultData;
    Cursor objCursor;
    String strSql;

    strSql  = SQL_SELECT;
    strSql += "RESULT." + ResultData.COL_MEAS_SBP + " AS " + ResultData.COL_MEAS_SBP + ",";
    strSql += "RESULT." + ResultData.COL_MEAS_DBP + " AS " + ResultData.COL_MEAS_DBP + ",";
    strSql += "RESULT." + ResultData.COL_MEAS_HR  + " AS " + ResultData.COL_MEAS_HR  + ",";
    strSql += "RESULT." + ResultData.COL_MEAS_PTT + " AS " + ResultData.COL_MEAS_PTT + ",";
    strSql += "RESULT." + ResultData.COL_DATE     + " AS " + ResultData.COL_DATE;
    strSql += " FROM ";
    strSql += ResultData.TABLE_NAME + " AS RESULT";
    strSql += " ORDER BY RESULT." + ResultData.COL_DATE + " DESC";

    objCursor = this.getReadableDatabase().rawQuery(strSql, null);
    if(objCursor == null) {
      return (null);
    }
    if(!objCursor.moveToFirst()) {
      objCursor.close();
      return (null);
    }
    resultData = new ResultData();
    resultData.NMeasSbp      = objCursor.getInt(objCursor.getColumnIndex(ResultData.COL_MEAS_SBP));
    resultData.NMeasDbp      = objCursor.getInt(objCursor.getColumnIndex(ResultData.COL_MEAS_DBP));
    resultData.NMeasHR       = objCursor.getInt(objCursor.getColumnIndex(ResultData.COL_MEAS_HR));
    resultData.NMeasPtt      = objCursor.getInt(objCursor.getColumnIndex(ResultData.COL_MEAS_PTT));
    resultData.StrDate       = objCursor.getString(objCursor.getColumnIndex(ResultData.COL_DATE));
    objCursor.close();
    return (resultData);
  }

  /**
   * brief GetResultTraining
   *
   * Get the result for training
   *
   * param nMinSbp minimum SBP threshold
   * param nMinDbp minimum DBP threshold
   * param nMinHR minimum HR threshold
   * return ResultData []
   */
  public ResultData[] GetResultTraining(int nMinSbp, int nMinDbp, int nMinHR)
  {
    String strSql;
    Cursor objCursor;
    SQLiteDatabase objDB = this.getReadableDatabase();
    int nResultCnt;
    int nResultIdx;
    ResultData[] arrPttResult;

    strSql = SQL_SELECT;
    strSql += " RESULT." + ResultData.COL_ID +          " AS " + ResultData.COL_ID + ",";
    strSql += " RESULT." + ResultData.COL_DATE +        " AS " + ResultData.COL_DATE + ",";
    strSql += " RESULT." + ResultData.COL_MEAS_SBP +    " AS " + ResultData.COL_MEAS_SBP + ",";
    strSql += " RESULT." + ResultData.COL_MEAS_DBP +    " AS " + ResultData.COL_MEAS_DBP + ",";
    strSql += " RESULT." + ResultData.COL_MEAS_HR +     " AS " + ResultData.COL_MEAS_HR + ",";
    strSql += " RESULT." + ResultData.COL_MEAS_PTT +    " AS " + ResultData.COL_MEAS_PTT + ",";
    strSql += " RESULT." + ResultData.COL_MEAS_PW +     " AS " + ResultData.COL_MEAS_PW + ",";
    strSql += " RESULT." + ResultData.COL_REAL_SBP +    " AS " + ResultData.COL_REAL_SBP + ",";
    strSql += " RESULT." + ResultData.COL_REAL_DBP +    " AS " + ResultData.COL_REAL_DBP + ",";
    strSql += " RESULT." + ResultData.COL_REAL_HR +     " AS " + ResultData.COL_REAL_HR;
    strSql += " FROM " + ResultData.TABLE_NAME +        " AS RESULT";
    if(nMinSbp > 0)
    {
      strSql += " WHERE RESULT." + ResultData.COL_REAL_SBP + " > " + Integer.toString(nMinSbp);
      if(nMinDbp > 0)
      {
        strSql += " AND RESULT." + ResultData.COL_REAL_DBP + " > " + Integer.toString(nMinDbp);
      }
      if(nMinHR > 0)
      {
        strSql += " AND RESULT." + ResultData.COL_REAL_HR + " > " + Integer.toString(nMinHR);
        strSql += " AND RESULT." + ResultData.COL_MEAS_HR + " > " + Integer.toString(nMinHR);
      }
    }
    else if(nMinDbp > 0)
    {
      strSql += " WHERE RESULT." + ResultData.COL_REAL_DBP + " > " + Integer.toString(nMinDbp);
      if(nMinSbp > 0)
      {
        strSql += " AND RESULT." + ResultData.COL_REAL_SBP + " > " + Integer.toString(nMinSbp);
      }
      if(nMinHR > 0)
      {
        strSql += " AND RESULT." + ResultData.COL_REAL_HR + " > " + Integer.toString(nMinHR);
        strSql += " AND RESULT." + ResultData.COL_MEAS_HR + " > " + Integer.toString(nMinHR);
      }

    }
    else if(nMinHR > 0)
    {
      strSql += " WHERE RESULT." + ResultData.COL_REAL_HR + " > " + Integer.toString(nMinHR);
      strSql += " AND RESULT." + ResultData.COL_MEAS_HR + " > " + Integer.toString(nMinHR);
      if(nMinSbp > 0)
      {
        strSql += " AND RESULT." + ResultData.COL_REAL_SBP + " > " + Integer.toString(nMinSbp);
      }
      if(nMinDbp > 0)
      {
        strSql += " AND RESULT." + ResultData.COL_REAL_DBP + " > " + Integer.toString(nMinDbp);
      }
    }

    strSql += " ORDER BY " + ResultData.COL_DATE + " DESC";
    strSql += " LIMIT " + LIMIT_TRAINING_CNT;
    objCursor = objDB.rawQuery(strSql, null);
    if(objCursor == null)
    {
      return (null);
    }
    if(!objCursor.moveToFirst())
    {
      return (null);
    }
    nResultCnt = objCursor.getCount();
    if(nResultCnt == 0)
    {
      return (null);
    }

    arrPttResult = new ResultData[nResultCnt];

    nResultIdx = 0;
    while(nResultIdx < nResultCnt)
    {
      Log.d(LOG_TAG, "Index = " +  Integer.toString(nResultIdx) + " / " + Integer.toString(nResultCnt));
      arrPttResult[nResultIdx] = new ResultData(objCursor);

      if(!objCursor.moveToNext())
      {
        objCursor.close();
        return (arrPttResult);
      }

      nResultIdx++;
    }

    return (null);
  }

  /**
   * brief GetResultMaxPtt
   *
   * Get maximum PTT from ptt_result
   *
   * return maximum ptt value
   */
  public int GetResultMaxPtt()
  {
    String strSql;
    Cursor objCursor;
    int maxPTT;
    strSql = SQL_SELECT;
    strSql += "MAX(RESULT." + ResultData.COL_MEAS_PTT + ") AS " + ResultData.COL_MEAS_PTT;
    strSql += " FROM " + ResultData.TABLE_NAME + " AS RESULT";
    strSql += " WHERE RESULT." + ResultData.COL_REAL_SBP + " > 60";
    strSql += " AND RESULT." + ResultData.COL_REAL_DBP + " > 30";
    strSql += " AND RESULT." + ResultData.COL_REAL_HR + " > 15";

    objCursor = this.getReadableDatabase().rawQuery(strSql, null);
    if(objCursor == null)
    {
      Log.d(LOG_TAG, "FetchResultMaxPtt() -> NULL result");
      return (-1);
    }
    if(!objCursor.moveToFirst())
    {
      Log.d(LOG_TAG, "FetchResultMaxPtt() -> moveToFirst fail");
      return (-1);
    }
    maxPTT = objCursor.getInt(objCursor.getColumnIndex(ResultData.COL_MEAS_PTT));
    objCursor.close();
    return (maxPTT);
  }

  /**
   * brief GetResultMaxPtt
   *
   * Get maximum PTT from ptt_result
   *
   * return maximum ptt value
   */
  public int GetResultMinPtt()
  {
    String strSql;
    Cursor objCursor;
    int minPTT;
    strSql = SQL_SELECT;
    strSql += "MIN(RESULT." + ResultData.COL_MEAS_PTT + ") AS " + ResultData.COL_MEAS_PTT;
    strSql += " FROM " + ResultData.TABLE_NAME + " AS RESULT";
    strSql += " WHERE RESULT." + ResultData.COL_REAL_SBP + " > 60";
    strSql += " AND RESULT." + ResultData.COL_REAL_DBP + " > 30";
    strSql += " AND RESULT." + ResultData.COL_REAL_HR + " > 15";

    objCursor = this.getReadableDatabase().rawQuery(strSql, null);
    if(objCursor == null)
    {
      Log.d(LOG_TAG, "FetchResultMaxPtt() -> NULL result");
      return (-1);
    }
    if(!objCursor.moveToFirst())
    {
      Log.d(LOG_TAG, "FetchResultMaxPtt() -> moveToFirst fail");
      return (-1);
    }
    minPTT = objCursor.getInt(objCursor.getColumnIndex(ResultData.COL_MEAS_PTT));
    objCursor.close();
    return (minPTT);
  }

  /**
   * brief saveResult
   *
   * Save result
   *
   * param pttResult ResultData
   * return NULL
   */
  public void saveResult(ResultData result)
  {
    SQLiteDatabase db = this.getWritableDatabase();

    String strSql = SQL_INSERT_INTO + ResultData.TABLE_NAME;
    strSql += " (";
    strSql += ResultData.COL_DATE + ",";
    strSql += ResultData.COL_MEAS_SBP + ",";
    strSql += ResultData.COL_MEAS_DBP + ",";
    strSql += ResultData.COL_MEAS_HR + ",";
    strSql += ResultData.COL_MEAS_PTT + ",";
    strSql += ResultData.COL_MEAS_PW + ",";
    strSql += ResultData.COL_REAL_SBP + ",";
    strSql += ResultData.COL_REAL_DBP + ",";
    strSql += ResultData.COL_REAL_HR;
    strSql += ") VALUES (";
    strSql += "'" + result.StrDate + "',";
    strSql += Integer.toString(result.NMeasSbp) + ",";
    strSql += Integer.toString(result.NMeasDbp) + ",";
    strSql += Integer.toString(result.NMeasHR) + ",";
    strSql += Integer.toString(result.NMeasPtt) + ",";
    strSql += Integer.toString(result.NMeasPW) + ",";
    strSql += Integer.toString(result.NRealSbp) + ",";
    strSql += Integer.toString(result.NRealDbp) + ",";
    strSql += Integer.toString(result.NRealHR);
    strSql += ");";

    db.execSQL(strSql);
  }
}
