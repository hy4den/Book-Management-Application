package com.ornek.kitapyonetimuygulamasi;

public class Review {
    private int id;
    private int bookId;
    private String reviewerName;
    private String comment;
    private int rating; // 1-5 arası puan

    public Review(int id, int bookId, String reviewerName, String comment, int rating) {
        this.id = id;
        this.bookId = bookId;
        this.reviewerName = reviewerName;
        this.comment = comment;
        this.rating = rating;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
}
