package me.kobosil.picturecrypt.tools;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import me.kobosil.picturecrypt.MainActivity;
import me.kobosil.picturecrypt.async.MyAsyncTask;
import me.kobosil.picturecrypt.async.TaskResult;
import me.kobosil.picturecrypt.async.interfaces.AsyncCallBack;
import me.kobosil.picturecrypt.async.interfaces.CustomAsyncTask;

/**
 * Created by roman on 29.02.2016.
 */
public class DirectoryDeCrypter {

    private byte[] password;
    private ArrayList<File> files;
    private ArrayList<MyAsyncTask> myAsyncTasks = new ArrayList<>();
    private File myDir = new File(MainActivity.getMainActivity().getFilesDir() + "/.decrypted");

    AsyncCallBack callBack = new AsyncCallBack() {
        @Override
        public void done(TaskResult data) {
            myAsyncTasks.remove(data.getTask());
            data.getTask().cancel(true);
            data.setTask(null);
            data = null;
            nextFiles();
        }

        @Override
        public void error(TaskResult data) {
            myAsyncTasks.remove(data.getTask());
            data.getTask().cancel(true);
            data.setTask(null);
            data = null;
            nextFiles();
        }
    };

    CustomAsyncTask task = new CustomAsyncTask() {
        @Override
        public TaskResult run(TaskResult taskResult) {

            File file = (File)taskResult.getData();
            if(!file.getName().contains(".crypt")) {
                taskResult.setError(true);
                return null;
            }
            File file_out = new File(myDir + "/" + file.getName().replace(".crypt", ""));
            taskResult.setData(file_out);
            try {
                LegacyFileEncryption.decrypt(file, file_out, password);
                Log.d("fcrypt", "decrypted " + file_out.getAbsolutePath());
            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }
    };

    public void nextFiles(){
        myDir.mkdirs();
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
        this.password = LegacyFileEncryption.getHash(password);
    }

    public File getMyDir() {
        return myDir;
    }

    public void setMyDir(File myDir) {
        myDir.mkdirs();
        this.myDir = myDir;
    }
}
