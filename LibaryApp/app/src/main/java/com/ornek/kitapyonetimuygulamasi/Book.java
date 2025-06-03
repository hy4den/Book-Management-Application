package com.ornek.kitapyonetimuygulamasi;

public class Book {
    private int id;
    private String name;
    private int year;
    private String author;
    private String aciklama;
    private boolean favori;
    private Category category;

    public Book(int id, String name, int year, String author, String aciklama, boolean favori, Category category) {
        this.id = id;
        this.name = name;
        this.year = year;
        this.author = author;
        this.aciklama = aciklama;
        this.favori = favori;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

    public boolean isFavori() {
        return favori;
    }

    public void setFavori(boolean favori) {
        this.favori = favori;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}