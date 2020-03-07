package com.neptunedreams.jobs.data.sqlite;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import com.neptunedreams.framework.data.ConnectionSource;
import com.neptunedreams.framework.data.Dao;
import com.neptunedreams.jobs.data.LeadField;
import com.neptunedreams.jobs.gen.Tables;
import com.neptunedreams.jobs.gen.tables.Lead;
import com.neptunedreams.jobs.gen.tables.records.LeadRecord;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.ResultQuery;
import org.jooq.SelectConditionStep;
import org.jooq.SelectSeekStep1;
import org.jooq.SelectSelectStep;
import org.jooq.SelectWhereStep;
import org.jooq.TableField;
import org.jooq.impl.DSL;

import static com.neptunedreams.jobs.gen.Tables.*;
import static org.jooq.SQLDialect.*;
import static org.jooq.impl.DSL.*;

/**
 * Create statement: 
 * 
 * CREATE TABLE IF NOT EXISTS lead (
 *   id             INTEGER      NOT NULL PRIMARY KEY AUTOINCREMENT,
 *   company        VARCHAR(512) NOT NULL collate noCase,
 *   contact_name   VARCHAR(512) NOT NULL collate noCase,
 *   dice_posn      VARCHAR(512) NOT NULL collate noCase,
 *   dice_id        VARCHAR(512) NOT NULL collate noCase,
 *   email          VARCHAR(512) NOT NULL collate noCase,
 *   phone1         VARCHAR(512) NOT NULL collate noCase,
 *   phone2         VARCHAR(512) NOT NULL collate noCase,
 *   fax            VARCHAR(512) NOT NULL collate noCase,
 *   website        VARCHAR(512) NOT NULL collate noCase,
 *   skype          VARCHAR(512) NOT NULL collate noCase,
 *   description    VARCHAR      NOT NULL collate noCase,
 *   history        VARCHAR      NOT NULL collate nocase,
 *   createdOn      DATETIME     NOT NULL DEFAULT (DATETIME('now')),
 *   phone3         VARCHAR(512) NOT NULL DEFAULT '' collate nocase,
 *   client         VARCHAR(512) NOT NULL DEFAULT '' collate nocase
 * );
 * 
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 1:03 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings("StringConcatenation")
public final class SQLiteRecordDao implements Dao<LeadRecord, Integer, LeadField> {

  private static final Map<LeadField, @NonNull TableField<LeadRecord, ?>> fieldMap = makeFieldMap();
  private final ConnectionSource connectionSource;
  private @NonNull Connection connection;

  private static Map<LeadField, @NonNull TableField<LeadRecord, ?>> makeFieldMap() {
    final EnumMap<LeadField, @NonNull TableField<LeadRecord, ?>> fieldMap = new EnumMap<>(LeadField.class);
    fieldMap.put(LeadField.ID,       Lead.LEAD.ID);
    fieldMap.put(LeadField.Company,   Lead.LEAD.COMPANY);
    fieldMap.put(LeadField.ContactName, Lead.LEAD.CONTACT_NAME);
    fieldMap.put(LeadField.Client, Lead.LEAD.CLIENT);
    fieldMap.put(LeadField.DicePosn, Lead.LEAD.DICE_POSN);
    fieldMap.put(LeadField.DiceID, Lead.LEAD.DICE_ID);
    fieldMap.put(LeadField.EMail, Lead.LEAD.EMAIL);
    fieldMap.put(LeadField.Phone1, Lead.LEAD.PHONE1);
    fieldMap.put(LeadField.Phone2, Lead.LEAD.PHONE2);
    fieldMap.put(LeadField.Phone3, Lead.LEAD.PHONE3);
    fieldMap.put(LeadField.Fax, Lead.LEAD.FAX);
    fieldMap.put(LeadField.WebSite, Lead.LEAD.WEBSITE);
    fieldMap.put(LeadField.Skype, Lead.LEAD.SKYPE);
    fieldMap.put(LeadField.Description, Lead.LEAD.DESCRIPTION);
    fieldMap.put(LeadField.History, Lead.LEAD.HISTORY);
    fieldMap.put(LeadField.CreatedOn, Lead.LEAD.CREATEDON);
    for (LeadField leadField: LeadField.values()) {
      if (leadField.isField() && !fieldMap.containsKey(leadField)) {
        throw new IllegalStateException("Missing Field in FieldMap: " + leadField);
      }
    }
    return fieldMap;
  }

  // If you change the CREATE statement, you need to change it two other places. First, you should change the comment
  // at the beginning of this file. But more important, you should delete the master database 
  // at src/main/resources/sql/generateFromJobHunt.db anc re-create it using the revised CREATE statement.
  // Also, this statement specifies the primary key as a property, instead of as a constraint at the end of the 
  // statement. This is necessary so a null id will cause the database to generate a new valid id. If it's specified
  // in a CONSTRAINT clause, a null id will throw an exception instead. In fact, even if I specify the collate noCase
  // constraints as named constraints, a null ID will still throw an exception.
  @SuppressWarnings("HardcodedLineSeparator")
  private static final String CREATE_TABLE = 
      "CREATE TABLE IF NOT EXISTS lead (\n" +
          "  id             INTEGER      NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
          "  company        VARCHAR(512) NOT NULL collate noCase,\n" +
          "  contact_name   VARCHAR(512) NOT NULL collate noCase,\n" +
          "  dice_posn      VARCHAR(512) NOT NULL collate noCase,\n" +
          "  dice_id        VARCHAR(512) NOT NULL collate noCase,\n" +
          "  email          VARCHAR(512) NOT NULL collate noCase,\n" +
          "  phone1         VARCHAR(512) NOT NULL collate noCase,\n" +
          "  phone2         VARCHAR(512) NOT NULL collate noCase,\n" +
          "  fax            VARCHAR(512) NOT NULL collate noCase,\n" +
          "  website        VARCHAR(512) NOT NULL collate noCase,\n" +
          "  skype          VARCHAR(512) NOT NULL collate noCase,\n" +
          "  description    VARCHAR      NOT NULL collate noCase,\n" +
          "  history        VARCHAR      NOT NULL collate nocase,\n" +
          "  createdOn      DATETIME     NOT NULL DEFAULT (DATETIME('now')),\n" +
          "  phone3         VARCHAR(512) NOT NULL DEFAULT '' collate nocase,\n" +
          "  client         VARCHAR(512) NOT NULL DEFAULT '' collate nocase" +
          ");";
  private static final char WC = '%';
  private DSLContext getDslContext() throws SQLException {
    assert connection != null;
    assert connectionSource != null;
    if (connection.isClosed()) {
      connection = connectionSource.getConnection();
    }
    return DSL.using(connection, SQLITE);
  }

  private SQLiteRecordDao(ConnectionSource source) {
    connectionSource = source;
    connection = source.getConnection();
  }
  
  private SQLiteRecordDao launch() {
    try {
      createTableIfNeeded();
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }
    return this;
  }

  static SQLiteRecordDao create(ConnectionSource source) {
    return new SQLiteRecordDao(source).launch();
  }

  @Override
  public boolean createTableIfNeeded() throws SQLException {
    /* All my efforts to generate the table using jOOQ failed, so I had to resort to direct SQL. */

    DSLContext dslContext = getDslContext();

    // Creates the table using the sql statement
    dslContext.execute(CREATE_TABLE);
    
    // Neither of the following two code blocks works, so I needed to use my CREATE_TABLE String to create the table I needed.
