package com.neptunedreams.jobs;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.neptunedreams.framework.ErrorReport;
import com.neptunedreams.framework.data.ConnectionSource;
import com.neptunedreams.framework.data.Dao;
import com.neptunedreams.framework.data.DatabaseInfo;
import com.neptunedreams.framework.data.RecordModel;
import com.neptunedreams.framework.data.SearchOption;
import com.neptunedreams.framework.ui.RecordController;
import com.neptunedreams.framework.ui.TangoUtils;
import com.neptunedreams.jobs.data.LeadField;
import com.neptunedreams.jobs.data.sqlite.SQLiteInfo;
import com.neptunedreams.jobs.gen.tables.records.LeadRecord;
import com.neptunedreams.jobs.ui.RecordUI;
import com.neptunedreams.jobs.ui.RecordView;
import org.jetbrains.annotations.NotNull;

/**
 * JobHunt Key Application
 * <p>
 * Optional Arguments: <br>
 *   -export Upon launching, export all data to an xml file. <br>
 *   -import Upon launching, if there is no data, import all data from the same .xml file that you previously exported. <br>
 * Neither of these options assumes long-term storage. They use serialization, so import should be done 
 * immediately after exporting and deleting the database.
 * @author Miguel Muñoz
 */
@SuppressWarnings("HardCodedStringLiteral")
public final class JobSearch extends JPanel
{
  @SuppressWarnings("HardcodedFileSeparator")
  private static final String EXPORT_FILE = "/.JobHuntData.serial";
  // Done: Write an import mechanism.
  // Done: Test packaging
  // Done: Test Bundling: https://github.com/federkasten/appbundle-maven-plugin
  // Done: Add an info line: 4/15 (25 total)
  // Done: Write a query thread to handle find requests.
  // Done: immediately resort when changing sort field.
  // Done: BUG: Fix finding no records.
  // Done: BUG: Fix search in field.
  // Done: disable buttons when nothing is found.
  // Done: QUESTION: Are we properly setting currentRecord after a find? for each find type? Before updating the screen?
  // Done: BUG: New database. Inserting and saving changed records is still buggy. (see prev note for hypothesis)
  // Done: Test boundary issues on insertion index.
  // Done: enable buttons on new record. ??
  // Done: Convert to jOOQ
  // Done: Add a getTotal method for info line.
  // Done: Figure out a better way to get the ID of a new record. Can we ask the sequencer?
  //       For accessing a sequencer, see https://stackoverflow.com/questions/5729063/how-to-use-sequence-in-apache-derby
  // Done: BUG: Search that produces no results gives the user a data-entry screen to doesn't get saved.
  // Done: BUG: Search that produces one result gives the user an entry screen that gets treated as a new record 
  // Done: BUG: Key Queue in QueuedTask never reads the keys it saves. Can we get rid of it?
  // TODO:  Fix bug on adding: If I add a record, then do a find all by hitting return in the find field, it finds
  // todo   all the records except the one I just added. Doing another find all finds everything.
  // TODO: Replace CountDownDoor with CyclicBarrier?
  // Done: On changing sort column, search for previously selected card. (Search by id)
  // TODO: Redo layout: 
  // todo  1. Put Search Option Panel (in RecordUI) to the right of search field.
  // done  2. Dim instead of hide search options. (I had forgotten all about them!)
  // todo  3. Put search field options in a new sidebar. Allow show/hide.
  // todo  4. Add column header to sort buttons.
  // Done: BUG: Find All doesn't find everything. It may fail to search in the Description field. Search for
  // done  North Hollywood. Find all finds it on id 4 and 33. Find Exact finds it on 4, 33, and 82. 
  // Done: Highlight found text after a find. Add a next button that highlights the next example on the current card, 
  // done  or goes to the next card if there aren't any more. (This will be tricky.)
  // TODO: BUG Delete fails in certain circumstances. If I type some search text, then create a card (presumably not
  // todo  a part of the search text), the delete-card button will throw an exception: Detached Exception: Cannot
  // todo  execute query. No Connection Configured. (It may be because the card hasn't been inserted yet. But it's
  // todo  a strange message.)
  // Done: BUG Hold down L/R arrow keys defeats speed limit of swipe. (Holding down the mouse on the L/R buttons 
  // done  successfully limits the speed.)
  // TODO: Select found word (and use swipe animation) and set direction to forward on new search.
  // TODO: Wrap around at the end of Find-Next/Find-Previous
  // TODO: BUG After searching for North Hollywood, change search option to "Find Exact" Hilighter doesnt' get updated.
  // TODO: BUG AutoSave detects data changes generated by keystrokes, but not by paste operations. How do we catch them?
  
