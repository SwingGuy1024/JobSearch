package com.neptunedreams.framework.ui;

import java.awt.Component;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 12/8/19
 * <p>Time: 10:58 AM
 *
 * @author Miguel Mu\u00f1oz
 */
public class EnumComboBox<E extends Enum<E> & DisplayEnum> extends JComboBox<E> {

  @SuppressWarnings({"method.invocation.invalid", "argument.type.incompatible"})
  public EnumComboBox(E[] values) {
    super();
    @SuppressWarnings("unchecked")
    DefaultComboBoxModel<E> model = new DefaultComboBoxModel<>();
    setModel(model);
    for (E value: values) {
      model.addElement(value);
    }
    model.setSelectedItem(values[0]);
    setMaximumRowCount(values.length);
    setEditable(false);
    ListCellRenderer r = new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
        @SuppressWarnings("unchecked")
        E eValue = (E) value;
        
        return super.getListCellRendererComponent(list, eValue.getDisplay(), index, isSelected, cellHasFocus);
      }
    };
    //noinspection unchecked
    setRenderer(r);
  }
  
  @SuppressWarnings("return.type.incompatible")
  public E getSelected() {
    @SuppressWarnings("unchecked")
    final E selectedItem = (E) getModel().getSelectedItem();
    return selectedItem;
  }
}
