package com.neptunedreams.jobs.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.net.URL;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import com.neptunedreams.framework.ui.SwingUtils;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 10/29/17
 * <p>Time: 12:59 PM
 *
 * @author Miguel Mu\u00f1oz
 */
@SuppressWarnings("HardCodedStringLiteral")
enum Resource {

  ARROW_RIGHT_PNG("arrow_right.png", true),
  ARROW_LEFT_PNG("arrow_left.png", true),
  MAGNIFIER_16_PNG("magnifier16.png"),
  BIN_EMPTY_PNG("bin_empty.png"),
  BULLET_ADD_PNG("bullet_add.png", true),
  ARROW_FIRST_PNG("arrow_first.png", true),
  ARROW_LAST_PNG("arrow_last.png", true),
  FORWARD_CHEVRON("bullet_go.png", true),
  LEFT_CHEVRON("arrow_turn_left.png", true),
  COPY("copying_and_distribution.png"),
  BULLET_16("bullet_green16.png", true),
  SINGLE_SPACE("single_space-40x16.png", true),
  PAGE_COPY("page_copy.png"),
  ;

  private static final int SHIFT = 93;
  private final String name;
  private final boolean shift;

  Resource(String fileName) {
    this(fileName, false);
  }

  Resource(String fileName, boolean shift) {
    name = fileName;
    this.shift = shift; 
  }

  public static Icon getIcon(Resource resource) {
    URL url = Objects.requireNonNull(Resource.class.getResource(resource.name));
    ImageIcon imageIcon = new ImageIcon(url);
    if (resource.shift) {
      imageIcon = SwingUtils.shiftHue(imageIcon, SHIFT);
    }
    return imageIcon;
  }
  
  static JLabel getMagnifierLabel() {
    final Icon icon = getIcon(MAGNIFIER_16_PNG);
    return new JLabel(icon);
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    JPanel display = new JPanel(new GridLayout(0, 8));
    Icon copyIcon = getIcon(PAGE_COPY);
    frame.add(display, BorderLayout.CENTER);
    String name = ARROW_LAST_PNG.name;;
    for (int shift=80; shift< 112; shift++) {
      ImageIcon icon = new ImageIcon(Objects.requireNonNull(Resource.class.getResource(name)));
      icon = SwingUtils.shiftHue(icon, shift);
      final JLabel comp = new JLabel(String.valueOf(shift), icon, SwingConstants.LEFT);
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(new JLabel(copyIcon), BorderLayout.PAGE_START);
      panel.add(comp, BorderLayout.CENTER);
      JRadioButton radio = new JRadioButton("dummy");
      radio.setSelected(true);
      panel.add(radio, BorderLayout.PAGE_END);
      panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
      display.add(panel);
    }
    frame.pack();
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
  }
}
