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
 * @author Miguel Muñoz
 */
@SuppressWarnings({"StringConcatenation", "HardCodedStringLiteral"})
public class SQLiteInfo extends AbstractDatabaseInfo {

  private static final Class<LeadRecord> RECORD_RECORD_CLASS = LeadRecord.class;
  @SuppressWarnings("HardcodedFileSeparator")
  private static final String JOBS_DB = "/jobs.db";
  private final String dbName;

  @SuppressWarnings("JavaDoc")
  public SQLiteInfo() {
    //noinspection HardcodedFileSeparator
    this("/.sqlite.jobs", JOBS_DB);
  }

  /**
   * Create a new SQLiteInfo from a home directory and a database file name. For an in-memory database, use 
   * getInMemoryInfo().
   * @param homeDir The home directory
   * @param dbName The name of the database file
   * @see #getInMemoryInfo() 
   */
  SQLiteInfo(String homeDir, String dbName) {
    super(homeDir);
    this.dbName = dbName;
  }

  /**
   * Create a new SQLiteInfo set up to use an in-memory database. This is primarily for unit tests.
   * @return an SQLiteInfo using an in memory database.
   */
  static SQLiteInfo getInMemoryInfo() {
    return new SQLiteInfo("", ":memory:");
  }


  @Override
  public @NotNull String getUrl() {
    return "jdbc:sqlite:" + getHomeDir() + getHome();
  }

  @Override
  public @NotNull <T, PK, F extends @NotNull DBField> Dao<T, PK, F> getDao(
      final @NotNull Class<T> entityClass,
      final @NotNull ConnectionSource source
  ) {
    //noinspection EqualityOperatorComparesObjects
    if (entityClass == RECORD_RECORD_CLASS) {
      //noinspection unchecked
      return (Dao<T, PK, F>) SQLiteRecordDao.create(source);
    }
    throw new IllegalArgumentException(String.valueOf(entityClass));
  }


  @Override
  public @NotNull Class<?> getRecordClass() {
    return RECORD_RECORD_CLASS;
  }

  private String getHome() {
    return dbName;
  }

  @Override
  public void init() throws IOException, SQLException {
    // For an inMemory database, home will be empty.
    String home = getHomeDir();
    if (!home.isEmpty()) {
      File homeDir = new File(home);
      File databaseFile = new File(homeDir, getHome());
      if (!databaseFile.exists()) {
        boolean failed = !databaseFile.createNewFile();
        if (failed) {
          throw new IOException("Failed to create database file at " + databaseFile.getAbsolutePath());
        }
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
