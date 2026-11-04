import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class BookRepository {

    private final Path databasePath;

    // DEFAULT DATABASE
    public BookRepository() {
        this("book.txt");
    }

    // CUSTOM DATABASE LOCATION
    public BookRepository(String filename) {
        databasePath = Paths.get(filename)
            .toAbsolutePath()
            .normalize();
    }

    // GET DATABASE LOCATION
    public String getDatabaseLocation() {
        return databasePath.toString();
    }

    // LOAD BOOKS
    public List<Books> load() throws IOException {

        List<Books> books = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();

        // CREATE DATABASE IF MISSING
        if (!Files.exists(databasePath)) {

            Path parent = databasePath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.createFile(databasePath);

            return books;
        }

        int lineNumber = 0;

        try (BufferedReader reader = Files.newBufferedReader(
                databasePath,
                StandardCharsets.UTF_8)) {

            String line;

            while ((line = reader.readLine()) != null) {

                lineNumber++;
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] fields = line.split(",", -1);

                try {

                    int id;
                    String title;
                    String author;
                    String genre = "Uncategorized";
                    boolean borrowed = false;
                    String coverPath = null;

                    // ORIGINAL DATABASE FORMAT:
                    // Printed,Publisher,ID,Title,Author
                    if (fields.length == 5
                            && (fields[0].trim().equalsIgnoreCase("Printed")
                            || fields[0].trim().equalsIgnoreCase("EBook"))) {

                        genre = fields[0].trim();

                        id = Integer.parseInt(
                            fields[2].trim()
                        );

                        title = fields[3].trim();
                        author = fields[4].trim();

                    }

                    // NEW DATABASE FORMAT:
                    // ID,Title,Author,Genre,Borrowed,CoverPath
                    else if (fields.length == 6) {

                        id = Integer.parseInt(
                            fields[0].trim()
                        );

                        title = fields[1].trim();
                        author = fields[2].trim();
                        genre = fields[3].trim();

                        borrowed = parseBoolean(fields[4]);

                        coverPath = fields[5].trim();
                    }

                    // OLD DATABASE FORMAT:
                    // ID,Title,Author,Borrowed
                    else if (fields.length == 4
                            && isBoolean(fields[3])) {

                        id = Integer.parseInt(
                            fields[0].trim()
                        );

                        title = fields[1].trim();
                        author = fields[2].trim();

                        borrowed = parseBoolean(fields[3]);
                    }

                    // OLDER DATABASE FORMAT:
                    // ID,Title,Author
                    else if (fields.length == 3) {

                        id = Integer.parseInt(
                            fields[0].trim()
                        );

                        title = fields[1].trim();
                        author = fields[2].trim();
                    }

                    // NEW 5-FIELD DATABASE FORMAT:
// ID,Title,Author,Genre,Borrowed
else if (fields.length == 5
        && isBoolean(fields[4])) {

    id = Integer.parseInt(fields[0].trim());
    title = fields[1].trim();
    author = fields[2].trim();
    genre = fields[3].trim();
    borrowed = parseBoolean(fields[4]);
}

// LEGACY DATABASE FORMAT:
// unused,unused,ID,Title,Author
else if (fields.length == 5) {

    id = Integer.parseInt(fields[2].trim());
    title = fields[3].trim();
    author = fields[4].trim();
}

                    else {

                        throw new IllegalArgumentException(
                            "Unknown database format."
                        );
                    }

                    if (id <= 0) {
                        throw new IllegalArgumentException(
                            "Book ID must be positive."
                        );
                    }

                    if (!ids.add(id)) {
                        throw new IllegalArgumentException(
                            "Duplicate book ID: " + id
                        );
                    }

                    Books book = new Books(
                        id,
                        title,
                        author,
                        genre,
                        coverPath
                    );

                    book.setBorrowed(borrowed);

                    books.add(book);

                } catch (RuntimeException ex) {

                    throw new IOException(
                        "Invalid book record at line "
                            + lineNumber
                            + "\nRecord: " + line
                            + "\nReason: " + ex.getMessage()
                            + "\nDatabase: " + databasePath,
                        ex
                    );
                }
            }
        }

        return books;
    }

    // CHECK BOOLEAN
    private boolean isBoolean(String value) {

        return value.trim().equalsIgnoreCase("true")
            || value.trim().equalsIgnoreCase("false");
    }

    // CONVERT BOOLEAN
    private boolean parseBoolean(String value) {

        if (!isBoolean(value)) {

            throw new IllegalArgumentException(
                "Borrowed must be true or false."
            );
        }

        return Boolean.parseBoolean(value.trim());
    }

    // SAVE BOOKS
    public void save(List<Books> books) throws IOException {

        if (books == null) {
            throw new IOException(
                "Book list cannot be null."
            );
        }

        Path parent = databasePath.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        Path temp = Files.createTempFile(
            parent,
            "innerbook-",
            ".tmp"
        );

        boolean saved = false;

        try {

            Set<Integer> ids = new HashSet<>();

            try (BufferedWriter writer = Files.newBufferedWriter(
                    temp,
                    StandardCharsets.UTF_8)) {

                writer.write(
                    "# ID,Title,Author,Genre,Borrowed,CoverPath"
                );

                writer.newLine();

                for (Books book : books) {

                    if (book == null) {
                        throw new IOException(
                            "Book list contains an empty record."
                        );
                    }

                    if (!ids.add(book.getId())) {
                        throw new IOException(
                            "Duplicate book ID: " + book.getId()
                        );
                    }

                    String[] values = {
                        book.getTitle(),
                        book.getAuthor(),
                        book.getGenre(),
                        book.getCoverPath() == null
                            ? "" : book.getCoverPath()
                    };

                    for (String value : values) {

                        if (value.contains(",")
                                || value.contains("\n")
                                || value.contains("\r")) {

                            throw new IOException(
                                "Book fields cannot contain commas "
                                + "or line breaks: " + value
                            );
                        }
                    }

                    writer.write(
                        book.getId() + ","
                        + book.getTitle() + ","
                        + book.getAuthor() + ","
                        + book.getGenre() + ","
                        + book.isBorrowed() + ","
                        + (book.getCoverPath() == null
                            ? "" : book.getCoverPath())
                    );

                    writer.newLine();
                }
            }

            try {

                Files.move(
                    temp,
                    databasePath,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
                );

            } catch (AtomicMoveNotSupportedException ex) {

                Files.move(
                    temp,
                    databasePath,
                    StandardCopyOption.REPLACE_EXISTING
                );
            }

            saved = true;

        } finally {

            if (!saved) {
                Files.deleteIfExists(temp);
            }
        }
    }
}
