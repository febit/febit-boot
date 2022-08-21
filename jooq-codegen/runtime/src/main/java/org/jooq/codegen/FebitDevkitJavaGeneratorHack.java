package org.jooq.codegen;

import org.jooq.meta.Database;

public abstract class FebitDevkitJavaGeneratorHack extends JavaGenerator {

    @Override
    void logDatabaseParameters(Database db) {
        initDatabase(db);
        super.logDatabaseParameters(db);
    }

    protected abstract void initDatabase(Database db);
}
