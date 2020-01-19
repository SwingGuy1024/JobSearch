package com.neptunedreams.jobs.ui;

import java.awt.BorderLayout;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import com.neptunedreams.framework.data.Dao;
import com.neptunedreams.jobs.gen.tables.records.LeadRecord;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 11/3/17
 * <p>Time: 9:51 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"HardCodedStringLiteral", "MagicCharacter", "HardcodedLineSeparator"})
final class ImportDialog extends JDialog {
  private final Dao<LeadRecord, ?, ?> recordDao;
  private ImportDialog(Window parent, Dao<LeadRecord, ?, ?> dao) {
    super(parent, ModalityType.DOCUMENT_MODAL);
    recordDao = dao;
//    build();
  }
  
  static ImportDialog build(Window parent, Dao<LeadRecord, ?, ?> dao) {
    ImportDialog importDialog = new ImportDialog(parent, dao);
    importDialog.build();
    return importDialog;
  }

  private void build() {
    //noinspection MagicNumber
    JTextArea importArea = new JTextArea(40, 60);
    JScrollPane scrollPane = new JScrollPane(importArea, 
        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(scrollPane, BorderLayout.CENTER);
    //noinspection HardCodedStringLiteral
    JButton load = new JButton("Load");
    getContentPane().add(load, BorderLayout.PAGE_END);
    pack();
    importArea.setLineWrap(true);
    importArea.setWrapStyleWord(true);
    
    load.addActionListener((e -> doLoad(importArea.getText())));
  }
  
  // IntelliJ's null checking knows that (record != null) isn't necessary, but the NullCheckerFramework didn't figure that out.
  @SuppressWarnings("ConstantConditions")
  private void doLoad(String text) {
    try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
      LeadRecord record = null;
      String line = "";
      int count = 0;
      boolean active = false;
      while (line != null) {
        if (line.trim().isEmpty()) {
          if (record != null) {
            count++;
            recordDao.insert(record);
          }
          record = new LeadRecord(); // Always creates a record the first time through. So record is never null afterwards.
          active = false;
        } else {
          active = true;
          putLine(line, record);
        }
        line = reader.readLine();
      }
      if ((record != null) && active) {
        recordDao.insert(record);
      }
      JOptionPane.showMessageDialog(this, String.format("Imported %d records", count));
    } catch (IOException | SQLException e) {
      e.printStackTrace();
    }
  }

  private void putLine(String text, LeadRecord record) {
    int eqSpot = text.indexOf('=');
    String fieldName = text.substring(0, eqSpot);
    String fieldValue = text.substring(eqSpot+1);
    fieldValue = fieldValue.replaceAll("\\\\n", "\n");
    switch (fieldName) {
      case "company":
        record.setCompany(fieldValue);
        break;
      case "Contact Name":
        record.setContactName(fieldValue);
        break;
      case "Dice Position":
        record.setDicePosn(fieldValue);
        break;
      case "Dice ID":
        record.setDiceId(fieldValue);
        break;
      case "Email":
        record.setEmail(fieldValue);
        break;
      case "Phone1":
        record.setPhone1(fieldValue);
        break;
      case "Phone2":
        record.setPhone2(fieldValue);
        break;
      case "Fax":
        record.setFax(fieldValue);
        break;
      case "WebSite":
        record.setWebsite(fieldValue);
        break;
      case "skype":
        record.setSkype(fieldValue);
        break;
      case "Description":
        record.setDescription(fieldValue);
        break;
      case "History":
        record.setHistory(fieldValue);
        break;
      case "hourly":
      case "yearly":
        break;
      default:
        throw new IllegalArgumentException(text);
    }
  }
}
