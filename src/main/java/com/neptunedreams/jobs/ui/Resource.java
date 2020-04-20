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
@SuppressWarnings("HardCodedStringLiteral")
enum Resource {

  ARROW_RIGHT_PNG("arrow_right.png"),
  ARROW_LEFT_PNG("arrow_left.png"),
  MAGNIFIER_16_PNG("magnifier16.png"),
  BIN_EMPTY_PNG("bin_empty.png"),
  BULLET_ADD_PNG("bullet_add.png"),
  ARROW_FIRST_PNG("arrow_first.png"),
  ARROW_LAST_PNG("arrow_last.png"),
  FORWARD_CHEVRON("bullet_go.png"),
  LEFT_CHEVRON("arrow_turn_left.png"),
  COPY("copying_and_distribution.png"),
  BULLET_16("bullet_green16.png"),
  SINGLE_SPACE("single_space-40x16.png"),
  ;

  private final String name;

  Resource(String fileName) {
    name = fileName;
  }

  public static Icon getIcon(Resource resource) {
    URL url = Objects.requireNonNull(Resource.class.getResource(resource.name));
    return new ImageIcon(url);
  } 

  static JLabel getMagnifierLabel() {
    final Icon icon = getIcon(MAGNIFIER_16_PNG);
    return new JLabel(icon);
  }
}
