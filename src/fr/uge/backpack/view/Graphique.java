package fr.uge.backpack.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;

import fr.uge.backpack.model.Backpack;
import fr.uge.backpack.model.BlockFree;
import fr.uge.backpack.model.BlockInactive;
import fr.uge.backpack.model.Case;
import fr.uge.backpack.model.Dungeon;
import fr.uge.backpack.model.Enemy;
import fr.uge.backpack.model.Hero;
import fr.uge.backpack.model.Item;
import fr.uge.backpack.model.ItemType;
import fr.uge.backpack.model.Position;
import fr.uge.backpack.model.Room;
import fr.uge.backpack.model.RoomType;
import fr.uge.backpack.model.SimpleGameData;

/**
 * Utility class for drawing game elements.
 */
public class Graphique {
  /** Start X pos for drawing. */
  private static final int startX = 650;
  /** Start Y pos for drawing. */
  private static final int startY = 50;
  /** Map X pos. */
  private static final int mapX = 400;
  /** Map Y pos. */
  private static final int mapY = 100;

  /**
   * Default constructor.
   */
  public Graphique() {
  }

  /**
   * Draws a grid.
   * 
   * @param g        Graphics context.
   * @param rows     Number of rows.
   * @param cols     Number of columns.
   * @param cellSize Size of cells.
   * @param startX   X offset.
   * @param startY   Y offset.
   * @param color    Line color.
   */
  public static void drawGrid(Graphics2D g, int rows, int cols, int cellSize, int startX, int startY, Color color) {
    Objects.requireNonNull(g);
    Objects.requireNonNull(color);
    g.setColor(color);
    for (int r = 0; r <= rows; r++) {
      g.drawLine(startX, startY + r * cellSize, startX + cols * cellSize, startY + r * cellSize);
    }
    for (int c = 0; c <= cols; c++) {
      g.drawLine(startX + c * cellSize, startY, startX + c * cellSize, startY + rows * cellSize);
    }
  }

  /**
   * Draws the backpack slots.
   * 
   * @param g        Graphics context.
   * @param backpack The backpack model.
   * @param cellSize Cell size.
   * @param startX   X offset.
   * @param startY   Y offset.
   */
  public static void drawBackpack(Graphics2D g, Backpack backpack, int cellSize, int startX, int startY) {
    Objects.requireNonNull(g);
    Objects.requireNonNull(backpack);
    for (int r = 0; r < backpack.getMaxRows() - 1; r++) {
      for (int c = 0; c < backpack.getMaxCols() - 1; c++) {
        Case cell = backpack.get(r, c);
        int x = startX + c * cellSize;
        int y = startY + r * cellSize;

        g.setColor(switch (cell) {
        case BlockFree _ -> Color.LIGHT_GRAY;
        case Item _ -> Color.YELLOW;
        case BlockInactive _ -> Color.DARK_GRAY;

        });

        g.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
      }
    }

    drawGrid(g, backpack.getMaxRows() - 1, backpack.getMaxCols() - 1, cellSize, startX, startY, Color.RED);
  }

  /**
   * Draws a single item.
   * 
   * @param g        Graphics context.
   * @param item     The item to draw.
   * @param cellSize Cell size.
   */
  public static void drawItem(Graphics2D g, Item item, int cellSize) {
    Objects.requireNonNull(g);
    Objects.requireNonNull(item);
    boolean isCurse = (item.getType() == ItemType.CURSE);

    if (isCurse) {
      g.setColor(new Color(50, 0, 50));
    } else {
      g.setColor(switch (item.getType()) {
      case MELEE_WEAPON -> Color.RED;
      case SHIELD -> Color.BLUE;
      case MISC -> Color.GREEN;
      case MAGIC_WEAPON -> Color.MAGENTA;
      default -> Color.YELLOW;
      });
    }

    List<Position> shape = item.getShape();

    int originX, originY;
    if (item.isInGrid()) {
      originX = startX + item.getGridCol() * cellSize;
      originY = startY + item.getGridRow() * cellSize;
    } else {
      originX = item.getPixelX();
      originY = item.getPixelY();
    }

    for (Position p : shape) {
      int px = originX + p.column() * cellSize;
      int py = originY + p.row() * cellSize;

      g.fillRect(px + 2, py + 2, cellSize - 4, cellSize - 4);

      if (isCurse) {
        g.setColor(Color.RED);
        g.drawRect(px + 2, py + 2, cellSize - 4, cellSize - 4);
        g.setColor(new Color(50, 0, 50));
      }
    }

    if (!isCurse) {
      Position first = shape.get(0);
      int nameX = originX + first.column() * cellSize;
      int nameY = originY + first.row() * cellSize;

      g.setColor(Color.BLACK);
      g.drawString(item.getName(), nameX + 10, nameY + 25);
    }
  }

