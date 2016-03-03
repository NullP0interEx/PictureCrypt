package me.kobosil.picturecrypt;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;
import java.io.FileInputStream;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.crypto.SecretKey;

import io.fabric.sdk.android.Fabric;
import me.kobosil.picturecrypt.adapter.GridViewAdapter;
import me.kobosil.picturecrypt.async.MyAsyncTask;
import me.kobosil.picturecrypt.async.TaskResult;
import me.kobosil.picturecrypt.async.interfaces.AsyncCallBack;
import me.kobosil.picturecrypt.async.interfaces.CustomAsyncTask;
import me.kobosil.picturecrypt.models.ImageItem;
import me.kobosil.picturecrypt.tools.LegacyFileEncryption;
import me.kobosil.picturecrypt.tools.NewFileEncryption;


public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 2;
    static final int FILE_CODE = 3;
    private static final int SELECT_PHOTO = 100;
    private static AppCompatActivity mainActivity;
    private static SecureRandom sr;
    private GridView gridView;
    private GridViewAdapter gridAdapter;
    private ProgressBar spinner;
    private TextView loadingText;
    private byte[] password = new byte[10];
    private SecretKey secretKey;
    AsyncCallBack callBack = new AsyncCallBack() {
        @Override
        public void done(TaskResult data) {
            toggleLoading(false);
            loadingText.setText("OK: " + ((File) ((Object[]) data.getData())[1]).getName());
            reloadGrid();
        }

        @Override
        public void error(TaskResult data) {
            toggleLoading(false);
            loadingText.setText("OK: " + ((File) ((Object[]) data.getData())[1]).getName());
        }
    };
    CustomAsyncTask task = new CustomAsyncTask() {
        @Override
        public TaskResult run(TaskResult taskResult) {
            final Object[] data = (Object[]) taskResult.getData();
            MainActivity.getMainActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toggleLoading(true);
                    loadingText.setText("loading: " + ((File) data[1]).getName());
                }
            });

            if (data[0] instanceof Bitmap) {
                File selectedOut = (File) data[1];
                byte[] password = (byte[]) data[2];

                try {

                    if (selectedOut.getName().endsWith(".crypt"))
                        LegacyFileEncryption.encryptImage((Bitmap) data[0], selectedOut, password);
                    else
                        NewFileEncryption.encryptImage((Bitmap) data[0], selectedOut, secretKey.getEncoded());

                } catch (Exception e) {
                    taskResult.setError(true);
                    e.printStackTrace();
                }
                return null;
            }

            File selectedImage = (File) data[0];
            File selectedOut = (File) data[1];
            byte[] password = (byte[]) data[2];

            try {
                if (selectedOut.getName().endsWith(".crypt"))
                    LegacyFileEncryption.encryptImage(BitmapFactory.decodeStream(new FileInputStream(selectedImage)), selectedOut, password);
                else
                    NewFileEncryption.encryptImage(BitmapFactory.decodeStream(new FileInputStream(selectedImage)), selectedOut, secretKey.getEncoded());


            } catch (Exception e) {
                taskResult.setError(true);
                e.printStackTrace();
            }
            return null;
        }
    };
    private FloatingActionsMenu menuMultipleActions;
    private ArrayList<Uri> waitingImageUris = new ArrayList<>();

    private static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static AppCompatActivity getMainActivity() {
        return mainActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        mainActivity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.multiple_actions);

        spinner = (ProgressBar) findViewById(R.id.loadingP);
        loadingText = (TextView) findViewById(R.id.loadingText);

        try {
            sr = SecureRandom.getInstance("SHA1PRNG");
        }catch(Exception e){

        }
        setSeed();
        final FloatingActionButton btn_filesystem = (FloatingActionButton) findViewById(R.id.btn_filesystem);
        btn_filesystem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuMultipleActions.collapse();
                Intent i = new Intent(getMainActivity(), FilePickerActivity.class);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, true);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
                startActivityForResult(i, FILE_CODE);
            }
        });

        final FloatingActionButton btn_gallery = (FloatingActionButton) findViewById(R.id.btn_gallery);
        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuMultipleActions.collapse();
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });

        final FloatingActionButton btn_take = (FloatingActionButton) findViewById(R.id.btn_take);
        btn_take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuMultipleActions.collapse();
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        isStoragePermissionGranted();

        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ImageItem item = (ImageItem) parent.getItemAtPosition(position);
                //Create intent
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                intent.putExtra("title", item.getTitle());
                intent.putExtra("image", item.getImage().getAbsolutePath());
                intent.putExtra("password", password);
                intent.putExtra("secretKey", secretKey.getEncoded());

                //Start details activity
                startActivity(intent);
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final ImageItem item = (ImageItem) parent.getItemAtPosition(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(getMainActivity());
                builder.setMessage(getMainActivity().getString(R.string.delete) + "?\n" + item.getTitle())
                        .setCancelable(false)
                        .setNegativeButton(getMainActivity().getString(R.string.no), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                            }
                        })
                        .setPositiveButton(getMainActivity().getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                File thumbnail = new File(item.getImage().getAbsolutePath().substring(0, item.getImage().getAbsolutePath().lastIndexOf(File.separator)) + "/thumbnail/mini_" + item.getImage().getName());
                                File ivBytesFile = new File(item.getImage().getAbsolutePath() + ".iv");
                                File thumbnailIvBytesFile = new File(thumbnail.getAbsolutePath() + ".iv");
                                if (item.getImage().exists())
                                    item.getImage().delete();
                                if (thumbnail.exists())
                                    thumbnail.delete();
                                if (ivBytesFile.exists())
                                    ivBytesFile.delete();
                                if (thumbnailIvBytesFile.exists())
                                    thumbnailIvBytesFile.delete();
                                reloadGrid();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            }
        });


        toggleLoading(false);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendImage(intent);
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendMultipleImages(intent);
            }
        }

        Intent i = new Intent(this, MyConfirmPatternActivity.class);
        startActivityForResult(i, 1);
    }

    private void setSeed(){
        SharedPreferences prefs = this.getSharedPreferences("me.kobosil.picturecrypt", Context.MODE_PRIVATE);
        if (!prefs.contains("crypto.salt.seed")) {
            try {
                prefs.edit().putString("crypto.salt.seed", Base64.encodeToString(sr.generateSeed(16), Base64.DEFAULT)).apply();
            } catch (Exception ex) {
                System.out.println("Exception : " + ex);
            }
        }
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            waitingImageUris.add(imageUri);
        }
    }

    void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            for (Uri imageUri : imageUris)
                waitingImageUris.add(imageUri);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        Object[] data_ = new Object[3];
                        data_[0] = new File(getRealPathFromUri(getMainActivity(), data.getData()));
                        data_[1] = new File(MainActivity.getMainActivity().getFilesDir() + "/.crypted/" + ((File) data_[0]).getName() + ".2crypt");
                        data_[2] = password;

                        MyAsyncTask myAsyncTask = new MyAsyncTask(task, data_, callBack);
                        myAsyncTask.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    String pattern = data.getStringExtra("pattern");
                    SharedPreferences prefs = this.getSharedPreferences("me.kobosil.picturecrypt", Context.MODE_PRIVATE);
                    this.password = LegacyFileEncryption.getHash(pattern);
                    this.secretKey = NewFileEncryption.getPBKDF2(pattern, Base64.decode(prefs.getString("crypto.salt.seed", "").getBytes(), Base64.DEFAULT), 1000, 128);
                    if (!waitingImageUris.isEmpty()) {
                        for (Uri uri : waitingImageUris)
                            startEncryptionFromUri(uri);
                        waitingImageUris.clear();
                    }
                    reloadGrid();
                }
                if (resultCode == Activity.RESULT_CANCELED) {
                    finish();
                    return;
                }
                break;

            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");

                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

                    Object[] data_ = new Object[3];
                    data_[0] = imageBitmap;
                    data_[1] = new File(MainActivity.getMainActivity().getFilesDir() + "/.crypted/" + timeStamp + ".png.2crypt");
                    data_[2] = password;

                    MyAsyncTask myAsyncTask = new MyAsyncTask(task, data_, callBack);
                    myAsyncTask.execute();
                }
                break;
            case FILE_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                        // For JellyBean and above
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            ClipData clip = data.getClipData();

                            if (clip != null) {
                                for (int i = 0; i < clip.getItemCount(); i++) {
                                    Uri uri = clip.getItemAt(i).getUri();
                                    startEncryptionFromUri(uri);
                                }
                            }
                            // For Ice Cream Sandwich
                        } else {
                            ArrayList<String> paths = data.getStringArrayListExtra
                                    (FilePickerActivity.EXTRA_PATHS);

                            if (paths != null) {
                                for (String path : paths) {
                                    Uri uri = Uri.parse(path);
                                    startEncryptionFromUri(uri);
                                }
                            }
                        }

                    } else {
                        Uri uri = data.getData();
                        startEncryptionFromUri(uri);
                    }
                }
                break;
        }
    }

    private void startEncryptionFromUri(Uri uri) {
        try {
            Object[] data_ = new Object[3];
            data_[0] = new File(uri.getPath());
            data_[1] = new File(MainActivity.getMainActivity().getFilesDir() + "/.crypted/" + ((File) data_[0]).getName() + ".2crypt");
            data_[2] = password;

            MyAsyncTask myAsyncTask = new MyAsyncTask(task, data_, callBack);
            myAsyncTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<ImageItem> getData() {
        final ArrayList<ImageItem> imageItems = new ArrayList<>();
        File myDir = new File(MainActivity.getMainActivity().getFilesDir() + "/.crypted");

        if (myDir.listFiles() != null)
            for (File f : myDir.listFiles()) {
                if (f.isFile())
                    if (!f.getName().contains(".iv"))
                        imageItems.add(new ImageItem(f, f.getName()));
            }

        return imageItems;
    }

    private void reloadGrid() {
        gridAdapter = new GridViewAdapter(R.layout.grid_item_layout, getData(), secretKey);
        gridAdapter.setPassword(password);
        gridView.setAdapter(gridAdapter);
        gridView.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("sDp", "Permission is granted");
                return true;
            } else {

                Log.v("sDp", "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("sDp", "Permission is granted");
            return true;
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v("sDp", "Permission: " + permissions[0] + "was " + grantResults[0]);
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

    private void toggleLoading(boolean loading) {
        if (loading) {
            gridView.setVisibility(View.GONE);
            spinner.setVisibility(View.VISIBLE);
            loadingText.setVisibility(View.VISIBLE);
        } else {
            gridView.setVisibility(View.VISIBLE);
            spinner.setVisibility(View.GONE);
            loadingText.setVisibility(View.GONE);
        }
    }

    public static SecureRandom getSr() {
        return sr;
    }
}
