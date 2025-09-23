package com.example.mbtix;

public class Post {
    private String title;
    private String content;
    private String author;
    private String date;
    private int views;

    public Post() {}

    public Post(String title, String content, String author, String date, int views) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.date = date;
        this.views = views;
    }

    // getters & setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }
}
