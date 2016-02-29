package me.kobosil.picturecrypt.async.interfaces;

import me.kobosil.picturecrypt.async.TaskResult;

/**
 * Created by roman on 29.02.2016.
 */
public interface CustomAsyncTask {

    public TaskResult run(TaskResult taskResult);

}