package io.miscellanea.madison.dal;

import com.google.inject.throwingproviders.CheckedProvider;
import java.sql.SQLException;

public interface ConnectionProvider<T> extends CheckedProvider<T> {
    T get() throws SQLException;
}
