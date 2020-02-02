package com.neptunedreams.jobs.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.ButtonGroup;
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
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import com.google.common.eventbus.Subscribe;
import com.neptunedreams.framework.data.Dao;
import com.neptunedreams.framework.data.RecordSelectionModel;
import com.neptunedreams.framework.event.ChangeRecord;
import com.neptunedreams.framework.event.MasterEventBus;
import com.neptunedreams.framework.ui.FieldBinding;
import com.neptunedreams.framework.ui.RecordController;
import com.neptunedreams.framework.ui.SelectionSpy;
import com.neptunedreams.jobs.data.LeadField;
import org.checkerframework.checker.initialization.qual.NotOnlyInitialized;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;

import static com.neptunedreams.framework.ui.SwingUtils.*;

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
public final class RecordView<R> extends JPanel implements RecordSelectionModel<R> {
  private static final int TEXT_COLUMNS = 20;
  private static final int TEXT_ROWS = 15;
  private static final int HISTORY_COLUMNS = 40;
  private JPanel labelPanel = new JPanel(new GridLayout(0, 1));
  private JPanel fieldPanel = new JPanel(new GridLayout(0, 1));
  private JPanel checkBoxPanel = new JPanel(new GridLayout(0, 1));
  private ButtonGroup buttonGroup = new ButtonGroup();

  private R currentRecord; // = new Record("D", "D", "D", "D");

  @NotOnlyInitialized
  private RecordController<R, Integer, LeadField> controller;
  private final List<? extends FieldBinding<R, ? extends Serializable, ? extends JComponent>> allBindings;
  private final JTextComponent companyField;
  private final JTextArea historyField;
  private final JTextComponent descriptionField;
  private final JTextComponent dicePosnField;
  private final JTextComponent diceIdField;

  private RecordView(R record,
                     LeadField initialSort,
                     Dao<R, Integer, LeadField> dao,
                     Supplier<R> recordConstructor,
                     Function<R, Integer> getIdFunction, BiConsumer<R, Integer> setIdFunction,
                     Function<R, String> getCompanyFunction, BiConsumer<R, String> setCompanyFunction,
                     Function<R, String> getContactNameFunction, BiConsumer<R, String> setContactNameFunction,
                     Function<R, String> getDicePosnFunction, BiConsumer<R, String> setDicePosnFunction,
                     Function<R, String> getDiceIdFunction, BiConsumer<R, String> setDiceIdFunction,
                     Function<R, String> getEMailFunction, BiConsumer<R, String> setEMailFunction,
                     Function<R, String> getPhone1Function, BiConsumer<R, String> setPhone1Function,
                     Function<R, String> getPhone2Function, BiConsumer<R, String> setPhone2Function,
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
    dicePosnField = (JTextComponent) addField("Dice Position", true, LeadField.DicePosn, initialSort, makeScanButton());
    diceIdField = (JTextComponent) addField("Dice ID", true, LeadField.DiceID, initialSort);
    final JTextComponent eMailField = (JTextComponent) addFieldWithCopy("EMail", LeadField.EMail, initialSort);
    final JTextComponent phone1Field = (JTextComponent) addFieldWithCopy("Phone 1", LeadField.Phone1, initialSort);
    final JTextComponent phone2Field = (JTextComponent) addFieldWithCopy("Phone 2", LeadField.Phone2, initialSort);
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
    final FieldBinding.StringEditableBinding<R> userNameBinding = FieldBinding.bindEditableString(getContactNameFunction, setContactNameFunction, contactNameField);
    final FieldBinding.StringEditableBinding<R> dicePosnBinding = FieldBinding.bindEditableString(getDicePosnFunction, setDicePosnFunction, dicePosnField);
    final FieldBinding.StringEditableBinding<R> diceIdBinding = FieldBinding.bindEditableString(getDiceIdFunction, setDiceIdFunction, diceIdField);
    final FieldBinding.StringEditableBinding<R> eMailBinding = FieldBinding.bindEditableString(getEMailFunction, setEMailFunction, eMailField);
    final FieldBinding.StringEditableBinding<R> phone1Binding = FieldBinding.bindEditableString(getPhone1Function, setPhone1Function, phone1Field);
    final FieldBinding.StringEditableBinding<R> phone2Binding = FieldBinding.bindEditableString(getPhone2Function, setPhone2Function, phone2Field);
    final FieldBinding.StringEditableBinding<R> faxBinding = FieldBinding.bindEditableString(getFaxFunction, setFaxFunction, faxField);
    final FieldBinding.StringEditableBinding<R> webSiteBinding = FieldBinding.bindEditableString(getWebSiteFunction, setWebSiteFunction, webSiteField);
    final FieldBinding.StringEditableBinding<R> skypeBinding = FieldBinding.bindEditableString(getSkypeFunction, setSkypeFunction, skypeField);
    final FieldBinding.StringEditableBinding<R> descriptionBinding = FieldBinding.bindEditableString(getDescriptionFunction, setDescriptionFunction, descriptionField);
    final FieldBinding.StringEditableBinding<R> historyBinding = FieldBinding.bindEditableString(getHistoryFunction, setHistoryFunction, historyField);
    final FieldBinding.StringBinding<R> createdOnBinding = FieldBinding.bindConstantString(getCreatedOnFunction, createdOnField);
    allBindings = Arrays.asList(idBinding, sourceBinding, userNameBinding, dicePosnBinding, diceIdBinding, eMailBinding, phone1Binding, phone2Binding,
        faxBinding, webSiteBinding, skypeBinding, descriptionBinding, historyBinding, createdOnBinding);

    add(makeFieldAndHistoryPanel(makeFieldPanel(), historyField), BorderLayout.PAGE_START);

    installStandardCaret(companyField, contactNameField, dicePosnField, diceIdField, eMailField, phone1Field,
        phone2Field, faxField, webSiteField, skypeField, descriptionField, historyField);
  }

