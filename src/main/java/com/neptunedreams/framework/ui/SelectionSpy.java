package com.neptunedreams.framework.ui;

import java.awt.DefaultKeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.FocusManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 12/8/19
 * <p>Time: 4:18 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public enum SelectionSpy implements CaretListener {
  spy;
  @SuppressWarnings("FieldCanBeLocal")
  private final DefaultKeyboardFocusManager focusManager = FocusManager.getCurrentManager();
  @SuppressWarnings("NonFinalFieldInEnum")
  private @Nullable JTextComponent focusedComponent = null;
  @SuppressWarnings("NonFinalFieldInEnum")
  private String selectedText = "";
  
  private final List<BooleanListener> booleanListeners = new LinkedList<>();

  SelectionSpy() {
    focusManager.addPropertyChangeListener("focusOwner", new PListener());
  }
  
  @Override
  public void caretUpdate(final CaretEvent e) {
    boolean priorSelectionExists = !selectedText.isEmpty();
    if (e.getDot() == e.getMark()) {
      selectedText = "";
      if (priorSelectionExists) {
        fireListeners(false);
      }
    } else if (focusedComponent != null) { // if is always true, but required by the null checker
      selectedText = focusedComponent.getSelectedText();
      if (!priorSelectionExists) {
        fireListeners(true);
      }
    }
  }
  
  private void fireListeners(boolean value) {
    for (BooleanListener listener : booleanListeners) {
      listener.respond(value);
    }
  }
  
  public void addBooleanListener(BooleanListener listener) {
    booleanListeners.add(listener);
  }
  
  public void removeBooleanListener(BooleanListener listener) {
    booleanListeners.remove(listener);
  }

  public String getSelectedText() {
    return selectedText;
  }
  
  @FunctionalInterface
  public interface BooleanListener {
    void respond(boolean value);
  }
  
  private @Nullable JTextComponent getFocusedComponent() { return focusedComponent; }
  private void setFocusedComponent(@Nullable JTextComponent component) { focusedComponent = component; }
  
  private static class PListener implements PropertyChangeListener {
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
      Object newValue = evt.getNewValue();
      if (newValue instanceof JTextComponent) {
        reassignListener((JTextComponent) newValue);
      } else {
        reassignListener(null);
      }
      SelectionSpy.spy.fireListeners(false);
    }

    private void reassignListener(@Nullable JTextComponent component) {
      SelectionSpy sSpy = SelectionSpy.spy;
      JTextComponent focusedComponent = sSpy.getFocusedComponent();
      if (focusedComponent != null) {
        focusedComponent.removeCaretListener(sSpy);
      }
      sSpy.setFocusedComponent(component);
      
      if (component != null) {
        component.addCaretListener(sSpy);
      }
    }
  }
}
