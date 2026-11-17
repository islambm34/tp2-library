package library;

import java.time.LocalDate;

/**
 * One loan of a copy to a member. A loan lasts 14 days; it is open until the
 * copy is returned, and it is late when it is still open after the due date.
 */
public class Loan {

    /** Duration of a loan, in days. */
    public static final int LOAN_DAYS = 14;

    private final Member member;
    private final Copy copy;
    private final LocalDate startDate;
    private final LocalDate dueDate;
    private LocalDate returnDate;

    public Loan(Member member, Copy copy, LocalDate startDate) {
        if (member == null || copy == null || startDate == null) {
            throw new IllegalArgumentException("member, copy and start date are required");
        }
        this.member = member;
        this.copy = copy;
        this.startDate = startDate;
        this.dueDate = startDate.plusDays(LOAN_DAYS);
        this.returnDate = null;
    }

    public Member getMember() {
        return member;
    }

    public Copy getCopy() {
        return copy;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    /** The return date, or null while the loan is still open. */
    public LocalDate getReturnDate() {
        return returnDate;
    }

    /** An open loan is a loan that has not been returned yet. */
    public boolean isOpen() {
        return returnDate == null;
    }

    /** A loan is late when it is still open and today is after the due date. */
    public boolean isLate(LocalDate today) {
        if (today == null) {
            throw new IllegalArgumentException("the date is required");
        }
        return isOpen() && today.isAfter(dueDate);
    }

    /** Records the return of the copy; a loan can be closed only once. */
    void close(LocalDate returnDate) {
        if (!isOpen()) {
            throw new IllegalStateException("the loan is already closed");
        }
        if (returnDate == null || returnDate.isBefore(startDate)) {
            throw new IllegalArgumentException("invalid return date: " + returnDate);
        }
        this.returnDate = returnDate;
    }

    @Override
    public String toString() {
        return "Loan[" + member.getNumber() + ", " + copy.getInventoryNumber()
                + ", from " + startDate + " to " + dueDate
                + (isOpen() ? ", open" : ", returned " + returnDate) + "]";
    }
}