  /**
   * Draws the mini-map.
   * 
   * @param g        Graphics context.
   * @param dungeon  The dungeon model.
   * @param cellSize Cell size.
   */
  public static void drawMap(Graphics2D g, Dungeon dungeon, int cellSize) {
    Objects.requireNonNull(g);
    Objects.requireNonNull(dungeon);
    g.setColor(Color.WHITE);
    g.drawString("CARTE (Appuie sur 'E' pour l'inventaire)", mapX, mapY - 20);

    for (int x = 0; x < dungeon.getColumns(); x++) {
      for (int y = 0; y < dungeon.getRows(); y++) {
        Room room = dungeon.getRoom(x, y);

        if (room == null || room.getType() == RoomType.VOID) {
          continue;
        }

        int px = mapX + x * cellSize;
        int py = mapY + y * cellSize;

        if (x == dungeon.getPlayerX() && y == dungeon.getPlayerY()) {
          g.setColor(Color.BLUE);
          g.fillRect(px, py, cellSize - 2, cellSize - 2);
          g.setColor(Color.WHITE);
          g.drawString("HERO", px + 10, py + 30);
        } else if (room.isVisited()) {
          g.setColor(Color.GREEN);
          g.fillRect(px, py, cellSize - 2, cellSize - 2);
          g.setColor(Color.BLACK);
          switch (room.getType()) {
          case ENEMY -> g.drawString("ENEMY", px + 10, py + 30);
          case EXIT -> g.drawString("EXIT", px + 10, py + 30);
          case HEALER -> g.drawString("HEALER", px + 10, py + 30);
          case START -> g.drawString("START", px + 10, py + 30);
          case TREASURE -> g.drawString("LOOT", px + 10, py + 10);
          case MERCHANT -> g.drawString("SHOP", px + 10, py + 30);
          default -> g.drawString("EMPTY", px + 10, py + 30);
          }
        } else if (room.getType() != RoomType.EMPTY && room.getType() != RoomType.START) {
          g.setColor(Color.ORANGE);
          g.fillRect(px, py, cellSize - 2, cellSize - 2);
          g.setColor(Color.BLACK);

          switch (room.getType()) {
          case ENEMY -> g.drawString("ENEMY", px + 10, py + 30);
          case EXIT -> g.drawString("EXIT", px + 10, py + 30);
          case HEALER -> g.drawString("HEALER", px + 10, py + 30);
          case TREASURE -> g.drawString("LOOT", px + 10, py + 30);
          case MERCHANT -> g.drawString("SHOP", px + 10, py + 30);
          default -> {
          }
          }
        } else {
          g.setColor(Color.DARK_GRAY);
          g.fillRect(px, py, cellSize - 2, cellSize - 2);
        }
      }
    }
  }

  /**
   * Draws text logs.
   * 
   * @param g    Graphics context.
   * @param logs List of messages.
   * @param x    X position.
   * @param y    Y position.
   */
  public static void drawLogs(Graphics2D g, List<String> logs, int x, int y) {
    Objects.requireNonNull(g);
    Objects.requireNonNull(logs);
    g.setColor(Color.WHITE);
    for (String log : logs) {
      g.drawString("> " + log, x, y);
      y += 20;
    }
  }

