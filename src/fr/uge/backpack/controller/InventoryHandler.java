package fr.uge.backpack.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.github.forax.zen.PointerEvent;

import fr.uge.backpack.model.Backpack;
import fr.uge.backpack.model.BlockInactive;
import fr.uge.backpack.model.Case;
import fr.uge.backpack.model.Enemy;
import fr.uge.backpack.model.GameState;
import fr.uge.backpack.model.Item;
import fr.uge.backpack.model.ItemType;
import fr.uge.backpack.model.Position;
import fr.uge.backpack.model.Room;
import fr.uge.backpack.model.RoomType;
import fr.uge.backpack.model.SimpleGameData;

/**
 * Handles inventory interactions (mouse, drag and drop).
 */
public class InventoryHandler {
  /** Size of a cell in pixels. */
  private static final int CELL_SIZE = 70;
  /** Starting X coordinate for the inventory grid. */
  private static final int START_X = 650;
  /** Starting Y coordinate for the inventory grid. */
  private static final int START_Y = 50;

  /** The item currently being dragged. */
  private Item draggedItem = null;
  /** Offset X for dragging visualization. */
  private int dragOffsetX = 0;
  /** Offset Y for dragging visualization. */
  private int dragOffsetY = 0;

  /** Previous row index. */
  private int oldRow = -1;
  /** Previous column index. */
  private int oldCol = -1;
  /** Previous pixel X. */
  private int oldPixelX = 0;
  /** Previous pixel Y . */
  private int oldPixelY = 0;
  /** Flag indicating if the item was in the grid before dragging. */
  private boolean wasInGrid = false;
  /** Previous shape of the item. */
  private List<Position> oldShape = new ArrayList<>();

  /**
   * Default constructor.
   */
  public InventoryHandler() {
  }

  /**
   * Gets the item currently being dragged.
   * 
   * @return The item or null.
   */
  public Item getDraggedItem() {
    return draggedItem;
  }

  /**
   * Main dispatch for mouse events.
   * 
   * @param pe   The pointer event.
   * @param data The game data.
   */
  public void handlePointerEvent(PointerEvent pe, SimpleGameData data) {
    Objects.requireNonNull(pe);
    Objects.requireNonNull(data);
    if (data.getState() == GameState.COMBAT) {
      if (pe.action() == PointerEvent.Action.POINTER_DOWN) {
        handleCombatClick(pe, data);
      }
      return;
    }

    int mx = pe.location().x();
    int my = pe.location().y();
    Backpack backpack = data.getHero().getBackpack();

    switch (pe.action()) {
    case POINTER_DOWN -> handlePointerDown(mx, my, backpack, data);
    case POINTER_MOVE -> handlePointerMove(mx, my);
    case POINTER_UP -> handlePointerUp(backpack, data);
    }
  }

  /**
   * Handles pointer down events.
   * 
   * @param mx       Mouse X.
   * @param my       Mouse Y.
   * @param backpack The backpack.
   * @param data     The game data.
   */
  private void handlePointerDown(int mx, int my, Backpack backpack, SimpleGameData data) {
    if (tryUnlockCase(mx, my, backpack, data))
      return;

    List<Item> itemsInBag = new ArrayList<>(backpack.getItems());
    for (Item item : itemsInBag) {
      if (isMouseOnItem(item, mx, my, true)) {
        startDragging(item, mx, my, true);
        backpack.remove(item);
        return;
      }
    }

    if (draggedItem == null) {
      List<Item> itemsOnGround = new ArrayList<>(data.getItemsOut());
      for (Item item : itemsOnGround) {
        if (isMouseOnItem(item, mx, my, false)) {
          startDragging(item, mx, my, false);
          return;
        }
      }
    }
  }

  /**
   * Tries to unlock a cell.
   * 
   * @param mx       Mouse X.
   * @param my       Mouse Y.
   * @param backpack The backpack.
   * @param data     The game data.
   * @return true if unlocked.
   */
  private boolean tryUnlockCase(int mx, int my, Backpack backpack, SimpleGameData data) {
    int r = (my - START_Y) / CELL_SIZE;
    int c = (mx - START_X) / CELL_SIZE;

    if (r < 0 || r >= backpack.getMaxRows() || c < 0 || c >= backpack.getMaxCols())
      return false;

    Case cell = backpack.get(r, c);

    return switch (cell) {
    case BlockInactive _ -> {
      if (data.getHero().getUnlockPoints() > 0) {
        if (backpack.unlockCell(r, c)) {
          data.getHero().useUnlockPoint();
          yield true;
        }
      }
      yield false;
    }
    default -> false;
    };
  }

