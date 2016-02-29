package me.kobosil.picturecrypt.tools;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import me.kobosil.picturecrypt.MainActivity;
import me.kobosil.picturecrypt.async.MyAsyncTask;
import me.kobosil.picturecrypt.async.TaskResult;
import me.kobosil.picturecrypt.async.interfaces.AsyncCallBack;
import me.kobosil.picturecrypt.async.interfaces.CustomAsyncTask;

/**
 * Created by roman on 29.02.2016.
 */
public class DirectoryCrypter {


    byte[] password;
    ArrayList<File> files;
    ArrayList<MyAsyncTask> myAsyncTasks = new ArrayList<>();

    AsyncCallBack callBack = new AsyncCallBack() {
        @Override
        public void done(TaskResult data) {
            data.getTask().cancel(true);
            myAsyncTasks.remove(data.getTask());
            nextFiles();
        }

        @Override
        public void error(TaskResult data) {
            data.getTask().cancel(true);
            myAsyncTasks.remove(data.getTask());
            nextFiles();
        }
    };

    CustomAsyncTask task = new CustomAsyncTask() {
        @Override
        public TaskResult run(TaskResult taskResult) {

            File file = (File)taskResult.getData();
            File myDir = MainActivity.getMainActivity().getFilesDir();
            File file_out = new File(myDir + "/"+ file.getName() +".crypt");
            taskResult.setData(file_out);
            try {
                FileEncryption.encrypt(file, file_out, password);
                Log.d("fcrypt", "encrypted " + file.getAbsolutePath());
            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }
    };

    public void nextFiles(){
        while (true){
            if (myAsyncTasks.size() >= 4)
                return;
            if (files.size() < 1)
                return;
            File f = files.get(0);
            MyAsyncTask asyncTask = new MyAsyncTask(task, f, callBack);
            asyncTask.execute();
            myAsyncTasks.add(asyncTask);
            files.remove(0);

        }
    }

    public void setFiles(ArrayList<File> files){
        this.files = files;
    }

    public void setPassword(String password){
        this.password = FileEncryption.getHash(password);
    }

}
