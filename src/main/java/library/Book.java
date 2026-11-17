package library;

import java.util.Objects;

/**
 * A book of the library, identified by its ISBN.
 */
public class Book {

    private final String isbn;
    private final String title;
    private final int year;

    public Book(String isbn, String title, int year) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("the ISBN is required");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("the title is required");
        }
        this.isbn = isbn;
        this.title = title;
        this.year = year;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    /** Two books are equal when their ISBNs are equal. */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Book)) {
            return false;
        }
        return isbn.equals(((Book) other).isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }

    @Override
    public String toString() {
        return "Book[" + isbn + ", \"" + title + "\", " + year + "]";
    }
}
