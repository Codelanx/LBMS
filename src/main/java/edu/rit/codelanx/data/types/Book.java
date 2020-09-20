package edu.rit.codelanx.data.types;

import com.codelanx.commons.data.FileSerializable;
import com.codelanx.commons.data.ResultRow;
import edu.rit.codelanx.data.state.State;
import edu.rit.codelanx.data.state.UpdatableState;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Book extends UpdatableState implements FileSerializable {

    private final String title;
    private final String isbn;
    private final String author;
    private final String publisher;
    private final String publishDate;
    private final int pageCount;
    private int totalCopies;
    private int checkedOut;

    public Book(ResultRow sql) throws SQLException {
        super(sql.getLong("id"));
        this.title = sql.getString("title");
        this.isbn = sql.getString("isbn");
        this.author = sql.getString("author");
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
        this.author = (String) file.get("author");
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

    public String getAuthor() {
        return this.author;
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
    public Map<String, Object> serialize() {
        Map<String, Object> back = new LinkedHashMap<>();
        back.put("id", this.id);
        back.put("title", this.title);
        back.put("isbn", this.isbn);
        back.put("author", this.author);
        back.put("publisher", this.publisher);
        back.put("publish_date", this.publishDate);
        back.put("page_count", this.pageCount);
        back.put("total_copies", this.totalCopies);
        back.put("checked_out", this.checkedOut);
        return back;
    }
}
