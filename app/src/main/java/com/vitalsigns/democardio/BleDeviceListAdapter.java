package com.vitalsigns.democardio;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Created by allen_teng on 24/10/2016.
 */

public class BleDeviceListAdapter extends BaseAdapter
{
  List<BluetoothDevice> BleDeviceList = null;
  Map<String, Integer> MapRssiValue = null;
  LayoutInflater Inflater = null;

  public BleDeviceListAdapter(Context context, List<BluetoothDevice> devices, Map<String, Integer> rssiValue)
  {
    Inflater = LayoutInflater.from(context);
    BleDeviceList = devices;
    MapRssiValue = rssiValue;
  }

  @Override
  public int getCount()
  {
    return (BleDeviceList.size());
  }

  @Override
  public Object getItem(int i)
  {
    return (BleDeviceList.get(i));
  }

  @Override
  public long getItemId(int i)
  {
    return (i);
  }

  @Override
  public View getView(int i, View view, ViewGroup viewGroup)
  {
    ViewGroup vg;
    BluetoothDevice device;
    TextView tvadd;
    TextView tvname;
    TextView tvpaired;
    TextView tvrssi;

    device = BleDeviceList.get(i);

    vg = (view == null) ? (ViewGroup)Inflater.inflate(R.layout.device_list_adapter, null) : (ViewGroup)view;
    tvadd = ((TextView) vg.findViewById(R.id.address));
    tvname = ((TextView) vg.findViewById(R.id.name));
    tvpaired = (TextView) vg.findViewById(R.id.paired);
    tvrssi = (TextView) vg.findViewById(R.id.rssi);

    tvname.setText(device.getName());
    tvadd.setText(device.getAddress());
    tvrssi.setText("Rssi = " + String.valueOf(MapRssiValue.get(device.getAddress()).intValue()));
    tvrssi.setVisibility(View.VISIBLE);
    if(device.getBondState() == BluetoothDevice.BOND_BONDED)
    {
      tvname.setTextColor(Color.WHITE);
      tvadd.setTextColor(Color.WHITE);
      tvpaired.setTextColor(Color.GRAY);
      tvpaired.setVisibility(View.VISIBLE);
      tvpaired.setText(R.string.paired);
      tvrssi.setVisibility(View.VISIBLE);
      tvrssi.setTextColor(Color.WHITE);
    }
    else
    {
      tvname.setTextColor(Color.WHITE);
      tvadd.setTextColor(Color.WHITE);
      tvpaired.setVisibility(View.GONE);
      tvrssi.setVisibility(View.VISIBLE);
      tvrssi.setTextColor(Color.WHITE);
    }
    return vg;
  }
}
