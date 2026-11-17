package library;

import java.util.Objects;

/**
 * A physical copy of a book, identified by its inventory number.
 */
public class Copy {

    private final String inventoryNumber;
    private final Book book;
    private boolean available;

    public Copy(String inventoryNumber, Book book) {
        if (inventoryNumber == null || inventoryNumber.isBlank()) {
            throw new IllegalArgumentException("the inventory number is required");
        }
        if (book == null) {
            throw new IllegalArgumentException("the book is required");
        }
        this.inventoryNumber = inventoryNumber;
        this.book = book;
        this.available = true;
    }

    public String getInventoryNumber() {
        return inventoryNumber;
    }

    public Book getBook() {
        return book;
    }

    public boolean isAvailable() {
        return available;
    }

    /** Only the Library is allowed to change the availability of a copy. */
    void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Copy)) {
            return false;
        }
        return inventoryNumber.equals(((Copy) other).inventoryNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inventoryNumber);
    }

    @Override
    public String toString() {
        return "Copy[" + inventoryNumber + ", " + book.getTitle()
                + ", " + (available ? "available" : "on loan") + "]";
    }
}
