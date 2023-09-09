package vn.techres.line.data.model.utils;

import android.graphics.drawable.Drawable;

public class EmojiChat {
    private String unicode;
    private Drawable resource;

    public String getUnicode() {
        return unicode;
    }

    public void setUnicode(String unicode) {
        this.unicode = unicode;
    }

    public Drawable getResource() {
        return resource;
    }

    public void setResource(Drawable resource) {
        this.resource = resource;
    }

    public EmojiChat(String unicode, Drawable resource) {
        this.unicode = unicode;
        this.resource = resource;
    }
}
