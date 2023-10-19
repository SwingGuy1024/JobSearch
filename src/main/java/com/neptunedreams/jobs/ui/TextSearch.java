package com.neptunedreams.jobs.ui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.text.JTextComponent;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

/**
 * <p>Created by IntelliJ IDEA.
 * <p>Date: 1/19/20
 * <p>Time: 4:47 PM
 * TODO: Finish this class and integrate it.
 *
 * @author Miguel Muñoz
 */
public final class TextSearch implements Iterable<String> {
  private final List<JTextComponent> componentList;
  private @Nullable SearchStatusInfo searchStatusInfo;
  
  public static TextSearch build(JTextComponent... components) {
    return new TextSearch(Arrays.asList(components));
  }
  
  private TextSearch(List<JTextComponent> components) {
    componentList = components;
  }
  

  @Override
  public @NotNull Iterator<String> iterator() {
    final Iterator<JTextComponent> componentIterator = componentList.iterator();
    return new Iterator<>() {
      @Override
      public boolean hasNext() {
        return componentIterator.hasNext();
      }

      @Override
      public String next() {
        return componentIterator.next().getText();
      }
    };
  }
  
  public void beginSearch(String... targets) {
    searchStatusInfo = new SearchStatusInfo(iterator(), lower(targets));
    searchStatusInfo.continueSearch();
  }
  
  private String[] lower(String[] targets) {
    String[] lowered = new String[targets.length];
    for (int ii=0; ii<targets.length; ++ii) {
      lowered[ii] = targets[ii].toLowerCase();
    }
    return lowered;
  }
  
  private static class SearchStatusInfo {
    private final Iterator<String> iterator;
    private final String[] lowTargets;

    private final Map<String, Integer> searchWordPositionMap = new HashMap<>();
    SearchStatusInfo(Iterator<String> iterator, String... targets) {
      this.iterator = iterator;
      this.lowTargets = targets;
      for (String t: lowTargets) {
        searchWordPositionMap.put(t, -1); // start each word at "not found" (-1)
      }
    }

    public void continueSearch() {
      // To do: Search each string for each target.
      // Find the lowest index.
      // Save the index to begin the next search.
      // Save the component as well.
      // Only proceed to the next component when you can't find any String.

      while (iterator.hasNext()) {
        String haystack = iterator.next().toLowerCase();
        for (String t : lowTargets) {
          int index = haystack.indexOf(t);
          if (index >= 0) {
            searchWordPositionMap.put(t, index);
          }
        }
      }

    }

  }
}
