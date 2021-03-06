package me.kobosil.picturecrypt.async;

/**
 * Created by roman on 29.02.2016.
 */
public class TaskResult {
    private Object data;
    private boolean error = false;
    private MyAsyncTask task;

    public TaskResult(Object data, boolean error, MyAsyncTask task) {
        this.data = data;
        this.error = error;
        this.task = task;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public MyAsyncTask getTask() {
        return task;
    }

    public void setTask(MyAsyncTask task) {
        this.task = task;
    }
}
