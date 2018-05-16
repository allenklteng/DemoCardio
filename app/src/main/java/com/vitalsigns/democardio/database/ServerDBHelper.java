package com.vitalsigns.democardio.database;

import android.content.Context;
import android.util.Log;

import com.vitalsigns.democardio.Calibration.DownloadNetFileAsyncTask;
import com.vitalsigns.democardio.Calibration.NetTableAsyncTask;

/**
 * Created by coge on 2018/5/11.
 */

public class ServerDBHelper
{
  private static final String LOG_TAG = "[SDH]: ";
  private String StrErrMsg = null;

  public ServerDBHelper()
  {
    StrErrMsg = null;
  }

  /**
   * brief FetchNetTable
   *
   * Fetch NetTable data from server
   *
   * return NetTableData object
   */
  public NetTableData FetchNetTable(Context context)
  {
    NetTableAsyncTask netTableAsyncTask;

    StrErrMsg = null;

    /// [AT-PM] : Execute the async task ; 07/27/2016
    netTableAsyncTask = new NetTableAsyncTask(context);
    netTableAsyncTask.execute("");

    /// [AT-PM] : Wait for the task done ; 07/27/2016
    synchronized(netTableAsyncTask.GetSyncToken())
    {
      while(!netTableAsyncTask.TaskDone())
      {
        try
        {
          Log.d(LOG_TAG, "Wait for the task done");
          netTableAsyncTask.GetSyncToken().wait();
        }
        catch(InterruptedException e)
        {
          e.printStackTrace();
          StrErrMsg = e.getMessage();
          return (null);
        }
      }
      if(!netTableAsyncTask.TaskSuccess())
      {
        StrErrMsg = netTableAsyncTask.TaskResult();
        return (null);
      }
    }
    return (netTableAsyncTask.GetNetTableData());
  }

  /**
   * brief DownloadNetFile
   *
   * Download the NET file from server
   *
   * return true if success
   */
  public boolean DownloadNetFile(Context context)
  {
    DownloadNetFileAsyncTask downloadNetFileAsyncTask = new DownloadNetFileAsyncTask(context);
    downloadNetFileAsyncTask.execute(DownloadNetFileAsyncTask.PARAM_DEFAULT);

    synchronized(downloadNetFileAsyncTask.GetSyncToken())
    {
      try
      {
        while(!downloadNetFileAsyncTask.TaskDone())
        {
          downloadNetFileAsyncTask.GetSyncToken().wait();
        }
        StrErrMsg = downloadNetFileAsyncTask.TaskSuccess() ? null : downloadNetFileAsyncTask.TaskResult();
      }
      catch(InterruptedException e)
      {
        e.printStackTrace();
        StrErrMsg = e.getMessage();
      }
    }
    return (downloadNetFileAsyncTask.TaskSuccess());
  }
}
