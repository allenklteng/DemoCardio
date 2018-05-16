package com.vitalsigns.democardio.Calibration;

import android.content.Context;

import com.vitalsigns.democardio.GlobalData;
import com.vitalsigns.democardio.database.NetTableData;
import com.vitalsigns.sdk.server.BaseServerAsyncTask;
import com.vitalsigns.sdk.utility.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by coge on 2018/5/11.
 */

public class NetTableAsyncTask extends BaseServerAsyncTask<String, Void, String>
{
  private NetTableData ObjNetTableData = null;

  public NetTableAsyncTask(Context context)
  {
    super(context);
    ObjNetTableData = null;
  }

  @Override
  protected String doInBackgroundTask(String param)
  {
    buildJsonGet();
    if(getJsonObjectSend() == null)
    {
      return (RTN_FAIL);
    }

    Utility.PRINTFD("[CC] httpPost(" + GlobalData.bSelectChina + ")");
    if(!httpPost(GlobalData.bSelectChina))
    {
      return (RTN_HTTP_AUTH_FAIL);
    }

    if(getJsonObjectReceive() == null)
    {
      return (RTN_FAIL);
    }
    if(parseJsonGet() == false)
    {
      return (RTN_FAIL);
    }
    return (RTN_SUCCESS);
  }

  @Override
  protected void onPostExecuteTask(String param)
  {
  }

  /**
   * @brief buildJsonGet
   *
   * Build the JSON object for action GET
   *
   * @return NULL
   */
  private void buildJsonGet()
  {
    JSONObject jsonObject = null;

    try
    {
      jsonObject = new JSONObject();
      jsonObject.put(JSON_KEY_TABLE, NetTableData.TABLE_NAME);
      jsonObject.put(JSON_KEY_DB, JSON_VALUE_DB);
      jsonObject.put(JSON_KEY_ACTION, JSON_VALUE_ACTION_SELECT);
      jsonObject.put(JSON_KEY_KEY, NetTableData.COL_ACTIVE);
      jsonObject.put(JSON_KEY_KEY_VALUE, NetTableData.ACTIVE);
    }
    catch(JSONException e)
    {
      e.printStackTrace();
    }

    setJsonObjectSend(jsonObject);
  }

  /**
   * @brief parseJsonGet
   *
   * Parse the returned JSON object of action GET
   *
   * @return true if success
   */
  private boolean parseJsonGet()
  {
    int nIdx;
    String strCol;
    JSONArray jsonArray;
    JSONObject jsonObject;

    try
    {
      jsonObject = getJsonObjectReceive();
      if(jsonObject.length() == 0)
      {
        return (false);
      }
      if(jsonObject.getString(JSON_KEY_RETURN).equals(JSON_VALUE_RETURN_PASS) == false)
      {
        return (false);
      }

      jsonArray = jsonObject.getJSONArray(JSON_KEY_DATA);
      if(jsonArray.length() == 0)
      {
        return (false);
      }
      nIdx = 0;
      ObjNetTableData = new NetTableData();
      while(nIdx < jsonArray.length())
      {
        jsonObject = jsonArray.getJSONObject(nIdx);
        strCol = jsonObject.getString(JSON_KEY_DATA_COL);
        if(strCol.equals(NetTableData.COL_ID))
        {
          ObjNetTableData.NIdx = Integer.parseInt(jsonObject.getString(JSON_KEY_DATA_VALUE));
        }
        else if(strCol.equals(NetTableData.COL_DATE))
        {
          ObjNetTableData.StrDate = jsonObject.getString(JSON_KEY_DATA_VALUE);
        }
        else if(strCol.equals(NetTableData.COL_PTT_MAX))
        {
          ObjNetTableData.NPttMax = jsonObject.getInt(JSON_KEY_DATA_VALUE);
        }
        else if(strCol.equals(NetTableData.COL_PTT_MIN))
        {
          ObjNetTableData.NPttMin = jsonObject.getInt(JSON_KEY_DATA_VALUE);
        }
        else if(strCol.equals(NetTableData.COL_SBP_GAIN))
        {
          ObjNetTableData.FSbpGain = (float)jsonObject.getDouble(JSON_KEY_DATA_VALUE);
        }
        else if(strCol.equals(NetTableData.COL_SBP_OFFSET))
        {
          ObjNetTableData.FSbpOffset = (float)jsonObject.getDouble(JSON_KEY_DATA_VALUE);
        }
        else if(strCol.equals(NetTableData.COL_DBP_GAIN))
        {
          ObjNetTableData.FDbpGain = (float)jsonObject.getDouble(JSON_KEY_DATA_VALUE);
        }
        else if(strCol.equals(NetTableData.COL_DBP_OFFSET))
        {
          ObjNetTableData.FDbpOffset = (float)jsonObject.getDouble(JSON_KEY_DATA_VALUE);
        }
        else if(strCol.equals(NetTableData.COL_ACTIVE))
        {
          ObjNetTableData.NActive = jsonObject.getInt(JSON_KEY_DATA_VALUE);
        }
        else if(strCol.equals(NetTableData.COL_CHECKSUM))
        {
          ObjNetTableData.NCheckSum = jsonObject.getInt(JSON_KEY_DATA_VALUE);
        }
        nIdx ++;
      }
      jsonObject = getJsonObjectReceive();
      if(jsonObject.length() == 0)
      {
        return (false);
      }
    }
    catch(JSONException e)
    {
      e.printStackTrace();
    }
    return (true);
  }

  /**
   * @brief GetNetTableData
   *
   * Get the NetTableData from server
   *
   * @return NetTableData object
   */
  public NetTableData GetNetTableData()
  {
    return (ObjNetTableData);
  }
}
