package com.neptunedreams.jobs.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import com.google.common.eventbus.Subscribe;
import com.neptunedreams.framework.data.Dao;
import com.neptunedreams.framework.data.RecordModel;
import com.neptunedreams.framework.data.RecordSelectionModel;
import com.neptunedreams.framework.data.SearchOption;
import com.neptunedreams.framework.event.ChangeRecord;
import com.neptunedreams.framework.event.MasterEventBus;
import com.neptunedreams.framework.ui.EnhancedCaret;
import com.neptunedreams.framework.ui.FieldBinding;
import com.neptunedreams.framework.ui.FieldIterator;
import com.neptunedreams.framework.ui.FieldIterator.Direction;
import com.neptunedreams.framework.ui.Keystrokes;
import com.neptunedreams.framework.ui.RecordController;
import com.neptunedreams.framework.ui.SelectionSpy;
import com.neptunedreams.framework.ui.SwingUtils;
import com.neptunedreams.framework.ui.SwipeDirection;
import com.neptunedreams.framework.ui.SwipeView;
import com.neptunedreams.jobs.data.LeadField;
import com.neptunedreams.jobs.gen.tables.records.LeadRecord;
import com.neptunedreams.util.StringStuff;
import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.UnknownKeyFor;

import static com.neptunedreams.framework.ui.FieldIterator.Direction.*;
import static com.neptunedreams.framework.ui.SwingUtils.*;
import static com.neptunedreams.jobs.ui.Resource.*;
import static com.neptunedreams.util.StringStuff.*;

//import org.checkerframework.checker.initialization.qual.Initialized;
//import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 10:54 AM
 * <p>The panel to show the value of each retrieved record. This is mostly display components and subsidiary ui
 * components, but not the main ui controls. It sits inside the RecordUI.
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings({"WeakerAccess", "HardCodedStringLiteral"})
public final class RecordView<@NonNull R> extends JPanel implements RecordSelectionModel<R> {
  private static final int TEXT_COLUMNS = 20;
  private static final int TEXT_ROWS = 15;
  private static final int HISTORY_COLUMNS = 40;
  private static final String[] EMPTY_ARRAY = new String[0];
  private final JPanel labelPanel = new JPanel(new GridLayout(0, 1));
  private final JPanel fieldPanel = new JPanel(new GridLayout(0, 1));
  private final JPanel checkBoxPanel = new JPanel(new GridLayout(0, 1));
  private final ButtonGroup buttonGroup = new ButtonGroup();

  private R currentRecord;

  private final RecordController<R, Integer, LeadField> controller;
  private final List<? extends FieldBinding<R, ? extends Serializable, ? extends JComponent>> allBindings;
  private final JTextComponent companyField;
  private final JTextArea historyField;
  private final JTextComponent descriptionField;
  private final JTextComponent dicePosnField;
  private final JTextComponent diceIdField;
  private final List<JTextComponent> componentList;
  private FieldIterator fieldIterator;
  private final AtomicReference<String[]> searchTerms = new AtomicReference<>(RecordView.EMPTY_ARRAY);

