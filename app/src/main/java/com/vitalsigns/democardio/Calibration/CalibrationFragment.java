package com.vitalsigns.democardio.Calibration;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.vitalsigns.democardio.GlobalData;
import com.vitalsigns.democardio.R;

/**
 * Created by coge on 2018/5/15.
 */

public class CalibrationFragment extends DialogFragment
{
  private final String LOG_TAG = "CalibrationFragment:";
  private Button btnOk;
  private EditText caliSBPText;
  private EditText caliDBPText;
  private EditText caliHRText;
  private boolean bSBPValueOK = false;
  private boolean bDBPValueOK = false;
  private boolean bHRValueOK  = false;
  private OnCalibrationFragmentListener listener;
  private RadioGroup selectRegionRadioGroup;
  private RadioButton radioBtnChina;
  private RadioButton radioBtnOthers;

  public interface OnCalibrationFragmentListener {
    void onCalibration(int nCaliSbp, int nCaliDbp, int nCaliHr);
  }

  public void setCallback(OnCalibrationFragmentListener callback)
  {
    listener = callback;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
    View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_calibration, null);
    processViews(view);
    processControls();

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setView(view);
    Dialog dialog = builder.create();
    dialog.setCanceledOnTouchOutside(false);
    dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog);
    return (dialog);
  }

  private void processViews(View view)
  {
    btnOk = (Button) view.findViewById(R.id.calibration_ok);
    caliSBPText = (EditText) view.findViewById(R.id.cali_sbp_edit_text);
    caliDBPText = (EditText) view.findViewById(R.id.cali_dbp_edit_text);
    caliHRText = (EditText) view.findViewById(R.id.cali_hr_edit_text);
    selectRegionRadioGroup = (RadioGroup) view.findViewById(R.id.select_region_radio_btn_group);
    radioBtnChina          = (RadioButton) view.findViewById(R.id.radio_btn_china);
    radioBtnOthers         = (RadioButton) view.findViewById(R.id.radio_btn_others);

    if(GlobalData.bSelectChina == true)
    {
      radioBtnChina.setChecked(true);
    }
    else
    {
      radioBtnOthers.setChecked(true);
    }
  }

  private void processControls()
  {
    caliSBPText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        if(!s.toString().equals("")) {
          bSBPValueOK = checkValid(s.toString());
        }
      }
    });
    caliDBPText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        if(!s.toString().equals("")) {
          bDBPValueOK = checkValid(s.toString());
        }
      }
    });
    caliHRText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        if(!s.toString().equals("")) {
          bHRValueOK = checkValid(s.toString());
        }
      }
    });

    btnOk.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v)
      {
        if((bSBPValueOK == true) && (bDBPValueOK == true) && (bHRValueOK == true))
        {
          listener.onCalibration(Integer.parseInt(caliSBPText.getText().toString()),
                                 Integer.parseInt(caliDBPText.getText().toString()),
                                 Integer.parseInt(caliHRText.getText().toString()));
          dismiss();
        }
        else
        {
          if(getActivity() != null)
          {
            Toast.makeText(getActivity(), "Please input valid value", Toast.LENGTH_LONG).show();
          }
        }
      }
    });

    selectRegionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId)
      {
        /// [CC] : CheckedId is the RadioButton selected ; 05/16/2018
        switch (checkedId)
        {
          case R.id.radio_btn_china:
            GlobalData.bSelectChina = true;
            break;
          case R.id.radio_btn_others:
            GlobalData.bSelectChina = false;
            radioBtnOthers.setChecked(true);
            break;
        }
      }
    });
  }

  private boolean checkValid(String strValue)
  {
    try {
      Integer.parseInt(strValue);
      return (true);
    } catch(Exception ex) {
      return (false);
    }
  }
}
