package fr.uge.backpack.model;

import java.util.List;
import java.util.Objects;

/**
 * Record for item information.
 * 
 * @param name         Item name.
 * @param type         Item type.
 * @param price        Item price.
 * @param energyCost   Energy cost.
 * @param mana         Mana cost.
 * @param value        Item value.
 * @param defaultShape Default shape.
 */
public record ItemInfo(String name, ItemType type, int price, int energyCost, int mana, int value,
    List<Position> defaultShape) {

  /**
   * Constructor.
   */
  public ItemInfo {
    Objects.requireNonNull(name);
    Objects.requireNonNull(type);
    Objects.requireNonNull(defaultShape);
    defaultShape = List.copyOf(defaultShape);
  }
}