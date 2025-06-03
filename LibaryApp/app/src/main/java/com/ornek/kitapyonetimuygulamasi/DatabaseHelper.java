package com.ornek.kitapyonetimuygulamasi;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "kitaplar.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_BOOKS = "books";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_YEAR = "year";
    private static final String COLUMN_AUTHOR = "author";
    private static final String COLUMN_ACIKLAMA = "aciklama";
    private static final String COLUMN_FAVORI = "favori";
    private static final String COLUMN_CATEGORY = "category";

    private static final String TABLE_LOANS = "loans";
    private static final String COLUMN_LOAN_ID = "id";
    private static final String COLUMN_LOAN_BOOK_ID = "book_id";
    private static final String COLUMN_BORROWER_NAME = "borrower_name";
    private static final String COLUMN_LOAN_DATE = "loan_date";
    private static final String COLUMN_RETURN_DATE = "return_date";

    private static final String TABLE_REVIEWS = "reviews";
    private static final String COLUMN_REVIEW_ID = "id";
    private static final String COLUMN_REVIEW_BOOK_ID = "book_id";
    private static final String COLUMN_REVIEWER_NAME = "reviewer_name";
    private static final String COLUMN_COMMENT = "comment";
    private static final String COLUMN_RATING = "rating";

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_BOOKS_TABLE = "CREATE TABLE " + TABLE_BOOKS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT, "
                + COLUMN_YEAR + " INTEGER, "
                + COLUMN_AUTHOR + " TEXT, "
                + COLUMN_ACIKLAMA + " TEXT, "
                + COLUMN_FAVORI + " INTEGER DEFAULT 0, "
                + COLUMN_CATEGORY + " TEXT" + ")";
        db.execSQL(CREATE_BOOKS_TABLE);

        String CREATE_LOANS_TABLE = "CREATE TABLE " + TABLE_LOANS + " ("
                + COLUMN_LOAN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_LOAN_BOOK_ID + " INTEGER, "
                + COLUMN_BORROWER_NAME + " TEXT, "
                + COLUMN_LOAN_DATE + " TEXT, "
                + COLUMN_RETURN_DATE + " TEXT, "
                + "FOREIGN KEY(" + COLUMN_LOAN_BOOK_ID + ") REFERENCES " + TABLE_BOOKS + "(" + COLUMN_ID + ")" + ")";
        db.execSQL(CREATE_LOANS_TABLE);

        String CREATE_REVIEWS_TABLE = "CREATE TABLE " + TABLE_REVIEWS + " ("
                + COLUMN_REVIEW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_REVIEW_BOOK_ID + " INTEGER, "
                + COLUMN_REVIEWER_NAME + " TEXT, "
                + COLUMN_COMMENT + " TEXT, "
                + COLUMN_RATING + " INTEGER, "
                + "FOREIGN KEY(" + COLUMN_REVIEW_BOOK_ID + ") REFERENCES " + TABLE_BOOKS + "(" + COLUMN_ID + ")" + ")";
        db.execSQL(CREATE_REVIEWS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOANS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVIEWS);
        onCreate(db);
    }

    public long addBook(Book book) { // void yerine long oldu
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, book.getName());
        values.put(COLUMN_YEAR, book.getYear());
        values.put(COLUMN_AUTHOR, book.getAuthor());
        values.put(COLUMN_ACIKLAMA, book.getAciklama());
        values.put(COLUMN_FAVORI, book.isFavori() ? 1 : 0);
        values.put(COLUMN_CATEGORY, book.getCategory() != null ? book.getCategory().getName() : null);
        long id = db.insert(TABLE_BOOKS, null, values); // Eklenen kaydın ID'sini al
        db.close();
        return id; // ID'yi döndür
    }

    public void updateBook(Book book) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, book.getName());
        values.put(COLUMN_YEAR, book.getYear());
        values.put(COLUMN_AUTHOR, book.getAuthor());
        values.put(COLUMN_ACIKLAMA, book.getAciklama());
        values.put(COLUMN_FAVORI, book.isFavori() ? 1 : 0);
        values.put(COLUMN_CATEGORY, book.getCategory() != null ? book.getCategory().getName() : null);
        db.update(TABLE_BOOKS, values, COLUMN_ID + " = ?", new String[] { String.valueOf(book.getId()) });
        db.close();
    }

    public void deleteBook(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BOOKS, COLUMN_ID + " = ?", new String[] { String.valueOf(id) });
        db.close();
    }

    public void deleteAllBooks() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BOOKS, null, null);
        db.close();
    }

    public Book getBook(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BOOKS,
                new String[] { COLUMN_ID, COLUMN_NAME, COLUMN_YEAR, COLUMN_AUTHOR, COLUMN_ACIKLAMA, COLUMN_FAVORI, COLUMN_CATEGORY },
                COLUMN_ID + " = ?", new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        Category category = null;
        if (cursor.getString(6) != null) {
            category = new Category(0, cursor.getString(6));
        }
        Book book = new Book(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getInt(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getInt(5) == 1,
                category);
        cursor.close();
        db.close();
        return book;
    }

    public List<Book> getAllBooks() {
        List<Book> bookList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_BOOKS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Category category = null;
                if (cursor.getString(6) != null) {
                    category = new Category(0, cursor.getString(6));
                }
                Book book = new Book(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getInt(5) == 1,
                        category);
                bookList.add(book);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return bookList;
    }

    // Ödünç işlemleri
    public long addLoan(Loan loan) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_LOAN_BOOK_ID, loan.getBookId());
        values.put(COLUMN_BORROWER_NAME, loan.getBorrowerName());
        // Tarihleri doğru formatta kaydet
        values.put(COLUMN_LOAN_DATE, dateFormat.format(loan.getLoanDate()));
        values.put(COLUMN_RETURN_DATE, loan.getReturnDate() != null ? dateFormat.format(loan.getReturnDate()) : null);
        long id = db.insert(TABLE_LOANS, null, values); // Eklenen kaydın ID'sini al
        db.close();
        return id; // ID'yi döndür
    }

    public boolean isBookOnLoan(int bookId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_LOANS,
                new String[]{COLUMN_LOAN_ID},
                COLUMN_LOAN_BOOK_ID + " = ? AND (" + COLUMN_RETURN_DATE + " IS NULL OR " + COLUMN_RETURN_DATE + " = '')",
                new String[]{String.valueOf(bookId)},
                null, null, null);
        boolean result = (cursor != null && cursor.moveToFirst());
        if (cursor != null) cursor.close();
        db.close();
        return result;
    }

    public boolean isBookLoaned(int bookId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_LOANS +
                " WHERE " + COLUMN_LOAN_BOOK_ID + " = ? AND " +
                COLUMN_RETURN_DATE + " IS NULL";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(bookId)});
        boolean isLoaned = false;

        if (cursor.moveToFirst()) {
            isLoaned = cursor.getInt(0) > 0;
        }

        cursor.close();
        db.close();
        return isLoaned;
    }

    public List<Loan> getBookLoans(int bookId) {
        List<Loan> loans = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_LOANS +
                " WHERE " + COLUMN_LOAN_BOOK_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(bookId)});
        if (cursor.moveToFirst()) {
            do {
                try {
                    Loan loan = new Loan(
                            cursor.getInt(cursor.getColumnIndex(COLUMN_LOAN_ID)),
                            cursor.getInt(cursor.getColumnIndex(COLUMN_LOAN_BOOK_ID)),
                            cursor.getString(cursor.getColumnIndex(COLUMN_BORROWER_NAME)),
                            dateFormat.parse(cursor.getString(cursor.getColumnIndex(COLUMN_LOAN_DATE))),
                            cursor.getString(cursor.getColumnIndex(COLUMN_RETURN_DATE)) != null && !cursor.getString(cursor.getColumnIndex(COLUMN_RETURN_DATE)).isEmpty()
                                    ? dateFormat.parse(cursor.getString(cursor.getColumnIndex(COLUMN_RETURN_DATE))) : null,
                            false // returned alanı yok, isterseniz ekleyin
                    );
                    loans.add(loan);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return loans;
    }

    public int updateLoan(Loan loan) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BORROWER_NAME, loan.getBorrowerName());
        values.put(COLUMN_LOAN_DATE, dateFormat.format(loan.getLoanDate()));
        values.put(COLUMN_RETURN_DATE, loan.getDueDate() != null ? dateFormat.format(loan.getDueDate()) : null);
        // Eğer returned alanı eklenirse burada da ekleyin
        int result = db.update(TABLE_LOANS, values,
                COLUMN_LOAN_ID + " = ?",
                new String[]{String.valueOf(loan.getId())});
        db.close();
        return result;
    }

    // --- Yorum ekleme ---
    public void addReview(Review review) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REVIEW_BOOK_ID, review.getBookId());
        values.put(COLUMN_REVIEWER_NAME, review.getReviewerName());
        values.put(COLUMN_COMMENT, review.getComment());
        values.put(COLUMN_RATING, review.getRating());
        db.insert(TABLE_REVIEWS, null, values);
        db.close();
    }

    public List<Review> getReviewsForBook(int bookId) {
        List<Review> reviews = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_REVIEWS,
                new String[]{COLUMN_REVIEW_ID, COLUMN_REVIEW_BOOK_ID, COLUMN_REVIEWER_NAME, COLUMN_COMMENT, COLUMN_RATING},
                COLUMN_REVIEW_BOOK_ID + " = ?", new String[]{String.valueOf(bookId)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Review review = new Review(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getInt(4)
                );
                reviews.add(review);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return reviews;
    }
}