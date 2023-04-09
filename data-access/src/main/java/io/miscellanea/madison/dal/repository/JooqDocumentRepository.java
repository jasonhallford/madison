package io.miscellanea.madison.dal.repository;

import static io.miscellanea.madison.dal.jooq.Tables.AUTHOR;
import static io.miscellanea.madison.dal.jooq.Tables.AUTHOR_DOCUMENT;
import static io.miscellanea.madison.dal.jooq.tables.Document.DOCUMENT;

import io.miscellanea.madison.dal.jooq.tables.records.AuthorDocumentRecord;
import io.miscellanea.madison.dal.jooq.tables.records.AuthorRecord;
import io.miscellanea.madison.dal.jooq.tables.records.DocumentRecord;
import io.miscellanea.madison.document.Document;
import io.miscellanea.madison.entity.Author;
import io.miscellanea.madison.repository.DocumentRepository;
import io.miscellanea.madison.repository.RepositoryException;
import java.sql.Connection;
import java.util.List;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
      DocumentRecord documentRecord =
          create.selectFrom(DOCUMENT).where(DOCUMENT.ID.eq(id)).fetchOne();
    } catch (Exception e) {
      throw new RepositoryException("Unable to retrieve document with ID " + id + ".", e);
    }

    return doc;
  }

  @Override
  public void add(Document document) {
    DSLContext create = DSL.using(this.connection, SQLDialect.POSTGRES);
    create.transaction(
        trx -> {
          // Does the document already exist? If so, then we don't need to add it to the database.
          DocumentRecord documentRecord =
              trx.dsl()
                  .selectFrom(DOCUMENT)
                  .where(DOCUMENT.FINGERPRINT.eq(document.getFingerprint().toString()))
                  .fetchOne();
          if (documentRecord == null) {
            logger.debug(
                "INSERTing document with fingerprint {} into database.", document.getFingerprint());
            documentRecord =
                trx.dsl()
                    .insertInto(DOCUMENT)
                    .set(DOCUMENT.CONTENT_TYPE, document.getContentType())
                    .set(DOCUMENT.FINGERPRINT, document.getFingerprint().toString())
                    .set(DOCUMENT.PAGE_COUNT, document.getPageCount())
                    .set(DOCUMENT.TITLE, document.getTitle())
                    .set(DOCUMENT.ISBN10, document.getIsbn10())
                    .set(DOCUMENT.ISBN13, document.getIsbn13())
                    .returning()
                    .fetchOne();

            document.setId(documentRecord.getId());
            logger.debug(
                "Document with fingerprint {} inserted into database with ID {}.",
                document.getFingerprint(),
                document.getId());
            if (document.getAuthors() != null && document.getAuthors().size() > 0) {
              for (Author author : document.getAuthors()) {
                AuthorRecord authorRecord =
                    trx.dsl().selectFrom(AUTHOR).where(AUTHOR.CODE.eq(author.getCode())).fetchOne();
                if (authorRecord == null) {
                  logger.debug("Adding new author with code {} to database.", author.getCode());
                  authorRecord =
                      trx.dsl()
                          .insertInto(AUTHOR)
                          .set(AUTHOR.FIRST_NAME, author.getFirstName())
                          .set(AUTHOR.LAST_NAME, author.getLastName())
                          .set(AUTHOR.MIDDLE_NAME, author.getMiddleName())
                          .set(AUTHOR.SUFFIX, author.getSuffix())
                          .set(AUTHOR.CODE, author.getCode())
                          .returning()
                          .fetchOne();

                  author.setId(authorRecord.getId());
                  logger.debug(
                      "Successfully inserted author with code {} into database with ID {}.",
                      author.getCode(),
                      author.getId());
                } else {
                  logger.debug(
                      "Author with code {} already exists in database with ID {}.",
                      authorRecord.getCode(),
                      authorRecord.getId());
                  author.setId(authorRecord.getId());
                }

                logger.debug("Connecting author to document.");
                AuthorDocumentRecord authorDocumentRecord =
                    trx.dsl()
                        .selectFrom(AUTHOR_DOCUMENT)
                        .where(AUTHOR_DOCUMENT.AUTHOR_ID.eq(author.getId()))
                        .and(AUTHOR_DOCUMENT.DOCUMENT_ID.eq(document.getId()))
                        .fetchOne();
                if (authorDocumentRecord == null) {
                  trx.dsl()
                      .insertInto(AUTHOR_DOCUMENT)
                      .set(AUTHOR_DOCUMENT.DOCUMENT_ID, document.getId())
                      .set(AUTHOR_DOCUMENT.AUTHOR_ID, author.getId())
                      .execute();
                  logger.debug("Successfully linked author and document.");
                } else {
                  logger.debug("Linkage already exists between author and document.");
                }
              }
            }
          } else {
            logger.warn(
                "A document with fingerprint {} already exists in the collection; ignoring request.",
                document.getFingerprint());
          }
        });
  }

  @Override
  public void update(Document document) {}

  @Override
  public void remove(Document document) {}

  @Override
  public List<Document> subList(int from, int to) throws RepositoryException {
    return null;
  }

  @Override
  public int size() {
    DSLContext create = DSL.using(this.connection, SQLDialect.POSTGRES);
    return create.fetchCount(DOCUMENT);
  }

  @Override
  public void close() throws RepositoryException {
    try {
      if (!this.connection.isClosed()) {
        logger.debug("Closing JDBC connection.");
        this.connection.close();
        logger.debug("JDBC connection successfully closed.");
      }
    } catch (Exception e) {
      throw new RepositoryException("Unable to close document repository.", e);
    }
  }
}
