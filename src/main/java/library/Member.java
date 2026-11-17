package library;

import java.util.Objects;

/**
 * A person registered at the library, identified by a member number.
 */
public class Member {

    private final String number;
    private final String name;

    public Member(String number, String name) {
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("the member number is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("the name is required");
        }
        this.number = number;
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Member)) {
            return false;
        }
        return number.equals(((Member) other).number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public String toString() {
        return "Member[" + number + ", " + name + "]";
    }
}
