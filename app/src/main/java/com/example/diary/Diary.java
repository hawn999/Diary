package com.example.diary;

public class Diary {
    private String title;
    private String time;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Diary(String title, String time) {
        this.title = title;
        this.time = time;
    }
}
