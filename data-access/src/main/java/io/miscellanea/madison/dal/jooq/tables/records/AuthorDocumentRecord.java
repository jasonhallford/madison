/*
 * This file is generated by jOOQ.
 */
package io.miscellanea.madison.dal.jooq.tables.records;


import io.miscellanea.madison.dal.jooq.tables.AuthorDocument;
import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UpdatableRecordImpl;

/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AuthorDocumentRecord extends UpdatableRecordImpl<AuthorDocumentRecord> implements Record2<Long, Long> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>catalog.author_document.author_id</code>.
     */
    public void setAuthorId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>catalog.author_document.author_id</code>.
     */
    public Long getAuthorId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>catalog.author_document.document_id</code>.
     */
    public void setDocumentId(Long value) {
        set(1, value);
    }

    /**
     * Getter for <code>catalog.author_document.document_id</code>.
     */
    public Long getDocumentId() {
        return (Long) get(1);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record2<Long, Long> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row2<Long, Long> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    @Override
    public Row2<Long, Long> valuesRow() {
        return (Row2) super.valuesRow();
    }

    @Override
    public Field<Long> field1() {
        return AuthorDocument.AUTHOR_DOCUMENT.AUTHOR_ID;
    }

    @Override
    public Field<Long> field2() {
        return AuthorDocument.AUTHOR_DOCUMENT.DOCUMENT_ID;
    }

    @Override
    public Long component1() {
        return getAuthorId();
    }

    @Override
    public Long component2() {
        return getDocumentId();
    }

    @Override
    public Long value1() {
        return getAuthorId();
    }

    @Override
    public Long value2() {
        return getDocumentId();
    }

    @Override
    public AuthorDocumentRecord value1(Long value) {
        setAuthorId(value);
        return this;
    }

    @Override
    public AuthorDocumentRecord value2(Long value) {
        setDocumentId(value);
        return this;
    }

    @Override
    public AuthorDocumentRecord values(Long value1, Long value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached AuthorDocumentRecord
     */
    public AuthorDocumentRecord() {
        super(AuthorDocument.AUTHOR_DOCUMENT);
    }

    /**
     * Create a detached, initialised AuthorDocumentRecord
     */
    public AuthorDocumentRecord(Long authorId, Long documentId) {
        super(AuthorDocument.AUTHOR_DOCUMENT);

        setAuthorId(authorId);
        setDocumentId(documentId);
    }
}
