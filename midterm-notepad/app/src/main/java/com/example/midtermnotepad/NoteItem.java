package com.example.midtermnotepad;

/** 列表与编辑器之间传递的一条笔记。 */
public class NoteItem {

    public long id;
    public String title;
    public String note;
    public long created;
    public long modified;
    public int category;

    public NoteItem() {
    }

    public NoteItem(long id, String title, String note, long created, long modified, int category) {
        this.id = id;
        this.title = title;
        this.note = note;
        this.created = created;
        this.modified = modified;
        this.category = category;
    }

    public String displayTitle() {
        return title == null || title.trim().isEmpty() ? "无标题笔记" : title;
    }
}
