package fr.uge.backpack.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the Hero's inventory grid. Manages item placement, removal, and
 * slot unlocking.
 */
public class Backpack {

  /** Maximum number of rows in the grid. */
  private final int maxRows;
  /** Maximum number of columns in the grid. */
  private final int maxCols;
  /** The grid containing cases (items or blocks). */
  private final Case[][] grid;

  /**
   * Creates a backpack with a specific size. Initializes the grid with inactive
   * blocks and a central free area.
   * 
   * @param maxRows Maximum rows.
   * @param maxCols Maximum columns.
   */
  public Backpack(int maxRows, int maxCols) {
    this.maxRows = maxRows;
    this.maxCols = maxCols;
    this.grid = new Case[maxRows][maxCols];

    for (int r = 0; r < maxRows; r++) {
      for (int c = 0; c < maxCols; c++) {
        grid[r][c] = new BlockInactive();
      }
    }

    int initialActiveRows = 3;
    int initialActiveCols = 3;
    int offsetRow = (maxRows - initialActiveRows) / 2;
    int offsetCol = (maxCols - initialActiveCols) / 2;

    for (int r = 0; r < initialActiveRows; r++) {
      for (int c = 0; c < initialActiveCols; c++) {
        grid[offsetRow + r][offsetCol + c] = new BlockFree();
      }
    }
  }

  /**
   * Gets the cell content at specific coordinates.
   * 
   * @param row Row index.
   * @param col Column index.
   * @return The cell content (Item, BlockFree, or BlockInactive).
   */
  public Case get(int row, int col) {
    if (row < 0 || col < 0 || row >= maxRows || col >= maxCols)
      return new BlockInactive();
    return grid[row][col];
  }

  /**
   * Gets the item at specific coordinates.
   * 
   * @param row Row index.
   * @param col Column index.
   * @return The Item or null if empty/inactive.
   */
  public Item getItemAt(int row, int col) {
    return switch (get(row, col)) {
    case Item i -> i;
    default -> null;
    };
  }

  /**
   * Attempts to unlock an inactive cell.
   * 
   * @param row Row index.
   * @param col Column index.
   * @return true if the cell was successfully unlocked.
   */
  public boolean unlockCell(int row, int col) {
    if (row < 0 || col < 0 || row >= maxRows || col >= maxCols)
      return false;

    boolean alreadyFree = switch (grid[row][col]) {
    case BlockFree _ -> true;
    default -> false;
    };
    if (alreadyFree)
      return false;

    if (checkNeighborFree(row + 1, col) || checkNeighborFree(row - 1, col) || checkNeighborFree(row, col + 1)
        || checkNeighborFree(row, col - 1)) {

      grid[row][col] = new BlockFree();
      return true;
    }
    return false;
  }

  /**
   * Checks if a neighbor cell is free or contains an item.
   * 
   * @param r Row index.
   * @param c Column index.
   * @return true if free or item.
   */
  private boolean checkNeighborFree(int r, int c) {
    if (r < 0 || c < 0 || r >= maxRows || c >= maxCols)
      return false;
    return switch (grid[r][c]) {
    case BlockFree _ -> true;
    case Item _ -> true;
    default -> false;
    };
  }

  /**
   * Adds an item to the backpack at specific coordinates.
   * 
   * @param item      The item to add.
   * @param targetRow Target row.
   * @param targetCol Target column.
   * @return 1 on success, 0 or -1 on failure.
   */
  public int addItem(Item item, int targetRow, int targetCol) {
    Objects.requireNonNull(item);

    for (Position p : item.getShape()) {
      int row = targetRow + p.row();
      int col = targetCol + p.column();

      if (row < 0 || col < 0 || row >= maxRows || col >= maxCols)
        return 0;

      Case cell = grid[row][col];

      boolean canPlace = switch (cell) {
      case BlockFree _ -> true;
      case Item _ -> false;
      case BlockInactive _ -> false;
      };

      if (!canPlace)
        return -1;
    }

    for (Position p : item.getShape()) {
      grid[targetRow + p.row()][targetCol + p.column()] = item;
    }
    item.setGridPos(targetRow, targetCol);
    return 1;
  }

  /**
   * Removes an item from the backpack.
   * 
   * @param item The item to remove.
   */
  public void remove(Item item) {
    Objects.requireNonNull(item);
    if (!item.isInGrid())
      return;
    for (Position p : item.getShape()) {
      int r = item.getGridRow() + p.row();
      int c = item.getGridCol() + p.column();
      grid[r][c] = new BlockFree();
    }
    item.clearGridPos();
  }

  /**
   * Forces placement of a curse item, removing any overlapping items.
   * 
   * @param curse The curse item.
   * @param row   Target row.
   * @param col   Target column.
   * @return true if placed successfully.
   */
  public boolean forcePlaceCurse(Item curse, int row, int col) {
    Objects.requireNonNull(curse);
    if (!canCurseBeHere(curse, row, col))
      return false;

    List<Item> victims = findItemsUnder(curse, row, col);
    for (Item victim : victims)
      remove(victim);

    addItem(curse, row, col);
    return true;
  }

  /**
   * Checks if a curse can be placed at a position (no inactive blocks).
   * 
   * @param item The item.
   * @param row  Row index.
   * @param col  Column index.
   * @return true if valid.
   */
  private boolean canCurseBeHere(Item item, int row, int col) {
    for (Position p : item.getShape()) {
      int r = row + p.row();
      int c = col + p.column();

      if (r < 0 || r >= maxRows || c < 0 || c >= maxCols)
        return false;

      boolean isInactive = switch (grid[r][c]) {
      case BlockInactive _ -> true;
      default -> false;
      };

      if (isInactive)
        return false;
    }
    return true;
  }

  /**
   * Finds items under a given area.
   * 
   * @param item The item defining the shape.
   * @param row  Row index.
   * @param col  Column index.
   * @return List of victims.
   */
  private List<Item> findItemsUnder(Item item, int row, int col) {
    List<Item> victims = new ArrayList<>();
    for (Position p : item.getShape()) {
      Case cell = grid[row + p.row()][col + p.column()];

      switch (cell) {
      case Item victimItem -> {
        if (!victims.contains(victimItem)) {
          victims.add(victimItem);
        }
      }
      default -> {
      }
      }
    }
    return victims;
  }

  /**
   * Gets all items currently in the backpack.
   * 
   * @return A list of items.
   */
  public List<Item> getItems() {
    List<Item> items = new ArrayList<>();
    for (int r = 0; r < maxRows; r++) {
      for (int c = 0; c < maxCols; c++) {
        switch (grid[r][c]) {
        case Item item -> {
          if (!items.contains(item))
            items.add(item);
        }
        default -> {
        }
        }
      }
    }
    return items;
  }

  /**
   * Gets the maximum number of rows.
   * 
   * @return max rows.
   */
  public int getMaxRows() {
    return maxRows;
  }

  /**
   * Gets the maximum number of columns.
   * 
   * @return max columns.
   */
  public int getMaxCols() {
    return maxCols;
  }

  /**
   * Gets the grid array.
   * 
   * @return 2D array of Cases.
   */
  public Case[][] getGrid() {
    return grid;
  }
}