  private RecordView(R record,
                     LeadField initialSort,
                     Dao<R, Integer, LeadField> dao,
                     Supplier<R> recordConstructor,
                     Function<R, Integer> getIdFunction, BiConsumer<R, Integer> setIdFunction,
                     Function<R, String> getCompanyFunction, BiConsumer<R, String> setCompanyFunction,
                     Function<R, String> getContactNameFunction, BiConsumer<R, String> setContactNameFunction,
                     Function<R, String> getClientFunction, BiConsumer<R, String> setClientFunction,
                     Function<R, String> getDicePosnFunction, BiConsumer<R, String> setDicePosnFunction,
                     Function<R, String> getDiceIdFunction, BiConsumer<R, String> setDiceIdFunction,
                     Function<R, String> getEMailFunction, BiConsumer<R, String> setEMailFunction,
                     Function<R, String> getPhone1Function, BiConsumer<R, String> setPhone1Function,
                     Function<R, String> getPhone2Function, BiConsumer<R, String> setPhone2Function,
                     Function<R, String> getPhone3Function, BiConsumer<R, String> setPhone3Function,
                     Function<R, String> getFaxFunction, BiConsumer<R, String> setFaxFunction,
                     Function<R, String> getWebSiteFunction, BiConsumer<R, String> setWebSiteFunction,
                     Function<R, String> getSkypeFunction, BiConsumer<R, String> setSkypeFunction,
                     Function<R, String> getDescriptionFunction, BiConsumer<R, String> setDescriptionFunction,
                     Function<R, String> getHistoryFunction, BiConsumer<R, String> setHistoryFunction,
                     Function<R, String> getCreatedOnFunction
  ) {
    super(new BorderLayout());
    currentRecord = record;
    controller = makeController(initialSort, dao, recordConstructor, getIdFunction);
    final JLabel idField = (JLabel) addField("ID", false, LeadField.ID, initialSort);
    companyField = (JTextComponent) addField("Company", true, LeadField.Company, initialSort);
    final JTextComponent contactNameField = (JTextComponent) addField("Contact Name", true, LeadField.ContactName, initialSort);
    final JTextComponent clientField = (JTextComponent) addField("Client", true, LeadField.Client, initialSort);
    dicePosnField = (JTextComponent) addField("Dice Position", true, LeadField.DicePosn, initialSort, makeScanButton());
    diceIdField = (JTextComponent) addField("Dice ID", true, LeadField.DiceID, initialSort);
    final JTextComponent eMailField = (JTextComponent) addFieldWithCopy("EMail", LeadField.EMail, initialSort);
    final JTextComponent phone1Field = (JTextComponent) addFieldWithCopy("Phone 1", LeadField.Phone1, initialSort);
    final JTextComponent phone2Field = (JTextComponent) addFieldWithCopy("Phone 2", LeadField.Phone2, initialSort);
    final JTextComponent phone3Field = (JTextComponent) addFieldWithCopy("Phone 3", LeadField.Phone3, initialSort);
    final JTextComponent faxField = (JTextComponent) addFieldWithCopy("Fax", LeadField.Fax, initialSort);
    final JTextComponent webSiteField = (JTextComponent) addFieldWithCopy("Web Site", LeadField.WebSite, initialSort);
    final JTextComponent skypeField = (JTextComponent) addFieldWithCopy("Skype", LeadField.Skype, initialSort);
    final JLabel createdOnField = (JLabel) addField("", false, LeadField.CreatedOn, initialSort);
    descriptionField = addDescriptionField();
    historyField = new JTextArea(TEXT_ROWS, HISTORY_COLUMNS);
    assert getIdFunction != null : "Null id getter";
    assert setIdFunction != null : "Null id Setter";
    final FieldBinding.IntegerBinding<R> idBinding = FieldBinding.bindInteger(getIdFunction, idField);
    final FieldBinding.StringEditableBinding<R> sourceBinding = FieldBinding.bindEditableString(getCompanyFunction, setCompanyFunction, companyField);
    final FieldBinding.StringEditableBinding<R> contactNameBinding = FieldBinding.bindEditableString(getContactNameFunction, setContactNameFunction, contactNameField);
    final FieldBinding.StringEditableBinding<R> clientBinding = FieldBinding.bindEditableString(getClientFunction, setClientFunction, clientField);
    final FieldBinding.StringEditableBinding<R> dicePosnBinding = FieldBinding.bindEditableString(getDicePosnFunction, setDicePosnFunction, dicePosnField);
    final FieldBinding.StringEditableBinding<R> diceIdBinding = FieldBinding.bindEditableString(getDiceIdFunction, setDiceIdFunction, diceIdField);
    final FieldBinding.StringEditableBinding<R> eMailBinding = FieldBinding.bindEditableString(getEMailFunction, setEMailFunction, eMailField);
    final FieldBinding.StringEditableBinding<R> phone1Binding = FieldBinding.bindEditableString(getPhone1Function, setPhone1Function, phone1Field);
    final FieldBinding.StringEditableBinding<R> phone2Binding = FieldBinding.bindEditableString(getPhone2Function, setPhone2Function, phone2Field);
    final FieldBinding.StringEditableBinding<R> phone3Binding = FieldBinding.bindEditableString(getPhone3Function, setPhone3Function, phone3Field);
    final FieldBinding.StringEditableBinding<R> faxBinding = FieldBinding.bindEditableString(getFaxFunction, setFaxFunction, faxField);
    final FieldBinding.StringEditableBinding<R> webSiteBinding = FieldBinding.bindEditableString(getWebSiteFunction, setWebSiteFunction, webSiteField);
    final FieldBinding.StringEditableBinding<R> skypeBinding = FieldBinding.bindEditableString(getSkypeFunction, setSkypeFunction, skypeField);
    final FieldBinding.StringEditableBinding<R> descriptionBinding = FieldBinding.bindEditableString(getDescriptionFunction, setDescriptionFunction, descriptionField);
    final FieldBinding.StringEditableBinding<R> historyBinding = FieldBinding.bindEditableString(getHistoryFunction, setHistoryFunction, historyField);
    final FieldBinding.StringBinding<R> createdOnBinding = FieldBinding.bindConstantString(getCreatedOnFunction, createdOnField);
    allBindings = Arrays.asList(idBinding, sourceBinding, contactNameBinding, clientBinding, dicePosnBinding, diceIdBinding, eMailBinding,
        phone1Binding, phone2Binding, phone3Binding, faxBinding, webSiteBinding, skypeBinding, descriptionBinding, historyBinding, createdOnBinding);

    JPanel tempFieldPanel = makeFieldPanel();
    @NonNull JPanel historyPanel = makeFieldAndHistoryPanel(tempFieldPanel, historyField);
    add(historyPanel, BorderLayout.PAGE_START);

    SwingUtils.installCustomCaret(EnhancedCaret::new,
        companyField,
        contactNameField,
        clientField,
        dicePosnField,
        diceIdField,
        eMailField,
        phone1Field,
        phone2Field,
        phone3Field,
        faxField,
        webSiteField,
        skypeField,
        descriptionField,
        historyField
    );
    componentList = Arrays.asList(
        companyField,
        contactNameField,
        clientField,
        dicePosnField,
        diceIdField,
        eMailField,
        phone1Field,
        phone2Field,
        phone3Field,
        faxField,
        webSiteField,
        skypeField,
        historyField,
        descriptionField
    );
    // start with empty list. 
    fieldIterator = new FieldIterator(componentList, FORWARD, -1);
    SwingUtils.executeOnDisplay(this, this::installIteratorActions);
  }

