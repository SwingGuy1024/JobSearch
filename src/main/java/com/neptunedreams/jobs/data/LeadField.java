package com.neptunedreams.jobs.data;

import com.neptunedreams.framework.data.DBField;
import com.neptunedreams.framework.ui.DisplayEnum;
import org.jetbrains.annotations.NotNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/22/17
 * <p>Time: 5:18 PM
 *
 * @author Miguel Mu\u00f1oz
 */
//@SuppressWarnings("assignment.type.incompatible") // Not sure if this makes @KeyFor pointless
public enum LeadField implements DisplayEnum, DBField {
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
  History,
  CreatedOn;

  private final boolean isField;

  LeadField() {
    isField = true;
  }

  LeadField(boolean field) {
    isField = field;
  }

  @Override
  public boolean isField() {
    return isField;
  }

  @NotNull
  @Override
  public String getDisplay() {
    return toString();
  }
}
