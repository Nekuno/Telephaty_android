package com.qnoow.telephaty.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.KeyAgreement;

public class ecdh {
	
	private PublicKey pubKey;
	private PrivateKey privKey;
	byte[] sharedKey;
	
	static {
		Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
	}
	
	
	/* Parámetros de la curva secp128r1
	* 
	*p = FFFFFFFD FFFFFFFF FFFFFFFF FFFFFFFF = 2^128 - 2^97 - 1
	*a = FFFFFFFD FFFFFFFF FFFFFFFF FFFFFFFC
	*b = E87579C1 1079F43D D824993C 2CEE5ED3
	*S = 03 161FF752 8B899B2D 0C28607C A52C5B86
	*G in comppressed form = 03 161FF752 8B899B2D 0C28607C A52C5B86
	*G in uncompressed form = 04 161FF752 8B899B2D 0C28607C A52C5B86 
	*   CF5AC839 5BAFEB13 C02DA292 DDED7A83
	*n = FFFFFFFE 00000000 75A30D1B 9038A115
	*h = 1
	*/
	
	
	public ecdh() throws InvalidAlgorithmParameterException, NoSuchProviderException, InvalidKeySpecException {
		
		/* Generamos una curva según los parámetros de la curva  secp128r1 */
		ECGenParameterSpec ecParamSpec = new ECGenParameterSpec("secp128r1");
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDH", "SC");
			kpg.initialize(ecParamSpec);
			KeyPair kp = kpg.generateKeyPair();
			KeyFactory kf = KeyFactory.getInstance("ECDH", "SC");
			/* Generamos la clave pública*/ 
			pubKey = kf.generatePublic(new X509EncodedKeySpec(kp.getPublic().getEncoded()));	
			/* Generamos la clave privada*/ 
			privKey = kf.generatePrivate(new PKCS8EncodedKeySpec(kp.getPrivate().getEncoded()));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	
	public PublicKey getPubKey() {
		return pubKey;
	}



	public void setPubKey(PublicKey pubKey) {
		this.pubKey = pubKey;
	}



	public PrivateKey getPrivKey() {
		return privKey;
	}



	public void setPrivKey(PrivateKey privKey) {
		this.privKey = privKey;
	}



	public byte[] getSharedKey() {
		return sharedKey;
	}



	public void setSharedKey(byte[] sharedKey) {
		this.sharedKey = sharedKey;
	}


	/* Función encargada de generar la clave compartida entre 2 usuarios
	 * input: Public Key
	 * output: Shared key
	 * */
	public byte[] Generate_Shared(PublicKey PubK) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException{
		KeyAgreement KA = KeyAgreement.getInstance("ECDH", "SC");
		KA.init(privKey);
		KA.doPhase(PubK, true);
		return KA.generateSecret();
	}

	


}
