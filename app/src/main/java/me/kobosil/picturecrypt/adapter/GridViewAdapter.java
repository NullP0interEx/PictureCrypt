package me.kobosil.picturecrypt.adapter;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import me.kobosil.picturecrypt.MainActivity;
import me.kobosil.picturecrypt.R;
import me.kobosil.picturecrypt.async.MyAsyncTask;
import me.kobosil.picturecrypt.async.TaskResult;
import me.kobosil.picturecrypt.async.interfaces.AsyncCallBack;
import me.kobosil.picturecrypt.async.interfaces.CustomAsyncTask;
import me.kobosil.picturecrypt.models.ImageItem;
import me.kobosil.picturecrypt.tools.LegacyFileEncryption;
import me.kobosil.picturecrypt.tools.NewFileEncryption;

/**
 * Created by roman on 01.03.2016.
 */
public class GridViewAdapter extends ArrayAdapter {
    private int layoutResourceId;
    private ArrayList data = new ArrayList();
    private int size = 200;
    private byte[] password = new byte[10];
    private HashMap<ImageView, MyAsyncTask> myAsyncTaskHashMap = new HashMap<>();

    public GridViewAdapter(int layoutResourceId, ArrayList data) {
        super(MainActivity.getMainActivity(), layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = MainActivity.getMainActivity().getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageTitle = (TextView) row.findViewById(R.id.text);
            holder.image = (ImageView) row.findViewById(R.id.image);
            final ViewHolder _holder = holder;
            holder.image.post(new Runnable() {
                @Override
                public void run() {
                    if(_holder.image.getMeasuredWidth() != 0 && _holder.image.getMeasuredWidth() > 100)
                        size = _holder.image.getMeasuredWidth();
                }
            });
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        ImageItem item = (ImageItem)data.get(position);
        holder.imageTitle.setText(item.getTitle());

        Object[] data = new Object[3];
        data[0] = item;
        data[1] = holder;
        data[2] = size;

        MyAsyncTask myAsyncTask = new MyAsyncTask(task, data, callBack);
        myAsyncTask.execute();
        if(myAsyncTaskHashMap.containsKey(holder.image)){
            myAsyncTaskHashMap.get(holder.image).cancel(true);
            myAsyncTaskHashMap.remove(holder.image);
        }
            myAsyncTaskHashMap.put(holder.image, myAsyncTask);
        holder.image.setImageResource(0);
        return row;
    }

    AsyncCallBack callBack = new AsyncCallBack() {
        @Override
        public void done(TaskResult data) {
            Object[] data_ = (Object[])data.getData();
            ViewHolder holder = (ViewHolder)data_[1];
            Bitmap result  = (Bitmap)data_[2];

            holder.image.setImageBitmap(result);
        }

        @Override
        public void error(TaskResult data) {
            Object[] data_ = (Object[])data.getData();
            ViewHolder holder = (ViewHolder)data_[1];
            holder.image.setImageResource(R.drawable.locked);
        }
    };

    CustomAsyncTask task = new CustomAsyncTask() {
        @Override
        public TaskResult run(TaskResult taskResult) {

            try{

            Object[] data = (Object[])taskResult.getData();
            ImageItem item = (ImageItem)data[0];
            int size_ = (int)data[2];

            (new File(item.getImage().getAbsolutePath().substring(0,item.getImage().getAbsolutePath().lastIndexOf(File.separator)) + "/thumbnail/")).mkdirs();
            File thumbnail = new File(item.getImage().getAbsolutePath().substring(0,item.getImage().getAbsolutePath().lastIndexOf(File.separator)) + "/thumbnail/mini_" + item.getImage().getName());

            Bitmap source = null;

                if(item.getImage().getName().contains(".crypt"))
                    source = BitmapFactory.decodeStream(LegacyFileEncryption.decryptStream((thumbnail.exists() ? thumbnail : item.getImage()), password));
                else
                    source = BitmapFactory.decodeStream(NewFileEncryption.decryptStream((thumbnail.exists() ? thumbnail : item.getImage()), NewFileEncryption.keyBytes, NewFileEncryption.ivBytes));

                if(source == null){
                taskResult.setError(true);
                return null;
            }

            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;
            Bitmap result = Bitmap.createScaledBitmap(Bitmap.createBitmap(source, x, y, size, size), size_, size_, false);
            if (result != source) {
                source.recycle();
            }

            if(thumbnail != null){
                if(thumbnail.getName().contains(".crypt"))
                    LegacyFileEncryption.encryptImage(result, thumbnail,  password);
                else
                    NewFileEncryption.encryptImage(result, thumbnail, NewFileEncryption.keyBytes, NewFileEncryption.ivBytes);

            }

            data[2] = result;
            taskResult.setData(data);
            }catch (Exception e){
                e.printStackTrace();
                taskResult.setError(true);
            }
            return null;
        }
    };

    static class ViewHolder {
        TextView imageTitle;
        ImageView image;
    }

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }
}