  private void installIteratorActions() {
    @UnknownKeyFor @Initialized JComponent root = Keystrokes.getLastAncestorOf(this);
    Keystrokes.installKeystrokeAction(root, "nextFoundText", KeyEvent.VK_F3, 0, this::goToNextHilight);
    Keystrokes.installKeystrokeAction(root, "previousFoundText", KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK, this::goToPreviousHilight);
  }

  @SuppressWarnings("method.invocation.invalid")
  private JComponent makeScanButton(@UnderInitialization RecordView<R>this) {
    JButton button = new JButton(getIcon(LEFT_CHEVRON));
    button.addActionListener(e -> scanForDiceInfo());
    button.setFocusable(false);
    return button;
  }

  private void scanForDiceInfo(@Initialized RecordView<R>this) {
    // description is never null, but the test is here to satisfy the Nullness Checker 
    String description = (descriptionField == null) ? "" : emptyIfNull(descriptionField.getText());
    if (scanForDiceInfo(description)) { return; }
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    try {
      String data = clipboard.getData(DataFlavor.stringFlavor).toString();
      scanForDiceInfo(data);
    } catch (UnsupportedFlavorException | IOException ignored) {}
  }

  private boolean scanForDiceInfo(@Initialized RecordView<R> this, final String description) {
    String posnHead = "Position Id :";
    String idHead = "Dice Id :";
    String position = getNextWord(description, posnHead);
    String id = getNextWord(description, idHead);
    if ((position != null) && (id != null)) {
      loadPositionAndId(position, id);
      return true;
    }
    return false;
  }
  
