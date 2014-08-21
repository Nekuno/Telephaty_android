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

import org.spongycastle.asn1.eac.PublicKeyDataObject;
import org.spongycastle.crypto.util.PublicKeyFactory;

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
	private final BluetoothSocket mSocket;
	private final InputStream mInStream;
	private final OutputStream mOutStream;
	private BluetoothDevice mRemoteDevice;
	private ECDH ecdh;
	private byte[] sharedKey;

	public ConnectedThread(Bluetooth service, BluetoothSocket socket) {
		Log.d(Utilities.TAG, "create ConnectedThread.");
		mService = service;
		mSocket = socket;
		InputStream tmpIn = null;
		OutputStream tmpOut = null;
		mRemoteDevice = mSocket.getRemoteDevice();

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
		byte[] buffer = new byte[1024];
		int bytes;
		
		Boolean setECDH = false;
		
		try {
			ecdh = new ECDH();
			PublicKey pubKey = ecdh.getPubKey();
			
			PrivateKey privKey = ecdh.getPrivKey();
			ObjectOutputStream oos = new ObjectOutputStream(mSocket.getOutputStream());
			//enviamos al servidor nuestra clave pública
			oos.writeObject(pubKey);

		} catch (InvalidAlgorithmParameterException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchProviderException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidKeySpecException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
					// Send the obtained bytes to the UI Activity
					mService.getHandler().obtainMessage(Utilities.MESSAGE_READ, sharedKey.length, -1,
							sharedKey).sendToTarget();
					setECDH = true;
					
				}
				
			} catch (IOException e) {
				Log.e(Utilities.TAG, "disconnected", e);
				mService.connectionLost();
				// Start the service over to restart listening mode
				mService.start();
				break;
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchProviderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
		// Keep listening to the InputStream while connected
		while (true) {
			try {
				// Read from the InputStream
				bytes = mInStream.read(buffer);
				byte[] byte_pad = Support.crypt_decrypt(sharedKey, sharedKey, buffer);
				byte[] original_byte = Support.delete_padding(byte_pad);
				byte[] original_data = (byte[]) Support.deserialize(original_byte);
				
				// Send the obtained bytes to the UI Activity
				mService.getHandler().obtainMessage(Utilities.MESSAGE_READ, original_data.length, -1,
						original_data).sendToTarget();
			} catch (IOException e) {
				Log.e(Utilities.TAG, "disconnected", e);
				mService.connectionLost();
				// Start the service over to restart listening mode
      			mService.start();
				break;
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Write to the connected OutStream.
	 * 
	 * @param buffer
	 *            The bytes to write
	 */
	public void write(byte[] buffer) {
		try {
			final byte data_tmp[] = Support.serialize(buffer);
			byte[] crypt_data = Support.crypt_decrypt(sharedKey, sharedKey, Support.padding(data_tmp));
			mOutStream.write(crypt_data);

			// Share the sent message back to the UI Activity
			mService.getHandler().obtainMessage(Utilities.MESSAGE_WRITE, -1, -1, crypt_data)
					.sendToTarget();
		} catch (IOException e) {
			Log.e(Utilities.TAG, "Exception during write", e);
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