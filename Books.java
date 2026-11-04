import java.io.File;

public class Books {
    private final int id;
    private String title;
    private String author;
    private String genre;
    private boolean borrowed;
    private String coverPath;

    // Compatibility with older code
    public Books(int id, String title, String author) {
        this(id, title, author, "Uncategorized", null);
    }

    public Books(int id, String title, String author, String genre) {
        this(id, title, author, genre, null);
    }

    public Books(int id, String title, String author,
                 String genre, String coverPath) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                "Book ID must be positive."
            );
        }

        this.id = id;
        setTitle(title);
        setAuthor(author);
        setGenre(genre);
        setCoverPath(coverPath);
        this.borrowed = false;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getGenre() {
        return genre;
    }

    public boolean isBorrowed() {
        return borrowed;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Book title cannot be empty."
            );
        }
        this.title = title.trim();
    }

    public void setAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Author name cannot be empty."
            );
        }
        this.author = author.trim();
    }

    public void setGenre(String genre) {
        if (genre == null || genre.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Genre cannot be empty."
            );
        }
        this.genre = genre.trim();
    }

    public void setBorrowed(boolean borrowed) {
        this.borrowed = borrowed;
    }

    public void setCoverPath(String coverPath) {
        if (coverPath == null || coverPath.trim().isEmpty()) {
            this.coverPath = null;
        } else {
            this.coverPath = coverPath.trim();
        }
    }

    public boolean borrowBook() {
        if (borrowed) {
            return false;
        }
        borrowed = true;
        return true;
    }

    public boolean returnBook() {
        if (!borrowed) {
            return false;
        }
        borrowed = false;
        return true;
    }

    public boolean hasCover() {
        return coverPath != null
            && new File(coverPath).isFile();
    }

    @Override
    public String toString() {
        return "Book ID: " + id
            + "\nTitle: " + title
            + "\nAuthor: " + author
            + "\nGenre: " + genre
            + "\nStatus: "
            + (borrowed ? "Borrowed" : "Available");
    }
}
