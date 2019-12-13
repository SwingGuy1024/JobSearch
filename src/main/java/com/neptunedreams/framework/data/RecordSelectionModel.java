package com.neptunedreams.framework.data;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 1/2/18
 * <p>Time: 12:33 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public interface RecordSelectionModel<R> {
  
  boolean recordHasChanged();
  R getCurrentRecord();
}
