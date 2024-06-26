package com.neptunedreams.jobs.data.sqlite;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.neptunedreams.framework.data.ConnectionSource;
import com.neptunedreams.framework.data.DatabaseInfo;
import com.neptunedreams.jobs.data.LeadField;
import com.neptunedreams.jobs.gen.tables.records.LeadRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

//import com.neptunedreams.jobs.data.Record;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/27/17
 * <p>Time: 5:42 PM
 *
 * @author Miguel Muñoz
 */
@SuppressWarnings({"HardCodedStringLiteral", "HardcodedLineSeparator", "MagicNumber"})
public class SqLiteRecordDaoTest {

  @SuppressWarnings("initialization.static.field.uninitialized")
  private static ConnectionSource connectionSource;
  @SuppressWarnings("initialization.static.field.uninitialized")
  private static SQLiteRecordDao dao;

  @Before
  public void setup() throws SQLException, IOException {
    System.err.printf("Before%n%n"); // NON-NLS
    final DatabaseInfo info = SQLiteInfo.getInMemoryInfo();
    System.out.printf("URL: %s%n", info.getUrl());
    info.init();
    connectionSource = info.getConnectionSource();
    dao = (SQLiteRecordDao) info.<LeadRecord, Integer, LeadField>getDao(LeadRecord.class, connectionSource);
  }
  
  @After
  public void tearDown() throws SQLException {
    System.err.printf("After%n%n"); // NON-NLS
    assert dao != null;
    Collection<LeadRecord> results = dao.findAll(null);
    for (LeadRecord leadRecord : results) {
      leadRecord.delete();
    }
  }

  private void doTestDao(SQLiteRecordDao dao, ConnectionSource connectionSource) throws SQLException {
    assertNotNull(connectionSource);
//    ensureHomeExists(info.getHomeDir());
    assertNotNull(dao);
    // todo: fix CreateTableIfNeeded when the whole schema was created.
//    dao.createTableIfNeeded();
//    if (info.isCreateSchemaAllowed()) {
//      info.createSchema();
//    }
    LeadRecord record1 = new LeadRecord(0,"TestLeadAlpha", "testName", "testPw", "testDiceID\nNote line 2\nNoteLine 3", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now()));
//    LeadRecord record1 = createRecord("TestLeadAlpha", "testName", "testPw", "testDiceID\nNote line 2\nNoteLine 3");
    //noinspection recompany,resource
    assertFalse(connectionSource.getConnection().isClosed());

    Collection<LeadRecord> allRecords = showAllRecords(dao, 0);
    assertEquals(0, allRecords.size());

    dao.insert(record1);
    Integer r1Id = record1.getId();
    allRecords = showAllRecords(dao, 1);
    assertEquals(1, allRecords.size());
    assertEquals(allRecords.iterator().next().getId(), r1Id);

    LeadRecord record2 = new LeadRecord(0, "t2Lead", "t2User", "t2Pw", "t2Note", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now()));
//    LeadRecord record2 = createRecord("t2Lead", "t2User", "t2Pw", "t2Note");
    dao.insert(record2);
    Integer r2Id = record2.getId();
    assertNotEquals(r1Id, r2Id);
    allRecords = showAllRecords(dao, 2);
    assertEquals(2, allRecords.size());
//    Set<Integer> set1 = new HashSet<>();for (LeadRecord rr: allRecords) set1.add(rr.getId());
    Set<Integer> set2 = allRecords
        .stream()
        .map(LeadRecord::getId)
        .collect(Collectors.toSet());
    assertThat(set2, hasItems(r1Id, r2Id));

    allRecords = dao.getAll(LeadField.Company);
    System.out.printf("getAll() returned %d records, expecting 2%n", allRecords.size());
    assertEquals(2, allRecords.size());
    showAllRecords(dao, 2);

    Collection<LeadRecord> foundRecords = dao.find("alpha", LeadField.Company);
    System.out.printf("find(alpha) returned %d records, expecting 1%n", foundRecords.size());
    assertEquals(1, foundRecords.size()); // I expect this to fail
    record1 = foundRecords.iterator().next();

    // Test update
    final String revisedName = "revisedName";
    String originalName = record1.getContactName();
    record1.setContactName(revisedName);
    System.out.printf("Changing the Name from %s to %s%n", originalName, revisedName);
    dao.update(record1);
    foundRecords = dao.find("alpha", LeadField.Company);
    System.out.printf("Found %d records%n", foundRecords.size());
    assertEquals(1, foundRecords.size());
    LeadRecord revisedRecord = foundRecords.iterator().next();
    assertEquals("revisedName", revisedRecord.getContactName());
    testShowRecord(revisedRecord);

    int deletedId = revisedRecord.getId();
    dao.delete(revisedRecord);
    allRecords = dao.getAll(LeadField.Company);
    System.out.printf("Total of %d records after deleting id %d%n", allRecords.size(), deletedId);
    assertEquals(1, allRecords.size());
    LeadRecord remainingRecord = allRecords.iterator().next();
    dao.delete(remainingRecord);
    allRecords = dao.getAll(LeadField.Company);
    assertEquals(0, allRecords.size());
    System.out.printf("Total of %d records after deleting 1%n", allRecords.size());

