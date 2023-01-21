package io.miscellanea.madison.repository;

import io.miscellanea.madison.entity.Document;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface DocumentRepository extends AutoCloseable {
    Document get(@NotNull Long id);

    void add(Document document);

    void update(Document document);

    void remove(Document document);

    /**
     * Provides a view of the underlying Document collection.
     *
     * @param from The zero-based from which to begin the view. This value is
     *             inclusive.
     * @param to   The zero-based index at which to end the view. This value is
     *             exclusive.
     * @return A subset of the Document collection projected as a <code>List</code>.
     * @throws RepositoryException When unable to generate list.
     */
    List<Document> subList(int from, int to) throws RepositoryException;

    int size();

    @Override
    public void close() throws RepositoryException;
}
