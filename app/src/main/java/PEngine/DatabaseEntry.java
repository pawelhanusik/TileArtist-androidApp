package PEngine;

import android.content.ContentValues;

public class DatabaseEntry {
    public long id;
    public String title, category, author;

    public DatabaseEntry(){
        this(-1, "Untitled", "uncategorized", "unknown");
    }
    public DatabaseEntry(long id){
        this(id, "Untitled", "uncategorized", "unknown");
    }
    public DatabaseEntry(long id, String title){
        this(id, title, "uncategorized", "unknown");
    }
    public DatabaseEntry(long id, String title, String author){
        this(id, title, author, "unknown");
    }
    public DatabaseEntry(String title, String category, String author){
        this(-1, title, category, author);
    }
    public DatabaseEntry(long id, String title, String category, String author){
        this.id = id;
        this.title = title;
        this.category = category;
        this.author = author;
    }

    public static String[] getProjection() {
        return new String[]{
                "ID",
                "title",
                "category",
                "author"
        };
    }
    public ContentValues getContentValues() {
        ContentValues ret = new ContentValues();
        if(id != -1) {
            ret.put("ID", id);
        }
        ret.put("title", title);
        ret.put("category", category);
        ret.put("author", author);
        return ret;
    }
}
