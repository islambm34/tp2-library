package library;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The library: it registers books, copies and members, and manages the loans.
 *
 * <p>Rules enforced here:</p>
 * <ul>
 *   <li>a copy that is not available cannot be lent (<i>IllegalStateException</i>);</li>
 *   <li>a member cannot have more than {@value #MAX_OPEN_LOANS} loans in progress
 *       (<i>IllegalStateException</i>);</li>
 *   <li>an unknown ISBN, inventory number or member number causes an
 *       <i>IllegalArgumentException</i>.</li>
 * </ul>
 */
public class Library {

    /** Maximum number of loans in progress for one member. */
    public static final int MAX_OPEN_LOANS = 5;

    private final Map<String, Book> books = new LinkedHashMap<>();
    private final Map<String, Copy> copies = new LinkedHashMap<>();
    private final Map<String, Member> members = new LinkedHashMap<>();
    private final List<Loan> loans = new ArrayList<>();

    // ---------------------------------------------------------------- catalogue

    /** Registers a book; an already known ISBN is refused. */
    public void addBook(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("the book is required");
        }
        if (books.containsKey(book.getIsbn())) {
            throw new IllegalStateException("book already registered: " + book.getIsbn());
        }
        books.put(book.getIsbn(), book);
    }

    /** Adds a physical copy of an already registered book. */
    public void addCopy(String isbn, String inventoryNumber) {
        if (isbn == null || inventoryNumber == null) {
            throw new IllegalArgumentException("ISBN and inventory number are required");
        }
        Book book = books.get(isbn);
        if (book == null) {
            throw new IllegalArgumentException("unknown ISBN: " + isbn);
        }
        if (copies.containsKey(inventoryNumber)) {
            throw new IllegalStateException("inventory number already used: " + inventoryNumber);
        }
        copies.put(inventoryNumber, new Copy(inventoryNumber, book));
    }

    /** Registers a member; an already known number is refused. */
    public void registerMember(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("the member is required");
        }
        if (members.containsKey(member.getNumber())) {
            throw new IllegalStateException("member already registered: " + member.getNumber());
        }
        members.put(member.getNumber(), member);
    }

    // ----------------------------------------------------------------- lookups

    /** Finds a book by ISBN, or returns null when it is unknown. */
    public Book findBook(String isbn) {
        return books.get(isbn);
    }

    /** Finds a copy by inventory number, or returns null when it is unknown. */
    public Copy findCopy(String inventoryNumber) {
        return copies.get(inventoryNumber);
    }

    /** Finds a member by number, or returns null when it is unknown. */
    public Member findMember(String number) {
        return members.get(number);
    }

    /** All the copies of one book, in registration order. */
    public List<Copy> copiesOf(String isbn) {
        requireBook(isbn);
        List<Copy> result = new ArrayList<>();
        for (Copy copy : copies.values()) {
            if (copy.getBook().getIsbn().equals(isbn)) {
                result.add(copy);
            }
        }
        return result;
    }

    /**
     * Searches the books whose title contains {@code keyword}, ignoring the case.
     * An empty or null keyword returns an empty list; the order of registration
     * is kept.
     */
    public List<Book> searchByTitle(String keyword) {
        List<Book> result = new ArrayList<>();
        if (keyword == null || keyword.isBlank()) {
            return result;
        }
        String needle = keyword.trim().toLowerCase();
        for (Book book : books.values()) {
            if (book.getTitle().toLowerCase().contains(needle)) {
                result.add(book);
            }
        }
        return result;
    }

    /** All the loans, open or closed, in chronological order. */
    public List<Loan> getLoans() {
        return List.copyOf(loans);
    }

    /** The loans in progress of one member. */
    public List<Loan> openLoansOf(String memberNumber) {
        requireMember(memberNumber);
        List<Loan> result = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.isOpen() && loan.getMember().getNumber().equals(memberNumber)) {
                result.add(loan);
            }
        }
        return result;
    }

    // ------------------------------------------------------------------- loans

    /**
     * Lends the copy {@code inventoryNumber} to the member {@code memberNumber}.
     *
     * @throws IllegalArgumentException if a number is unknown
     * @throws IllegalStateException    if the copy is not available, or if the
     *                                  member already has {@value #MAX_OPEN_LOANS}
     *                                  loans in progress
     */
    public Loan lend(String memberNumber, String inventoryNumber, LocalDate today) {
        Member member = requireMember(memberNumber);
        Copy copy = requireCopy(inventoryNumber);
        if (today == null) {
            throw new IllegalArgumentException("the date is required");
        }
        if (!copy.isAvailable()) {
            throw new IllegalStateException(
                    "copy " + inventoryNumber + " is not available");
        }
        if (openLoansOf(memberNumber).size() >= MAX_OPEN_LOANS) {
            throw new IllegalStateException("member " + memberNumber
                    + " already has " + MAX_OPEN_LOANS + " loans in progress");
        }
        Loan loan = new Loan(member, copy, today);
        copy.setAvailable(false);
        loans.add(loan);
        return loan;
    }

    /**
     * Returns the copy to the library and closes its open loan.
     *
     * @throws IllegalArgumentException if the inventory number is unknown
     * @throws IllegalStateException    if the copy is not on loan
     */
    public Loan returnCopy(String inventoryNumber, LocalDate today) {
        Copy copy = requireCopy(inventoryNumber);
        if (today == null) {
            throw new IllegalArgumentException("the date is required");
        }
        if (copy.isAvailable()) {
            throw new IllegalStateException("copy " + inventoryNumber + " is not on loan");
        }
        Loan open = null;
        for (Loan loan : loans) {
            if (loan.isOpen() && loan.getCopy().equals(copy)) {
                open = loan;
            }
        }
        if (open == null) {
            throw new IllegalStateException("no open loan for copy " + inventoryNumber);
        }
        open.close(today);
        copy.setAvailable(true);
        return open;
    }

    // ----------------------------------------------------------------- helpers

    private Member requireMember(String number) {
        if (number == null) {
            throw new IllegalArgumentException("the member number is required");
        }
        Member member = members.get(number);
        if (member == null) {
            throw new IllegalArgumentException("unknown member number: " + number);
        }
        return member;
    }

    private Copy requireCopy(String inventoryNumber) {
        if (inventoryNumber == null) {
            throw new IllegalArgumentException("the inventory number is required");
        }
        Copy copy = copies.get(inventoryNumber);
        if (copy == null) {
            throw new IllegalArgumentException("unknown inventory number: " + inventoryNumber);
        }
        return copy;
    }

    private Book requireBook(String isbn) {
        if (isbn == null) {
            throw new IllegalArgumentException("the ISBN is required");
        }
        Book book = books.get(isbn);
        if (book == null) {
            throw new IllegalArgumentException("unknown ISBN: " + isbn);
        }
        return book;
    }
}
