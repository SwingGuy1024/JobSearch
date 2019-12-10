package com.neptunedreams.framework.ui;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 12/8/19
 * <p>Time: 3:15 PM
 *
 * @author Miguel Mu\u00f1oz
 */
public enum SwingUtils {
  ;

  public static JPanel wrapEast(JComponent component) {
//    JPanel wrapper = new JPanel(new BorderLayout());
//    wrapper.add(component, BorderLayout.LINE_START);
//    return wrapper;
    return wrap(component, BorderLayout.LINE_END);
  }
  
  public static JPanel wrapWest(JComponent component) {
    return wrap(component, BorderLayout.LINE_START);
  }

  public static JPanel wrapNorth(JComponent component) {
    return wrap(component, BorderLayout.PAGE_START);
  }

  public static JPanel wrapSouth(JComponent component) {
    return wrap(component, BorderLayout.PAGE_END);
  }

  private static JPanel wrap(JComponent component, String direction) {
    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.add(component, direction);
    return wrapper;
  }

  public static JComponent scrollArea(JTextArea wrappedField) {
    wrappedField.setWrapStyleWord(true);
    wrappedField.setLineWrap(true);
    return verticalScroll(wrappedField);
  }

  public static JComponent verticalScroll(JComponent wrapped) {
    return new JScrollPane(wrapped, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
  }

}
