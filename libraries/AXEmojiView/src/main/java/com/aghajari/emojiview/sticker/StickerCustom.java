package com.aghajari.emojiview.sticker;

import java.io.Serializable;
import java.util.ArrayList;

public class StickerCustom implements Serializable {
    public StickerCustom(String _id, String name, String link_original, String id_category, ArrayList<String> key_search) {
        this._id = _id;
        this.name = name;
        this.link_original = link_original;
        this.id_category = id_category;
        this.key_search = key_search;
    }

    String _id = "";
    String name = "";
    String link_original = "";
    String id_category = "";

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink_original() {
        return link_original;
    }

    public void setLink_original(String link_original) {
        this.link_original = link_original;
    }

    public String getId_category() {
        return id_category;
    }

    public void setId_category(String id_category) {
        this.id_category = id_category;
    }

    public ArrayList<String> getKey_search() {
        return key_search;
    }

    public void setKey_search(ArrayList<String> key_search) {
        this.key_search = key_search;
    }

    ArrayList<String> key_search = new ArrayList<>();



}
