package library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests of the rules of the library. One test per rule, each one named with a
 * sentence that describes the expected behaviour.
 */
class LibraryTest {

    private static final String ISBN = "978-0-00-000001-1";
    private static final LocalDate DAY = LocalDate.of(2026, 10, 1);

    private Library library;

    @BeforeEach
    void setUp() {
        library = new Library();
        library.addBook(new Book(ISBN, "Software Engineering", 2016));
        library.addCopy(ISBN, "C-001");
        library.registerMember(new Member("M-01", "First member"));
    }

    // ----------------------------------------------------- the given test

    @Test
    void lendingMakesTheCopyUnavailable() {
        Loan loan = library.lend("M-01", "C-001", DAY);
        assertFalse(library.findCopy("C-001").isAvailable());
        assertEquals(DAY.plusDays(14), loan.getDueDate());
    }

    // ------------------------------------------------------- the entities

    @Test
    @DisplayName("Two books with the same ISBN are equal")
    void booksAreEqualWhenTheIsbnsAreEqual() {
        Book sameIsbn = new Book(ISBN, "Another title", 2020);
        assertEquals(new Book(ISBN, "Software Engineering", 2016), sameIsbn);
        assertEquals(sameIsbn.hashCode(), new Book(ISBN, "x", 1).hashCode());
    }

    @Test
    @DisplayName("A new copy is available")
    void aNewCopyIsAvailable() {
        assertTrue(library.findCopy("C-001").isAvailable());
        assertSame(library.findBook(ISBN), library.findCopy("C-001").getBook());
    }

    // --------------------------------------------------------- the loans

    @Test
    @DisplayName("A loan lasts 14 days and is open until the return")
    void aLoanIsOpenUntilTheReturn() {
        Loan loan = library.lend("M-01", "C-001", DAY);
        assertTrue(loan.isOpen());
        assertEquals(DAY, loan.getStartDate());
        assertEquals(DAY.plusDays(14), loan.getDueDate());
        assertFalse(loan.isLate(DAY.plusDays(14)));
        assertTrue(loan.isLate(DAY.plusDays(15)));
    }

    @Test
    @DisplayName("Returning a copy closes the loan and frees the copy")
    void returningACopyClosesTheLoan() {
        library.lend("M-01", "C-001", DAY);
        Loan returned = library.returnCopy("C-001", DAY.plusDays(3));
        assertFalse(returned.isOpen());
        assertEquals(DAY.plusDays(3), returned.getReturnDate());
        assertTrue(library.findCopy("C-001").isAvailable());
        assertTrue(library.openLoansOf("M-01").isEmpty());
    }

    @Test
    @DisplayName("A returned loan is never late")
    void aReturnedLoanIsNotLate() {
        Loan loan = library.lend("M-01", "C-001", DAY);
        library.returnCopy("C-001", DAY.plusDays(20));
        assertFalse(loan.isLate(DAY.plusDays(30)));
    }

    // ------------------------------------------------------ the refusals

    @Test
    @DisplayName("A copy on loan cannot be lent twice")
    void anUnavailableCopyCannotBeLent() {
        library.lend("M-01", "C-001", DAY);
        library.registerMember(new Member("M-02", "Second member"));
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> library.lend("M-02", "C-001", DAY));
        assertTrue(e.getMessage().contains("not available"));
    }

    @Test
    @DisplayName("A member cannot have more than 5 loans in progress")
    void aMemberCannotExceedFiveOpenLoans() {
        for (int i = 2; i <= 6; i++) {           // C-002 ... C-006
            library.addCopy(ISBN, "C-00" + i);
        }
        for (int i = 1; i <= 5; i++) {
            library.lend("M-01", "C-00" + i, DAY);
        }
        assertEquals(5, library.openLoansOf("M-01").size());

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> library.lend("M-01", "C-006", DAY));
        assertTrue(e.getMessage().contains("5"));
        assertTrue(library.findCopy("C-006").isAvailable());
    }

    @Test
    @DisplayName("Returning a copy frees a place for a new loan")
    void theLoanCounterDropsWhenACopyIsReturned() {
        for (int i = 2; i <= 6; i++) {
            library.addCopy(ISBN, "C-00" + i);
        }
        for (int i = 1; i <= 5; i++) {
            library.lend("M-01", "C-00" + i, DAY);
        }
        library.returnCopy("C-001", DAY.plusDays(1));
        Loan sixth = library.lend("M-01", "C-006", DAY.plusDays(2));
        assertTrue(sixth.isOpen());
    }

    @Test
    @DisplayName("An unknown member number is refused")
    void anUnknownMemberIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> library.lend("M-99", "C-001", DAY));
    }

    @Test
    @DisplayName("An unknown inventory number is refused")
    void anUnknownCopyIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> library.lend("M-01", "C-999", DAY));
    }

    @Test
    @DisplayName("An unknown ISBN is refused when a copy is added")
    void anUnknownIsbnIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> library.addCopy("000-0", "C-002"));
    }

    @Test
    @DisplayName("Returning a copy that is not on loan is refused")
    void returningACopyThatIsNotOnLoanIsRefused() {
        assertThrows(IllegalStateException.class, () -> library.returnCopy("C-001", DAY));
    }

    @Test
    @DisplayName("A book and a member cannot be registered twice")
    void duplicatesAreRefused() {
        assertThrows(IllegalStateException.class,
                () -> library.addBook(new Book(ISBN, "Duplicate", 2020)));
        assertThrows(IllegalStateException.class,
                () -> library.registerMember(new Member("M-01", "Duplicate")));
    }

    @Test
    @DisplayName("A loan in progress is counted for its member only")
    void openLoansAreTrackedPerMember() {
        library.registerMember(new Member("M-02", "Second member"));
        library.lend("M-01", "C-001", DAY);
        assertTrue(library.openLoansOf("M-02").isEmpty());
        List<Loan> loans = library.openLoansOf("M-01");
        assertEquals(1, loans.size());
        assertEquals("C-001", loans.get(0).getCopy().getInventoryNumber());
    }
}
