package io.miscellanea.madison.api.catalog.entity;

import io.miscellanea.madison.entity.EntityMapper;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DocumentToDocumentEntityMapper implements EntityMapper<io.miscellanea.madison.document.Document, Document> {
    // Constructors
    public DocumentToDocumentEntityMapper() {
    }

    // EntityMapper
    @Override
    public Document map(io.miscellanea.madison.document.Document from) {
        Document to = new Document();

        to.setId(from.getId());
        to.setFingerprint(from.getFingerprint().toString());
        to.setContentType(from.getContentType());
        to.setTitle(from.getTitle());
        to.setPageCount(from.getPageCount());

        Identifiers identifiers = new Identifiers();
        identifiers.setIsbn10(from.getIsbn10());
        identifiers.setIsbn13(from.getIsbn13());
        identifiers.setDocument(to);
        to.setIdentifiers(identifiers);

        for (var author : from.getAuthors()) {
            Author toAuthor = new Author();
            toAuthor.setId(author.getId());
            toAuthor.setCode(author.getCode());
            toAuthor.setFirstName(author.getFirstName());
            toAuthor.setMiddleName(author.getMiddleName());
            toAuthor.setLastName(author.getLastName());
            toAuthor.setSuffix(author.getSuffix());

            to.addAuthor(toAuthor);
        }

        return to;
    }
}
