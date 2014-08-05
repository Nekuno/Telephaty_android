package com.qnoow.telephaty;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.qnoow.telephaty.Bluetooth.Bluetooth;


public class MainActivity extends ActionBarActivity {

    private ArrayAdapter mArrayAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private Bluetooth bluetooth = new Bluetooth();;
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
                
        
        if (!bluetooth.isSupported())
        	Toast.makeText(this, "bluetooth no soportado", Toast.LENGTH_SHORT).show();
        bluetooth.setEnable(this);
        
        BroadcastReceiver mReceiver = bluetooth.setBroadcastReceiver(mBluetoothAdapter, mArrayAdapter);
    	bluetooth.registerBroadcastReceiver(getApplicationContext(), mReceiver);
    	


	
	 }
	
	
//	class MyClickListener implements OnClickListener {
//
//	    private Bluetooth bluetooth;
//	    private BluetoothAdapter mBluetoothAdapter;
//
//	    public MyClickListener(Bluetooth bluetooth,BluetoothAdapter mBluetoothAdapter) {
//	       this.bluetooth = bluetooth;
//	       this.mBluetoothAdapter = mBluetoothAdapter;
//	    }
//
//	    public void onClick(View view){
//	    	BroadcastReceiver mReceiver = bluetooth.setBroadcastReceiver(mBluetoothAdapter, mArrayAdapter);
//	    	bluetooth.registerBroadcastReceiver(getApplicationContext(), mReceiver);
//	    }
//
//	}
//    
    public void scan(View view){
    	Toast.makeText(this, "pulsado scan", Toast.LENGTH_SHORT).show();
    	mBluetoothAdapter.startDiscovery();
    }


    public void paired(View view){
    	bluetooth.getPairedDevices(this,"Dispositivos emparejados anteriormente"); 
    }

    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
