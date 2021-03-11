package com.sayedidrees.blplugin;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.unity3d.player.UnityPlayer;

public class responder {
    private final String TAG= "hacker";
    public void onDeviceConnected(String DeviceName)
    {
        Log.d(TAG, "Responder : Ondevice Conneceted : "+ DeviceName);
        UnityPlayer.UnitySendMessage("BluetoothController","deviceConnected",DeviceName);
    }

    public void onDataReceive(String data)
    {
        Log.d(TAG, "Responder : Ondevice DATA RECEIVE : "+ data);
        UnityPlayer.UnitySendMessage("BluetoothController","dataReceive",data);
    }

    public void onDeviceFound(BluetoothDevice device)
    {
        Log.d(TAG, "Responder : Ondevice FOUND : "+ device.getName());
        UnityPlayer.UnitySendMessage("BluetoothController","deviceFound",device.getName());
    }

    public void onDevice_setStatus(String status)
    {
        Log.d(TAG, "Responder : Ondevice SET STATUS : "+ status);
        UnityPlayer.UnitySendMessage("BluetoothController","setStatus",status);
    }

    public void onCheck_BluetoothStatus(boolean isEnabled)
    {
        Log.d(TAG, "Responder : Device Status Bluetooth : "+ isEnabled);
        UnityPlayer.UnitySendMessage("BluetoothController","bluetoothStatus",isEnabled?"true":"false");
    }

}


class CustomBluetoothDevice
{
    public String DeviceName;
    public String DeviceAddress;
}
