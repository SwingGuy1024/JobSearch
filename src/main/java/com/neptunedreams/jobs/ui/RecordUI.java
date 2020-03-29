package com.neptunedreams.jobs.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import com.google.common.eventbus.Subscribe;
import com.neptunedreams.framework.ErrorReport;
import com.neptunedreams.framework.data.RecordModel;
import com.neptunedreams.framework.data.RecordModelListener;
import com.neptunedreams.framework.data.SearchOption;
import com.neptunedreams.framework.event.MasterEventBus;
import com.neptunedreams.framework.task.ParameterizedCallable;
import com.neptunedreams.framework.task.QueuedTask;
import com.neptunedreams.framework.ui.ClipFix;
import com.neptunedreams.framework.ui.EnumComboBox;
import com.neptunedreams.framework.ui.EnumGroup;
import com.neptunedreams.framework.ui.HidingPanel;
import com.neptunedreams.framework.ui.RecordController;
import com.neptunedreams.framework.ui.SelectionSpy;
import com.neptunedreams.framework.ui.SwingUtils;
import com.neptunedreams.framework.ui.SwipeDirection;
import com.neptunedreams.framework.ui.SwipeView;
import com.neptunedreams.jobs.data.LeadField;
import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.UnknownKeyFor;

//import org.checkerframework.checker.initialization.qual.UnknownInitialization;

