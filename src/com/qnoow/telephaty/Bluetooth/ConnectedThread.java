package com.qnoow.telephaty.Bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Random;

import org.spongycastle.asn1.eac.PublicKeyDataObject;
import org.spongycastle.crypto.util.PublicKeyFactory;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.qnoow.telephaty.security.ECDH;
import com.qnoow.telephaty.security.Support;

/**
 * This thread runs during a connection with a remote device.
 * It handles all incoming and outgoing transmissions.
 */
public class ConnectedThread extends Thread {

	// Member fields
	private final Bluetooth mService;
	private BluetoothSocket mSocket;
	private final InputStream mInStream;
	private final OutputStream mOutStream;
	private BluetoothDevice mRemoteDevice;
	private ECDH ecdh;
	private byte[] sharedKey;
	private boolean mWait;
	
	public ConnectedThread(Bluetooth service, BluetoothSocket socket, boolean wait) {
		Log.d(Utilities.TAG, "create ConnectedThread.");
		mService = service;
		mSocket = socket;
		InputStream tmpIn = null;
		OutputStream tmpOut = null;
		mRemoteDevice = mSocket.getRemoteDevice();
		mWait = wait;
		
		// Get the BluetoothSocket input and output streams
		try {
			tmpIn = socket.getInputStream();
			tmpOut = socket.getOutputStream();
		} catch (IOException e) {
			Log.e(Utilities.TAG, "temp sockets not created", e);
		}

		mInStream = tmpIn;
		mOutStream = tmpOut;
	}

	public void run() {
		Log.i(Utilities.TAG, "BEGIN mConnectedThread");
		byte[] buffer = null;
		int bytes;
		
		Boolean setECDH = false;
		
		try {
			ecdh = new ECDH();
			PublicKey pubKey = ecdh.getPubKey();
			
			PrivateKey privKey = ecdh.getPrivKey();
			ObjectOutputStream oos = new ObjectOutputStream(mSocket.getOutputStream());
			//enviamos al servidor nuestra clave p�blica
			oos.writeObject(pubKey);

		} catch (InvalidAlgorithmParameterException e1) {
			e1.printStackTrace();
		} catch (NoSuchProviderException e1) {
			e1.printStackTrace();
		} catch (InvalidKeySpecException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		// get the public key of the other part and calculate the shared key
		while (!setECDH) {
			try {
				// Read from the InputStream
				if (mSocket.getInputStream() != null) {
					//en escucha para recibir un mensaje
					ObjectInputStream ois = new ObjectInputStream(mSocket.getInputStream());
					Object line = ois.readObject();
					PublicKey pubk = (PublicKey) line;
					//generamos la clave compartida
					ecdh.setSharedKey(ecdh.Generate_Shared(pubk));
					sharedKey = ecdh.getSharedKey();
					Utilities.sharedKey = sharedKey;
					// Send the obtained bytes to the UI Activity
					mService.getHandler().obtainMessage(Utilities.SHARED_KEY, sharedKey.length, -1,
							sharedKey).sendToTarget();
					mService.setState(Utilities.STATE_CONNECTED_ECDH_FINISH);
					setECDH = true;
					
				}
				
			} catch (IOException e) {
				Log.e(Utilities.TAG, "disconnected", e);
				mService.connectionLost();
				// Start the service over to restart listening mode
				mService.start();
				break;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (NoSuchProviderException e) {
				e.printStackTrace();
			}
		}
		

		if (mWait){

			// Keep listening to the InputStream while connected
			while (true) {
				try {
					// Read from the InputStream
					if (mSocket.getInputStream() != null) {
						//en escucha para recibir un mensaje
						ObjectInputStream ois = new ObjectInputStream(
								mSocket.getInputStream());
						Object line = ois.readObject();
						byte[] decryptedData = Support.decrypt(sharedKey,
								(byte[]) line);

						String receivedMsg = new String(decryptedData, "UTF-8");
						String msgId = receivedMsg.substring(1, 15);
						byte[] originalMsg = receivedMsg.substring(15).getBytes();

						// Send the obtained bytes to the UI Activity
						mService.getHandler()
						.obtainMessage(Utilities.MESSAGE_READ,
								originalMsg.length, -1, originalMsg)
								.sendToTarget();
						if (receivedMsg.substring(0, 1).equals("1")) {
						
							if (Utilities.BBDDmensajes.insert(msgId, mSocket.getRemoteDevice().toString())){
//								TODO Reenviar
							}
							mService.stop();
							mService.start();
						}

					}

				} catch (IOException e) {
					Log.e(Utilities.TAG, "disconnected", e);
					mService.connectionLost();
					// Start the service over to restart listening mode
					mService.start();
					break;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Write to the connected OutStream.
	 * 
	 * @param buffer
	 *            The bytes to write
	 * @param diffusion
	 * 			  True = difussion message 
	 */ 
	public void write(byte[] buffer, boolean diffusion) {
		try {
			String msg =  new String(buffer, "UTF-8");;
			if (diffusion == true){
				// currentDateTimeString is the id of the message
				String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
				currentDateTimeString = currentDateTimeString.replaceAll("/", "").replaceAll(":", "").replaceAll(" ", "");
				msg = "1".concat(currentDateTimeString).concat(msg);  
				Utilities.BBDDmensajes.insert(currentDateTimeString, BluetoothAdapter.getDefaultAdapter().getAddress());
			}
			else{
				msg = "0".concat(msg);
			}
			byte[] encryptedData = Support.encrypt(sharedKey, msg.getBytes());
			
			ObjectOutputStream oos = new ObjectOutputStream(mSocket.getOutputStream());
			//enviamos al servidor nuestra clave p�blica
			oos.writeObject(encryptedData);

			// Share the sent message back to the UI Activity
			mService.getHandler().obtainMessage(Utilities.MESSAGE_WRITE, -1, -1, encryptedData)
					.sendToTarget();
			if (diffusion == true && (mSocket.getInputStream() != null) ){
				ObjectInputStream ois = new ObjectInputStream(
						mSocket.getInputStream());
				
			}

		} catch (IOException e) {
			Log.e(Utilities.TAG, "disconnected", e);
			mService.connectionLost();
			// Start the service over to restart listening mode
			mService.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void cancel() {
		try {
			mSocket.close();
		} catch (IOException e) {
			Log.e(Utilities.TAG, "close() of connect socket failed", e);
		}
	}

	public BluetoothDevice getRemoteDevice() {
		return mRemoteDevice;
	}
}