//    dslContext.ddl(Tables.LEAD).executeBatch();

//    DefaultSchema schema = DefaultSchema.DEFAULT_SCHEMA;
//    dslContext.createSchemaIfNotExists("jobs").execute();
//    Name recordName = new 
//    Query[] queries = dslContext.ddl(schema).queries();
//    System.out.printf("Total of %d queries.%n", queries.length);
//    for (Query q: queries) {
//      System.out.printf("Query: %s%n", q);
////      if (!q.toString().contains("sqlite_sequence")) {
////        q.execute();
////      }
//    }
    
    // Here's what fails when I try to use jOOQ to create my table:
    // 1. The created table does not have collate noCase for each text field. This means my sorting will be
    //    case-sensitive, which I hate.
    // 2. The create statement looks has the primary key specified as a constraint, rather than a property, like this:
    //       create table if not exists site (id integer NOT NULL, ... CONSTRAINT pk_site primary key(id));
    //    instead of this:
    //       create table if not exists site (id integer NOT NULL primary key, ... );
    //    I don't see why this should make a difference, but it does. The effect is that when I specify null for 
    //    the ID, the first case will throw an SQLiteException with this message: 
    //    A NOT NULL constraint failed (NOT NULL constraint failed: site.id). The second case will work because the 
    //    database generates a valid id. I don't know why they behave differently.
    
    return true;
  }

  @NotNull
  @Override
  public Collection<LeadRecord> getAll(final @Nullable LeadField orderBy) throws SQLException {

    DSLContext dslContext = getDslContext();
    try (SelectWhereStep<LeadRecord> LeadRecords = dslContext.selectFrom(LEAD)) {
      if (orderBy == null) {
        return LeadRecords.fetch();
      } else {
        return getOrderedLeadRecords(orderBy, LeadRecords);
      }
    }
  }

  private Collection<LeadRecord> getOrderedLeadRecords(final @NonNull LeadField orderBy, final SelectWhereStep<LeadRecord> LeadRecords) {
    try (final SelectSeekStep1<LeadRecord, ?> foundRecords = LeadRecords.orderBy(getField(orderBy))) {
      return foundRecords.fetch();
    }
  }

  @NotNull
  @Override
  public Collection<LeadRecord> find(@NotNull final String text, final @Nullable LeadField orderBy) throws SQLException {
    final String wildCardText = wrapWithWildCards(text);

    DSLContext dslContext = getDslContext();

    try (
        final SelectWhereStep<LeadRecord> LeadRecords = dslContext.selectFrom(LEAD);
        SelectConditionStep<LeadRecord> where = LeadRecords.where(
          LEAD.COMPANY.like(wildCardText).or(
          LEAD.CONTACT_NAME.like(wildCardText).or(
          LEAD.DICE_POSN.like(wildCardText).or(
          LEAD.DICE_ID.like(wildCardText).or(
          LEAD.EMAIL.like(wildCardText).or(
          LEAD.PHONE1.like(wildCardText).or(
          LEAD.PHONE2.like(wildCardText).or(
          LEAD.FAX.like(wildCardText).or(
          LEAD.WEBSITE.like(wildCardText).or(
          LEAD.SKYPE.like(wildCardText).or(
          LEAD.DESCRIPTION.like(wildCardText).or(
          LEAD.HISTORY.like(wildCardText)))))))))))));
        final ResultQuery<LeadRecord> query = (orderBy == null) ?
          where : 
          where.orderBy(getField(orderBy))
    ) {
      return query.fetch();
    }
  }

  /**
   * This used to cast to upper() before returning the field, to implement case-insensitive sorting. Now
   * this is done in the table definitions, this just extracts the right TableField from the fieldMap.
   * @param orderBy The orderBy field
   * @return A Field{@literal <String>} to pass to the orderBy() method to support case insensitive ordering.
   */
  private @NonNull TableField<LeadRecord, ?> getField(final LeadField orderBy) {
    return Objects.requireNonNull(fieldMap.get(orderBy));
  }

  private @NonNull String wrapWithWildCards(final String text) {
    return WC + text + WC;
  }

  @NotNull
  @Override
  public Collection<LeadRecord> findAny(final @Nullable LeadField orderBy, final String... text) throws SQLException {

    DSLContext dslContext = getDslContext();
    Condition condition = LEAD.COMPANY.lt(""); // Should always be false

    try (final SelectWhereStep<LeadRecord> LeadRecords = dslContext.selectFrom(LEAD))
    {
      for (String txt : text) {
        String wildCardText = wrapWithWildCards(txt);
          condition = condition.or(
            LEAD.COMPANY.like(wildCardText)).or(
            LEAD.CONTACT_NAME.like(wildCardText)).or(
            LEAD.DICE_POSN.like(wildCardText)).or(
            LEAD.DICE_ID.like(wildCardText)).or(
            LEAD.EMAIL.like(wildCardText)).or(
            LEAD.PHONE1.like(wildCardText)).or(
            LEAD.PHONE2.like(wildCardText)).or(
            LEAD.FAX.like(wildCardText)).or(
            LEAD.SKYPE.like(wildCardText)).or(
            LEAD.WEBSITE.like(wildCardText)).or(
            LEAD.DICE_ID.like(wildCardText)).or(
            LEAD.HISTORY.like(wildCardText));
      }
      return getFromQuery(orderBy, LeadRecords, condition);
    }
  }

  @NotNull
  @Override
  public Collection<LeadRecord> findAll(final @Nullable LeadField orderBy, final String... text) throws SQLException {

    DSLContext dslContext = getDslContext();
    Condition condition = LEAD.COMPANY.ge(""); // Should always be true
    try (final SelectWhereStep<LeadRecord> leadRecords = dslContext.selectFrom(LEAD)) {
      for (String txt : text) {
        String wildCardText = wrapWithWildCards(txt);
        condition = condition.and(
                LEAD.COMPANY.like(wildCardText).or(
                LEAD.CONTACT_NAME.like(wildCardText)).or(
                LEAD.DICE_POSN.like(wildCardText)).or(
                LEAD.DICE_ID.like(wildCardText)).or(
                LEAD.EMAIL.like(wildCardText)).or(
                LEAD.PHONE1.like(wildCardText)).or(
                LEAD.PHONE2.like(wildCardText)).or(
                LEAD.FAX.like(wildCardText)).or(
                LEAD.WEBSITE.like(wildCardText)).or(
                LEAD.SKYPE.like(wildCardText)).or(
                LEAD.DICE_ID.like(wildCardText)).or(
                LEAD.HISTORY.like(wildCardText)));
      }
      return getFromQuery(orderBy, leadRecords, condition);
    }
  }

  @NotNull
  @Override
  public Collection<LeadRecord> findInField(
      @NotNull final String text,
      final @NonNull LeadField findBy,
      final @Nullable LeadField orderBy
  ) throws SQLException {
    String wildCardText = wrapWithWildCards(text);

    DSLContext dslContext = getDslContext();

    final @NonNull TableField<LeadRecord, ?> findByField = Objects.requireNonNull(fieldMap.get(findBy));
    try (
        final SelectWhereStep<LeadRecord> leadRecords = dslContext.selectFrom(LEAD);
        final SelectConditionStep<LeadRecord> where = leadRecords.where((findByField.like(wildCardText)))
    ) {
      final ResultQuery<LeadRecord> query;
      query = (orderBy == null) ?
        where :
        where.orderBy(getField(orderBy));
      return query.fetch();
    }
  }

  @NotNull
  @Override
  public Collection<LeadRecord> findAnyInField(final @NonNull LeadField findBy, final @Nullable LeadField orderBy, final String... text) throws SQLException {
    DSLContext dslContext = getDslContext();

    final @NonNull TableField<LeadRecord, ?> findByField = Objects.requireNonNull(fieldMap.get(findBy));
    Condition condition = LEAD.COMPANY.lt(""); // Should always be false
    try (SelectWhereStep<LeadRecord> leadRecords = dslContext.selectFrom(LEAD)) {
      for (String txt : text) {
        String wildCardText = wrapWithWildCards(txt);
        condition = condition.or(findByField.like(wildCardText));
      }
      return getFromQuery(orderBy, leadRecords, condition);
    }
  }

  @NotNull
  @Override
  public Collection<LeadRecord> findAllInField(final @NonNull LeadField findBy, final @Nullable LeadField orderBy, final String... text) throws SQLException {

    DSLContext dslContext = getDslContext();

    final @NonNull TableField<LeadRecord, ?> findByField = Objects.requireNonNull(fieldMap.get(findBy));
    Condition condition = LEAD.COMPANY.ge(""); // Should always be true
    try (SelectWhereStep<LeadRecord> leadRecords = dslContext.selectFrom(LEAD)) {
      for (String txt : text) {
        String wildCardText = wrapWithWildCards(txt);
        condition = condition.and(findByField.like(wildCardText));
      }
      return getFromQuery(orderBy, leadRecords, condition);
    }
  }

  private Collection<LeadRecord> getFromQuery(
      final @Nullable LeadField orderBy,
      final SelectWhereStep<LeadRecord> leadRecords,
      final Condition condition
  ) {
    final ResultQuery<LeadRecord> query;
    try (SelectConditionStep<LeadRecord> where = leadRecords.where(condition)) {
      query = (orderBy == null) ?
          where :
          where.orderBy(getField(orderBy));
    }
    return query.fetch();
  }

  @Override
  public void update(final LeadRecord entity) { // throws SQLException {
    entity.store();
  }

  @Override
  public void insertOrUpdate(final LeadRecord entity) throws SQLException {
    final Integer id = entity.getId();
    if ((id == null) || (id == 0)) {
      insert(entity);
    } else {
      update(entity);
    }
  }

  @Override
  @SuppressWarnings("argument.type.incompatible")
  public void insert(final LeadRecord entity) throws SQLException {

    DSLContext dslContext = getDslContext();
    //noinspection ConstantConditions
    entity.setId(null); // argument.type.incompatible null assumed not allowed in generated code.
    Integer id = entity.getId(); 
    entity.setId(id);
    dslContext.attach(entity);
    entity.insert();
  }

  @Override
  public void delete(final LeadRecord entity) { // throws SQLException {
    entity.delete();
  }
  
  @Override
  public Integer getNextId() throws SQLException {

    DSLContext dslContext = getDslContext();
    try (
        final SelectSelectStep<Record1<Integer>> select = dslContext.select(max(Lead.LEAD.ID))
    ) {
      Result<Record1<Integer>> result = select.fetch();
      return result.get(0).getValue(1, Integer.class); // I'm guessing that Result (a List) is zero-based, but Record1 is one-based.
    }
  }

  @Override
  public Integer getPrimaryKey(final LeadRecord entity) {
    return entity.getId();
  }

  @Override
  public void setPrimaryKey(final LeadRecord entity, final Integer primaryKey) {
    entity.setId(primaryKey);
  }

  @Override
  public int getTotal() throws SQLException {
    DSLContext dslContext = getDslContext();
    return dslContext.fetchCount(Tables.LEAD);
  }
}
