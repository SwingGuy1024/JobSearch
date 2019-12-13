package com.neptunedreams.jobs.ui;

import java.net.URL;
import java.util.Objects;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 12:59 PM
 *
 * @author Miguel Mu\u00f1oz
 */
enum Resource {
  ;

  private static final String ARROW_RIGHT_PNG = "arrow_right.png";
  private static final String ARROW_LEFT_PNG = "arrow_left.png";
  private static final String MAGNIFIER_16_PNG = "magnifier16.png";
  private static final String BIN_EMPTY_PNG = "bin_empty.png";
  private static final String BULLET_ADD_PNG = "bullet_add.png";
  private static final String ARROW_FIRST_PNG = "arrow_first.png";
  private static final String ARROW_LAST_PNG = "arrow_last.png";
  private static final String FORWARD_CHEVRON = "bullet_go.png";
  private static final String LEFT_CHEVRON = "arrow_turn_left.png";
  private static final String COPY = "copying_and_distribution.png";

  private static Icon getIcon(String name) {
    URL resource = Objects.requireNonNull(Resource.class.getResource(name));
//    if (resource == null) {
//      resource = Resource.class.getResource("/com/jobs/ui/" + name);
//    }
//    if (resource == null) {
//      resource = Resource.class.getResource("/Users/miguelmunoz/Documents/jobs/src/main/resource/com/jobs/ui/" + name);
//    }
//    System.out.printf("Resource: %s from %s%n", resource, name);
    return new ImageIcon(resource);
  }

  static Icon getRightArrow() {
    return getIcon(ARROW_RIGHT_PNG);
  }

  static Icon getLeftArrow() {
    return getIcon(ARROW_LEFT_PNG);
  }
  
  static Icon getBin() {
    return getIcon(BIN_EMPTY_PNG);
  }
  
  static JLabel getMagnifierLabel() {
    final Icon icon = getIcon(MAGNIFIER_16_PNG);
    return new JLabel(icon);
  }
  
  static Icon getAdd() {
    return getIcon(BULLET_ADD_PNG);
  }
  
  static Icon getFirst() {
    return getIcon(ARROW_FIRST_PNG);
  }
  
  static Icon getLast() {
    return getIcon(ARROW_LAST_PNG);
  }
  
  static Icon getFwdChevron() { return getIcon(FORWARD_CHEVRON); }
  
  static Icon getLeftChevron() { return getIcon(LEFT_CHEVRON); }
  
  static Icon getCopy() { return getIcon(COPY); }
}
