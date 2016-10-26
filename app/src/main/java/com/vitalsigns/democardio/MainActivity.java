package com.vitalsigns.democardio;

import android.app.DialogFragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.vitalsigns.sdk.ble.BleControl;
import com.vitalsigns.sdk.ble.BleEventListener;

import java.util.Locale;

import static com.vitalsigns.democardio.GlobalData.LOG_TAG;

public class MainActivity extends AppCompatActivity
  implements BleDeviceListDialog.OnBleDeviceSelectedListener,
             BleEventListener,
             VitalSignsDsp.OnUpdateResult
{
  private final int QUERY_BLE_TIMER = 7000;

  private FloatingActionButton FabStart = null;
  private Handler QueryBleAlive = null;
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

    initBle();
    scanBle();

    VSDsp = new VitalSignsDsp(MainActivity.this);
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
    if(id == R.id.action_settings)
    {
      return true;
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
      return (false);
    }
    if(!VSDsp.Start())
    {
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
      if(GlobalData.Recording)
      {
        stop();

        Snackbar.make(view, "Blood pressure measurement -> STOPPED", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show();
      }
      else
      {
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
    }
  };

  @Override
  public void onBleDeviceSelected(String bleDeviceAddress)
  {
    if(bleDeviceAddress == null)
    {
      /// [AT-PM] : Re-scan the BLE device ; 10/24/2016
      scanBle();
      return;
    }

    /// [AT-PM] : Connect BLE device ; 10/24/2016
    GlobalData.BleControl.Connect(bleDeviceAddress);

    /// [AT-PM] : Start a handler to check BLE device connection ; 10/24/2016
    QueryBleAlive.removeCallbacksAndMessages(null);
    QueryBleAlive.postDelayed(queryBleAliveRunnable, QUERY_BLE_TIMER);
  }

  private void scanBle()
  {
    DialogFragment fragment = new BleDeviceListDialog();
    fragment.show(getFragmentManager(), getResources().getString(R.string.device_list_fragment_tag));
  }

  private void initBle()
  {
    GlobalData.BleControl = new BleControl(MainActivity.this, MainActivity.this, BleControl.APP_TYPE_CARDIO);

    if(!GlobalData.BleControl.Initialize(MainActivity.this))
    {
      GlobalData.showToast(MainActivity.this,
                           getResources().getString(R.string.bt_not_available),
                           Toast.LENGTH_LONG);
      return;
    }

    QueryBleAlive = new Handler();
  }

  @Override
  protected void onResume()
  {
    super.onResume();

    if(GlobalData.BleControl.Resume())
    {
      QueryBleAlive.postDelayed(queryBleAliveRunnable, QUERY_BLE_TIMER);
    }
  }

  @Override
  protected void onPause()
  {
    super.onPause();

    if(GlobalData.BleControl.Pause())
    {
      QueryBleAlive.removeCallbacksAndMessages(null);
    }
  }

  @Override
  protected void onDestroy()
  {
    super.onDestroy();

    if(GlobalData.BleControl != null)
    {
      GlobalData.BleControl.Destroy();
    }
  }

  final Runnable queryBleAliveRunnable = new Runnable()
  {
    public void run()
    {
      if(!GlobalData.BleControl.isBleDeviceAlive())
      {
        GlobalData.showToast(MainActivity.this,
                             getResources().getString(R.string.bt_connect_device_again),
                             Toast.LENGTH_SHORT);
      }
      else
      {
        GlobalData.showToast(MainActivity.this,
                             getResources().getString(R.string.bt_connect_device_success),
                             Toast.LENGTH_SHORT);
      }
    }
  };

  @Override
  public void onDisconnect(boolean b)
  {
    stop();

    /// [AT-PM] : Scan the BLE device ; 10/24/2016
    scanBle();
  }

  @Override
  public void onConnect()
  {
  }

  @Override
  public void onDataReceived(float[] floats)
  {
    /// [AT-PM] : Data received ; 10/24/2016
    if(VSDsp != null)
    {
      VSDsp.SetBleData(floats);
    }
  }

  @Override
  public void onBleFirmwareUpdate()
  {
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
}
