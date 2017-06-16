package com.vitalsigns.democardio;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Locale;

import static com.vitalsigns.democardio.GlobalData.PERMISSION_REQUEST_COARSE_LOCATION;
import static com.vitalsigns.democardio.GlobalData.PERMISSION_REQUEST_EXTERNAL_STORAGE;
import static com.vitalsigns.democardio.GlobalData.PERMISSION_REQUEST_READ_PHONE_STATE;

public class MainActivity extends AppCompatActivity
  implements BleDeviceListDialog.OnBleDeviceSelectedListener,
             VitalSignsDsp.OnUpdateResult
{
  private static final String LOG_TAG = "MainActivity";

  private FloatingActionButton FabStart = null;
  private VitalSignsDsp VSDsp = null;
  private int Sbp = -1;
  private int Dbp = -1;
  private int HR = -1;

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
    showBioSignal(0, 0, 0);

    if(GlobalData.requestPermissionForAndroidM(this))
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
      if(GlobalData.requestPermissionForAndroidM(this))
      {
        Log.d(LOG_TAG, "scanBle @ onOptionsItemSelected()");
        scanBle();
      }
      return (true);
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

  private void showBioSignal(int nSbp, int nDbp, int nHR)
  {
    Sbp = nSbp;
    Dbp = nDbp;
    HR = nHR;

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

  private void stop()
  {
    /// [AT-PM] : Stop the measurement ; 10/24/2016
    if(VSDsp == null)
    {
      return;
    }
    VSDsp.Stop();

    GlobalData.Recording = false;

    FabStart.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.ic_media_play));
  }

  private View.OnClickListener onClickListenerFab = new View.OnClickListener()
  {
    @Override
    public void onClick(View view)
    {
      /// [AT-PM] : Check connected ; 06/16/2017
      if(!GlobalData.BleControl.isConnect())
      {
        Snackbar.make(view, "Blood pressure measurement -> STOPPED", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show();
        return;
      }

      /// [AT-PM] : Stop the recording ; 06/16/2017
      if(GlobalData.Recording)
      {
        stop();

        Snackbar.make(view, "Blood pressure measurement -> STOPPED", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show();
        return;
      }

      /// [AT-PM] : Start recording ; 06/16/2017
      if(start())
      {
        Snackbar.make(view, "Blood pressure measurement -> STARTED", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show();
      }
      else
      {
        Snackbar.make(view, "Blood pressure measurement -> FAILED", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show();
      }
    }
  };

  @Override
  public void onBleDeviceSelected(String bleDeviceAddress)
  {
    if(bleDeviceAddress == null)
    {
      /// [AT-PM] : Re-scan the BLE device ; 10/24/2016
      Log.d(LOG_TAG, "scanBle @ onBleDeviceSelected()");
      scanBle();
      return;
    }

    /// [AT-PM] : Connect BLE device ; 10/24/2016
    GlobalData.BleControl.connect(bleDeviceAddress);
  }

  private void scanBle()
  {
    if(GlobalData.requestPermissionForAndroidM(this))
    {
      DialogFragment fragment = new BleDeviceListDialog();
      fragment.show(getFragmentManager(), getResources().getString(R.string.device_list_fragment_tag));
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
  }

  @Override
  public void onUpdateResult(float fSbp, float fDbp, float fHR)
  {
    showBioSignal((int)fSbp, (int)fDbp, (int)fHR);
    Log.d(LOG_TAG, "SBP = " + Float.toString(fSbp) + ", DBP = " + Float.toString(fDbp) + ", HR = " + Float.toString(fHR));
  }

  @Override
  public void onInterrupt()
  {
    stop();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         @NonNull String[] permissions,
                                         @NonNull int[] grantResults)
  {
    switch (requestCode)
    {
      case PERMISSION_REQUEST_COARSE_LOCATION:
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
      case PERMISSION_REQUEST_EXTERNAL_STORAGE:
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
      case PERMISSION_REQUEST_READ_PHONE_STATE:
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
      stop();

      GlobalData.BleControl.disconnect();
    }

    @Override
    public void onConnect()
    {
      Log.d(LOG_TAG, "onConnect()");
    }
  };
}