  private JComponent makeScanButton() {
    JButton button = new JButton(Resource.getLeftChevron());
    button.addActionListener(e -> scanForDiceInfo());
    button.setFocusable(false);
    return button;
  }

  private void scanForDiceInfo() {
    String description = descriptionField.getText();
    if (scanForDiceInfo(description)) { return; }
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    try {
      String data = clipboard.getData(DataFlavor.stringFlavor).toString();
      scanForDiceInfo(data);
    } catch (UnsupportedFlavorException | IOException ignored) {}
  }

  private boolean scanForDiceInfo(final String description) {
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

  @SuppressWarnings("method.invocation.invalid")
  private JPanel makeFieldAndHistoryPanel(JPanel fieldPanel, JTextArea historyField) {
    JPanel fieldAndHistoryPanel = new JPanel(new BorderLayout());
    fieldAndHistoryPanel.add(fieldPanel, BorderLayout.LINE_START);
    fieldAndHistoryPanel.add(scrollArea(historyField), BorderLayout.CENTER);
    return fieldAndHistoryPanel;
  }

  @SuppressWarnings("method.invocation.invalid")
  private RecordController<R, Integer, LeadField> makeController(
      final LeadField initialSort, 
      final Dao<R, Integer, LeadField> dao, 
      final Supplier<@NonNull R> recordConstructor,
      final Function<R, Integer> getIdFunction
  ) {
    return new RecordController<>(
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

  @SuppressWarnings("method.invocation.invalid")
  @RequiresNonNull({"labelPanel", "fieldPanel", "checkBoxPanel"})
  private JPanel makeFieldPanel(@UnderInitialization RecordView<R>this) {
    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(labelPanel, BorderLayout.LINE_START);
    topPanel.add(fieldPanel, BorderLayout.CENTER);
    topPanel.add(checkBoxPanel, BorderLayout.LINE_END);

    // Put a line at the top, one pixel wide.
    topPanel.setBorder(new MatteBorder(1, 0, 0, 0, Color.black));
    return topPanel;
  }

  public RecordController<R, Integer, LeadField> getController() { return controller; }

  /**
   * On the Mac, the AquaCaret will get installed. This caret has an annoying feature of selecting all the text on a
   * focus-gained event. If this isn't bad enough, it also fails to check temporary vs permanent focus gain, so it
   * gets triggered on a focused JTextComponent whenever a menu is released! This method removes the Aqua Caret and
   * installs a standard caret. It's only needed on the Mac, but it's safe to use on any platform.
   *
   * @param components The components to repair. This is usually a JTextField or JTextArea.
   */
  public static void installStandardCaret(JTextComponent... components) {
    for (JTextComponent component : components) {
      final Caret priorCaret = component.getCaret();
      int blinkRate = priorCaret.getBlinkRate();
      if (priorCaret instanceof PropertyChangeListener) {
        // com.apple.laf.AquaCaret, the troublemaker, installs this listener which doesn't get removed when the Caret 
        // gets uninstalled.
        component.removePropertyChangeListener((PropertyChangeListener) priorCaret);
      }
      DefaultCaret caret = new DefaultCaret();
      component.setCaret(caret);
      caret.setBlinkRate(blinkRate); // Starts the new caret blinking.
    }
  }

  @RequiresNonNull({"labelPanel", "fieldPanel", "buttonGroup", "checkBoxPanel", "controller"})
  private JComponent addField(@UnderInitialization RecordView<R> this, final String labelText, final boolean editable, final LeadField orderField, LeadField initialSort) {
    return addField(labelText, editable, orderField, initialSort, null);
  }

  @RequiresNonNull({"labelPanel", "fieldPanel", "buttonGroup", "checkBoxPanel", "controller"})
  private JComponent addFieldWithCopy(@UnderInitialization RecordView<R>this, final String labelText, final LeadField orderField, LeadField initialSort) {
    JButton copyButton = new JButton(Resource.getCopy());

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

  @RequiresNonNull({"labelPanel", "fieldPanel", "buttonGroup", "checkBoxPanel", "controller"})
  private JComponent addField(@UnderInitialization RecordView<R>this, final String labelText, final boolean editable, final LeadField orderField, LeadField initialSort, @Nullable JComponent extraComponent) {
    //noinspection StringConcatenation,MagicCharacter
    JLabel label = new JLabel(labelText + ':');
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

  @SuppressWarnings("method.invocation.invalid")
  private JTextComponent addDescriptionField(@UnderInitialization RecordView<R> this) {
    final JTextArea wrappedField = new JTextArea(TEXT_ROWS, TEXT_COLUMNS);
    JComponent scrollPane = scrollArea(wrappedField);
    add(BorderLayout.CENTER, scrollPane);
    return wrappedField;
  }

  JComponent wrapField(@UnderInitialization RecordView<R> this, JComponent component, String label) {
    if (!(component instanceof JTextField)) {
      return component;
    }
    JButton button = new JButton(Resource.getFwdChevron());
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
  public boolean recordHasChanged() {
    for (FieldBinding<R, ?, ?> binding: allBindings) {
      if (binding.isEditable() && binding.propertyHasChanged(currentRecord)) {
        return true;
      }
    }
    return false;
  }

  public boolean saveOnExit() {// throws SQLException {
    // Test four cases:
    // 1. New Record with data
    // 2. New Record with no data
    // 3. Existing record with changes
    // 4. Existing record with no changes
    final boolean hasChanged = recordHasChanged();
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

  @SuppressWarnings("initialization.fields.uninitialized")
  public static class Builder<RR> {
    private RR record;
    private LeadField initialSort;
    private Function<RR, Integer> getId;
    private BiConsumer<RR, Integer> setId;
    private Function<RR, String> getCompany;
    private BiConsumer<RR, String> setCompany;
    private Function<RR, String> getContactName;
    private BiConsumer<RR, String> setContactName;
    private Function<RR, String> getDicePosn;
    private BiConsumer<RR, String> setDicePosn;
    private Function<RR, String> getDiceId;
    private BiConsumer<RR, String> setDiceId;
    private Function<RR, String> getEMail;
    private BiConsumer<RR, String> setEMail;
    private Function<RR, String> getPhone1;
    private BiConsumer<RR, String> setPhone1;
    private Function<RR, String> getPhone2;
    private BiConsumer<RR, String> setPhone2;
    private Function<RR, String> getFax;
    private BiConsumer<RR, String> setFax;
    private Function<RR, String> getWebSite;
    private BiConsumer<RR, String> setWebSite;
    private Function<RR, String> getSkype;
    private BiConsumer<RR, String> setSkype;
    private Function<RR, String> getDescription;
    private BiConsumer<RR, String> setDescription;
    private Function<RR, String> getHistory;
    private BiConsumer<RR, String> setHistory;
    private Function<RR, String> getCreatedOn;
    private Dao<RR, Integer, LeadField> dao;
    private Supplier<RR> recordConstructor;

    public Builder(RR record, LeadField initialSort) {
      this.record = record;
      this.initialSort = initialSort;
    }

    public Builder<RR> id(Function<RR, Integer> getter, BiConsumer<RR, Integer> setter) {
      getId = getter;
      setId = setter;
      return this;
    }

    public Builder<RR> company(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getCompany = getter;
      setCompany = setter;
      return this;
    }

    public Builder<RR> contactName(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getContactName = getter;
      setContactName = setter;
      return this;
    }

    public Builder<RR> dicePosn(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getDicePosn = getter;
      setDicePosn = setter;
      return this;
    }

    public Builder<RR> diceId(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getDiceId = getter;
      setDiceId = setter;
      return this;
    }

    public Builder<RR> email(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getEMail = getter;
      setEMail = setter;
      return this;
    }

    public Builder<RR> phone1(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getPhone1 = getter;
      setPhone1 = setter;
      return this;
    }

    public Builder<RR> phone2(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getPhone2 = getter;
      setPhone2 = setter;
      return this;
    }

    public Builder<RR> fax(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getFax = getter;
      setFax = setter;
      return this;
    }

    public Builder<RR> website(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getWebSite = getter;
      setWebSite = setter;
      return this;
    }

    public Builder<RR> skype(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getSkype = getter;
      setSkype = setter;
      return this;
    }

    public Builder<RR> description(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getDescription = getter;
      setDescription = setter;
      return this;
    }

    public Builder<RR> history(Function<RR, String> getter, BiConsumer<RR, String> setter) {
      getHistory = getter;
      setHistory = setter;
      return this;
    }
    
    public Builder<RR> createdOn(Function<RR, Timestamp> getter) {
      getCreatedOn = (d) -> getter.apply(d).toString();
      return this;
    }

    public Builder<RR> withDao(Dao<RR, Integer, LeadField> dao) {
      this.dao = dao;
      return this;
    }

    public Builder<RR> withConstructor(Supplier<RR> constructor) {
      recordConstructor = constructor;
      return this;
    }

    public RecordView<RR> build() {
      final RecordView<RR> view = new RecordView<>(
          record,
          initialSort,
          dao,
          recordConstructor,
          getId, setId,
          getCompany, setCompany,
          getContactName, setContactName,
          getDicePosn, setDicePosn,
          getDiceId, setDiceId,
          getEMail, setEMail,
          getPhone1, setPhone1,
          getPhone2, setPhone2,
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
