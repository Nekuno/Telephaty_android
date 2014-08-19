package com.qnoow.telephaty.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class support {


	
	public static byte[] serialize(Object obj) throws IOException {
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ObjectOutputStream os = new ObjectOutputStream(out);
	    os.writeObject(obj);
	    return out.toByteArray();
	}
	public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
	    ByteArrayInputStream in = new ByteArrayInputStream(data);
	    ObjectInputStream is = new ObjectInputStream(in);
	    return is.readObject();
	}
	

	public static byte[] crypt_decrypt(byte[] sharedKey,byte[] iv,byte[] msg){
		Snow3G snow3g = new Snow3G();
		int k[] = toIntArray(sharedKey);
		int IniVec[] = toIntArray(iv);
		long[] Key = int2long(k);
		long[] IV = int2long(IniVec);
		int msga[] = toIntArray(msg);
		long[] msgint = int2long(msga);
		long ks[];
		ks = new long[msg.length / 4];
		snow3g.Initialize(Key, IV);
		snow3g.GenerateKeystream(msg.length / 4, ks);
		int cipher[] = xor(msgint, ks);
		byte result[] = int2byte(cipher);
		return result;
	}
	

	public static byte[] padding(byte[] msg){
		int flag_m = 0;
		while ((msg.length % 4) != 3){
			byte [] c= new byte[msg.length+1];
			for(int i=0;i<msg.length;i++) 
				c[i]=msg[i];
			c[msg.length]=0;
			msg = c;
			flag_m ++;
		}
			byte [] c= new byte[msg.length+1];
			for(int i=0;i<msg.length;i++) 
				c[i]=msg[i];
			c[msg.length]=(byte) flag_m;
			return c;
			
	}
	
	public static byte[] delete_padding(byte[] msg){
		int flag_m = msg[msg.length-1]+1;
		byte [] original_byte= new byte[msg.length-flag_m];
		for(int i=0;i<original_byte.length;i++) 
			original_byte[i]=msg[i];
		return original_byte;
			
	}
	
		
	
	
	public static long[] int2long(int[] cipher) {
		long result[] = new long[cipher.length];
		for (int i = 0; i < cipher.length; i++) {
			result[i] = (long) cipher[i];
		}
		return result;
	}









	public static int[] xor(long[] msgar, long[] ks) {
		int[] cipher = new int[ks.length];;
		for (int i = 0; i < msgar.length; i++) {
			cipher[i]= (int) (ks[i] ^ msgar[i]);
		}
		return cipher;
	}



	public static String asciiToHex(String ascii){
        StringBuilder hex = new StringBuilder();
        
        for (int i=0; i < ascii.length(); i++) {
            hex.append(Integer.toHexString(ascii.charAt(i)));
        }       
        return hex.toString();
    } 
	
	
	
	public static long[] String2Array(String key,int tam){
		long k[] = new long[tam];
		String clave = asciiToHex(key);
		String cl[] = new String[tam];
		int j = 0;
		for (int i = 0; i < clave.length();i=i+8){
			
			int end = 0;
			if (i+8 < clave.length())
				end = i+8;
			else
				end = clave.length();
			cl[j] = clave.substring(i, end);
			cl[j] = "0x" + cl[j];

			//System.out.println(cl[j]);
			long n = Long.decode(cl[j]);
			k[j] = n;
			//System.out.println(j);
			j++;
		}
		return k;
	}
	
	
	
	public static int[] toIntArray(byte[] barr) { 
        //Pad the size to multiple of 4 
        int size = (barr.length / 4) + ((barr.length % 4 == 0) ? 0 : 1);      

        ByteBuffer bb = ByteBuffer.allocate(size *4); 
        bb.put(barr);

        //Java uses Big Endian. Network program uses Little Endian. 
        bb.order(ByteOrder.LITTLE_ENDIAN); 
        bb.rewind(); 
        IntBuffer ib =  bb.asIntBuffer();         
        int [] result = new int [size]; 
        ib.get(result); 

        return result; 
}
	
	
	
	public static byte[] int2byte(int[]src) {
	    int srcLength = src.length;
	    byte[]dst = new byte[srcLength << 2];
	    
	    for (int i=0; i<srcLength; i++) {
	        int x = src[i];
	        int j = i << 2;
	        dst[j++] = (byte) ((x >>> 0) & 0xff);           
	        dst[j++] = (byte) ((x >>> 8) & 0xff);
	        dst[j++] = (byte) ((x >>> 16) & 0xff);
	        dst[j++] = (byte) ((x >>> 24) & 0xff);
	    }
	    return dst;
	}
	
	
	
}
