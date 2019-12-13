package com.neptunedreams.jobs.data.sqlite;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import com.neptunedreams.framework.data.AbstractDatabaseInfo;
import com.neptunedreams.framework.data.ConnectionSource;
import com.neptunedreams.framework.data.DBField;
import com.neptunedreams.framework.data.Dao;
import com.neptunedreams.jobs.gen.DefaultSchema;
import com.neptunedreams.jobs.gen.tables.records.LeadRecord;
import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.UnknownKeyFor;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import static org.jooq.SQLDialect.SQLITE;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/10/17
 * <p>Time: 11:31 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"StringConcatenation", "HardCodedStringLiteral"})
public class SQLiteInfo extends AbstractDatabaseInfo {

  private static final Class<LeadRecord> RECORD_RECORD_CLASS = LeadRecord.class;

  @SuppressWarnings("JavaDoc")
  public SQLiteInfo() {
    //noinspection HardcodedFileSeparator
    this("/.sqlite.jobs");
  }

  @SuppressWarnings("JavaDoc")
  SQLiteInfo(String homeDir) {
    super(homeDir);
//    init();
  }
  @NotNull
  @Override
  public String getUrl() {
    //noinspection StringConcatenationMissingWhitespace
    return "jdbc:sqlite:" + getHomeDir() + getHome();
  }

  @Override
  public @UnknownKeyFor @NonNull @Initialized <T, PK, F extends DBField> Dao<T, PK, F> getDao(final @UnknownKeyFor @NonNull @Initialized Class<T> entityClass, final @UnknownKeyFor @NonNull @Initialized ConnectionSource source) {
    //noinspection EqualityOperatorComparesObjects
    if (entityClass == RECORD_RECORD_CLASS) {
      //noinspection unchecked
      return (Dao<T, PK, F>) SQLiteRecordDao.create(source);
    }
    throw new IllegalArgumentException(String.valueOf(entityClass));
  }

  @NotNull
  @Override
  public Class<?> getRecordClass() {
    return RECORD_RECORD_CLASS;
  }

  private String getHome() {
    //noinspection HardcodedFileSeparator
    return "/jobs.db";
  }

  @Override
  public void init() throws IOException, SQLException {
    File homeDir = new File(getHomeDir());
    File databaseFile = new File(homeDir, getHome());
    if (!databaseFile.exists()) {
      boolean failed = !databaseFile.createNewFile();
      if (failed) {
        throw new IOException("Failed to create database file at " + databaseFile.getAbsolutePath());
      }
    }
    try {
      Class.forName("org.sqlite.JDBC"); // Not needed for Mac bundle. Needed for execution from a single jar file.
      initialize();
    } catch (ClassNotFoundException e) {
      throw new IOException(e);
    }
  }

  @Override
  public boolean isCreateSchemaAllowed() {
    return true;
  }

  @SuppressWarnings("UseOfSystemOutOrSystemErr")
  @Override
  public void createSchema() {
    //noinspection resource
    DSLContext dslContext = DSL.using(getConnectionSource().getConnection(), SQLITE);
    System.out.printf("DSLContext of %s%n", dslContext.getClass());
    DefaultSchema schema = DefaultSchema.DEFAULT_SCHEMA;
//    dslContext.createSchemaIfNotExists("jobs").execute();
//    Name recordName = new 
    Query[] queries = dslContext.ddl(schema).queries();
    System.out.printf("Total of %d queries.%n", queries.length);
    for (Query q : queries) {
      System.out.printf("Query: %s%n", q);
      if (q.toString().contains("sqlite_sequence")) {
        //noinspection HardcodedLineSeparator
        System.out.println(("(Not Executed)\n"));
      } else {
        try {
          q.execute();
        } catch (DataAccessException e) {
          if (!Objects.toString(e.getMessage()).contains("table record already exists")) {
            throw e;
          }
        }
      }
    }
  }
}