/**
 * The RecordUI is the main global UI component. This is what gets added to the main window. It consists of a 
 * RecordView, along with a control panel at the top and a trash panel at the bottom.
 * <p>
 * Functions
 * Next, Previous
 * Find all
 * Find Text
 * Sort By
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 12:50 PM
 * 
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"HardCodedStringLiteral", "HardcodedLineSeparator"})
public class RecordUI<@NonNull R> extends JPanel implements RecordModelListener {

  // TODO:  The QueuedTask is terrific, but it doesn't belong in this class. It belongs in the Controller. That way,
  // todo   it can be accessed by other UI classes like RecordView. To do this, I also need to move the SearchOption
  // todo   to the controller, as well as the searchField. (Maybe I should just write an API so the Controller can just
  // todo   query the RecordUI for the selected state.) It will be a bit of work, but it will be a cleaner UI, which
  // todo   is easier to maintain. This will also allow me to implement instant response to changing the sort order.
  // todo   Maybe the way to do this would be to create a UIModel class that keeps track of all the UI state. Maybe 
  // todo   that way, the controller won't need to keep an instance of RecordView.
  
  // Todo:  Add keyboard listener to JLayer to handle left and right arrow keys. They should only activate when the 
  // todo   focus is not held by a JTextComponent.

  private static final long DELAY = 1000L;
  private static final int ONE_MINUTE_MILLIS = 60000;
  @SuppressWarnings("HardcodedLineSeparator")
  private static final char NEW_LINE = '\n';

  // We set the initial text to a space, so we can fire the initial search by setting the text to the empty String.
  private JTextField findField = new JTextField(" ",10);
  private final RecordController<R, Integer, LeadField> controller;
  private EnumComboBox<LeadField> searchFieldCombo = EnumComboBox.createComboBox(LeadField.values());
  //  private EnumGroup<LeadField> searchFieldGroup = new EnumGroup<>();
  private final @NonNull RecordModel<R> recordModel;
  private JButton prev = new JButton(Resource.getLeftArrow());
  private JButton next = new JButton(Resource.getRightArrow());
  private JButton first = new JButton(Resource.getFirst());
  private JButton last = new JButton(Resource.getLast());

  private JLabel infoLine = new JLabel("");
  private final EnumGroup<SearchOption> optionsGroup = new EnumGroup<>();
  
  private @MonotonicNonNull SwipeView<RecordView<R>> swipeView=null;

  private final HidingPanel searchOptionsPanel = makeSearchOptionsPanel(optionsGroup);
  private RecordView<R> recordView;

  // recordConsumer is how the QueuedTask communicates with the application code.
  private final Consumer<Collection<@NonNull R>> recordConsumer;
  private @NonNull QueuedTask<String, Collection<@NonNull R>> queuedTask;
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);

  private HidingPanel makeSearchOptionsPanel(@UnderInitialization RecordUI<R> this, EnumGroup<SearchOption> optionsGroup) {
    JPanel optionsPanel = new JPanel(new GridLayout(0, 1));
    JRadioButton findExact = optionsGroup.add(SearchOption.findWhole);
    JRadioButton findAll = optionsGroup.add(SearchOption.findAll);
    JRadioButton findAny = optionsGroup.add(SearchOption.findAny);
    optionsPanel.add(findAny);
    optionsPanel.add(findAll);
    optionsPanel.add(findExact);
    optionsGroup.setSelected(SearchOption.findAny);
    addButtonGroupListener(optionsGroup);

    final HidingPanel hidingPanel = HidingPanel.create(optionsPanel);
    hidingPanel.setDisableInsteadOfHide(true);
    return hidingPanel;
  }

  // Moved to a separate method so we can limit the SuppressWarnings to one line.
  @SuppressWarnings("methodref.receiver.bound.invalid")
  private void addButtonGroupListener(@UnderInitialization RecordUI<R> this, final EnumGroup<SearchOption> optionsGroup) {
//    optionsGroup.addButtonGroupListener(selectedButtonModel -> selectionChanged(selectedButtonModel));
    optionsGroup.addButtonGroupListener(this::searchOptionChanged); // Using a lambda is an error. This is a warning. 
  }

    @SuppressWarnings({"method.invocation.invalid", "argument.type.incompatible"})
  // add(), setBorder(), etc not properly annotated in JDK.
  public RecordUI(@NonNull RecordModel<R> model, RecordView<R> theView, RecordController<R, Integer, LeadField> theController) {
    super(new BorderLayout());
    recordModel = model;
    recordView = theView;
    final JLayer<RecordView<R>> layer = wrapInLayer(theView);
    add(layer, BorderLayout.CENTER);
    add(createControlPanel(), BorderLayout.PAGE_START);
    add(createTrashPanel(), BorderLayout.PAGE_END);
    controller = theController;
    recordConsumer = createRecordConsumer(theController);
    setBorder(new MatteBorder(4, 4, 4, 4, getBackground()));
    recordModel.addModelListener(this); // argument.type.incompatible checker error suppressed
    
    findField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(final DocumentEvent e) {
        process(e);
      }

      @Override
      public void removeUpdate(final DocumentEvent e) {
        process(e);
      }

      @Override
      public void changedUpdate(final DocumentEvent e) {
        process(e);
      }
      
    });
    
    MasterEventBus.registerMasterEventHandler(this);
    queuedTask = new QueuedTask<>(DELAY, createCallable(), recordConsumer);
    queuedTask.launch();
    // Send focus to find field on launch. This sets the enabled state of some of the buttons
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentShown(final ComponentEvent e) {
        SwingUtilities.invokeLater(() -> {
          findField.requestFocus();
          removeComponentListener(this);
        });
      }
    });
  }

  @SuppressWarnings("Convert2MethodRef")
  private void setupActions(SwipeView<RecordView<R>> swipeView) {
    swipeView.assignRepeatingKeystrokeAction("Previous", KeyEvent.VK_LEFT, 0, () -> recordModel.goPrev(), SwipeDirection.SWIPE_RIGHT);
    swipeView.assignRepeatingKeystrokeAction("Next", KeyEvent.VK_RIGHT, 0, () -> recordModel.goNext(), SwipeDirection.SWIPE_LEFT);
    
    // These don't work, with either Alt or meta masks. I don't know why, but they're not that important.
    swipeView.assignKeyStrokeAction("First Record", KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK, 
        () -> recordModel.goFirst(), SwipeDirection.SWIPE_RIGHT);
    swipeView.assignKeyStrokeAction("Last Record", KeyEvent.VK_RIGHT, InputEvent.META_DOWN_MASK, 
        () -> recordModel.goLast(), SwipeDirection.SWIPE_LEFT);
  }

  private JLayer<RecordView<R>> wrapInLayer(@UnderInitialization RecordUI<R> this, RecordView<R> recordView) {
    swipeView = SwipeView.wrap(recordView);
    return swipeView.getLayer();
  }
  
  public void launchInitialSearch() {
    SwingUtilities.invokeLater(() -> {
      findField.setText(""); // This fires the initial search in queuedTask.
    });
    try {
      // This is how long it takes before the find starts working
      Thread.sleep(queuedTask.getDelayMilliSeconds());
    } catch (InterruptedException ignored) { }
  }

  private void process(DocumentEvent e) {
    final Document document = e.getDocument();
    try {
      final String text = document.getText(0, document.getLength());
      queuedTask.feedData(text);
      // I'm assuming here that text can't contain \n, \r, \f, or \t, or even nbsp. If this turns out to be false,
      // I should probably filter them out in the process method.

      final boolean vis = text.trim().contains(" ");
      searchOptionsPanel.setContentVisible(vis);
    } catch (BadLocationException e1) {
      e1.printStackTrace();
    }
  }
  
  private JPanel createControlPanel() {
    JPanel buttonPanel = new JPanel(new BorderLayout());
    buttonPanel.add(getSearchField(), BorderLayout.PAGE_START);
    buttonPanel.add(BorderLayout.LINE_START, searchOptionsPanel);
    @UnknownKeyFor @Initialized JPanel navigationPanel = SwingUtils.wrapCenter(getButtons());
    JComponent utilityPanel = makeUtilityPanel();
    JPanel navUtilityPanel = new JPanel(new BorderLayout());
    navUtilityPanel.add(BorderLayout.CENTER, navigationPanel);
    navUtilityPanel.add(SwingUtils.wrapEast(utilityPanel), BorderLayout.PAGE_END);
    buttonPanel.add(BorderLayout.CENTER, navUtilityPanel);
    return buttonPanel;
  }
  
  private JComponent makeUtilityPanel() {
    Box navUtilPanel = new Box(BoxLayout.LINE_AXIS);
    
    navUtilPanel.add(makePasteHtmlButton());
    navUtilPanel.add(makeStripBlankButton());
    navUtilPanel.add(makeBulletButton());
    //noinspection MagicNumber
    navUtilPanel.add(Box.createHorizontalStrut(15));
    navUtilPanel.add(makeTimeButton());
    return navUtilPanel;
  }

  private Component makePasteHtmlButton() {
    JButton pasteHtmlButton = new JButton("Paste HTML");
    pasteHtmlButton.setEnabled(false);
    pasteHtmlButton.setFocusable(false);
    SelectionSpy.spy.addFocusInTextFieldListener(pasteHtmlButton::setEnabled);
    pasteHtmlButton.addActionListener(a -> {
      @UnknownKeyFor @Initialized String htmlAsText = ClipFix.getHtmlAsText();
      if (htmlAsText != null) {
        SelectionSpy.spy.replaceSelectedText(htmlAsText);
      }
    });
    return pasteHtmlButton;
  }

  private JButton makeStripBlankButton() {
    JButton stripBlankButton = new JButton("Strip Blank Lines");
    stripBlankButton.setEnabled(false);
    stripBlankButton.setFocusable(false);
    SelectionSpy.spy.addSelectionExistsListener(stripBlankButton::setEnabled);
    stripBlankButton.addActionListener(e -> {
      String selectedText = SelectionSpy.spy.getSelectedText();
      StringBuilder revisedText = new StringBuilder();
      try (BufferedReader reader = new BufferedReader(new StringReader(selectedText))) {
        String line = reader.readLine();
        while (line != null) {
          if (!line.isEmpty()) {
            revisedText.append(line).append(NEW_LINE);
          }
          line = reader.readLine();
        }
      } catch (IOException ignored) { } // can't happen with a StringReader.
      if (selectedText.endsWith("\n")) {
        revisedText.deleteCharAt(revisedText.length()-1);
      }
      SelectionSpy.spy.replaceSelectedText(revisedText.toString());
    });
    return stripBlankButton;
  }

  private JButton makeBulletButton() {
    JButton bullet = new JButton(Resource.getBullet16());
    bullet.setEnabled(false);
    bullet.setFocusable(false);
    bullet.setToolTipText("Add bullets to selected text");
    SelectionSpy.spy.addSelectionExistsListener(bullet::setEnabled);
    bullet.addActionListener(e -> {
      String selectedText = SelectionSpy.spy.getSelectedText();
      StringBuilder revisedText = new StringBuilder();
      try (BufferedReader reader = new BufferedReader(new StringReader(selectedText))) {
        String line = reader.readLine();
        while (line != null) {
          if (!line.isEmpty()) {
            revisedText.append("• ");
          }
          revisedText.append(line).append(NEW_LINE);
          line = reader.readLine();
        }
      } catch (IOException ignored) { } // can't happen with a StringReader.
      if (!selectedText.endsWith("\n")) {
        revisedText.deleteCharAt(revisedText.length() - 1);
      }
      SelectionSpy.spy.replaceSelectedText(revisedText.toString());
    });
    return bullet;
  }

  private JPanel createTrashPanel() {
    JPanel trashPanel = new JPanel(new BorderLayout());
    JButton trashRecord = new JButton(Resource.getBin());
    trashPanel.add(trashRecord, BorderLayout.LINE_END);
    trashRecord.addActionListener((e)->delete());

    assert infoLine != null;
    trashPanel.add(infoLine, BorderLayout.LINE_START);
    assert recordModel != null;
    return trashPanel;
  }

  private void delete() {
    if (JOptionPane.showConfirmDialog(this,
        "Are you sure?",
        "Delete Record", 
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION
    ) {
      R selectedRecord = recordModel.getFoundRecord();
      try {
        controller.delete(selectedRecord); // Removes from database
        recordModel.deleteSelected(true, recordModel.getRecordIndex());
        MasterEventBus.postChangeRecordEvent(recordModel.getFoundRecord());
      } catch (SQLException e) {
        ErrorReport.reportException("delete current record", e);
      }
    }
  }

  private JPanel getButtons() {
    JPanel buttons = new JPanel(new GridLayout(1, 0));
    JButton add = new JButton(Resource.getAdd());
//    final JButton importBtn = new JButton("Imp");
    buttons.add(add);
    buttons.add(first);
    buttons.add(prev);
    buttons.add(next);
    buttons.add(last);
//    buttons.add(importBtn);
    
    add.addActionListener((e)->addBlankRecord());
    SwipeView<RecordView<R>> sView = Objects.requireNonNull(swipeView);
    //noinspection Convert2MethodRef
    sView.assignMouseDownAction(prev, () -> recordModel.goPrev(), SwipeDirection.SWIPE_RIGHT);
    //noinspection Convert2MethodRef
    sView.assignMouseDownAction(next, () -> recordModel.goNext(), SwipeDirection.SWIPE_LEFT);
    //noinspection Convert2MethodRef
    first.addActionListener((e) -> sView.swipeRight(() -> recordModel.goFirst()));
    //noinspection Convert2MethodRef
    last.addActionListener((e)  -> sView.swipeLeft(() -> recordModel.goLast()));
//    importBtn.addActionListener((e) -> doImport());
    JPanel buttonPanel = new JPanel(new BorderLayout());
    buttonPanel.add(buttons, BorderLayout.LINE_START);
    setupActions(sView);

    return buttonPanel;
  }
  
  private JButton makeTimeButton() {
    JButton timeButton = new JButton();
    timeButton.addActionListener(e -> newHistoryEvent(timeButton.getText()));
    SwingWorker<String, String> timeWorker = new SwingWorker<String, String>() {
      @Override
      protected String doInBackground() throws InterruptedException {
        long time = System.currentTimeMillis();
        //noinspection InfiniteLoopStatement
        while (true) {
          String nowText = formattedTime();
          publish(nowText);
          long elapsedMinute = time % ONE_MINUTE_MILLIS;
          long remainingInMinute = ONE_MINUTE_MILLIS - elapsedMinute;
          Thread.sleep(remainingInMinute);
          time = System.currentTimeMillis();
        }
      }

      @Override
      protected void process(final List<String> chunks) {
        timeButton.setText(chunks.get(chunks.size()-1));
      }
    };
    timeWorker.execute();
    return timeButton;
  }

  private String formattedTime() {
    return ZonedDateTime.now().format(FORMATTER);
  }

  private void newHistoryEvent(String time) {
    String timeText = String.format("• %s%n", time);
    recordView.addHistoryEvent(timeText);
  }

  private void addBlankRecord() {
    controller.addBlankRecord();
    MasterEventBus.postUserRequestedNewRecordEvent();
    loadInfoLine();
    newHistoryEvent(formattedTime());
  }

//  private void doImport() {
//    //noinspection unchecked
//    ImportDialog importDialog = ImportDialog.build((Window) getRootPane().getParent(), (Dao<LeadRecord, ?, ?>) controller.getDao());
//    importDialog.setVisible(true); 
//  }

  private JPanel getSearchField() {
    JLabel findIcon = Resource.getMagnifierLabel();
    SwingUtils.installStandardCaret(findField);
    JPanel searchPanel = new JPanel(new BorderLayout());
    searchPanel.add(findIcon, BorderLayout.LINE_START);
    searchPanel.add(searchFieldCombo, BorderLayout.LINE_END);
    searchPanel.add(findField, BorderLayout.CENTER);
    findField.addActionListener((e) -> findText());
    return searchPanel;
  }
  
  private void findText() {
    LeadField field = searchFieldCombo.getSelected();
    final SearchOption searchOption = getSearchOption();
    if (field.isField()) {
      controller.findTextInField(findField.getText(), field, searchOption);
    } else {
      controller.findTextAnywhere(findField.getText(), searchOption);
    }
  }

  /**
   * Loads the info line. The info line looks like this:
   * 3/12 of 165
   * This means we're looking at record 3 of the 12 found records, from a total of 165 records in the database.
   */
  private void loadInfoLine() {
    final int index;
    final int foundSize;
    index = recordModel.getRecordIndex() + 1;
    foundSize = recordModel.getSize();
    try {
      int total = controller.getDao().getTotal();
      if (total < foundSize) {
        // This happens when the user hits the + button.
        total = foundSize;
      }
      //noinspection HardcodedFileSeparator
      String info = String.format("%d/%d of %d", index, foundSize, total);
      infoLine.setText(info);
    } catch (SQLException e) {
      ErrorReport.reportException("loadInfoLine()", e);
    }
  }

  @Override
  public void modelListChanged(final int newSize) {
    boolean pnEnabled = newSize > 1;
    prev.setEnabled(pnEnabled);
    next.setEnabled(pnEnabled);
    first.setEnabled(pnEnabled);
    last.setEnabled(pnEnabled);
    loadInfoLine();
  }
  
  /*
    NullnessChecker notes:
    This used to be called during construction. It specified an implicit parameter. But this created a compiler
    error when I called getSearchOption. This is due to the nullness checker bug where it doesn't know that the
    lambda or anonymous class only gets called after initialization is complete. 
    
    I fixed this by lazily instantiating the QueuedTask that used the return value of this method. That way, I could
    remove the @UnderInitialization annotation of the implicit this parameter. This method is now called after
    construction completes, so it works fine. Very annoying that I need to do this, but it's a relatively clean
    solution.  
   */
  private ParameterizedCallable<String, Collection<@NonNull R>> createCallable() {
    return new ParameterizedCallable<String, Collection<@NonNull R>>(null) {
      @Override
      public Collection<@NonNull R> call(String inputData) {
        return retrieveNow(inputData);
      }
    };
  }
  
  private Collection<@NonNull R> retrieveNow(String text) {
    assert controller != null;
    assert searchFieldCombo != null;
    return controller.retrieveNow(searchFieldCombo.getSelected(), getSearchOption(), text);
  }
  
  @Subscribe
  public void doSearchNow(MasterEventBus.SearchNowEvent searchNowEvent) {
    searchNow();
  }

  // This is public because I expect other classes to use it in the future. 
  @SuppressWarnings("WeakerAccess")
  public void searchNow() {
    assert SwingUtilities.isEventDispatchThread();
    assert findField != null;
    recordConsumer.accept(retrieveNow(findField.getText()));
  }

  private SearchOption getSearchOption() {
    return searchOptionsPanel.isContentVisible() ? optionsGroup.getSelected() : SearchOption.findWhole;
  }

  // This needs to be a separate object so we can pass it to the QueuedTask.
  // I passed the RecordController as a parameter to avoid a nullness checker warning. It complains that the 
  // controller member may be null.
  private static <RR> Consumer<Collection<@NonNull RR>> createRecordConsumer(
      RecordController<RR, Integer, LeadField> theController
  ) {
    return records -> SwingUtilities.invokeLater(() -> theController.setFoundRecords(records));
  }

  @Override
  public void indexChanged(final int index, int prior) {
    loadInfoLine();
  }

  private void searchOptionChanged(@SuppressWarnings("unused") ButtonModel selectedButtonModel) { searchNow(); }
}