  /**
   * Draws enemies in combat.
   * 
   * @param g       Graphics context.
   * @param enemies List of enemies.
   */
  public static void drawEnemies(Graphics2D g, List<Enemy> enemies) {
    Objects.requireNonNull(g);
    Objects.requireNonNull(enemies);
    int baseX = 1000;
    int baseY = 700;
    int spacing = 200;
    int size = 150;

    for (int i = 0; i < enemies.size(); i++) {
      Enemy e = enemies.get(i);
      int x = baseX + i * spacing;
      int y = baseY;
      BufferedImage img = ImageManager.getEnemy(e.getName());

      if (img != null) {
        g.drawImage(img, x, y, size, size, null);
      } else {
        g.setColor(Color.RED);
        g.fillRect(x, y, size, size);
      }

      g.setColor(Color.WHITE);
      g.drawString(e.getName(), x, y - 10);

      String stats = e.getCurrentPv() + " PV";
      if (e.getShield() > 0) {
        stats += " [" + e.getShield() + " Armor]";
        g.setColor(Color.CYAN);
      } else {
        g.setColor(Color.WHITE);
      }
      g.drawString(stats, x + 10, y + size + 15);

      int damage = e.getNextMoveDamage();

      if (damage > 0) {
        g.setColor(Color.YELLOW);
        g.drawString("Attaque: " + damage, x, y + size + 35);
      } else {
        g.setColor(Color.CYAN);
        g.drawString("Se Protège (+5)", x, y + size + 35);
      }
    }
  }

  /**
   * Draws the hero and their stats.
   * 
   * @param g    Graphics context.
   * @param hero The hero model.
   */
  public static void drawHero(Graphics2D g, Hero hero) {
    Objects.requireNonNull(g);
    Objects.requireNonNull(hero);
    int x = 400;
    int y = 700;
    int size = 160;

    BufferedImage img = ImageManager.getHero();
    if (img != null) {
      g.drawImage(img, x, y, size, size, null);
    } else {
      g.setColor(Color.BLUE);
      g.fillRect(x, y, size, size);
    }

    g.setColor(Color.WHITE);
    g.drawString("Hero", x, y - 10);

    g.setColor(Color.WHITE);
    g.drawString(hero.getCurrentPv() + " / " + hero.getMaxPv() + " PV", x + 10, y + size + 15);

    if (hero.getArmor() > 0) {
      g.setColor(Color.CYAN);
      g.drawString("ARMURE: " + hero.getArmor(), x + 10, y + size + 30);
      y += 15;
    }

    g.setColor(Color.CYAN);
    g.drawString("Énergie: " + hero.getStamina(), x, y + size + 35);

    g.setColor(Color.MAGENTA);
    g.drawString("Mana: " + hero.getMana(), x, y + size + 50);

    g.setColor(Color.YELLOW);
    g.drawString("Gold: " + hero.getGold(), x, y + size + 65);

    g.setColor(Color.MAGENTA);
    g.drawString("Niveau: " + hero.getLevel(), x, y + size + 80);

    g.setColor(Color.LIGHT_GRAY);
    String xpText = "XP: " + hero.getCurrentXp() + " / " + hero.getXpToNextLevel();
    g.drawString(xpText, x, y + size + 95);
  }

  /**
   * Draws the merchant screen.
   * 
   * @param g    Graphics context.
   * @param data Game data.
   */
  public static void drawMerchantScreen(Graphics2D g, SimpleGameData data) {
    Objects.requireNonNull(g);
    Objects.requireNonNull(data);
    int merchantX = 1400;
    int merchantY = 700;
    int size = 160;
    var merchantImg = ImageManager.getMerchant();
    if (merchantImg != null) {
      g.drawImage(merchantImg, merchantX, merchantY, size, size, null);
      g.setColor(Color.WHITE);
      g.drawString("Merchant", merchantX + 40, merchantY - 10);
    }
    drawHero(g, data.getHero());

    List<Item> shopItems = data.getDungeon().getRoom(data.getDungeon().getPlayerX(), data.getDungeon().getPlayerY())
        .getShopItems();

    drawMerchantItems(g, shopItems, 500, 300, 70);
  }

  /**
   * Draws items for sale.
   * 
   * @param g        Graphics context.
   * @param items    List of items.
   * @param startX   X position.
   * @param startY   Y position.
   * @param cellSize Cell size.
   */
  private static void drawMerchantItems(Graphics2D g, List<Item> items, int startX, int startY, int cellSize) {
    int x = startX;
    int y = startY;
    int spacing = 20;

    for (Item item : items) {
      item.setPixelPos(x, y);

      drawItem(g, item, cellSize);

      g.setColor(Color.YELLOW);
      g.drawString(item.getPrice() + " g", x, y + cellSize * 2 + 15);

      x += cellSize * 2 + spacing;
    }
  }
}