package com.vitalsigns.democardio;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by AllenTeng on 7/20/2017.
 */

public class SaveWaveform
{
  private static final String LOG_TAG = "SaveWaveform";
  private static final float UPDATE_WINDOW = 1000f;

  /**
   * Save the waveform
   * @param dsp VitalSignsDsp object
   * @param filename filename to be saved
   */
  public void save(VitalSignsDsp dsp, String filename)
  {
    /// [AT-PM] : Write header ; 07/20/2017
    writeHeader(filename);

    /// [AT-PM] : Get total record time ; 07/20/2017
    float fEndTime = dsp.getEndTime();

    /// [AT-PM] : Get the view every second and save to file ; 07/20/2017
    float fStartTime = 0;
    while(fStartTime < fEndTime)
    {
      /// [AT-PM] : Update view ; 07/20/2017
      int cnt = dsp.updateView(fStartTime, fStartTime + UPDATE_WINDOW, VitalSignsDsp.CODE_TYPE.STANDARD);

      /// [AT-PM] : Get waveform ; 07/20/2017
      ArrayList<float []> arrayEcg = new ArrayList<>(cnt);
      ArrayList<float []> arrayPpg = new ArrayList<>(cnt);
      int idx = 0;
      while(idx < cnt)
      {
        arrayEcg.add(dsp.getEcg(idx));
        arrayPpg.add(dsp.getPpg(idx));
        idx ++;
      }

      /// [AT-PM] : Save to file ; 07/20/2017
      writeData(filename, arrayEcg, arrayPpg);
      Log.d(LOG_TAG, String.format("Write %.3f ~ %.3f to file", fStartTime, fStartTime + UPDATE_WINDOW));

      /// [AT-PM] : Next window ; 07/20/2017
      fStartTime = fStartTime + UPDATE_WINDOW;
    }
  }

  /**
   * Write file header
   * @param filename filename to be saved
   */
  private void writeHeader(String filename)
  {
    String logFilename = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + filename;
    Log.d(LOG_TAG, "Save file to " + logFilename);
    try
    {
      FileWriter fileWriter = new FileWriter(logFilename, true);
      BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
      bufferedWriter.write("ECG_X, ECG_Y, ECG_PEAK, PPG_X, PPG_Y, PPG_PEAK");
      bufferedWriter.newLine();
      bufferedWriter.close();
      fileWriter.close();
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Write data to file
   * @param filename filename to be saved
   * @param ecgs ECG data array list
   * @param ppgs PPG data array list
   */
  private void writeData(String filename, ArrayList<float []> ecgs, ArrayList<float []> ppgs)
  {
    String logFilename = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + filename;

    try
    {
      FileWriter fileWriter = new FileWriter(logFilename, true);
      BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

      int idx = 0;
      while(idx < Math.min(ecgs.size(), ppgs.size()))
      {
        float [] ecg = (idx < ecgs.size()) ? ecgs.get(idx) : null;
        float [] ppg = (idx < ppgs.size()) ? ppgs.get(idx) : null;
        bufferedWriter.write(String.format("%.3f, %.3f, %.0f, %.3f, %.3f, %.0f",
                                           (ecg == null) ? 0f : ecg[0],
                                           (ecg == null) ? 0f : ecg[1],
                                           (ecg == null) ? 0f : ecg[2],
                                           (ppg == null) ? 0f : ppg[0],
                                           (ppg == null) ? 0f : ppg[1],
                                           (ppg == null) ? 0f : ppg[2]));
        bufferedWriter.newLine();
        idx ++;
      }

      bufferedWriter.close();
      fileWriter.close();
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
  }
}