//    LeadRecord record1b = new LeadRecord(1, "TestLeadAlpha", "testName", "testPw", "testDiceID\nNote line 2\nNoteLine 3");
//    dao.insert(record1b);
//    allRecords = dao.getAll(com.neptunedreams.jobs.data.LeadField.COMPANY);
//    assertEquals(0, allRecords.size());
//    System.out.printf("Total of %d records after lastInsert 1%n", allRecords.size());

  }

  private Collection<LeadRecord> showAllRecords(final SQLiteRecordDao dao, int expectedCount) throws SQLException {
    Collection<LeadRecord> allRecords = dao.getAll(LeadField.Company);
    System.out.printf("getAll() returned %d records, expecting %d%n", allRecords.size(), expectedCount);
//    DataUtil.printRecord(allRecords, LeadRecord::getId, LeadRecord::getCompany);
    for (LeadRecord rr: allRecords) {
      System.out.println(rr);
    }
    if (expectedCount >= 0) {
      assertEquals(expectedCount, allRecords.size());
    }
    return allRecords;
  }

  @SuppressWarnings("unused")
  private LeadRecord createRecord(String company, String contactName, String dicePosn, String note) {
    LeadRecord record = new LeadRecord();
    record.setContactName(contactName);
    record.setCompany(company);
    record.setDicePosn(dicePosn);
    record.setDiceId(note);
    return record;
  }

  private void testShowRecord(final LeadRecord record) {
    System.out.printf("Record: %n  id: %d%n  sr: %s%n  un: %s%n  pw: %s%n  Nt: %s%n",
        record.getId(), record.getCompany(), record.getContactName(),
        record.getDicePosn(), record.getDiceId());
  }
  
  @Test
  public void testFindAny() throws SQLException {
    System.err.println("testFindAny()");
    assert connectionSource != null;
    assert dao != null;
    //noinspection recompany,resource
    assertFalse(connectionSource.getConnection().isClosed());
    Collection<LeadRecord> allRecords = showAllRecords(dao, 0);
    assertEquals(0, allRecords.size());
    setupFindTests();
    Collection<LeadRecord> results;
    assert dao != null;

    // Find Any
    results = dao.findAny(null, "bravo", "charlie", "delta");
    List<Integer> ids = getIds(results);
    System.out.printf("IDs: %s%n", ids);
    assertThat(ids, hasItems(1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 13, 14, 15, 16, 17, 18));
    assertEquals(17, results.size());

    results = dao.findAny(LeadField.Company, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 7, 4, 12, 2, 11, 14, 6, 13, 15, 3, 8, 1, 10, 16, 5, 18, 17));

    results = dao.findAny(null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(1, 2, 3, 4, 5, 6, 8, 13, 16, 17, 18));
    assertEquals(11, results.size());

    results = dao.findAny(LeadField.Company, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 4, 2, 6, 13, 3, 8, 1, 16, 5, 18, 17));

    // Find All

    results = dao.findAll(null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(4, 17, 18));
    assertEquals(3, results.size());

    results = dao.findAll(LeadField.Company, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 4, 18, 17));

    results = dao.findAll(null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(1, 3, 4, 17, 18));
    assertEquals(5, results.size());

    results = dao.findAll(LeadField.Company, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 4, 3, 1, 18, 17));

    // Find Any In Field

    results = dao.findAnyInField(LeadField.Company, null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(1, 2, 3, 4, 5, 6, 7, 10, 13, 14, 16, 17, 18));
    assertEquals(13, results.size());

    results = dao.findAnyInField(LeadField.Company, LeadField.Company, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 7, 4, 2, 14, 6, 13, 3, 1, 10, 16, 5, 18, 17));

    results = dao.findAnyInField(LeadField.ContactName, null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(1, 2, 5, 6, 7, 8, 11, 17, 18));
    assertEquals(9, results.size());

    results = dao.findAnyInField(LeadField.ContactName, LeadField.Company, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 7, 2, 11, 6, 8, 1, 5, 18, 17));

    results = dao.findAnyInField(LeadField.DicePosn, null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(3, 4, 6, 12));
    assertEquals(4, results.size());

    results = dao.findAnyInField(LeadField.DicePosn, LeadField.Company, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 4, 12, 6, 3));

    results = dao.findAnyInField(LeadField.DiceID, null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(4, 15, 17, 18));
    assertEquals(4, results.size());

    results = dao.findAnyInField(LeadField.DiceID, LeadField.Company, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 4, 15, 18, 17));


    results = dao.findAnyInField(LeadField.Company, null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(1, 2, 3, 4, 5, 13, 16, 17, 18));
    assertEquals(9, results.size());

    results = dao.findAnyInField(LeadField.Company, LeadField.Company, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 4, 2, 13, 3, 1, 16, 5, 18, 17));

    results = dao.findAnyInField(LeadField.ContactName, null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(1, 5, 6, 8));
    assertEquals(4, results.size());

    results = dao.findAnyInField(LeadField.ContactName, LeadField.Company, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 6, 8, 1, 5));

    results = dao.findAnyInField(LeadField.DicePosn, null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(3));
    assertEquals(1, results.size());

    results = dao.findAnyInField(LeadField.DicePosn, LeadField.Company, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 3));

    results = dao.findAnyInField(LeadField.DiceID, null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(4, 17, 18));
    assertEquals(3, results.size());

    results = dao.findAnyInField(LeadField.DiceID, LeadField.Company, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 4, 18, 17));
  }
  
  private List<Integer> getIds(Collection<LeadRecord> records) {
    List<Integer> ids = new LinkedList<>();
    for (LeadRecord r : records) {
      ids.add(r.getId());
    }
    return ids;
  }
  
  private boolean arraysMatch(List<Integer> list, int... values) {
    if (list.size() != values.length) {
      throw new IllegalStateException(String.format("Size mismatch: expected %d, found %d", values.length, list.size()));
    }
    Iterator<Integer> itr = list.iterator();
    for (int i: values) {
      final Integer next = itr.next();
      if (i != next) {
        throw new IllegalStateException(String.format("Order mismatch: Expected %d found %s: %nExpected %s%n   Found %s",
            i, next, Arrays.toString(values), list));
      }
    }
    return true;
  }
  
  private void setupFindTests() throws SQLException {
    LeadRecord[] records = {
        new LeadRecord(1, "mBravoCompany", "xCharlieName", "", "pw", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(2, "EBravoCompany", "xDeltaName", "", "pw", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(3, "kBravoCompany", "dummy", "", "xCharliePw", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(4, "BBravoCompany", "name", "", "xDeltaPw", "xCharlieDiceID", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(5, "pCharlieCompany", "xCharlieName", "", "pw", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(6, "HDeltaCompany", "xCharlieName", "", "xDeltaPw", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(7, "aDeltaCompany", "xDeltaName", "", "pw", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(8, "lCompany", "xCharlieName", "", "pw", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(9, "dCompany", "name", "", "pw", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(10, "NDeltaCompany", "name", "", "pw", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(11, "fCompany", "xDeltaName", "", "pw", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(12, "cCompany", "xName", "", "xDeltaPw", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(13, "iBravoCompany", "xEchoName", "", "pw", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(14, "GDeltaCompany", "xEchoName", "", "pw", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(15, "jCompany", "xName", "", "xpw", "xDeltaZ", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(16, "OBravoCompany", "name", "", "pw", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(17, "RBravoCompany", "xDelta", "", "pw", "xCharlieDiceID", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(18, "qCharlieCompany", "xDeltaName", "", "pw", "zBravoDiceID", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
    };
    assert dao != null;
    for (LeadRecord r: records) {
      dao.insert(r);
    }
    showAllRecords(dao, 18);
//    Collection<LeadRecord> all = dao.getAll(LeadField.ID);
//    System
  }
  
  private void setUpFindAllTests() throws SQLException {
    LeadRecord[] records = {
        new LeadRecord(1, "mBravoCompany CharlieX", "xCharlieName", "", "pw", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(2, "NBravoCompany DeltaX", "xDeltaName", "", "aBravo bEchoX cCharlieZ", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(3, "KBravoCompany EchoX", "dummy", "", "xCharliePw bBravoX aDeltaZ", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(4, "bBravoCompany CharlieX Delta Force", "name", "", "xDeltaPw", "xCharlieDiceID", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(5, "pCharlieCompany", "xCharlieName yBravo", "", "pw", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(6, "HDeltaCompany", "xCharlieNameX yDeltaX", "", "xDeltaPw", "xDeltaNameX PBravoNameX ZCharlieX", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(7, "aDeltaCompany", "xDeltaNameX PBravoNameX ZCharlieX", "", "pw", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(8, "LCompany", "xCharlieName", "", "pw", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(9, "dCompany", "name", "", "CharlieXpw", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(10, "eDeltaCompany", "name", "", "CharlieX BravoPw XDeltaZ", "XBravoZ aCharlieZ", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(11, "JCompany", "xDeltaName zCharlieX bBravoZ", "", "pw", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(12, "CCompany", "xName", "", "xDeltaPw", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(13, "qBravoCompany dDeltaX aCharlieB", "xEchoName", "", "ABravoZ BDeltaX bCharlieX pw", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(14, "GDeltaCompany", "xEchoName", "", "pw", "xBravo bCharlie ZDeltaX", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(15, "fCompany", "xName", "", "xpw", "xDeltaNameX PBravoNameX ZCharlieX", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(16, "OBravoCompany", "name", "", "pw", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(17, "rBravoCompany", "xDelta", "", "pw", "xCharlieDiceID", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
        new LeadRecord(18, "ICharlieCompany aDeltaX bBravoX", "xDeltaName aBravoX bCharlieZ", "", "bEcho", "zBravoDiceID", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now())),
    };
    assert dao != null;
    for (LeadRecord r : records) {
      dao.insert(r);
    }
  }

  @Test
  public void findAllInFieldTest() throws SQLException {
    Collection<LeadRecord> results;
    assert dao != null;
    setUpFindAllTests();

    // Find All In Field
    results = dao.findAllInField(LeadField.Company, null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(4, 13, 18));
    assertEquals(3, results.size());

    results = dao.findAllInField(LeadField.Company, LeadField.Company, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 4, 18, 13));

    results = dao.findAllInField(LeadField.ContactName, null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(7, 11, 18));
    assertEquals(3, results.size());

    results = dao.findAllInField(LeadField.ContactName, LeadField.Company, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 7, 18, 11));

    results = dao.findAllInField(LeadField.DicePosn, null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(3, 10, 13));
    assertEquals(3, results.size());

    results = dao.findAllInField(LeadField.DicePosn, LeadField.Company, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 10, 3, 13));

    results = dao.findAllInField(LeadField.DiceID, null, "bravo", "charlie", "delta");
    assertThat(getIds(results), hasItems(6, 14, 15));
    assertEquals(3, results.size());

    results = dao.findAllInField(LeadField.DiceID, LeadField.Company, "bravo", "charlie", "delta");
    assertTrue(arraysMatch(getIds(results), 15, 14, 6));


    results = dao.findAllInField(LeadField.Company, null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(1, 4, 13, 18));
    assertEquals(4, results.size());

    results = dao.findAllInField(LeadField.Company, LeadField.Company, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 4, 18, 1, 13));

    results = dao.findAllInField(LeadField.ContactName, null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(5, 7, 11, 18));
    assertEquals(4, results.size());

    results = dao.findAllInField(LeadField.ContactName, LeadField.Company, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 7, 18, 11, 5));

    results = dao.findAllInField(LeadField.DicePosn, null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(2, 3, 10, 13));
    assertEquals(4, results.size());

    results = dao.findAllInField(LeadField.DicePosn, LeadField.Company, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 10, 3, 2, 13));

    results = dao.findAllInField(LeadField.DiceID, null, "bravo", "charlie");
    assertThat(getIds(results), hasItems(6, 10, 14, 15));
    assertEquals(4, results.size());

    results = dao.findAllInField(LeadField.DiceID, LeadField.Company, "bravo", "charlie");
    assertTrue(arraysMatch(getIds(results), 10, 15, 14, 6));
  }


  @Test
  @SuppressWarnings({"HardCodedStringLiteral", "unused"})
  public void testDao() throws SQLException {
    System.err.println("testDao");
    assert dao != null;
    assert connectionSource != null;
    try {
      doTestDao(dao, connectionSource);
    } finally {

      // cleanup even on failure.
      Collection<LeadRecord> allRecords = showAllRecords(dao, -1);
      int count = allRecords.size();
      for (LeadRecord leadRecord : allRecords) {
        dao.delete(leadRecord);
      }
      allRecords = showAllRecords(dao, 0);
      assertEquals(0, allRecords.size());
    }
  }
}
