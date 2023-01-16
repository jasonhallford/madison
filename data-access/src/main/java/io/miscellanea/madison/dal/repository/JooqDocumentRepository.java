package io.miscellanea.madison.dal.repository;

import io.miscellanea.madison.dal.jooq.tables.records.DocumentRecord;
import io.miscellanea.madison.entity.Document;
import io.miscellanea.madison.repository.DocumentRepository;
import io.miscellanea.madison.repository.RepositoryException;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import static io.miscellanea.madison.dal.jooq.tables.Document.DOCUMENT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.Connection;
import java.util.List;

public class JooqDocumentRepository implements DocumentRepository {
    // Fields
    private static final Logger logger = LoggerFactory.getLogger(JooqDocumentRepository.class);

    private final Connection connection;

    // Constructors
    @Inject
    public JooqDocumentRepository(@NotNull Connection connection) {
        this.connection = connection;
    }

    // DocumentRepository
    @Override
    public Document get(@NotNull Long id) {
        Document doc = null;

        try {
            logger.debug("Creating JOOQ query to retrieve document {}.", id);
            DSLContext create = DSL.using(this.connection, SQLDialect.POSTGRES);
            DocumentRecord documentRecord = create.selectFrom(DOCUMENT).
                    where(DOCUMENT.ID.eq(id)).fetchOne();
        } catch (Exception e) {
            throw new RepositoryException("Unable to retrieve document with ID " + id + ".", e);
        }

        return doc;
    }

    @Override
    public void add(Document document) {

    }

    @Override
    public void update(Document document) {

    }

    @Override
    public void remove(Document document) {

    }

    @Override
    public List<Document> subList(int from, int to) throws RepositoryException {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void close() throws Exception {
        if (!this.connection.isClosed()) {
            logger.debug("Closing JDBC connection.");
            this.connection.close();
            logger.debug("JDBC connection successfully closed.");
        }
    }
}