  // https://db.apache.org/ojb/docu/howtos/howto-use-db-sequences.html
  // https://db.apache.org/derby/docs/10.8/ref/rrefsqljcreatesequence.html 
  // https://db.apache.org/derby/docs/10.9/ref/rrefsistabssyssequences.html
  // Derby System Tables: https://db.apache.org/derby/docs/10.7/ref/rrefsistabs38369.html
  // .

//  private static final String DERBY_SYSTEM_HOME = "derby.system.home";
//  private Connection connection;
  
  private final RecordUI<@NotNull LeadRecord> mainPanel;
  //    org.jooq.util.JavaGenerator generator;
  private static final JFrame frame = new JFrame("Job Hunt");
  private final @NotNull DatabaseInfo info;
  private final @NotNull RecordController<LeadRecord, Integer, @NotNull LeadField> controller;

  /**
   * Launch the App!
   * @param args Unused
   */
  public static void main(String[] args) { //throws IOException, ClassNotFoundException {
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> ErrorReport.reportException("Unknown", e));
    TangoUtils.installDarkLookAndFeel();

    //noinspection ErrorNotRethrown
    try {
      boolean doImport = (args.length > 0) && Objects.equals(args[0], "-import");
      frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      frame.setLocationByPlatform(true);
      final JobSearch jobSearch = new JobSearch(doImport);
      frame.add(jobSearch.getPanel());
      frame.pack();
      frame.addWindowListener(jobSearch.shutdownListener());
//    UIMenus.Menu.installMenu(frame);
      jobSearch.mainPanel.launchInitialSearch();
      frame.setVisible(true);
    } catch (IOException | ClassNotFoundException | RuntimeException | Error e) {
      ErrorReport.reportException("Initialization Error", e);
    }

//    doExport(args, jobSearch);
  }

//  private static void doExport(final String[] args, final JobSearch jobSearch) {
//    if ((args.length > 0) && Objects.equals(args[0], "-export")) {
//      try {
//        // There has to be a delay, because there's a 1-second delay built into the launchInitialSearch() method,
//        // and this needs to take place after that finishes, or we won't see any records. 
//        Thread.sleep(1000); // Yeah, this is kludgy, but it's only for the export, which is only done in development.
//      } catch (InterruptedException ignored) { }
//
//      SwingUtilities.invokeLater(() -> {
//        RecordModel<LeadRecord> model = jobSearch.controller.getModel();
//        // noinspection StringConcatenation
//        String exportPath = System.getProperty("user.home") + EXPORT_FILE;
//        System.err.printf("Exporting %d records to %s%n", model.getSize(), exportPath); // NON-NLS
//        //noinspection OverlyBroadCatchBlock
//        try (ObjectOutputStream bos = new ObjectOutputStream(new FileOutputStream(exportPath))) {
//          bos.writeObject(model);
//        } catch (IOException e) {
//          ErrorReport.reportException("Error during export", e);
//        }
//        System.err.printf("Export done%n"); // NON-NLS
//      });
//    }
//  }

  private JobSearch(boolean doImport) throws IOException, ClassNotFoundException {
    super();

    info = new SQLiteInfo();
    try {
      info.init();
      final ConnectionSource connectionSource = info.getConnectionSource();
      Dao<LeadRecord, Integer, @NotNull LeadField> dao = info.getDao(LeadRecord.class, connectionSource);
      LeadRecord dummyRecord = new LeadRecord(0, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now()));
      final RecordView<@NotNull LeadRecord> view = new RecordView.Builder<>(dummyRecord, LeadField.CreatedOn)
          .id      (LeadRecord::getId,       LeadRecord::setId)
          .company  (LeadRecord::getCompany,   LeadRecord::setCompany)
          .contactName(LeadRecord::getContactName, LeadRecord::setContactName)
          .client(LeadRecord::getClient, LeadRecord::setClient)
          .dicePosn(LeadRecord::getDicePosn, LeadRecord::setDicePosn)
          .diceId(LeadRecord::getDiceId, LeadRecord::setDiceId)
          .email(LeadRecord::getEmail, LeadRecord::setEmail)
          .phone1(LeadRecord::getPhone1, LeadRecord::setPhone1)
          .phone2(LeadRecord::getPhone2, LeadRecord::setPhone2)
          .phone3(LeadRecord::getPhone3, LeadRecord::setPhone3)
          .fax(LeadRecord::getFax, LeadRecord::setFax)
          .website(LeadRecord::getWebsite, LeadRecord::setWebsite)
          .linkedIn(LeadRecord::getLinkedIn, LeadRecord::setLinkedIn)
          .skype(LeadRecord::getSkype, LeadRecord::setSkype)
          .description(LeadRecord::getDescription, LeadRecord::setDescription)
          .history(LeadRecord::getHistory, LeadRecord::setHistory)
          .createdOn(LeadRecord::getCreatedOn)
          .withDao(dao)
          .withConstructor(this::recordConstructor)
          .build();
      controller = view.getController();
      final RecordModel<LeadRecord> model = controller.getModel();
      mainPanel = RecordUI.createRecordUI(model, view, controller, view.getEditModel()); // RecordUI launches the initial search

      if ((model.getSize() == 1) && (model.getRecordAt(0).getId() == 0) && doImport) {
        importFromFile(dao, controller); // throws ClassNotFoundException
      }

      // Make sure you save the last change before shutting down.
      frame.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(final WindowEvent e) {
          //noinspection ErrorNotRethrown
          try {
            if (view.saveOnExit()) {
              assert controller != null;
              controller.getDao().insertOrUpdate(view.getCurrentRecord());
            }
          } catch (SQLException | RuntimeException | Error e1) {
            ErrorReport.reportException("Saving last change", e1);
          }
        }
      });

      //      // Import from Derby
