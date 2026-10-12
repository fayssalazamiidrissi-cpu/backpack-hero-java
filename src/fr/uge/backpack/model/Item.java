package fr.uge.backpack.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a game item.
 */
public final class Item implements Case {

  /** Metadata of the item. */
  private final ItemInfo info;

  /** Current price of the item. */
  private int price;
  /** Value of the item (damage, heal amount, etc.). */
  private int value;
  /** Current shape of the item (list of positions). */
  private final ArrayList<Position> currentShape = new ArrayList<>();

  /** Row position in grid (if placed). */
  private Integer gridRow = null;
  /** Column position in grid (if placed). */
  private Integer gridCol = null;
  /** Pixel X position (if floating). */
  private int pixelX = 0;
  /** Pixel Y position (if floating). */
  private int pixelY = 0;
  /** Flag to track usage per turn. */
  private boolean usedThisTurn = false;

  /**
   * Constructor.
   * 
   * @param info Item information.
   */
  public Item(ItemInfo info) {
    this.info = Objects.requireNonNull(info);
    this.value = info.value();
    this.price = info.price();
    this.currentShape.addAll(info.defaultShape());
  }

  /**
   * Factory method to create an item.
   * 
   * @param name   Name.
   * @param type   Type.
   * @param price  Price.
   * @param energy Energy cost.
   * @param mana   Mana cost.
   * @param val    Value.
   * @param shape  Shape.
   * @return The new item.
   */
  public static Item create(String name, ItemType type, int price, int energy, int mana, int val,
      List<Position> shape) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(type);
    Objects.requireNonNull(shape);
    ItemInfo info = new ItemInfo(name, type, price, energy, mana, val, shape);
    return new Item(info);
  }

  /**
   * Gets item name.
   * 
   * @return Name.
   */
  public String getName() {
    return info.name();
  }

  /**
   * Gets item type.
   * 
   * @return Type.
   */
  public ItemType getType() {
    return info.type();
  }

  /**
   * Gets energy cost.
   * 
   * @return Cost.
   */
  public int getEnergyCost() {
    return info.energyCost();
  }

  /**
   * Gets current shape.
   * 
   * @return List of positions.
   */
  public List<Position> getShape() {
    return List.copyOf(currentShape);
  }

  /**
   * Sets the shape of the item.
   * 
   * @param newShape The new shape.
   */
  public void setShape(List<Position> newShape) {
    Objects.requireNonNull(newShape);
    currentShape.clear();
    currentShape.addAll(newShape);
  }

  /**
   * Rotates the item 90 degrees.
   */
  public void rotate() {
    List<Position> newShape = new ArrayList<>();

    for (Position p : currentShape) {
      newShape.add(new Position(p.column(), -p.row()));
    }

    setShape(newShape);
  }

  /**
   * Adds a cell to the item shape.
   * 
   * @param p Position to add.
   */
  public void addShapeCell(Position p) {
    Objects.requireNonNull(p);
    currentShape.add(p);
  }

  /**
   * Gets item value.
   * 
   * @return Value.
   */
  public int getValue() {
    return value;
  }

  /**
   * Sets item value.
   * 
   * @param value New value.
   */
  public void setValue(int value) {
    if (value < 0) {
      throw new IllegalArgumentException("valeur negative");
    }
    this.value = value;
  }

  /**
   * Gets item price.
   * 
   * @return Price.
   */
  public int getPrice() {
    return price;
  }

  /**
   * Sets item price.
   * 
   * @param price New price.
   */
  public void setPrice(int price) {
    if (price < 0)
      throw new IllegalArgumentException("prix négatif");
    this.price = price;
  }

  /**
   * Checks if item is in grid.
   * 
   * @return true if in grid.
   */
  public boolean isInGrid() {
    return gridRow != null && gridCol != null;
  }

  /**
   * Gets grid row.
   * 
   * @return Row index.
   */
  public int getGridRow() {
    if (gridRow == null)
      throw new IllegalStateException("Hors grille");
    return gridRow;
  }

  /**
   * Gets grid column.
   * 
   * @return Column index.
   */
  public int getGridCol() {
    if (gridCol == null)
      throw new IllegalStateException("Hors grille");
    return gridCol;
  }

  /**
   * Sets grid position.
   * 
   * @param row Row index.
   * @param col Column index.
   */
  public void setGridPos(int row, int col) {
    this.gridRow = row;
    this.gridCol = col;
  }

  /**
   * Clears grid position (removes from grid).
   */
  public void clearGridPos() {
    this.gridRow = null;
    this.gridCol = null;
  }

  /**
   * Sets pixel position for drawing.
   * 
   * @param x X coordinate.
   * @param y Y coordinate.
   */
  public void setPixelPos(int x, int y) {
    this.pixelX = x;
    this.pixelY = y;
  }

  /**
   * Gets mana cost.
   * 
   * @return Mana cost.
   */
  public int getManaComponent() {
    return info.mana();
  }

  /**
   * Checks if item was used this turn.
   * 
   * @return true if used.
   */
  public boolean isUsedThisTurn() {
    return usedThisTurn;
  }

  /**
   * Sets used status.
   * 
   * @param used True if used.
   */
  public void setUsedThisTurn(boolean used) {
    this.usedThisTurn = used;
  }

  /**
   * Gets pixel X.
   * 
   * @return X.
   */
  public int getPixelX() {
    return pixelX;
  }

  /**
   * Gets pixel Y.
   * 
   * @return Y.
   */
  public int getPixelY() {
    return pixelY;
  }

  @Override
  public String toString() {
    return "Item{" + getName() + "}";
  }
}