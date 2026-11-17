package library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests added by the second student on the cloned repository (Part E).
 */
class LoanRulesTest {

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

    @Test
    @DisplayName("A loan is late only after the due date")
    void aLoanIsLateOnlyAfterTheDueDate() {
        Loan loan = library.lend("M-01", "C-001", DAY);
        assertFalse(loan.isLate(DAY));
        assertFalse(loan.isLate(DAY.plusDays(13)));
        assertFalse(loan.isLate(DAY.plusDays(14)));   // the due date itself is allowed
        assertTrue(loan.isLate(DAY.plusDays(15)));
    }

    @Test
    @DisplayName("The due date is exactly 14 days after the start date")
    void theDueDateIsFourteenDaysLater() {
        Loan loan = library.lend("M-01", "C-001", DAY);
        assertEquals(14, java.time.temporal.ChronoUnit.DAYS
                .between(loan.getStartDate(), loan.getDueDate()));
    }

    @Test
    @DisplayName("A copy can be lent again after its return")
    void aCopyCanBeLentAgainAfterItsReturn() {
        library.lend("M-01", "C-001", DAY);
        library.returnCopy("C-001", DAY.plusDays(2));
        assertTrue(library.findCopy("C-001").isAvailable());
        Loan again = library.lend("M-01", "C-001", DAY.plusDays(3));
        assertEquals(DAY.plusDays(17), again.getDueDate());
    }
}
