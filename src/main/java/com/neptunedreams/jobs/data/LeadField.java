package com.neptunedreams.jobs.data;

import com.neptunedreams.framework.ui.DisplayEnum;
import org.checkerframework.checker.nullness.qual.KeyFor;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/22/17
 * <p>Time: 5:18 PM
 *
 * @author Miguel Mu\u00f1oz
 */
//@SuppressWarnings("assignment.type.incompatible") // Not sure if this makes @KeyFor pointless
public enum LeadField implements DisplayEnum {
//  @KeyFor("com.neptunedreams.jobs.data.sqlite.SQLiteRecordDao.fieldMap") All(false),
//  @KeyFor("com.neptunedreams.jobs.data.sqlite.SQLiteRecordDao.fieldMap") ID,
//  @KeyFor("com.neptunedreams.jobs.data.sqlite.SQLiteRecordDao.fieldMap") Company,
//  @KeyFor("com.neptunedreams.jobs.data.sqlite.SQLiteRecordDao.fieldMap") ContactName,
//  @KeyFor("com.neptunedreams.jobs.data.sqlite.SQLiteRecordDao.fieldMap") DicePosn,
//  @KeyFor("com.neptunedreams.jobs.data.sqlite.SQLiteRecordDao.fieldMap") DiceID,
//  @KeyFor("com.neptunedreams.jobs.data.sqlite.SQLiteRecordDao.fieldMap") EMail,
//  @KeyFor("com.neptunedreams.jobs.data.sqlite.SQLiteRecordDao.fieldMap") Phone1,
//  @KeyFor("com.neptunedreams.jobs.data.sqlite.SQLiteRecordDao.fieldMap") Phone2,
//  @KeyFor("com.neptunedreams.jobs.data.sqlite.SQLiteRecordDao.fieldMap") Fax,
//  @KeyFor("com.neptunedreams.jobs.data.sqlite.SQLiteRecordDao.fieldMap") WebSite,
//  @KeyFor("com.neptunedreams.jobs.data.sqlite.SQLiteRecordDao.fieldMap") Skype,
//  @KeyFor("com.neptunedreams.jobs.data.sqlite.SQLiteRecordDao.fieldMap") Description,
//  @KeyFor("com.neptunedreams.jobs.data.sqlite.SQLiteRecordDao.fieldMap") History
  All(false),
  ID,
  Company,
  ContactName,
  DicePosn,
  DiceID,
  EMail,
  Phone1,
  Phone2,
  Fax,
  WebSite,
  Skype,
  Description,
  History;

  private final boolean isField;

  LeadField() {
    isField = true;
  }

  LeadField(boolean field) {
    isField = field;
  }

  public boolean isField() {
    return isField;
  }

  @Override
  public String getDisplay() {
    return toString();
  }
}
