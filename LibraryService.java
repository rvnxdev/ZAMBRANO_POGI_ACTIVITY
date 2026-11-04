import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class LibraryService {

    private final List<Books> books;

    // Constructor
    public LibraryService(List<Books> initialBooks) {
        books = initialBooks == null
            ? new ArrayList<>()
            : new ArrayList<>(initialBooks);
    }

    // ADD BOOK
    public void addBook(Books book) {
        if (book == null) {
            throw new IllegalArgumentException(
                "Book cannot be empty."
            );
        }

        if (findBook(book.getId()) != null) {
            throw new IllegalArgumentException(
                "Book ID already exists."
            );
        }

        books.add(book);
    }

    // EDIT BOOK
    public void editBook(
        int id,
        String title,
        String author,
        String genre,
        String coverPath
    ) {
        Books book = requireBook(id);

        book.setTitle(title);
        book.setAuthor(author);
        book.setGenre(genre);
        book.setCoverPath(coverPath);
    }

    // DELETE BOOK
    public void deleteBook(int id) {
        Books book = requireBook(id);
        books.remove(book);
    }

    // BORROW BOOK
    public void borrowBook(int id) {
        Books book = requireBook(id);

        if (!book.borrowBook()) {
            throw new IllegalStateException(
                "This book is already borrowed."
            );
        }
    }

    // RETURN BOOK
    public void returnBook(int id) {
        Books book = requireBook(id);

        if (!book.returnBook()) {
            throw new IllegalStateException(
                "This book is not currently borrowed."
            );
        }
    }

    // FIND BOOK
    public Books findBook(int id) {
        for (Books book : books) {
            if (book.getId() == id) {
                return book;
            }
        }

        return null;
    }

    // CHECK IF BOOK EXISTS
    private Books requireBook(int id) {
        Books book = findBook(id);

        if (book == null) {
            throw new IllegalArgumentException(
                "Book with ID " + id + " was not found."
            );
        }

        return book;
    }

    // TOTAL BOOKS
    public int getTotalCount() {
        return books.size();
    }

    // AVAILABLE BOOKS
    public int getAvailableCount() {
        int count = 0;

        for (Books book : books) {
            if (!book.isBorrowed()) {
                count++;
            }
        }

        return count;
    }

    // BORROWED BOOKS
    public int getBorrowedCount() {
        int count = 0;

        for (Books book : books) {
            if (book.isBorrowed()) {
                count++;
            }
        }

        return count;
    }

    // GET ALL GENRES
    public Set<String> getGenres() {
        Set<String> genres = new TreeSet<>(
            String.CASE_INSENSITIVE_ORDER
        );

        for (Books book : books) {
            genres.add(book.getGenre());
        }

        return genres;
    }

    // SEARCH AND FILTER BOOKS
    public List<Books> search(
        String query,
        String genre
    ) {
        List<Books> results = new ArrayList<>();

        String text = query == null
            ? "" : query.trim().toLowerCase();

        for (Books book : books) {

            boolean matchesText =
                String.valueOf(book.getId()).contains(text)
                || book.getTitle().toLowerCase().contains(text)
                || book.getAuthor().toLowerCase().contains(text)
                || book.getGenre().toLowerCase().contains(text);

            boolean matchesGenre =
                genre == null
                || genre.equals("All Genres")
                || book.getGenre().equalsIgnoreCase(genre);

            if (matchesText && matchesGenre) {
                results.add(book);
            }
        }

        return results;
    }

    // GET ALL BOOKS
    public List<Books> getAllBooks() {
        return new ArrayList<>(books);
    }
}
