package com.ornek.kitapyonetimuygulamasi;

import android.content.Context;
import java.util.List;

public class BookManager {
    private DatabaseHelper dbHelper;

    public BookManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public long kitapEkle(Book book) { // void yerine long oldu
        return dbHelper.addBook(book); // Dönen ID'yi MainActivity'ye ilet
    }

    public void kitapGuncelle(Book book) {
        dbHelper.updateBook(book);
    }

    public void kitapSil(int id) {
        dbHelper.deleteBook(id);
    }

    public void tumKitaplariSil() {
        dbHelper.deleteAllBooks();
    }

    public Book kitapGetir(int id) {
        return dbHelper.getBook(id);
    }

    public List<Book> kitaplariListele() {
        return dbHelper.getAllBooks();
    }

    public void oduncVer(Loan loan) {
        dbHelper.addLoan(loan);
    }

    public boolean kitapOduncVerildiMi(int bookId) {
        return dbHelper.isBookLoaned(bookId);
    }

    // Bu metod MainActivity tarafından kullanılacak
    public List<Loan> kitapOduncleriGetir(int bookId) {
        return dbHelper.getBookLoans(bookId);
    }

    public List<Loan> kitabinOduncKayitlari(int bookId) {
        return dbHelper.getBookLoans(bookId);
    }

    public void oduncKaydiGuncelle(Loan loan) {
        dbHelper.updateLoan(loan);
    }

    public void yorumEkle(Review review) {
        dbHelper.addReview(review);
    }

    public List<Review> yorumlariGetir(int bookId) {
        return dbHelper.getReviewsForBook(bookId);
    }

    // İstatistikler
    public int toplamKitapSayisi() {
        return kitaplariListele().size();
    }

    public int favoriKitapSayisi() {
        int count = 0;
        for (Book b : kitaplariListele()) {
            if (b.isFavori()) count++;
        }
        return count;
    }
}