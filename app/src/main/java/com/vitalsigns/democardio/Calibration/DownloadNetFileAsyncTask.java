package com.vitalsigns.democardio.Calibration;

import android.content.Context;
import android.util.Log;

import com.vitalsigns.democardio.GlobalData;
import com.vitalsigns.sdk.server.BaseServerAsyncTask;
import com.vitalsigns.sdk.utility.Utility;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by coge on 2018/5/11.
 */

public class DownloadNetFileAsyncTask extends BaseServerAsyncTask<String, Void, String>
{
  public static final String PARAM_DEFAULT = "__DEFAULT_NET_FILE__";

  private static byte[] BFileBuffer = null;

  public DownloadNetFileAsyncTask(Context context)
  {
    super(context);
  }

  @Override
  protected String doInBackgroundTask(String param)
  {
    String strFilename;
    OutputStream outputStream;

    /// [AT-PM] : Set the file to be downloaded ; 06/08/2016
    strFilename = getContext().getExternalFilesDir(null).getPath() + "/";
    if(param.equals(PARAM_DEFAULT) == true)
    {
      setDownloadDefault();
      strFilename += "default_server.net";
    }
    else
    {
      setDownloadFilename(param);
      strFilename += param;
    }

    httpDownload(GlobalData.bSelectChina);

    BFileBuffer = getInputStreamReceiveData();
    if(BFileBuffer == null)
    {
      return (RTN_FAIL);
    }

    /// [AT-PM] : Save to sdcard ; 06/08/2016
    try
    {
      outputStream = new FileOutputStream(strFilename);
      outputStream.write(BFileBuffer, 0, BFileBuffer.length);
      outputStream.flush();
      outputStream.close();
    }
    catch(FileNotFoundException e)
    {
      e.printStackTrace();
      return (RTN_FAIL);
    }
    catch(IOException e)
    {
      e.printStackTrace();
      return (RTN_FAIL);
    }
    return (RTN_SUCCESS);
  }

  @Override
  protected void onPostExecuteTask(String param)
  {
  }
}
