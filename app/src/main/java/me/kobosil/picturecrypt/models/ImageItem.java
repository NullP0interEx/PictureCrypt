package me.kobosil.picturecrypt.models;


import java.io.File;

/**
 * Created by roman on 01.03.2016.
 */
public class ImageItem {
    private File image;
    private String title;

    public ImageItem(File image, String title) {
        super();
        this.image = image;
        this.title = title;
    }

    public File getImage() {
        return image;
    }

    public void setImage(File image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}