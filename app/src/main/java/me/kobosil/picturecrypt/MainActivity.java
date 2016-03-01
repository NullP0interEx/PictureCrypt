package me.kobosil.picturecrypt;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.GridView;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import me.kobosil.picturecrypt.adapter.GridViewAdapter;
import me.kobosil.picturecrypt.models.ImageItem;
import me.kobosil.picturecrypt.tools.DirectoryCrypter;
import me.kobosil.picturecrypt.tools.FileEncryption;


public class MainActivity extends AppCompatActivity {

    private static AppCompatActivity mainActivity;
    private GridView gridView;
    private GridViewAdapter gridAdapter;
    private byte[] password = new byte[10];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        mainActivity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file_in = new File(Environment.getExternalStorageDirectory() + "/Pictures/Instagram");

                DirectoryCrypter directoryCrypter = new DirectoryCrypter();
                directoryCrypter.setFiles(new ArrayList<File>(Arrays.asList(file_in.listFiles())));
                directoryCrypter.setPassword(password);
                directoryCrypter.nextFiles();
            }
        });
        isStoragePermissionGranted();

        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ImageItem item = (ImageItem) parent.getItemAtPosition(position);
                //Create intent
               /* Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                intent.putExtra("title", item.getTitle());
                intent.putExtra("image", item.getImage());

                //Start details activity
                startActivity(intent);*/
            }
        });
        Intent i = new Intent(this, MyConfirmPatternActivity.class);
        startActivityForResult(i, 1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                String pattern = data.getStringExtra("pattern");
                this.password = FileEncryption.getHash(pattern);
                reloadGrid();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
                return;
            }
        }
    }

    private ArrayList<ImageItem> getData() {
        final ArrayList<ImageItem> imageItems = new ArrayList<>();
        File myDir = new File(MainActivity.getMainActivity().getFilesDir() + "/.crypted");

        if(myDir.listFiles() != null)
        for(File f : myDir.listFiles()) {
            if(f.isFile())
                imageItems.add(new ImageItem(f, f.getName()));
        }

        return imageItems;
    }

    private void reloadGrid(){
        gridAdapter = new GridViewAdapter(R.layout.grid_item_layout, getData());
        gridAdapter.setPassword(password);
        gridView.setAdapter(gridAdapter);
        gridView.invalidate();
    }


    @Override
    protected void onResume() {
        super.onResume();

        try {
/*
            File file_in = new File(Environment.getExternalStorageDirectory() + "/WhatsApp/Media/WhatsApp Images/");
            File file_inCrypted = new File(MainActivity.getMainActivity().getFilesDir() + "/.crypted");

            DirectoryCrypter directoryCrypter = new DirectoryCrypter();
            directoryCrypter.setFiles(new ArrayList<File>(Arrays.asList(file_in.listFiles())));
            directoryCrypter.setPassword("hallo");
            directoryCrypter.nextFiles();


            DirectoryDeCrypter directoryDeCrypter = new DirectoryDeCrypter();
            directoryDeCrypter.setFiles(new ArrayList<File>(Arrays.asList(file_inCrypted.listFiles())));
            directoryDeCrypter.setPassword("hallo");
            directoryDeCrypter.nextFiles();*/

        }catch (Exception e){
            e.printStackTrace();
        }

        //FileEncryption crypter = new FileEncryption();
        //crypter.test();
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("sDp","Permission is granted");
                return true;
            } else {

                Log.v("sDp","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("sDp","Permission is granted");
            return true;
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v("sDp","Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_password) {
            Intent i = new Intent(this, MyConfirmPatternActivity.class);
            startActivityForResult(i, 1);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static AppCompatActivity getMainActivity() {
        return mainActivity;
    }
}
