package me.kobosil.picturecrypt;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.File;

import me.kobosil.picturecrypt.async.MyAsyncTask;
import me.kobosil.picturecrypt.async.TaskResult;
import me.kobosil.picturecrypt.async.interfaces.AsyncCallBack;
import me.kobosil.picturecrypt.async.interfaces.CustomAsyncTask;
import me.kobosil.picturecrypt.tools.LegacyFileEncryption;
import me.kobosil.picturecrypt.tools.NewFileEncryption;
import me.kobosil.picturecrypt.tools.ZoomableImageView;

public class DetailsActivity extends AppCompatActivity {

    private TextView titleTextView;
    private ZoomableImageView imageView;
    private byte[] password = new byte[10];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        Bundle bundle = getIntent().getExtras();

        String title = bundle.getString("title");
        String image = bundle.getString("image");
        password = bundle.getByteArray("password");


        titleTextView = (TextView) findViewById(R.id.title);
        imageView = (ZoomableImageView) findViewById(R.id.grid_item_image);
        titleTextView.setText(getText(R.string.wait_for_encryption));
        imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.unlocking));

        Object[] data = new Object[3];
        data[0] = title;
        data[1] = image;
        data[2] = password;

        MyAsyncTask myAsyncTask = new MyAsyncTask(task, data, callBack);
        myAsyncTask.execute();
    }

    AsyncCallBack callBack = new AsyncCallBack() {
        @Override
        public void done(TaskResult data) {
            Object[] data_ = (Object[])data.getData();
            Bitmap image = (Bitmap)data_[2];
            String title = (String)data_[0];
            titleTextView.setText(title);
            imageView.setImageBitmap(image);
        }

        @Override
        public void error(TaskResult data) {
            titleTextView.setText(getText(R.string.bad_password));
            imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.locked_big));
        }
    };

    CustomAsyncTask task = new CustomAsyncTask() {
        @Override
        public TaskResult run(TaskResult taskResult) {
            Object[] data = (Object[])taskResult.getData();
            String file = (String)data[1];
            byte[] password = (byte[])data[2];

            try{
                Bitmap image = null;
                if(file.contains(".crypt"))
                    image = BitmapFactory.decodeStream(LegacyFileEncryption.decryptStream((new File(file)), password));
                else
                    image = BitmapFactory.decodeStream(NewFileEncryption.decryptStream((new File(file)), NewFileEncryption.keyBytes, NewFileEncryption.ivBytes));
                if(image == null)
                    taskResult.setError(true);
                data[2] = image;
                taskResult.setData(data);
            }catch (Exception e){
                taskResult.setError(true);
                e.printStackTrace();
            }
            return null;
        }
    };
}
