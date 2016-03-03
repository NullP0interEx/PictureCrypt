package me.kobosil.picturecrypt.tools;

import android.graphics.Bitmap;
import android.provider.Settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import me.kobosil.picturecrypt.MainActivity;

/**
 * Created by roman on 29.02.2016.
 */
public class NewFileEncryption {

    public static byte[] keyBytes = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
            0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17 };
    public static byte[] ivBytes = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x00, 0x01, 0x02, 0x03, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x01 };

    public static void encrypt(File in, File out,  byte[] keyBytes,  byte[] ivBytes) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, NoSuchProviderException, InvalidAlgorithmParameterException {
        (new File(MainActivity.getMainActivity().getFilesDir() + "/.crypted/")).mkdirs();

        FileInputStream fis = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

        CipherOutputStream cos = new CipherOutputStream(fos, cipher);
        int b;
        byte[] d = new byte[8];
        while((b = fis.read(d)) != -1) {
            cos.write(d, 0, b);
        }
        cos.flush();
        cos.close();
        fis.close();
    }

    public static void decrypt(File in, File out,  byte[] keyBytes,  byte[] ivBytes) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, NoSuchProviderException, InvalidAlgorithmParameterException {
        FileInputStream fis = new FileInputStream(in);

        (new File(MainActivity.getMainActivity().getFilesDir() + "/.crypted/")).mkdirs();
        FileOutputStream fos = new FileOutputStream(out);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        CipherInputStream cis = new CipherInputStream(fis, cipher);
        int b;
        byte[] d = new byte[8];
        while((b = cis.read(d)) != -1) {
            fos.write(d, 0, b);
        }
        fos.flush();
        fos.close();
        cis.close();
    }

    public static CipherInputStream decryptStream(File in,  byte[] keyBytes,  byte[] ivBytes) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, NoSuchProviderException, InvalidAlgorithmParameterException {
        (new File(MainActivity.getMainActivity().getFilesDir() + "/.crypted/")).mkdirs();
        FileInputStream fis = new FileInputStream(in);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        return new CipherInputStream(fis, cipher);
    }

    public static void encryptImage(Bitmap in, File out,  byte[] keyBytes,  byte[] ivBytes) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, NoSuchProviderException, InvalidAlgorithmParameterException {
        (new File(MainActivity.getMainActivity().getFilesDir() + "/.crypted/")).mkdirs();
        FileOutputStream fos = new FileOutputStream(out);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        CipherOutputStream cos = new CipherOutputStream(fos, cipher);
        in.compress(Bitmap.CompressFormat.PNG, 85, cos);
        cos.flush();
        cos.close();
    }

    public static byte[] getHash(String password)  {
        byte[] key = (getDeviceID() + password).getBytes();
        try{
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
        }catch (Exception e){
            e.printStackTrace();
        }
        return key;
    }

    public static String getDeviceID(){
        return Settings.Secure.getString(MainActivity.getMainActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

}
