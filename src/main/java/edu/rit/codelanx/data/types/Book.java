package edu.rit.codelanx.data.types;

import com.codelanx.commons.data.FileSerializable;
import com.codelanx.commons.data.ResultRow;
import edu.rit.codelanx.data.DataStorage;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.state.StateBuilder;
import edu.rit.codelanx.data.state.UpdatableState;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Book extends UpdatableState implements FileSerializable {

    private final String title;
    private final String isbn;
    private final List<String> authors;
    private final String publisher;
    private final String publishDate;
    private final int pageCount;
    private int totalCopies;
    private int checkedOut;
    private int libraryVisitors;

    private Book(long id, Builder builder) {
        super(id);
        this.title = builder.title;
        this.isbn = builder.isbn;
        this.authors = Collections.unmodifiableList(Arrays.asList(builder.authors));
        this.publisher = builder.publisher;
        this.publishDate = builder.publishDate;
        this.pageCount = builder.pageCount;
        this.totalCopies = builder.totalCopies;
        this.checkedOut = builder.checkedOut;
    }

    public Book(ResultRow sql) throws SQLException {
        super(sql.getLong("id"));
        this.title = sql.getString("title");
        this.isbn = sql.getString("isbn");
        this.authors = null; //TODO
        this.publisher = sql.getString("publisher");
        this.publishDate = sql.getString("publish_date");
        this.pageCount = sql.getInt("page_count");
        this.totalCopies = sql.getInt("total_copies");
        this.checkedOut = sql.getInt("checked_out");
    }

    public Book(Map<String, Object> file) {
        super((Long) file.get("id"));
        this.title = (String) file.get("title");
        this.isbn = (String) file.get("isbn");
        this.authors = null; //TODO
        this.publisher = (String) file.get("publisher");
        this.publishDate = (String) file.get("publish_date");
        this.pageCount = (Integer) file.get("page_count");
        this.totalCopies = (Integer) file.get("total_copies");
        this.checkedOut = (Integer) file.get("checked_out");
    }

    public String getTitle() {
        return this.title;
    }

    public String getISBN() {
        return this.isbn;
    }

    public List<String> getAuthors() {
        return this.authors;
    }

    public String getPublisher() {
        return this.publisher;
    }

    public String getPublishDate() {
        return this.publishDate;
    }

    public int getPageCount() {
        return this.pageCount;
    }

    public int getTotalCopies() {
        return this.totalCopies;
    }

    public int getCheckedOut() {
        return this.checkedOut;
    }

    @Override
    public State.Type getType() {
        return State.Type.BOOK;
    }

    @Override
    public String toFormattedText() {
        String authors = String.join(", ", this.getAuthors());
        return String.format("Title: %s | ISBN: %s| Author: %s| Publisher: %s| publish date:%s| Total pages: %d| Total copies: %d| Total checkout: %d",
                this.getTitle(), this.getISBN(), authors, this.getPublisher(),
                this.getPublishDate(), this.getPageCount(), this.getTotalCopies(), this.getCheckedOut());
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> back = new LinkedHashMap<>();
        back.put("id", this.id);
        back.put("title", this.title);
        back.put("isbn", this.isbn);
        back.put("author", this.authors);
        back.put("publisher", this.publisher);
        back.put("publish_date", this.publishDate);
        back.put("page_count", this.pageCount);
        back.put("total_copies", this.totalCopies);
        back.put("checked_out", this.checkedOut);
        return back;
    }

    public static Builder create(DataStorage storage) {
        return new Builder(storage);
    }

    public static class Builder extends StateBuilder<Book> {

        private String title;
        private String isbn;
        private String[] authors;
        private String publisher;
        private String publishDate;
        private int pageCount = -1;
        private int totalCopies = -1;
        private int checkedOut = -1;

        public Builder(DataStorage storage) {
            super(storage);
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder isbn(String isbn) {
            this.isbn = isbn;
            return this;
        }

        public Builder authors(String... author) {
            this.authors = author;
            return this;
        }

        public Builder publisher(String publisher) {
            this.publisher = publisher;
            return this;
        }

        public Builder publishDate(String publishDate) {
            this.publishDate = publishDate;
            return this;
        }

        public Builder pageCount(int pageCount) {
            this.pageCount = pageCount;
            return this;
        }

        public Builder totalCopies(int totalCopies) {
            this.totalCopies = totalCopies;
            return this;
        }

        public Builder checkedOut(int checkedOut) {
            this.checkedOut = checkedOut;
            return this;
        }

        @Override
        public boolean isValid() {
            return this.title != null && this.isbn != null
                    && this.authors != null && this.authors.length > 0
                    && this.publisher != null && this.publishDate != null
                    && this.pageCount >= 0 && this.totalCopies >= 0 && this.checkedOut >= 0;
        }

        //TODO: Supply #authors somehow
        @Override
        public Object[] asSQLArguments() {
            return new Object[] {this.title, this.isbn,
                                    this.publisher, this.publishDate,
                                    this.pageCount, this.totalCopies, this.checkedOut};
        }

        @Override
        protected Book buildObj(long id) {
            return new Book(id, this);
        }
    }
}
