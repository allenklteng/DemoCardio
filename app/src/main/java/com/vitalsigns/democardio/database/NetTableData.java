package com.vitalsigns.democardio.database;

/**
 * Created by coge on 2018/5/11.
 */

public class NetTableData {
  public static final String TABLE_NAME = "net_table";

  public static final String COL_ID = "idx";
  public static final String COL_DATE = "date";
  public static final String COL_PTT_MAX = "ptt_max";
  public static final String COL_PTT_MIN = "ptt_min";
  public static final String COL_SBP_GAIN = "sbp_gain";
  public static final String COL_SBP_OFFSET = "sbp_offset";
  public static final String COL_DBP_GAIN = "dbp_gain";
  public static final String COL_DBP_OFFSET = "dbp_offset";
  public static final String COL_ACTIVE = "active";
  public static final String COL_CHECKSUM = "checksum";

  public static final String TYPE_ID = "INTEGER PRIMARY KEY";
  public static final String TYPE_DATE = "DATETIME";
  public static final String TYPE_PTT_MAX = "INTEGER";
  public static final String TYPE_PTT_MIN = "INTEGER";
  public static final String TYPE_SBP_GAIN = "FLOAT";
  public static final String TYPE_SBP_OFFSET = "FLOAT";
  public static final String TYPE_DBP_GAIN = "FLOAT";
  public static final String TYPE_DBP_OFFSET = "FLOAT";
  public static final String TYPE_ACTIVE = "INTEGER";
  public static final String TYPE_CHECKSUM = "INTEGER";

  public static final int DEFAULT_ID = -1;
  public static final String DEFAULT_DATE = "1970-01-01 00:00:00";
  public static final int DEFAULT_PTT_MAX = -1;
  public static final int DEFAULT_PTT_MIN = -1;
  public static final float DEFAULT_SBP_GAIN = 0;
  public static final float DEFAULT_SBP_OFFSET = 0;
  public static final float DEFAULT_DBP_GAIN = 0;
  public static final float DEFAULT_DBP_OFFSET = 0;
  public static final int ACTIVE = 1;
  public static final int INACTIVE = 0;
  public static final int CHECKSUM = 0;

  public int NIdx;
  public String StrDate;
  public int NPttMax;
  public int NPttMin;
  public float FSbpGain;
  public float FSbpOffset;
  public float FDbpGain;
  public float FDbpOffset;
  public int NActive;
  public int NCheckSum;

  public NetTableData()
  {
    NIdx = DEFAULT_ID;
    StrDate = DEFAULT_DATE;
    NPttMax = DEFAULT_PTT_MAX;
    NPttMin = DEFAULT_PTT_MIN;
    FSbpGain = DEFAULT_SBP_GAIN;
    FSbpOffset = DEFAULT_SBP_OFFSET;
    FDbpGain = DEFAULT_DBP_GAIN;
    FDbpOffset = DEFAULT_DBP_OFFSET;
    NActive = INACTIVE;
    NCheckSum = CHECKSUM;
  }
}
