package com.vitalsigns.democardio;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.vitalsigns.democardio.Calibration.Calibration;
import com.vitalsigns.democardio.Calibration.CalibrationFragment;
import com.vitalsigns.democardio.database.NetTableData;
import com.vitalsigns.democardio.database.ResultData;
import com.vitalsigns.democardio.database.ServerDBHelper;
import com.vitalsigns.sdk.ble.scan.DeviceListFragment;
import com.vitalsigns.sdk.utility.RequestPermission;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
  implements DeviceListFragment.OnEvent,
             VitalSignsDsp.OnUpdateResult
{
  private static final String LOG_TAG = "MainActivity";

  private FloatingActionButton FabStart          = null;
  private VitalSignsDsp        VSDsp             = null;
  private int                  Sbp               = -1;
  private int                  Dbp               = -1;
  private int                  HR                = -1;
  private int                  Ptt               = -1;
  private int                  PW                = -1;
  private HandlerThread        mBackgroundThread = null;
  private ProgressDialog mProgressDialog;
  private Menu mMenu;
  private final int SHOW_ECG_WARING_DELEY_TIME = 5000; // 5 second
  
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FabStart = (FloatingActionButton)findViewById(R.id.fab);
    FabStart.setOnClickListener(onClickListenerFab);

    setFont();
    setTextSize();
    GlobalData.setContext(MainActivity.this);
    GlobalData.initDatabase(MainActivity.this);
    showBioSignal(0, 0, 0,0, 0);

    if(GlobalData.requestPermissionForAndroidM(MainActivity.this))
    {
      initBle();
      Log.d(LOG_TAG, "scanBle @ onCreate()");

      VSDsp = new VitalSignsDsp(MainActivity.this);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    mMenu = menu;
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if(id == R.id.action_scan_ble_device)
    {
      if(GlobalData.requestPermissionForAndroidM(MainActivity.this))
      {
        Log.d(LOG_TAG, "scanBle @ onOptionsItemSelected()");

        if(GlobalData.BleControl != null)
        {
          GlobalData.BleControl.disconnect();
        }

        scanBle();
      }
      return (true);
    }
    if(id == R.id.action_disconnect)
    {
      if(GlobalData.BleControl != null)
      {
        GlobalData.BleControl.disconnect();
        if(mMenu != null)
        {
          mMenu.findItem(R.id.action_disconnect).setTitle(getString(R.string.action_disconnect));
        }
      }
    }
    if(id == R.id.action_read_fw_version)
    {
      if(GlobalData.BleControl != null)
      {
        Toast.makeText(this, "FW Verseion : " + GlobalData.BleControl.getVersion(), Toast.LENGTH_LONG)
          .show();
      }
    }
    if(id == R.id.action_read_battery_level)
    {
      if(GlobalData.BleControl != null)
      {
        Toast.makeText(this, "Battery Level : " + Integer.toString(GlobalData.BleControl.getBatteryLevel()), Toast.LENGTH_LONG)
          .show();
      }
    }
    if(id == R.id.action_calibration)
    {
      doCalibration();
    }

    return super.onOptionsItemSelected(item);
  }

  private void setFont()
  {
    Typeface typeface = Typeface.createFromAsset(getAssets(),"fonts/digital-7 (mono).ttf");
    ((TextView)findViewById(R.id.textSbpTitle)).setTypeface(typeface);
    ((TextView)findViewById(R.id.textSbpValue)).setTypeface(typeface);
    ((TextView)findViewById(R.id.textDbpTitle)).setTypeface(typeface);
    ((TextView)findViewById(R.id.textDbpValue)).setTypeface(typeface);
    ((TextView)findViewById(R.id.textHRTitle)).setTypeface(typeface);
    ((TextView)findViewById(R.id.textHRValue)).setTypeface(typeface);
  }

  private void setTextSize()
  {
    float titleSize;
    float textSize;

    titleSize = getResources().getDimensionPixelSize(R.dimen.title_text_size);
    textSize = getResources().getDimensionPixelSize(R.dimen.value_text_size);

    ((TextView)findViewById(R.id.textSbpTitle)).setTextSize(titleSize);
    ((TextView)findViewById(R.id.textSbpValue)).setTextSize(textSize);
    ((TextView)findViewById(R.id.textDbpTitle)).setTextSize(titleSize);
    ((TextView)findViewById(R.id.textDbpValue)).setTextSize(textSize);
    ((TextView)findViewById(R.id.textHRTitle)).setTextSize(titleSize);
    ((TextView)findViewById(R.id.textHRValue)).setTextSize(textSize);
  }

  private void showBioSignal(int nSbp, int nDbp, int nHR, int nPtt, int nPW)
  {
    Sbp = nSbp;
    Dbp = nDbp;
    HR  = nHR;
    Ptt = nPtt;
    PW  = nPW;

    runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        /// [AT-PM] : Show systolic blood pressure ; 10/24/2016
        if(Sbp > 0)
        {
          ((TextView)findViewById(R.id.textSbpValue)).setText(String.format(Locale.ENGLISH, "%d", Sbp));
        }
        else
        {
          ((TextView)findViewById(R.id.textSbpValue)).setText("--");
        }

        /// [AT-PM] : Show diastolic blood pressure ; 10/24/2016
        if(Dbp > 0)
        {
          ((TextView)findViewById(R.id.textDbpValue)).setText(String.format(Locale.ENGLISH, "%d", Dbp));
        }
        else
        {
          ((TextView)findViewById(R.id.textDbpValue)).setText("--");
        }

        /// [AT-PM] : Show heart rate ; 10/24/2016
        if(HR > 0)
        {
          ((TextView)findViewById(R.id.textHRValue)).setText(String.format(Locale.ENGLISH, "%d", HR));
        }
        else
        {
          ((TextView)findViewById(R.id.textHRValue)).setText("--");
        }
      }
    });
  }

  private boolean start()
  {
    /// [AT-PM] : Start the measurement ; 10/24/2016
    if(VSDsp == null)
    {
      Log.d(LOG_TAG, "VSDsp == null");
      return (false);
    }
    if(!VSDsp.Start())
    {
      Log.d(LOG_TAG, "VSDsp.Start() == false");
      return (false);
    }

    GlobalData.Recording = true;

    FabStart.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.ic_media_pause));
    return (true);
  }

  /**
   * Stop the measurement
   * @param restart set true to restart the pre-start process
   */
  private void stop(boolean restart)
  {
    /// [AT-PM] : No need to stop if not in recording ; 07/20/2017
    if(!GlobalData.Recording)
    {
      return;
    }
    /// [AT-PM] : Stop the measurement ; 10/24/2016
    if(VSDsp == null)
    {
      return;
    }
    VSDsp.Stop(restart);

    GlobalData.Recording = false;

    runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        FabStart.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.ic_media_play));
      }
    });
  }

  private View.OnClickListener onClickListenerFab = new View.OnClickListener()
  {
    @Override
    public void onClick(View view)
    {
      /// [AT-PM] : Check connected ; 06/16/2017
      if(GlobalData.BleControl == null)
      {
        Snackbar.make(view, "Connection first", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show();
        return;
      }
      
      /// [AT-PM] : Check connected ; 06/16/2017
      if(!GlobalData.BleControl.isConnect())
      {
        Snackbar.make(view, "Connection first", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show();
        return;
      }

      /// [AT-PM] : Stop the recording ; 06/16/2017
      if(GlobalData.Recording)
      {
        stop(false);

        Snackbar.make(view, "Blood pressure measurement -> STOPPED", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show();
        return;
      }

      GlobalData.BleControl.start();
      waitEcgReady();
    }
  };

  @Override
  public void onBleDeviceSelected(String bleDeviceAddress)
  {
    if(bleDeviceAddress == null)
    {
      /// [AT-PM] : Re-scan the BLE device ; 10/24/2016
      Log.d(LOG_TAG, "No device selected");
      return;
    }
  
    showProgressDialog("Connecting...");
    /// [AT-PM] : Connect BLE device ; 10/24/2016
    GlobalData.BleControl.connect(bleDeviceAddress);
  }

  @Override
  public void onDfuDeviceSelected(BluetoothDevice bluetoothDevice)
  {
  }

  @Override
  public void onSendCrashMsg(String s, String s1)
  {
  }

  private void scanBle()
  {
    if(GlobalData.requestPermissionForAndroidM(MainActivity.this))
    {
      if(GlobalData.BleControl == null)
      {
        initBle();
      }
      
      /// [AT-PM] : Call a dialog to scan device ; 05/05/2017
      DeviceListFragment fragment = DeviceListFragment.newInstance(DeviceListFragment.ACTION_SCAN_BLE_DEVICE,
                                                                   DeviceListFragment.STYLE_WHITE);
      getFragmentManager().beginTransaction()
                          .add(fragment, getResources().getString(R.string.device_list_fragment_tag))
                          .commitAllowingStateLoss();
    }
  }

  private void initBle()
  {
    GlobalData.BleControl = new VitalSignsBle(MainActivity.this, mBleEvent);
  }

  @Override
  protected void onResume()
  {
    super.onResume();
  }

  @Override
  protected void onPause()
  {
    super.onPause();
  }

  @Override
  protected void onDestroy()
  {
    super.onDestroy();

    if(GlobalData.BleControl != null)
    {
      GlobalData.BleControl.destroy();
    }
    if(VSDsp != null)
    {
      VSDsp.destroy();
    }
  }

  @Override
  public void onUpdateResult(float fSbp, float fDbp, float fHR, float fPtt, float fPW)
  {
    showBioSignal((int)fSbp, (int)fDbp, (int)fHR, (int)fPtt, (int)fPW);
    Log.d(LOG_TAG, "SBP = " + Float.toString(fSbp) +
                         ", DBP = " + Float.toString(fDbp) +
                         ", HR = " + Float.toString(fHR) +
                         ", HR = " + Float.toString(fPtt) +
                         ", HR = " + Float.toString(fPW));
  }

  @Override
  public void onInterrupt()
  {
    stop(false);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         @NonNull String[] permissions,
                                         @NonNull int[] grantResults)
  {
    if((grantResults == null) || (grantResults.length == 0))
    {
      return;
    }
    switch (requestCode)
    {
      case RequestPermission.PERMISSION_REQUEST_COARSE_LOCATION:
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
          Log.d(LOG_TAG, "coarse location permission granted");
        }
        else
        {
          AlertDialog.Builder builder = new AlertDialog.Builder(this);
          builder.setTitle("Functionality limited");
          builder.setMessage("Since location access has not been granted, this app will not be able to discover devices.");
          builder.setPositiveButton(android.R.string.ok, null);
          builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
              finish();
            }
          });
          builder.show();
        }
        break;
      case RequestPermission.PERMISSION_REQUEST_EXTERNAL_STORAGE:
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
          Log.d(LOG_TAG, "external storage permission granted");
        }
        else
        {
          AlertDialog.Builder builder = new AlertDialog.Builder(this);
          builder.setTitle("Functionality limited");
          builder.setMessage("Since external storage has not been granted, this app will not be able to discover devices.");
          builder.setPositiveButton(android.R.string.ok, null);
          builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
              finish();
            }
          });
          builder.show();
        }
        break;
      case RequestPermission.PERMISSION_REQUEST_READ_PHONE_STATE:
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
          Log.d(LOG_TAG, "read phone state granted");
        }
        else
        {
          AlertDialog.Builder builder = new AlertDialog.Builder(this);
          builder.setTitle("Functionality limited");
          builder.setMessage("Since read phone state has not been granted, this app will not be able to discover devices.");
          builder.setPositiveButton(android.R.string.ok, null);
          builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
              finish();
            }
          });
          builder.show();
        }
        break;
    }
  }

  private VitalSignsBle.BleEvent mBleEvent = new VitalSignsBle.BleEvent()
  {
    @Override
    public void onDisconnect()
    {
      Log.d(LOG_TAG, "onDisconnect()");
      stop(false);

      GlobalData.BleControl.disconnect();
      hideProgressDialog();

      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if(mMenu != null)
          {
            mMenu.findItem(R.id.action_disconnect).setTitle(getString(R.string.action_disconnect));
          }
          Toast.makeText(MainActivity.this, "Disconnection", Toast.LENGTH_LONG).show();
        }
      });
    }

    @Override
    public void onConnect(final String strDeviceName)
    {
      Log.d(LOG_TAG, "onConnect()");
  
      hideProgressDialog();
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if(mMenu != null)
          {
            mMenu.findItem(R.id.action_disconnect).setTitle(getString(R.string.action_disconnect) + "(" + strDeviceName + ")");
          }
          Toast.makeText(MainActivity.this, "Successful connection! Let measurement now !", Toast.LENGTH_LONG).show();
        }
      });
    }
  };

  @Override
  protected void onStart()
  {
    super.onStart();

    /// [AT-PM] : Start background HandlerThread ; 07/20/2017
    mBackgroundThread = new HandlerThread("Background Thread", Process.THREAD_PRIORITY_BACKGROUND);
    mBackgroundThread.start();

    downloadNetFile();
  }

  @Override
  protected void onStop()
  {
    super.onStop();

    /// [AT-PM] : Release background HandlerThread ; 07/20/2017
    Utility.releaseHandlerThread(mBackgroundThread);
    mBackgroundThread = null;
  }

  /**
   * Start a runnable to wait ECG ready
   */
  private void waitEcgReady()
  {
    Handler waitEcgReadyHandler;
    final Handler showWarningHandler;

    /// [AT-PM] : Start a runnable to check ECG is ready ; 09/01/2017
    waitEcgReadyHandler = new Handler(mBackgroundThread.getLooper());
    waitEcgReadyHandler.post(new Runnable() {
      @Override
      public void run() {
        while(!GlobalData.BleControl.isEcgReady())
        {
          if(!GlobalData.BleControl.isConnect())
          {
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                Toast.makeText(MainActivity.this, "Disconnection", Toast.LENGTH_LONG).show();
              }
            });
            return;
          }
          com.vitalsigns.sdk.utility.Utility.SleepSomeTime(100);
        }

        /// [AT-PM] : Start the measurement ; 09/01/2017
        runOnUiThread(new Runnable()
        {
          @Override
          public void run()
          {
            if(start())
            {
              /// [AT-PM] : Start recording ; 06/16/2017
              Snackbar.make(findViewById(android.R.id.content),
                            "Blood pressure measurement -> STARTED",
                            Snackbar.LENGTH_LONG)
                      .setAction("Action", null)
                      .show();
            }
            else
            {
              Snackbar.make(findViewById(android.R.id.content),
                            "Blood pressure measurement -> FAILED",
                            Snackbar.LENGTH_LONG)
                      .setAction("Action", null)
                      .show();
            }
          }
        });
      }
    });

    showWarningHandler = new Handler();
    showWarningHandler.post(new Runnable() {
      @Override
      public void run() {
        if(!GlobalData.BleControl.isEcgReady())
        {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              Snackbar.make(findViewById(android.R.id.content),
                            "Put finger on watch and keep stable for a while",
                            Snackbar.LENGTH_LONG)
                      .setAction("Action", null)
                      .show();
            }
          });

          if((GlobalData.BleControl != null) &&
             (GlobalData.BleControl.isConnect() == true))
          {
            showWarningHandler.postDelayed(this, SHOW_ECG_WARING_DELEY_TIME);
          }
        }
      }
    });
  }
  
  /**
   * @brief showProgressDialog
   *
   * Show progress dialog
   *
   * @return NULL
   */
  private void showProgressDialog(final String strMsg) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (mProgressDialog == null)
        {
          mProgressDialog = new ProgressDialog(MainActivity.this, R.style.ProgressDialogStyle);
          mProgressDialog.setIndeterminate(true);
          mProgressDialog.setCanceledOnTouchOutside(false);
          mProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener()
          {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
            {
              if(mProgressDialog != null && mProgressDialog.isShowing())
              {
                /// [CC] : Do nothing if true ; 10/31/2017
                return (true);
              }
              return (false);
            }
          });
        }
        
        mProgressDialog.setMessage(strMsg);
        mProgressDialog.show();
      }
    });
  }
  
  /**
   * @brief hideProgressDialog
   *
   * Hide progress dialog
   *
   * @return NULL
   */
  private void hideProgressDialog() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if ((mProgressDialog != null) && (mProgressDialog.isShowing()))
        {
          mProgressDialog.dismiss();
        }
      }
    });
  }

  @Override
  public void onCancelDialog() {

  }

  @Override
  public void onBleOTADeviceSelected(String s, String s1) {

  }

  /**
   * brief downloadNetFile
   *
   * Download NET file from server
   *
   * return NULL
   */
  private void downloadNetFile()
  {
    Handler handler = new Handler(mBackgroundThread.getLooper());
    handler.post(new Runnable()
    {
      @Override
      public void run()
      {
        NetTableData netTableData;
        ServerDBHelper objServer = new ServerDBHelper();

        if(netFileExpired())
        {
          netTableData = objServer.FetchNetTable(getApplicationContext());
          if(netTableData != null)
          {
            com.vitalsigns.sdk.utility.Utility.PRINTFD("[CC] netTableData != null");
            GlobalData.DATABASE.DeleteNetTableAll();
            GlobalData.DATABASE.AddNetTable(netTableData);
            if(objServer.DownloadNetFile(getApplicationContext()))
            {
              if(netTableData.NCheckSum == GlobalData.netFileChkSum(MainActivity.this,"default_server.net"))
              {
                setNetFileTimestamp();
              }
            }
          }
          else
          {
            com.vitalsigns.sdk.utility.Utility.PRINTFD("[CC] netTableData == null");
          }
        }
      }
    });
  }

  /**
   * brief netFileExpired
   *
   * Check the NET file is expired or not
   *
   * return true if expired
   */
  private boolean netFileExpired()
  {
    String strPreDate;
    String [] strArrPreDate;
    int nExpiredDay;

    try
    {
      strPreDate = GlobalData.readString(getApplicationContext().getExternalFilesDir(null),
                                         "last_net_download");
      if(strPreDate == null)
      {
        Log.d(LOG_TAG, "No timestamp found");
        return (true);
      }
      strArrPreDate = strPreDate.split("-");
      Log.d(LOG_TAG, "NET file last downloaded at " + strArrPreDate[0] + "/" + strArrPreDate[1] + "/" + strArrPreDate[2]);
    }
    catch(IOException e)
    {
      e.printStackTrace();
      return (true);
    }

    nExpiredDay  = (GlobalData.GetYear() - Integer.parseInt(strArrPreDate[0])) * 12;
    nExpiredDay += (GlobalData.GetMonth() - Integer.parseInt(strArrPreDate[1])) * 30;
    nExpiredDay += (GlobalData.GetDay() - Integer.parseInt(strArrPreDate[2]));
    return (nExpiredDay > 1 ? true : false);
  }

  /**
   * brief setNetFileTimestamp
   *
   * Set the timestamp of the NET file
   *
   * return NULL
   */
  private void setNetFileTimestamp()
  {
    try {
      GlobalData.delete(getApplicationContext().getExternalFilesDir(null),
                        "last_net_download");
      GlobalData.writeString(getApplicationContext().getExternalFilesDir(null),
                             "last_net_download",
                             Integer.toString(GlobalData.GetYear()) + "-" +
                             Integer.toString(GlobalData.GetMonth()) + "-" +
                             Integer.toString(GlobalData.GetDay()));
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
  }

  /**
   * brief doCalibration
   *
   * Do Calibration
   *
   */
  private void doCalibration()
  {
    CalibrationFragment calibrationFragment;
    calibrationFragment = new CalibrationFragment();
    calibrationFragment.setCallback(listener);
    calibrationFragment.show(getFragmentManager(), getResources().getString(R.string.calibration_fragment_tag));
  }

  private CalibrationFragment.OnCalibrationFragmentListener listener = new CalibrationFragment.OnCalibrationFragmentListener() {
    @Override
    public void onCalibration(int nCaliSbp, int nCaliDbp, int nCaliHr) {
      saveDataToDatabase(nCaliSbp, nCaliDbp, nCaliHr);

      Intent intent = new Intent(MainActivity.this, Calibration.class);
      startService(intent);
    }
  };

  /**
   * @brief saveDataToDatabase
   *
   * Save data to database
   *
   * @return NULL
   */
  private void saveDataToDatabase(int nCaliSbp, int nCaliDbp, int nCaliHr)
  {
    Calendar objCalendar;
    ResultData objResult;
    SimpleDateFormat strSdf;

    objCalendar = Calendar.getInstance();
    strSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    objResult = new ResultData();
    objResult.StrDate = strSdf.format(objCalendar.getTime());
    objResult.NMeasSbp  = Sbp;
    objResult.NMeasDbp  = Dbp;
    objResult.NMeasHR   = HR;
    objResult.NMeasPtt  = Ptt;
    objResult.NMeasPW   = PW;
    objResult.NRealSbp  = nCaliSbp;
    objResult.NRealDbp  = nCaliDbp;
    objResult.NRealHR   = nCaliHr;

    /// [CC] : Save the calibration result to database ; 05/16/2018
    GlobalData.DATABASE.saveResult(objResult);
  }
}