  /**
   * Handles pointer up events (drop).
   * 
   * @param backpack The backpack.
   * @param data     The game data.
   */
  private void handlePointerUp(Backpack backpack, SimpleGameData data) {
    if (draggedItem == null)
      return;

    int targetRow = (draggedItem.getPixelY() - START_Y + CELL_SIZE / 2) / CELL_SIZE;
    int targetCol = (draggedItem.getPixelX() - START_X + CELL_SIZE / 2) / CELL_SIZE;

    boolean insideGrid = isInsideGridBounds(targetRow, targetCol, backpack);

    if (!insideGrid) {
      draggedItem.clearGridPos();
      if (!data.getItemsOut().contains(draggedItem)) {
        data.getItemsOut().add(draggedItem);
      }
      draggedItem = null;
      return;
    }

    if (draggedItem.getType() == ItemType.CURSE) {
      handleCurseDrop(backpack, data, targetRow, targetCol);
    } else {
      handleNormalDrop(backpack, data, targetRow, targetCol);
    }

    draggedItem = null;
  }

  /**
   * Drops a curse item.
   * 
   * @param backpack The backpack.
   * @param data     The game data.
   * @param row      Row index.
   * @param col      Column index.
   */
  private void handleCurseDrop(Backpack backpack, SimpleGameData data, int row, int col) {
    boolean success = backpack.forcePlaceCurse(draggedItem, row, col);
    if (success) {
      data.getItemsOut().remove(draggedItem);
    } else {
      cancelDrop(backpack, data);
    }
  }

  /**
   * Drops a normal item.
   * 
   * @param backpack The backpack.
   * @param data     The game data.
   * @param row      Row index.
   * @param col      Column index.
   */
  private void handleNormalDrop(Backpack backpack, SimpleGameData data, int row, int col) {
    List<Item> overlaps = getOverlappingItems(backpack, row, col);

    if (containsCurse(overlaps)) {
      data.log("Une malédiction bloque !");
      cancelDrop(backpack, data);
      return;
    }

    ejectItems(backpack, data, overlaps);

    if (backpack.addItem(draggedItem, row, col) == 1) {
      data.getItemsOut().remove(draggedItem);
    } else {
      cancelDrop(backpack, data);
    }
  }

  /**
   * Gets overlapping items.
   * 
   * @param backpack The backpack.
   * @param row      Row index.
   * @param col      Column index.
   * @return List of items.
   */
  private List<Item> getOverlappingItems(Backpack backpack, int row, int col) {
    List<Item> overlaps = new ArrayList<>();
    if (!isInsideGridBounds(row, col, backpack))
      return overlaps;

    for (Position p : draggedItem.getShape()) {
      Item it = backpack.getItemAt(row + p.row(), col + p.column());
      if (it != null && !overlaps.contains(it)) {
        overlaps.add(it);
      }
    }
    return overlaps;
  }

  /**
   * Checks if list contains a curse.
   * 
   * @param items List of items.
   * @return true if curse found.
   */
  private boolean containsCurse(List<Item> items) {
    for (Item item : items) {
      if (item.getType() == ItemType.CURSE)
        return true;
    }
    return false;
  }

  /**
   * Ejects items from backpack.
   * 
   * @param backpack The backpack.
   * @param data     The game data.
   * @param items    Items to eject.
   */
  private void ejectItems(Backpack backpack, SimpleGameData data, List<Item> items) {
    for (Item item : items) {
      backpack.remove(item);
      item.clearGridPos();
      item.setPixelPos(1500, 200 + data.getItemsOut().size() * 60);
      if (!data.getItemsOut().contains(item)) {
        data.getItemsOut().add(item);
      }
    }
  }

  /**
   * Cancels the drop operation.
   * 
   * @param backpack The backpack.
   * @param data     The game data.
   */
  private void cancelDrop(Backpack backpack, SimpleGameData data) {
    if (wasInGrid) {
      draggedItem.setShape(oldShape);
      backpack.addItem(draggedItem, oldRow, oldCol);
    } else {
      draggedItem.clearGridPos();
      draggedItem.setPixelPos(oldPixelX, oldPixelY);
      if (!data.getItemsOut().contains(draggedItem)) {
        data.getItemsOut().add(draggedItem);
      }
    }
  }

