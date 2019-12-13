package com.neptunedreams.jobs.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonModel;
import javax.swing.FocusManager;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import com.neptunedreams.framework.ErrorReport;
import com.google.common.eventbus.Subscribe;
import com.neptunedreams.framework.data.RecordModel;
import com.neptunedreams.framework.data.RecordModelListener;
import com.neptunedreams.framework.data.SearchOption;
import com.neptunedreams.framework.ui.EnumComboBox;
import com.neptunedreams.framework.ui.EnumGroup;
import com.neptunedreams.framework.ui.HidingPanel;
import com.neptunedreams.framework.ui.SwingUtils;
import com.neptunedreams.framework.ui.SwipeDirection;
import com.neptunedreams.framework.ui.SwipeView;
import com.neptunedreams.jobs.data.LeadField;
import com.neptunedreams.framework.event.MasterEventBus;
import com.neptunedreams.framework.task.ParameterizedCallable;
import com.neptunedreams.framework.task.QueuedTask;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;

//import org.checkerframework.checker.initialization.qual.UnknownInitialization;

/**
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
@SuppressWarnings("HardCodedStringLiteral")
public class RecordUI<R> extends JPanel implements RecordModelListener {

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

  // We set the initial text to a space, so we can fire the initial search by setting the text to the empty String.
  private JTextField findField = new JTextField(" ",10);
  private final RecordController<R, Integer> controller;
  private EnumComboBox<LeadField> searchFieldCombo = new EnumComboBox<>(LeadField.values());
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
  private final Consumer<Collection<R>> recordConsumer = createRecordConsumer();
  private @NonNull QueuedTask<String, Collection<R>> queuedTask;

  @SuppressWarnings({"methodref.inference.unimplemented", "methodref.receiver.bound.invalid"})
  private HidingPanel makeSearchOptionsPanel(@UnderInitialization RecordUI<R> this, EnumGroup<SearchOption> optionsGroup) {
    JPanel optionsPanel = new JPanel(new GridLayout(1, 0));
    JRadioButton findExact = optionsGroup.add(SearchOption.findWhole);
    JRadioButton findAll = optionsGroup.add(SearchOption.findAll);
    JRadioButton findAny = optionsGroup.add(SearchOption.findAny);
    optionsPanel.add(findAny);
    optionsPanel.add(findAll);
    optionsPanel.add(findExact);
    optionsGroup.setSelected(SearchOption.findAny);

//    optionsGroup.addButtonGroupListener(selectedButtonModel -> selectionChanged(selectedButtonModel));
    optionsGroup.addButtonGroupListener(this::selectionChanged); // Using a lambda is an error. This is a warning. 

    final HidingPanel hidingPanel = HidingPanel.create(optionsPanel);
    hidingPanel.setDisableInsteadOfHide(true);
    return hidingPanel;
  }

  @SuppressWarnings({"method.invocation.invalid", "argument.type.incompatible", "JavaDoc"})
  // add(), setBorder(), etc not properly annotated in JDK.
  public RecordUI(@NonNull RecordModel<R> model, RecordView<R> theView, RecordController<R, Integer> theController) {
    super(new BorderLayout());
    recordModel = model;
    recordView = theView;
    final JLayer<RecordView<R>> layer = wrapInLayer(theView);
    add(layer, BorderLayout.CENTER);
    add(createControlPanel(), BorderLayout.PAGE_START);
    add(createTrashPanel(), BorderLayout.PAGE_END);
    controller = theController;
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
  }
  
  @SuppressWarnings({"ResultOfObjectAllocationIgnored", "Convert2MethodRef"})
  private void setupActions(SwipeView<RecordView<R>> swipeView) {
    new ButtonAction("Previous", KeyEvent.VK_LEFT, 0, ()-> swipeView.swipeRight(() -> recordModel.goPrev()));
    new ButtonAction("Next", KeyEvent.VK_RIGHT, 0, ()-> swipeView.swipeLeft(() -> recordModel.goNext()));
    new ButtonAction("First Record", KeyEvent.VK_LEFT, InputEvent.META_DOWN_MASK, () -> swipeView.swipeRight(() -> recordModel.goFirst()));
    new ButtonAction("Last Record", KeyEvent.VK_RIGHT, InputEvent.META_DOWN_MASK, () -> swipeView.swipeLeft(() -> recordModel.goLast()));
  }

  @SuppressWarnings("CloneableClassWithoutClone")
  private final class ButtonAction extends AbstractAction {
    private final Runnable operation;
    private FocusManager focusManager = FocusManager.getCurrentManager();


    private ButtonAction(final String name, int key, int modifiers, final Runnable theOp) {
      super(name);
      operation = theOp;
      KeyStroke keyStroke = KeyStroke.getKeyStroke(key, modifiers);
      
      InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      ActionMap actionMap = getActionMap();
      inputMap.put(keyStroke, name);
      actionMap.put(name, this);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
      final Component owner = focusManager.getPermanentFocusOwner();
      // The second half of this conditional doesn't work. It may be because the text components already have
      // KeyStrokes mapped to arrow keys.
      if ((!(owner instanceof JTextComponent)) || (((JTextComponent) owner).getText().isEmpty())) {
        operation.run();
      }
    }
  }


  private JLayer<RecordView<R>> wrapInLayer(@UnderInitialization RecordUI<R> this, RecordView<R> recordView) {
    swipeView = SwipeView.wrap(recordView);
    return swipeView.getLayer();
  }
  
  @SuppressWarnings("JavaDoc")
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
    buttonPanel.add(SwingUtils.wrapWest(searchOptionsPanel), BorderLayout.CENTER);
    buttonPanel.add(getButtons(), BorderLayout.PAGE_END);
    return buttonPanel;
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
    
    JButton timeButton = makeTimeButton();
    buttonPanel.add(SwingUtils.wrapSouth(timeButton), BorderLayout.LINE_END);
    setupActions(sView);

    return buttonPanel;
  }

  private JButton makeTimeButton() {
    JButton timeButton = new JButton();
    timeButton.addActionListener(e -> newHistoryEvent(timeButton.getText()));
    SwingWorker<String, String> timeWorker = new SwingWorker<String, String>() {
      @Override
      protected String doInBackground() throws InterruptedException {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);
        long time = System.currentTimeMillis();
        //noinspection InfiniteLoopStatement
        while (true) {
          ZonedDateTime now = ZonedDateTime.now();
          String nowText = now.format(formatter);
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

  private void newHistoryEvent(String time) {
    String timeText = String.format("%n%n• %s%n", time);
    recordView.addHistoryEvent(timeText);
  }

  private void addBlankRecord() {
    controller.addBlankRecord();
    MasterEventBus.postUserRequestedNewRecordEvent();
    loadInfoLine();
  }

//  private void doImport() {
//    ImportDialog importDialog = new ImportDialog((Window) getRootPane().getParent(), controller.getDao());
//    importDialog.setVisible(true);
//  }

  @SuppressWarnings("method.invocation.invalid")
  private JPanel getSearchField() {
    JLabel findIcon = Resource.getMagnifierLabel();
    RecordView.installStandardCaret(findField);
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
//    Thread.dumpStack();
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
//      System.err.printf("Info: %S%n", info); // NON-NLS
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
  private ParameterizedCallable<String, Collection<R>> createCallable() {
    return new ParameterizedCallable<String, Collection<R>>(null) {
      @Override
      public Collection<R> call(String inputData) {
        return retrieveNow(inputData);
      }
    };
  }
  
  private Collection<R> retrieveNow(String text) {
    assert controller != null;
    assert searchFieldCombo != null;
    return controller.retrieveNow(searchFieldCombo.getSelected(), getSearchOption(), text);
  }
  
  @SuppressWarnings("JavaDoc")
  @Subscribe
  public void doSearchNow(MasterEventBus.SearchNowEvent searchNowEvent) {
    searchNow();
  }

  // This is public because I expect other classes to use it in the future. 
  @SuppressWarnings({"WeakerAccess", "JavaDoc"})
  public void searchNow() {
    assert SwingUtilities.isEventDispatchThread();
    assert findField != null;
    recordConsumer.accept(retrieveNow(findField.getText()));
  }

  private SearchOption getSearchOption() {
    return searchOptionsPanel.isContentVisible() ? optionsGroup.getSelected() : SearchOption.findWhole;
  }

  @SuppressWarnings("dereference.of.nullable") // controller is null when we call this, but not when we call the lambda.
  private Consumer<Collection<R>> createRecordConsumer(@UnderInitialization RecordUI<R>this) {
    return records -> SwingUtilities.invokeLater(() -> controller.setFoundRecords(records));
  }

  @Override
  public void indexChanged(final int index, int prior) {
    loadInfoLine();
  }

  private void selectionChanged(@SuppressWarnings("unused") ButtonModel selectedButtonModel) { searchNow(); }
}