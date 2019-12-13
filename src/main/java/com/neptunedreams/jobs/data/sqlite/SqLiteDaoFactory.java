package com.neptunedreams.jobs.data.sqlite;

import com.neptunedreams.framework.data.AbstractDaoFactory;
import com.neptunedreams.framework.data.ConnectionSource;
import com.neptunedreams.jobs.gen.tables.records.LeadRecord;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/12/17
 * <p>Time: 2:08 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public class SqLiteDaoFactory extends AbstractDaoFactory {
  @SuppressWarnings("JavaDoc")
  SqLiteDaoFactory(ConnectionSource connectionSource) {
    super();
    //noinspection UnnecessaryLocalVariable
    ConnectionSource source = connectionSource;
    addDao(LeadRecord.class, SQLiteRecordDao.create(source));
  }
}
