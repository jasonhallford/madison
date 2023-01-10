/*
 * This file is generated by jOOQ.
 */
package io.miscellanea.madison.dal.jooq;


import io.miscellanea.madison.dal.jooq.tables.Author;
import io.miscellanea.madison.dal.jooq.tables.AuthorDocument;
import io.miscellanea.madison.dal.jooq.tables.Document;
import io.miscellanea.madison.dal.jooq.tables.Identifiers;

import java.util.Arrays;
import java.util.List;

import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Catalog extends SchemaImpl {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>catalog</code>
     */
    public static final Catalog CATALOG = new Catalog();

    /**
     * The table <code>catalog.author</code>.
     */
    public final Author AUTHOR = Author.AUTHOR;

    /**
     * The table <code>catalog.author_document</code>.
     */
    public final AuthorDocument AUTHOR_DOCUMENT = AuthorDocument.AUTHOR_DOCUMENT;

    /**
     * The table <code>catalog.document</code>.
     */
    public final Document DOCUMENT = Document.DOCUMENT;

    /**
     * The table <code>catalog.identifiers</code>.
     */
    public final Identifiers IDENTIFIERS = Identifiers.IDENTIFIERS;

    /**
     * No further instances allowed
     */
    private Catalog() {
        super("catalog", null);
    }


    @Override
    public org.jooq.Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        return Arrays.asList(
            Author.AUTHOR,
            AuthorDocument.AUTHOR_DOCUMENT,
            Document.DOCUMENT,
            Identifiers.IDENTIFIERS
        );
    }
}
