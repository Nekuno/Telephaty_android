package com.qnoow.telephaty;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.qnoow.telephaty.Bluetooth.Bluetooth;


public class MainActivity extends ActionBarActivity {

    private ArrayAdapter mArrayAdapter;
    private Bluetooth bluetooth = new Bluetooth();;
    private ListView listDevicesFound;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
                
        
        if (!bluetooth.isSupported())
        	Toast.makeText(this, "bluetooth no soportado", Toast.LENGTH_SHORT).show();
        bluetooth.setEnable(this);
        listDevicesFound = (ListView)findViewById(R.id.devicesfound);
        mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listDevicesFound.setAdapter(mArrayAdapter);
    	bluetooth.registerBroadcastReceiver(getApplicationContext(), bluetooth.setBroadcastReceiver(getApplicationContext(),mArrayAdapter));



	
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
    	mArrayAdapter.clear();
    	BluetoothAdapter.getDefaultAdapter().startDiscovery();

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