  private void loadPositionAndId(final String position, final String id) {
    diceIdField.setText(id);
    dicePosnField.setText(position);
  }

  private @Nullable String getNextWord(final String source, final String head) {
    int headLoc = source.indexOf(head);
    if (headLoc >= 0) {
      int tailLoc = headLoc + head.length();
      String remainder = source.substring(tailLoc);
      StringTokenizer tokenizer = new StringTokenizer(remainder);
      if (tokenizer.hasMoreTokens()) {
        return tokenizer.nextToken().trim();
      }
    }
    return null;
  }

  private static JPanel makeFieldAndHistoryPanel(JPanel fieldPanel, JTextArea historyField) {
    JPanel fieldAndHistoryPanel = new JPanel(new BorderLayout());
    fieldAndHistoryPanel.add(fieldPanel, BorderLayout.LINE_START);
    fieldAndHistoryPanel.add(scrollArea(historyField), BorderLayout.CENTER);
    return fieldAndHistoryPanel;
  }

  @SuppressWarnings({"return.type.incompatible", "argument.type.incompatible"})
  private @NonNull RecordController<R, Integer, LeadField> makeController(
      @UnderInitialization RecordView<R> this,
      final LeadField initialSort, 
      final Dao<R, Integer, LeadField> dao, 
      final Supplier<R> recordConstructor,
      final Function<R, Integer> getIdFunction
  ) {
    return RecordController.createRecordController(
        dao,
        this,
        initialSort,
        recordConstructor,
        getIdFunction
    );
  }

  private void register() {
    MasterEventBus.registerMasterEventHandler(this);
  }

  // I'm not sure why I shouldn't say @UnderInitialization RecordView<R> this here, but it may be because the nullness
  // checker recognizes that no more members are set, so it changes the type of this to Initialized part way through
  // the constructor. It may also be that the @RequiresNonNull fixes this. 
//  @RequiresNonNull({"labelPanel", "fieldPanel", "checkBoxPanel"})
  private JPanel makeFieldPanel(@UnderInitialization RecordView<R> this) {
    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(labelPanel, BorderLayout.LINE_START);
    topPanel.add(fieldPanel, BorderLayout.CENTER);
    topPanel.add(checkBoxPanel, BorderLayout.LINE_END);

    // Put a line at the top, one pixel wide.
    topPanel.setBorder(new MatteBorder(1, 0, 0, 0, Color.black));
    return topPanel;
  }

