package library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests of the search by title added on the branch {@code feature/search}.
 */
class LibrarySearchTest {

    private static final String ISBN_SE = "978-0-00-000001-1";
    private static final String ISBN_ALGO = "978-0-00-000002-1";
    private static final String ISBN_JAVA = "978-0-00-000003-1";

    private Library library;

    @BeforeEach
    void setUp() {
        library = new Library();
        library.addBook(new Book(ISBN_SE, "Software Engineering", 2016));
        library.addBook(new Book(ISBN_ALGO, "Algorithms and Data Structures", 2011));
        library.addBook(new Book(ISBN_JAVA, "Effective Java", 2018));
    }

    @Test
    @DisplayName("The search finds the books whose title contains the keyword")
    void theSearchFindsTheMatchingBooks() {
        List<Book> found = library.searchByTitle("engineering");
        assertEquals(1, found.size());
        assertEquals(ISBN_SE, found.get(0).getIsbn());
    }

    @ParameterizedTest
    @ValueSource(strings = {"java", "JAVA", "JaVa", "  java  "})
    @DisplayName("The search ignores the case and the surrounding spaces")
    void theSearchIgnoresCaseAndSpaces(String keyword) {
        List<Book> found = library.searchByTitle(keyword);
        assertEquals(1, found.size());
        assertEquals(ISBN_JAVA, found.get(0).getIsbn());
    }

    @Test
    @DisplayName("The search returns several books when several titles match")
    void theSearchCanReturnSeveralBooks() {
        List<Book> found = library.searchByTitle("a");
        assertEquals(3, found.size());   // every title contains an "a"
    }

    @Test
    @DisplayName("The search returns an empty list when nothing matches")
    void theSearchReturnsAnEmptyListWhenNothingMatches() {
        assertTrue(library.searchByTitle("quantum").isEmpty());
    }

    @Test
    @DisplayName("An empty keyword returns no book")
    void anEmptyKeywordReturnsNoBook() {
        assertTrue(library.searchByTitle("   ").isEmpty());
        assertTrue(library.searchByTitle(null).isEmpty());
    }
}
