package me.kobosil.picturecrypt.tools;

import android.Manifest;
import android.os.Environment;
import android.util.Log;
import android.util.TimingLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import me.kobosil.picturecrypt.MainActivity;

/**
 * Created by roman on 29.02.2016.
 */
public class FileEncryption {


    public void test(){
        File myDir = MainActivity.getMainActivity().getFilesDir();
        File file_ori = new File(Environment.getExternalStorageDirectory() + "/DCIM/100MEDIA/IMAG0002.jpg");
        File file_crypted = new File(myDir + "/IMAG0002.jpg.crypt");
        File file_decrypted = new File(myDir + "/IMAG0002_decrypted.jpg");

        String string = "hello world!7777";
        try {

            for(File f : Environment.getExternalStorageDirectory().listFiles())
                Log.d("fcrypt", "found " + f.getAbsolutePath());

            Log.d("fcrypt", "start " + file_ori.getAbsolutePath());
          /*  FileOutputStream fos = new FileOutputStream(file_ori);
            fos.write(string.getBytes());
            fos.flush();
            fos.close();
            timings.addSplit("write file");*/
            encrypt(file_ori, file_crypted);
            Log.d("fcrypt", "encrypted " + file_crypted.getAbsolutePath());
            decrypt(file_crypted, file_decrypted);
            Log.d("fcrypt", "decrypted " + file_decrypted.getAbsolutePath());
        }catch (Exception e){
e.printStackTrace();
        }
    }

    static void encrypt(File in, File out) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        // Here you read the cleartext.
        FileInputStream fis = new FileInputStream(in);
        // This stream write the encrypted text. This stream will be wrapped by another stream.
        FileOutputStream fos = new FileOutputStream(out);

        // Length is 16 byte
        // Careful when taking user input!!! http://stackoverflow.com/a/3452620/1188357
        SecretKeySpec sks = new SecretKeySpec("MyDifficultPassw".getBytes(), "AES");
        // Create cipher
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, sks);
        // Wrap the output stream
        CipherOutputStream cos = new CipherOutputStream(fos, cipher);
        // Write bytes
        int b;
        byte[] d = new byte[8];
        while((b = fis.read(d)) != -1) {
            cos.write(d, 0, b);
        }
        // Flush and close streams.
        cos.flush();
        cos.close();
        fis.close();
    }

    static void decrypt(File in, File out) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        FileInputStream fis = new FileInputStream(in);

        FileOutputStream fos = new FileOutputStream(out);
        SecretKeySpec sks = new SecretKeySpec("MyDifficultPassw".getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, sks);
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

}