//      ObjectMapper objectMapper = new ObjectMapper();
//      final File file = new File(System.getProperty("user.home"), "jobHuntRecords.json");
//      
//      FileInputStream inputStream = new FileInputStream(file);
//      InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
//      List<LeadRecord> recordList = objectMapper.readValue(reader, new TypeReference<List<LeadRecord>>() {});
//      for (LeadRecord LeadRecord: recordList) {
////        System.out.println(LeadRecord);
////        objectMapper.writeValueAsString(LeadRecord); // Didn't work.
//        dao.insert(LeadRecord);
//      }
    } catch (SQLException e) {
      e.printStackTrace();
      shutDownDatabase(info);
      throw new IOException(e); // don't even open the window!
    }
  }

  @SuppressWarnings("OverlyBroadThrowsClause")
  private static void importFromFile(
      final Dao<@NotNull LeadRecord, Integer, @NotNull LeadField> dao, 
      RecordController<@NotNull LeadRecord, Integer, @NotNull LeadField> controller)
      throws SQLException, IOException, ClassNotFoundException {
    String exportPath = System.getProperty("user.home") + EXPORT_FILE;
    try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(exportPath))) {
      @SuppressWarnings("unchecked")
      RecordModel<@NotNull LeadRecord> model = (RecordModel<@NotNull LeadRecord>) objectInputStream.readObject();
      for (int ii=0; ii<model.getSize(); ++ii) {
        dao.insert(model.getRecordAt(ii));
      }
    }
    controller.findTextAnywhere("", SearchOption.findWhole);
  }

  @SuppressWarnings("unused")
  private LeadRecord recordConstructor() {
    return new LeadRecord(0, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", Timestamp.from(Instant.now()));
  }
  
  private JPanel getPanel() { return mainPanel; }

//  private Dao<?> connect(DatabaseInfo info) throws SQLException {
//    String connectionUrl = info.getUrl();
//    System.out.printf("URL: %s%n", connectionUrl);
//      //noinspection CallToDriverManagerGetConnection
//    try (Connection connection = DriverManager.getConnection(connectionUrl)) { // , props);
////    Map<String, Class<?>> typeMap =  connection.getTypeMap();
////    int size = typeMap.size();
////    System.out.printf("Total of %d types:%n", size);
////    for (String s: typeMap.keySet()) {
////      System.out.printf("%s: %s%n", s, typeMap.get(s));
////    }
//
//      ConnectionSource connectionSource = () -> connection;
//      Dao<Record> recordDao = info.getDao(Record.class, connectionSource);
//
////    String drop = "DROP TABLE record";
////    PreparedStatement statement = connection.prepareStatement(drop);
////    statement.execute();
//
//      recordDao.createTableIfNeeded();
//      return recordDao;
//    }
//  }

  private WindowListener shutdownListener() {
    return new WindowAdapter() {
      @Override
      public void windowClosed(final WindowEvent e) {
        shutDownDatabase(info);
      }
    };
  }

  private void shutDownDatabase(DatabaseInfo dInfo) {
    dInfo.shutdown();
  }
}