  /**
   * Starts dragging an item.
   * 
   * @param item   The item.
   * @param mx     Mouse X.
   * @param my     Mouse Y.
   * @param inGrid True if item was in grid.
   */
  private void startDragging(Item item, int mx, int my, boolean inGrid) {
    draggedItem = item;
    wasInGrid = inGrid;
    oldShape = new ArrayList<>(item.getShape());

    if (inGrid) {
      oldRow = item.getGridRow();
      oldCol = item.getGridCol();
      int itemPixelX = START_X + item.getGridCol() * CELL_SIZE;
      int itemPixelY = START_Y + item.getGridRow() * CELL_SIZE;
      dragOffsetX = mx - itemPixelX;
      dragOffsetY = my - itemPixelY;
      item.setPixelPos(itemPixelX, itemPixelY);
    } else {
      oldPixelX = item.getPixelX();
      oldPixelY = item.getPixelY();
      dragOffsetX = mx - item.getPixelX();
      dragOffsetY = my - item.getPixelY();
    }
  }

  /**
   * Updates dragged item position.
   * 
   * @param mx Mouse X.
   * @param my Mouse Y.
   */
  private void handlePointerMove(int mx, int my) {
    if (draggedItem != null) {
      draggedItem.setPixelPos(mx - dragOffsetX, my - dragOffsetY);
    }
  }

  /**
   * Rotates the dragged item.
   */
  public void rotateDraggedItem() {
    if (draggedItem == null || draggedItem.getType() == ItemType.CURSE)
      return;

    draggedItem.rotate();
  }

  /**
   * Handles click during combat.
   * 
   * @param pe   Pointer event.
   * @param data Game data.
   */
  private void handleCombatClick(PointerEvent pe, SimpleGameData data) {
    int mx = pe.location().x();
    int my = pe.location().y();
    Backpack backpack = data.getHero().getBackpack();

    Item clickedItem = null;
    for (Item item : backpack.getItems()) {
      if (isMouseOnItem(item, mx, my, true)) {
        clickedItem = item;
        break;
      }
    }

    if (clickedItem != null) {
      Enemy target = (!data.getEnemies().isEmpty()) ? data.getEnemies().get(0) : null;
      data.playerUseItem(clickedItem, target);
    }
  }

  /**
   * Handles clicks in the shop.
   * 
   * @param pe   Pointer event.
   * @param data Game data.
   */
  public void handleShopPointerEvent(PointerEvent pe, SimpleGameData data) {
    Objects.requireNonNull(pe);
    Objects.requireNonNull(data);
    if (pe.action() != PointerEvent.Action.POINTER_DOWN)
      return;

    Room room = data.getDungeon().getRoom(data.getDungeon().getPlayerX(), data.getDungeon().getPlayerY());
    if (room == null || room.getType() != RoomType.MERCHANT)
      return;

    int mx = pe.location().x();
    int my = pe.location().y();
    int x = 520, y = 260, cell = 50;

    for (Item item : room.getShopItems()) {
      int w = cell * 2;
      int h = cell * 2;
      if (mx >= x && mx <= x + w && my >= y && my <= y + h) {
        data.buyItem(item);
        return;
      }
      x += 180;
    }
  }

  /**
   * Checks if position is within grid.
   * 
   * @param row      Row index.
   * @param col      Column index.
   * @param backpack The backpack.
   * @return true if inside.
   */
  private boolean isInsideGridBounds(int row, int col, Backpack backpack) {
    return row >= 0 && row < backpack.getMaxRows() && col >= 0 && col < backpack.getMaxCols();
  }

  /**
   * Checks if mouse is over an item.
   * 
   * @param item         The item.
   * @param mouseX       Mouse X.
   * @param mouseY       Mouse Y.
   * @param checkGridPos Check grid coordinates instead of pixel coordinates.
   * @return true if mouse is over.
   */
  private boolean isMouseOnItem(Item item, int mouseX, int mouseY, boolean checkGridPos) {
    List<Position> shape = item.getShape();
    int baseX, baseY;

    if (checkGridPos) {
      if (!item.isInGrid())
        return false;
      baseX = START_X + item.getGridCol() * CELL_SIZE;
      baseY = START_Y + item.getGridRow() * CELL_SIZE;
    } else {
      if (item.isInGrid())
        return false;
      baseX = item.getPixelX();
      baseY = item.getPixelY();
    }

    for (Position p : shape) {
      int x = baseX + p.column() * CELL_SIZE;
      int y = baseY + p.row() * CELL_SIZE;
      if (mouseX >= x && mouseX <= x + CELL_SIZE && mouseY >= y && mouseY <= y + CELL_SIZE) {
        return true;
      }
    }
    return false;
  }

  /**
   * Handles the delete key action.
   * 
   * @param data The game data.
   */
  public void handleDeleteCurse(SimpleGameData data) {
    Objects.requireNonNull(data);
    if (draggedItem == null)
      return;
    if (draggedItem.getType() == ItemType.CURSE) {
      data.refuseCurse(draggedItem);
      draggedItem = null;
    } else {
      data.log("Impossible de jeter cet objet !");
    }
  }
}