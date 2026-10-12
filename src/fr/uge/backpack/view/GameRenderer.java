package fr.uge.backpack.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Objects;

import com.github.forax.zen.ApplicationContext;

import fr.uge.backpack.controller.InventoryHandler;
import fr.uge.backpack.model.GameState;
import fr.uge.backpack.model.Item;
import fr.uge.backpack.model.SimpleGameData;

/**
 * Handles the main rendering loop.
 */
public class GameRenderer {
  /** Size of inventory cell. */
  private static final int cellSize = 70;
  /** Size of map cell. */
  private static final int mapCellSize = 70;
  /** Starting X pos. */
  private static final int startX = 650;
  /** Starting Y pos. */
  private static final int startY = 50;
  /** X pos for logs. */
  private static final int xlogs = 50;
  /** Y pos for logs. */
  private static final int ylogs = 200;

  /**
   * Default constructor.
   */
  public GameRenderer() {
  }

  /**
   * Renders the game frame.
   * 
   * @param g                The graphics context.
   * @param context          The application context.
   * @param data             The game data.
   * @param inventoryHandler The inventory handler.
   */
  public static void render(Graphics2D g, ApplicationContext context, SimpleGameData data,
      InventoryHandler inventoryHandler) {

    Objects.requireNonNull(g);
    Objects.requireNonNull(context);
    Objects.requireNonNull(data);
    Objects.requireNonNull(inventoryHandler);

    drawBackground(g, context);
    drawGameContent(g, data, inventoryHandler);
    Graphique.drawLogs(g, data.getLogs(), xlogs, ylogs);
  }

  /**
   * Draws the background image.
   * 
   * @param g       The graphics context.
   * @param context The application context.
   */
  private static void drawBackground(Graphics2D g, ApplicationContext context) {
    BufferedImage bg = ImageManager.getDungeonMap();
    int w = context.getScreenInfo().width();
    int h = context.getScreenInfo().height();

    if (bg != null) {
      g.drawImage(bg, 0, 0, w, h, null);
    } else {
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, w, h);
    }
  }

  /**
   * Draws the content based on game state.
   * 
   * @param g       The graphics context.
   * @param data    The game data.
   * @param handler The inventory handler.
   */
  private static void drawGameContent(Graphics2D g, SimpleGameData data, InventoryHandler handler) {
    if (data.getState() == GameState.EXPLORATION) {
      Graphique.drawMap(g, data.getDungeon(), mapCellSize);
    } else if (data.getState() == GameState.SHOP) {
      Graphique.drawMerchantScreen(g, data);
    } else {
      renderInventoryMode(g, data, handler);
    }
  }

  /**
   * Renders inventory, hero, and enemies.
   * 
   * @param g       The graphics context.
   * @param data    The game data.
   * @param handler The inventory handler.
   */
  private static void renderInventoryMode(Graphics2D g, SimpleGameData data, InventoryHandler handler) {
    var backpack = data.getHero().getBackpack();
    Item draggedItem = handler.getDraggedItem();

    Graphique.drawBackpack(g, backpack, cellSize, startX, startY);
    Graphique.drawEnemies(g, data.getEnemies());
    Graphique.drawHero(g, data.getHero());

    drawItemList(g, backpack.getItems(), draggedItem);
    drawItemList(g, data.getItemsOut(), draggedItem);

    if (draggedItem != null) {
      Graphique.drawItem(g, draggedItem, cellSize);
    }
  }

  /**
   * Helper to draw a list of items.
   * 
   * @param g       The graphics context.
   * @param items   The items to draw.
   * @param ignored The item to ignore.
   */
  private static void drawItemList(Graphics2D g, java.util.List<Item> items, Item ignored) {
    for (Item item : items) {
      if (item != ignored) {
        Graphique.drawItem(g, item, cellSize);
      }
    }
  }
}