  /**
   * Get the Controller
   * @return The RecordController
   */
  public RecordController<R, Integer, LeadField> getController() { return controller; }

//  @RequiresNonNull({"labelPanel", "fieldPanel", "buttonGroup", "checkBoxPanel", "controller"})
  private JComponent addField(@UnderInitialization RecordView<R> this, final String labelText, final boolean editable, final LeadField orderField, LeadField initialSort) {
    return addField(labelText, editable, orderField, initialSort, null);
  }

//  @RequiresNonNull({"labelPanel", "fieldPanel", "buttonGroup", "checkBoxPanel", "controller"})
  private JComponent addFieldWithCopy(@UnderInitialization RecordView<R>this, final String labelText, final LeadField orderField, LeadField initialSort) {
    JButton copyButton = new JButton(getIcon(COPY));

    setNoBorder(copyButton);
    // For any field with a copy button, editable will be true
    JTextField field = (JTextField) addField(labelText, true, orderField, initialSort, copyButton);
    copyButton.addActionListener(e -> copyFrom(field));
    Document doc = field.getDocument();
    DocumentListener docListener = new DocumentListener() {
      @Override public void insertUpdate(final DocumentEvent e) { work();}
      @Override public void removeUpdate(final DocumentEvent e) { work();}
      @Override public void changedUpdate(final DocumentEvent e) { work();}
      
      private void work() {
        copyButton.setEnabled(doc.getLength() > 0);
      }
    };
    doc.addDocumentListener(docListener);
    copyButton.setEnabled(false);
    copyButton.setToolTipText(String.format("Copy %s", labelText));
    return field;
  }

//  @RequiresNonNull({"labelPanel", "fieldPanel", "buttonGroup", "checkBoxPanel", "controller"})
  private JComponent addField(@UnderInitialization RecordView<R>this, final String labelText, final boolean editable, final LeadField orderField, LeadField initialSort, @Nullable JComponent extraComponent) {
    JLabel label = new JLabel(String.format("%s:", labelText));
    labelPanel.add(label);
    JComponent field;
    if (editable) {
      field = new JTextField(TEXT_COLUMNS);
    } else {
      field = new JLabel();
    }
    JComponent comp = wrapField(field, labelText);
    if (extraComponent != null) {
      JPanel wrapPanel = new JPanel(new BorderLayout());
      wrapPanel.add(comp, BorderLayout.CENTER);
      wrapPanel.add(extraComponent, BorderLayout.LINE_END);
      comp = wrapPanel;
    }
    fieldPanel.add(comp);
    JRadioButton orderBy = new JRadioButton("");
    buttonGroup.add(orderBy);
    if (orderField == initialSort) {
      orderBy.setSelected(true);
    }
    checkBoxPanel.add(orderBy);
    @SuppressWarnings("method.invocation.invalid") // We are under initialization when we create this, not when calling
        ItemListener checkBoxListener = (itemEvent) -> itemStateChanged(orderField, itemEvent);
    orderBy.addItemListener(checkBoxListener);
    return field;
  }

