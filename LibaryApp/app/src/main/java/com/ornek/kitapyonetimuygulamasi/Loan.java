package com.ornek.kitapyonetimuygulamasi;

import java.util.Date;

public class Loan {
    private int id;
    private Book book;
    private String borrowerName;
    private Date loanDate;
    private Date returnDate;

    public Loan(int id, Book book, String borrowerName, Date loanDate, Date returnDate) {
        this.id = id;
        this.book = book;
        this.borrowerName = borrowerName;
        this.loanDate = loanDate;
        this.returnDate = returnDate;
    }

    // BookId ile de oluşturulabilen constructor
    public Loan(int id, int bookId, String borrowerName, Date loanDate, Date returnDate, boolean dummy) {
        this.id = id;
        this.book = new Book(bookId, "", 0, "", "", false, null);
        this.borrowerName = borrowerName;
        this.loanDate = loanDate;
        this.returnDate = returnDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Book getBook() { 
        if (book != null) {
            return book;
        } else {
            return new Book(-1, "", 0, "", "", false, null);
        }
    }
    public void setBook(Book book) { this.book = book; }

    public String getBorrowerName() { return borrowerName; }
    public void setBorrowerName(String borrowerName) { this.borrowerName = borrowerName; }

    public Date getLoanDate() { return loanDate; }
    public void setLoanDate(Date loanDate) { this.loanDate = loanDate; }

    public Date getReturnDate() { return returnDate; }
    public void setReturnDate(Date returnDate) { this.returnDate = returnDate; }

    public int getBookId() {
        return book != null ? book.getId() : -1;
    }
}
