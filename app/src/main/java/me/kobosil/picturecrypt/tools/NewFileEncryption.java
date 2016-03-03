package me.kobosil.picturecrypt.tools;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import me.kobosil.picturecrypt.MainActivity;

/**
 * Created by roman on 29.02.2016.
 */
public class NewFileEncryption {

    public static void encrypt(File in, File out, byte[] keyBytes) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, NoSuchProviderException, InvalidAlgorithmParameterException {
        (new File(MainActivity.getMainActivity().getFilesDir() + "/.crypted/")).mkdirs();

        File ivBytesFile = new File(out.getAbsolutePath() + ".iv");
        byte[] ivBytes = getIvBytes();
        writeIvBytes(ivBytesFile, ivBytes);

        FileInputStream fis = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

        CipherOutputStream cos = new CipherOutputStream(fos, cipher);
        int b;
        byte[] d = new byte[8];
        while ((b = fis.read(d)) != -1) {
            cos.write(d, 0, b);
        }
        cos.flush();
        cos.close();
        fis.close();
    }

    public static void decrypt(File in, File out, byte[] keyBytes) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, NoSuchProviderException, InvalidAlgorithmParameterException {
        FileInputStream fis = new FileInputStream(in);
        (new File(MainActivity.getMainActivity().getFilesDir() + "/.crypted/")).mkdirs();

        File ivBytesFile = new File(in.getAbsolutePath() + ".iv");
        byte[] ivBytes = readIvBytes(ivBytesFile);

        FileOutputStream fos = new FileOutputStream(out);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        CipherInputStream cis = new CipherInputStream(fis, cipher);
        int b;
        byte[] d = new byte[8];
        while ((b = cis.read(d)) != -1) {
            fos.write(d, 0, b);
        }
        fos.flush();
        fos.close();
        cis.close();
    }

    public static CipherInputStream decryptStream(File in, byte[] keyBytes) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, NoSuchProviderException, InvalidAlgorithmParameterException {
        (new File(MainActivity.getMainActivity().getFilesDir() + "/.crypted/")).mkdirs();

        File ivBytesFile = new File(in.getAbsolutePath() + ".iv");
        byte[] ivBytes = readIvBytes(ivBytesFile);

        FileInputStream fis = new FileInputStream(in);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        return new CipherInputStream(fis, cipher);
    }

    public static void encryptImage(Bitmap in, File out, byte[] keyBytes) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, NoSuchProviderException, InvalidAlgorithmParameterException {
        (new File(MainActivity.getMainActivity().getFilesDir() + "/.crypted/")).mkdirs();

        File ivBytesFile = new File(out.getAbsolutePath() + ".iv");
        byte[] ivBytes = getIvBytes();
        writeIvBytes(ivBytesFile, ivBytes);

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

    public static SecretKey getPBKDF2(String passphraseSHA1, byte[] salt, int iterations, int keyLength) {
        try {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec keySpec = new PBEKeySpec(passphraseSHA1.toCharArray(), salt, iterations, keyLength);
            SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
            return secretKey;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] readIvBytes(File file) {
        try {
            byte[] buffer = new byte[(int) file.length()];
            InputStream ios = null;
            try {
                ios = new FileInputStream(file);
                if (ios.read(buffer) == -1) {
                    throw new IOException(
                            "EOF reached while trying to read the whole file");
                }
            } finally {
                try {
                    if (ios != null)
                        ios.close();
                } catch (IOException e) {
                }
            }
            return buffer;
        } catch (IOException ioe) {
            System.out.println("IOException : " + ioe);
        }
        return null;
    }

    public static void writeIvBytes(File file, byte[] ivBytes) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(ivBytes);
            fos.close();
        } catch (FileNotFoundException ex) {
            System.out.println("FileNotFoundException : " + ex);
        } catch (IOException ioe) {
            System.out.println("IOException : " + ioe);
        }
    }

    public static byte[] getIvBytes() {
        try {

            SecureRandom sr = MainActivity.getSr();
            if(sr == null)
                sr = SecureRandom.getInstance("SHA1PRNG");
            return sr.generateSeed(16);
        } catch (Exception ex) {
            System.out.println("Exception : " + ex);
        }
        return null;
    }

}