  private void itemStateChanged(final LeadField orderField, final ItemEvent itemEvent) {
    if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
      controller.specifyOrder(orderField);
      // Here's where I want to call RecordUI.searchNow(). The code needs to be restructured before I can do that.
      MasterEventBus.postSearchNowEvent();
    }
  }

  /**
   * Copies the text of the field, regardless of if it's selected. (We can't just use field.copy() because that only
   * copies selected text.)
   *
   * @param field The field
   */
  private void copyFrom(@UnderInitialization RecordView<R> this, JTextField field) {
    String txt = field.getText();
    StringSelection stringSelection = new StringSelection(txt);
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, stringSelection);
  }

  private JTextComponent addDescriptionField(@UnderInitialization RecordView<R> this) {
    final JTextArea wrappedField = new JTextArea(TEXT_ROWS, TEXT_COLUMNS);
    JComponent scrollPane = scrollArea(wrappedField);
    add(BorderLayout.CENTER, scrollPane);
    return wrappedField;
  }

  JComponent wrapField(@UnderInitialization RecordView<R> this, JComponent component, String label) {
    if (!(component instanceof JTextField)) {
      Icon blankIcon = makeBlankIcon(getIcon(FORWARD_CHEVRON));
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(new JLabel(blankIcon), BorderLayout.LINE_START);
      panel.add(component, BorderLayout.CENTER);
      return panel;
    }
    JButton button = new JButton(getIcon(FORWARD_CHEVRON));
    setNoBorder(button);
    button.setToolTipText(String.format("Copy selected text to %s", label));
    button.setFocusable(false);
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(button, BorderLayout.LINE_START);
    panel.add(component, BorderLayout.CENTER);
    JTextField field = (JTextField) component;
    button.addActionListener(e -> {
      field.setText(SelectionSpy.spy.getSelectedText());
      button.setEnabled(false);
    });
    SelectionSpy.spy
        .addSelectionExistsListener((selectionExists) -> button.setEnabled(selectionExists && field.getText().isEmpty()));
    return panel;
  }
  
  private static Icon makeBlankIcon(final Icon template) {
    // Make a blank icon the same size as the template
    return new Icon() {
      @Override
      public void paintIcon(final Component c, final Graphics g, final int x, final int y) { }

      @Override
      public int getIconWidth() { 
        return template.getIconWidth();
      }

      @Override
      public int getIconHeight() {
        return template.getIconHeight();
      }
    };
  }

  /**
   * Responds to the RecordChange event sent by the MasterEventBus
   * @param recordEvent The record event which holds the new record
   */
  @Subscribe
  public void setCurrentRecord(ChangeRecord<R> recordEvent) {
    R newRecord = recordEvent.getNewRecord();
    assert newRecord != null;
    currentRecord = newRecord;
    for (FieldBinding<R, ?, ?> binding: allBindings) {
      binding.prepareEditor(newRecord);
    }
  }

  @Override
  public boolean isRecordDataModified() {
    for (FieldBinding<R, ?, ?> binding: allBindings) {
      if (binding.isEditable() && binding.propertyHasChanged(currentRecord)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Saves any changes to the current record before exiting the application
   * @return true if the edits were saved, false otherwise.
   */
  public boolean saveOnExit() {// throws SQLException {
    // Test four cases:
    // 1. New Record with data
    // 2. New Record with no data
    // 3. Existing record with changes
    // 4. Existing record with no changes
    final boolean hasChanged = isRecordDataModified();
    if (hasChanged) {
      loadUserEdits();
      return true;
    }
    return false;
  }

  @Override
  public R getCurrentRecord() {
    return currentRecord;
  }

  /**
   * Reads the data from the editor fields and loads them into the current record's model. This gets called by
   * the event bus in response to a LoadUIEvent.
   * @param event Unused. This used to be a Record, but we didn't need it.
   */
  @Subscribe
  public void loadUserEdits(MasterEventBus.LoadUIEvent event) {
    loadUserEdits();
  }

  private void loadUserEdits() {
    for (FieldBinding<R, ?, ?> binding : allBindings) {
      if (binding.isEditable()) {
        binding.getEditableBinding().saveEdit(currentRecord);
      }
    }
  }

  @Subscribe
  void userRequestedNewRecord(MasterEventBus.UserRequestedNewRecordEvent event) {
    companyField.requestFocus();
  }

  /**
   * Adds a new history event, with a date and time, to the history field
   * @param timeText The text of the date and time.
   */
  public void addHistoryEvent(final String timeText) {
    String historyText = historyField.getText().trim();
    historyField.setText(historyText);
    if (!historyText.isEmpty()) {
      //noinspection HardcodedLineSeparator
      historyField.append("\n\n");
    }
    historyField.append(timeText);
    historyField.setSelectionStart(historyField.getText().length());
    historyField.requestFocus();
  }
  
  void setNewSearch(String searchTerm, SearchOption searchOption) {
    if (searchOption == SearchOption.findWhole) {
      searchTerms.set(new String[]{searchTerm});
    } else {
      searchTerms.set(StringStuff.splitText(searchTerm));
    }
  }
  
  void hilightNextTerm(Direction direction, boolean termsChanged) {
    
    @UnknownKeyFor @Initialized RecordModel<R> recordModel = getController().getModel();
    LeadRecord foundRecord = (LeadRecord) recordModel.getFoundRecord();
    int id = foundRecord.getId();
    if (termsChanged) {
      // Start with forward for new terms.
      fieldIterator = new FieldIterator(componentList, FORWARD, id, searchTerms.get());
    } else if(fieldIterator.getId() != id) {
      // Maintain the current direction by getting it from the previous FieldIterator.
      fieldIterator = new FieldIterator(componentList, fieldIterator.getDirection(), id, searchTerms.get());
    }
    
    if (direction == FORWARD) {
      if (!fieldIterator.hasNext()) {
        recordModel.goNext();
        foundRecord = (LeadRecord) recordModel.getFoundRecord();
        fieldIterator = new FieldIterator(componentList, FORWARD, foundRecord.getId(), searchTerms.get());
      }
      fieldIterator.goToNext();
    } else {
      if (!fieldIterator.hasPrevious()) {
        recordModel.goPrev();
        foundRecord = (LeadRecord) recordModel.getFoundRecord();
        fieldIterator = new FieldIterator(componentList, BACKWARD, foundRecord.getId(), searchTerms.get());
      }
      fieldIterator.goToPrevious();
    }
  }
  
  void goToNextHilight() {
    SwipeView.animateAction(this, () -> hilightNextTerm(FORWARD, false), SwipeDirection.SWIPE_LEFT);
  }
  
  void goToPreviousHilight() {
    SwipeView.animateAction(this, () -> hilightNextTerm(BACKWARD, false), SwipeDirection.SWIPE_RIGHT);
  }
  
  void setForwardDirection() {
    fieldIterator.hasNext();
  }

  // If I don't suppress this warning, and initialize these values to null, I just get a assignment.type.incompatible
  // warning instead, because I haven't declared these values Nullable. But if I do that, I'll get warnings when I 
  // build, because they're NonNull in the constructor. Not sure what's the best approach, but this works for now.
  @SuppressWarnings("JavaDoc")
  public static class Builder<@NonNull RR> {
    private final RR record;
    private final LeadField initialSort;
    private @Nullable Function<RR, Integer> getId;
    private @Nullable BiConsumer<RR, Integer> setId;
    private @Nullable Function<RR, String> getCompany;
    private @Nullable BiConsumer<RR, String> setCompany;
    private @Nullable Function<RR, String> getContactName=null;
    private @Nullable BiConsumer<RR, String> setContactName=null;
    private @Nullable Function<RR, String> getClient=null;
    private @Nullable BiConsumer<RR, String> setClient=null;
    private @Nullable Function<RR, String> getDicePosn;
    private @Nullable BiConsumer<RR, String> setDicePosn;
    private @Nullable Function<RR, String> getDiceId=null;
    private @Nullable BiConsumer<RR, String> setDiceId=null;
    private @Nullable Function<RR, String> getEMail=null;
    private @Nullable BiConsumer<RR, String> setEMail=null;
    private @Nullable Function<RR, String> getPhone1=null;
    private @Nullable BiConsumer<RR, String> setPhone1=null;
    private @Nullable Function<RR, String> getPhone2=null;
    private @Nullable BiConsumer<RR, String> setPhone2=null;
    private @Nullable Function<RR, String> getPhone3=null;
    private @Nullable BiConsumer<RR, String> setPhone3=null;
    private @Nullable Function<RR, String> getFax=null;
    private @Nullable BiConsumer<RR, String> setFax=null;
    private @Nullable Function<RR, String> getWebSite=null;
    private @Nullable BiConsumer<RR, String> setWebSite=null;
    private @Nullable Function<RR, String> getSkype=null;
    private @Nullable BiConsumer<RR, String> setSkype=null;
    private @Nullable Function<RR, String> getDescription=null;
    private @Nullable BiConsumer<RR, String> setDescription=null;
    private @Nullable Function<RR, String> getHistory=null;
    private @Nullable BiConsumer<RR, String> setHistory=null;
    private @Nullable Function<RR, String> getCreatedOn=null;
    private @Nullable Dao<RR, Integer, LeadField> dao=null;
    private @Nullable Supplier<RR> recordConstructor=null;

    public Builder(RR record, LeadField initialSort) {
      this.record = record;
      this.initialSort = initialSort;
    }

    @EnsuresNonNull({"getId", "setId"})
    public Builder<RR> id(@NonNull Function<RR, Integer> getter, @NonNull BiConsumer<RR, Integer> setter) {
      getId = getter;
      setId = setter;
      return this;
    }

    @EnsuresNonNull({"getCompany", "setCompany"})
    public Builder<RR> company(@NonNull Function<RR, String> getter, @NonNull BiConsumer<RR, String> setter) {
      getCompany = getter;
      setCompany = setter;
      return this;
    }

    @EnsuresNonNull({"getContactName", "setContactName"})
    public Builder<RR> contactName(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getContactName = getter;
      setContactName = setter;
      return this;
    }

    @EnsuresNonNull({"getClient", "setClient"})
    public Builder<RR> client(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getClient = getter;
      setClient = setter;
      return this;
    }

    @EnsuresNonNull({"getDicePosn", "setDicePosn"})
    public Builder<RR> dicePosn(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getDicePosn = getter;
      setDicePosn = setter;
      return this;
    }

    @EnsuresNonNull({"getDiceId", "setDiceId"})
    public Builder<RR> diceId(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getDiceId = getter;
      setDiceId = setter;
      return this;
    }

    @EnsuresNonNull({"getEMail", "setEMail"})
    public Builder<RR> email(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getEMail = getter;
      setEMail = setter;
      return this;
    }

    @EnsuresNonNull({"getPhone1", "setPhone1"})
    public Builder<RR> phone1(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getPhone1 = getter;
      setPhone1 = setter;
      return this;
    }

    @EnsuresNonNull({"getPhone2", "setPhone2"})
    public Builder<RR> phone2(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getPhone2 = getter;
      setPhone2 = setter;
      return this;
    }

    @EnsuresNonNull({"getPhone3", "setPhone3"})
    public Builder<RR> phone3(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getPhone3 = getter;
      setPhone3 = setter;
      return this;
    }

    @EnsuresNonNull({"getFax", "setFax"})
    public Builder<RR> fax(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getFax = getter;
      setFax = setter;
      return this;
    }

    @EnsuresNonNull({"getWebSite", "setWebSite"})
    public Builder<RR> website(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getWebSite = getter;
      setWebSite = setter;
      return this;
    }

    @EnsuresNonNull({"getSkype", "setSkype"})
    public Builder<RR> skype(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getSkype = getter;
      setSkype = setter;
      return this;
    }

    @EnsuresNonNull({"getDescription", "setDescription"})
    public Builder<RR> description(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getDescription = getter;
      setDescription = setter;
      return this;
    }

    @EnsuresNonNull({"getHistory", "setHistory"})
    public Builder<RR> history(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getHistory = getter;
      setHistory = setter;
      return this;
    }

    @EnsuresNonNull("getCreatedOn")
    public Builder<RR> createdOn(Function<RR, Timestamp> getter) {
      getCreatedOn = (d) -> getter.apply(d).toString();
      return this;
    }

    @EnsuresNonNull("#1")
    public Builder<RR> withDao(Dao<RR, Integer, LeadField> dao) {
      this.dao = dao;
      return this;
    }

    @EnsuresNonNull("recordConstructor")
    public Builder<RR> withConstructor(Supplier<RR> constructor) {
      recordConstructor = constructor;
      return this;
    }
    
    // TODO: Make this work. The @RequiresNotNull annotation is supposed to fix the warning suppressed by the subsequent
    // annotation. I don't know why it doesn't work, but it may be a bug in the checker framework.
    // I have tried to replace the us of @RequiresNotNull with @EnsuresNonNull, but it doesn't work here. In smaller
    // test cases, I've been able to get to work. It seems like a more robust (if more verbose) approach if it would
    // work.

//      @RequiresNonNull({"dao", "recordConstructor", "getId", "setId", "getCompany", "setCompany", "getContactName", 
//          "setContactName", "getClient", "setClient", "getDicePosn", "setDicePosn", "getDiceId", "setDiceId",
//          "getEMail", "setEMail", "getPhone1", "setPhone1", "getPhone2", "setPhone2", "getPhone3", "setPhone3",
//          "getFax", "setFax", "getWebSite", "setWebSite", "getSkype", "setSkype", "getDescription", "setDescription",
//          "getHistory", "setHistory", "getCreatedOn"})
    @SuppressWarnings("argument.type.incompatible")
      public RecordView<RR> build() {
      final RecordView<RR> view = new RecordView<>(
          record,
          initialSort,
          dao,
          recordConstructor,
          getId, setId,
          getCompany, setCompany,
          getContactName, setContactName,
          getClient, setClient,
          getDicePosn, setDicePosn,
          getDiceId, setDiceId,
          getEMail, setEMail,
          getPhone1, setPhone1,
          getPhone2, setPhone2,
          getPhone3, setPhone3,
          getFax, setFax,
          getWebSite, setWebSite,
          getSkype, setSkype,
          getDescription, setDescription,
          getHistory, setHistory,
          getCreatedOn
      );
      view.register();
      return view;
    }
  }
}
