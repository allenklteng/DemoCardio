package com.vitalsigns.democardio;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by allen_teng on 24/10/2016.
 */

public class BleDeviceListDialog extends DialogFragment
{
  private static final long SCAN_PERIOD = 10000; //scanning for 10 seconds

  private BluetoothAdapter ObjBluetoothAdapter = null;
  private boolean BScanning = false;
  private List<BluetoothDevice> DeviceList = null;
  private BleDeviceListAdapter DeviceListAdapter = null;
  private ListView DeviceListView;
  private Map<String, Integer> MapDevRssiValues = null;
  private Button BtnCancel = null;
  private Handler BleScanHandler = null;
  private TextView TextEmptyList = null;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
    View view = getActivity().getLayoutInflater().inflate(R.layout.ble_device_list, null);

    BtnCancel = (Button)view.findViewById(R.id.ble_btn_cancel);
    BtnCancel.setOnClickListener(onBtnClick);
    TextEmptyList = (TextView)view.findViewById(R.id.ble_empty);
    DeviceListView = (ListView)view.findViewById(R.id.ble_new_devices);

    chkSupportBle();
    initBleAdapter();
    populateList();

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setView(view);
    Dialog dialog = builder.create();
    dialog.setCanceledOnTouchOutside(false);
    dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog);
    return dialog;
  }

  private void chkSupportBle()
  {
    /// Check whether device support BLE
    if(!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
      GlobalData.showToast(
        getActivity(),
        getActivity().getResources().getString(R.string.bt_ble_not_support),
        Toast.LENGTH_SHORT);
      dismiss();
      return;
    }
  }

  private void initBleAdapter()
  {
    /// Initializes a Bluetooth adapter
    final BluetoothManager bluetoothManager =
      (BluetoothManager)getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
    ObjBluetoothAdapter = bluetoothManager.getAdapter();

    /// Check is Bluetooth is supported on the device
    if(ObjBluetoothAdapter == null) {
      GlobalData.showToast(
        getActivity(),
        getActivity().getResources().getString(R.string.bt_bt_not_support),
        Toast.LENGTH_SHORT);
      dismiss();
      return;
    }
  }

  private View.OnClickListener onBtnClick = new View.OnClickListener()
  {
    @Override
    public void onClick(View v) {
      if(!BScanning) {
        scanBleDevice(true);
      } else {
        scanBleDevice(false);
        OnBleDeviceSelectedListener listener = (OnBleDeviceSelectedListener) getActivity();
        listener.onBleDeviceSelected(null);
        dismiss();
      }
    }
  };

  private void populateList()
  {
    /// Initialize device list container
    DeviceList = new ArrayList<BluetoothDevice>();
    MapDevRssiValues = new HashMap<String, Integer>();
    DeviceListAdapter = new BleDeviceListAdapter(getActivity(), DeviceList, MapDevRssiValues);

    DeviceListView.setAdapter(DeviceListAdapter);
    DeviceListView.setOnItemClickListener(mDeviceClickListener);

    BleScanHandler = new Handler();
    scanBleDevice(true);
  }

  private void scanBleDevice(final boolean enable)
  {
    if(enable)
    {
      /// Stops scanning after a pre-defined scan period
      BleScanHandler.postDelayed(new Runnable() {
        @Override
        public void run() {
          BScanning = false;
          ObjBluetoothAdapter.stopLeScan(bleScanCallback);
          BtnCancel.setText(R.string.scan);
        }
      }, SCAN_PERIOD);
      BScanning = true;
      ObjBluetoothAdapter.startLeScan(bleScanCallback);
      BtnCancel.setText(R.string.cancel);
    }
    else
    {
      BScanning = false;
      ObjBluetoothAdapter.stopLeScan(bleScanCallback);
      BtnCancel.setText(R.string.cancel);
    }
  }

  private BluetoothAdapter.LeScanCallback bleScanCallback = new BluetoothAdapter.LeScanCallback()
  {
    @Override
    public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
      addDevice(device, rssi);
    }
  };

  private void addDevice(BluetoothDevice device, int rssi)
  {
    boolean deviceFound = false;

    for(BluetoothDevice listDev : DeviceList)
    {
      if(listDev.getAddress().equals(device.getAddress()))
      {
        deviceFound = true;
        break;
      }
    }

    MapDevRssiValues.put(device.getAddress(), rssi);
    if(!deviceFound){
      DeviceList.add(device);
      TextEmptyList.setVisibility(View.GONE);
      DeviceListAdapter.notifyDataSetChanged();
    }
  }

  private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener()
  {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      ObjBluetoothAdapter.stopLeScan(bleScanCallback);
      GlobalData.BleDevice = DeviceList.get(position);
      GlobalData.showToast(
        getActivity(),
        "Connecting to " +
        DeviceList.get(position).getName()+"\n Please, wait BLE connected",
        Toast.LENGTH_SHORT);
      GlobalData.BleControl.SetDeviceAddress(DeviceList.get(position).getAddress());
      OnBleDeviceSelectedListener listener = (OnBleDeviceSelectedListener)getActivity();
      listener.onBleDeviceSelected(DeviceList.get(position).getAddress());
      dismiss();
    }
  };

  public interface OnBleDeviceSelectedListener
  {
    public void onBleDeviceSelected(String bleDeviceAddress);
  }
}
