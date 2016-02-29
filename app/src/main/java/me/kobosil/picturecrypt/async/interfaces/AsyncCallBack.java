package me.kobosil.picturecrypt.async.interfaces;

import me.kobosil.picturecrypt.async.TaskResult;

/**
 * Created by roman on 29.02.2016.
 */
public interface AsyncCallBack {

    public void done(TaskResult data);

    public void error(TaskResult data);

}