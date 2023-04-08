/*
 * This file is generated by jOOQ.
 */
package io.miscellanea.madison.dal.jooq;


import io.miscellanea.madison.dal.jooq.tables.Identifiers;
import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;

/**
 * A class modelling indexes of tables in catalog.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Indexes {

    // -------------------------------------------------------------------------
    // INDEX definitions
    // -------------------------------------------------------------------------

    public static final Index IDENTIFIERS_FK_DOC_ID = Internal.createIndex(DSL.name("identifiers_fk_doc_id"), Identifiers.IDENTIFIERS, new OrderField[] { Identifiers.IDENTIFIERS.DOC_ID }, false);
}
