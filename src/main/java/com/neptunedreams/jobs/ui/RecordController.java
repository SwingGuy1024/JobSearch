package com.neptunedreams.jobs.ui;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.function.Function;
import com.neptunedreams.framework.ErrorReport;
import com.neptunedreams.framework.data.DBField;
import com.neptunedreams.framework.data.Dao;
import com.neptunedreams.framework.data.RecordModel;
import com.neptunedreams.framework.data.RecordModelListener;
import com.neptunedreams.framework.data.RecordSelectionModel;
import com.neptunedreams.framework.data.SearchOption;
import com.neptunedreams.framework.event.MasterEventBus;
import org.checkerframework.checker.initialization.qual.NotOnlyInitialized;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 11:27 AM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr", "WeakerAccess", "HardCodedStringLiteral"})
public class RecordController<R, PK, F extends DBField> implements RecordModelListener {
  private static final Integer ZERO = 0;
  // For DerbyRecordDao, E was Record.FIELD
//  private E order = Record.FIELD.SOURCE;
  private F order;
  private final Dao<R, PK, F> dao;
  private final RecordSelectionModel<R> recordSelectionModel;
  @NotOnlyInitialized
  private final RecordModel<R> model;

  @SuppressWarnings({"argument.type.incompatible", "JavaDoc"})
  public RecordController(
      Dao<R, PK, F> theDao, 
      RecordSelectionModel<R> recordSelectionModel, 
      F initialOrder,
      Function<Void, R> recordConstructor
  ) {
    dao = theDao;
    this.recordSelectionModel = recordSelectionModel;
    model = new RecordModel<>(recordConstructor);
    model.addModelListener(this); // Type checker needs "this" to be initialized, so suppress the warning.
    order = initialOrder;
  }

  public RecordModel<R> getModel() {
    return model;
  }
  
  public Dao<R, PK, F> getDao() { return dao; }

  @SuppressWarnings("JavaDoc")
  public void specifyOrder(F theOrder) {
    order = theOrder;
  }

  public F getOrder() {
    return order;
  }

  private void loadNewRecord(@NonNull R record) {
    R currentRecord = recordSelectionModel.getCurrentRecord(); // Move this back to where the comment is
    
    assert currentRecord != null;

    if (recordSelectionModel.recordHasChanged()) {
      try {
        MasterEventBus.postLoadUserData();
        dao.insertOrUpdate(currentRecord);
      } catch (SQLException e) {
        ErrorReport.reportException("Insert", e);
      }
    }
    MasterEventBus.postChangeRecordEvent(record);
  }

  @SuppressWarnings("JavaDoc")
  public void addBlankRecord() {
    // If the last record is already blank, just go to it
    final int lastIndex = model.getSize() - 1;
    R lastRecord = model.getRecordAt(lastIndex);
    assert lastRecord != null;
    final PK lastRecordKey = dao.getPrimaryKey(lastRecord);
    
    // If we are already showing an unchanged blank record...
    if ((model.getRecordIndex() == lastIndex) && ((lastRecordKey == null) || (lastRecordKey.equals(ZERO))) && !recordSelectionModel.recordHasChanged()) {
      // ... we don't bother to create a new one.
      loadNewRecord(lastRecord);
    } else {
      R emptyRecord = model.createNewEmptyRecord();
      model.append(emptyRecord);
      loadNewRecord(emptyRecord);
    }
  }

  public void setFoundRecords(final Collection<R> theFoundItems) {
    model.setNewList(theFoundItems);
    if (model.getSize() > 0) {
      final R selectedRecord = model.getFoundRecord();
      assert selectedRecord != null;
      loadNewRecord(selectedRecord);
    }
  }

  @SuppressWarnings("JavaDoc")
  public void findTextInField(String dirtyText, final F field, SearchOption searchOption) {
    //noinspection TooBroadScope
    String text = dirtyText.trim();
    try {
      Collection<R> foundItems = findRecordsInField(text, field, searchOption);
      setFoundRecords(foundItems);
      model.goFirst();
    } catch (SQLException e) {
      ErrorReport.reportException(String.format("Find Text in Field %s with %s", field, searchOption), e);
    }
  }

  @SuppressWarnings("JavaDoc")
  Collection<R> findRecordsInField(final String text, final F field, SearchOption searchOption) throws SQLException {
    if (text.trim().isEmpty()) {
      return dao.getAll(getOrder());
    } else {
      switch (searchOption) {
        case findWhole:
          return dao.findInField(text, field, getOrder());
        case findAll:
          return dao.findAllInField(field, getOrder(), parseText(text));
        case findAny:
          return dao.findAnyInField(field, getOrder(), parseText(text));
        default:
          throw new AssertionError(String.format("Unhandled case: %s", searchOption));
      }
    }
  }

  @SuppressWarnings("JavaDoc")
  String[] parseText(@NonNull String text) {
    //noinspection EqualsReplaceableByObjectsCall
    assert text.trim().equals(text); // text should already be trimmed
    StringTokenizer tokenizer = new StringTokenizer(text, " ");
    String[] tokens = new String[tokenizer.countTokens()];
    int i = 0;
    while (tokenizer.hasMoreTokens()) {
      tokens[i++] = tokenizer.nextToken();
    }
    return tokens;
  }

  /**
   * Find text in any field of the database.
   * @param dirtyText The text to find, without cleaning or wildcards
   * @param searchOption The search option (Find all, find any, etc)
   */
  public void findTextAnywhere(String dirtyText, SearchOption searchOption) {
    //noinspection TooBroadScope
    String text = dirtyText.trim();
    try {
      Collection<R> foundItems = findRecordsAnywhere(text, searchOption);
      setFoundRecords(foundItems);
      model.goFirst();
    } catch (SQLException e) {
      ErrorReport.reportException("Find Text anywhere", e);
    }
  }
  
  @SuppressWarnings("JavaDoc")
  Collection<R> findRecordsAnywhere(final String text, SearchOption searchOption) throws SQLException {
    if (text.isEmpty()) {
      return dao.getAll(getOrder());
    } else {
      switch (searchOption) {
        case findWhole:
          return dao.find(text, getOrder());
        case findAll:
          return dao.findAll(getOrder(), parseText(text));
        case findAny:
          return dao.findAny(getOrder(), parseText(text));
        default:
          throw new AssertionError(String.format("Unhandled case: %s", searchOption));  
      }
    }
  }

  @Override
  public void modelListChanged(@SuppressWarnings("NullableProblems") final int newSize) {
    
  }

  @SuppressWarnings("JavaDoc")
  public Collection<R> retrieveNow(final F searchField, final SearchOption searchOption, final String searchText) {
    try {
      if (searchField.isField()) {
        return findRecordsInField(searchText, searchField, searchOption);
      } else {
        return findRecordsAnywhere(searchText, searchOption);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return new LinkedList<>();
    }
  }

  @Override
  public void indexChanged(@SuppressWarnings("NullableProblems") final int index, @SuppressWarnings("NullableProblems") int prior) {
    loadNewRecord(model.getFoundRecord());
  }

  @SuppressWarnings("JavaDoc")
  public void delete(final R selectedRecord) throws SQLException {
    dao.delete(selectedRecord);
  }
}
