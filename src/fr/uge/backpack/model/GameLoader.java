package fr.uge.backpack.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Handles loading of game data (Items and Enemies) from configuration files.
 */
public class GameLoader {

  /** Template map for enemies. */
  private final Map<String, EnemyInfo> enemyTemplates = new HashMap<>();
  /** Template map for items. */
  private final Map<String, ItemInfo> itemTemplates = new HashMap<>();

  /**
   * Record representing raw enemy data loaded from file.
   * 
   * @param name   Name of the enemy.
   * @param hp     Health points.
   * @param attack Attack power.
   * @param xp     Experience reward.
   */
  public record EnemyInfo(String name, int hp, int attack, int xp) {
  }

  /**
   * Default constructor.
   */
  public GameLoader() {
  }

  /**
   * Loads items from a specific file in the classpath.
   * 
   * @param fileName Name of the file to load.
   * @throws IOException If the file cannot be read.
   */
  public void loadItems(String fileName) throws IOException {
    Objects.requireNonNull(fileName);

    InputStream inputStream = getClass().getResourceAsStream("/" + fileName);

    if (inputStream == null) {
      throw new IOException("File not found in JAR/Classpath: " + fileName);
    }

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.isBlank() || line.startsWith("#"))
          continue;

        String[] parts = line.split(";");
        // Format: Name;Type;Price;Energy;Mana;Value;Shape
        if (parts.length >= 7) {
          try {
            String name = parts[0].trim();
            ItemType type = ItemType.valueOf(parts[1].trim().toUpperCase());
            int price = Integer.parseInt(parts[2].trim());
            int energy = Integer.parseInt(parts[3].trim());
            int mana = Integer.parseInt(parts[4].trim());
            int value = Integer.parseInt(parts[5].trim());
            String shapeString = parts[6].trim();

            List<Position> shape = parseShape(shapeString);

            itemTemplates.put(name.toLowerCase(), new ItemInfo(name, type, price, energy, mana, value, shape));

          } catch (Exception e) {
            System.err.println("Ignored Item line: " + line + " -> " + e.getMessage());
          }
        }
      }
    }
  }

  /**
   * Loads enemies from a specific file in the classpath.
   * 
   * @param fileName Name of the file to load.
   * @throws IOException If the file cannot be read.
   */
  public void loadEnemies(String fileName) throws IOException {
    Objects.requireNonNull(fileName);

    InputStream inputStream = getClass().getResourceAsStream("/" + fileName);

    if (inputStream == null) {
      throw new IOException("File not found in JAR/Classpath: " + fileName);
    }

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.isBlank() || line.startsWith("#"))
          continue;

        String[] parts = line.split(";");
        // Format: Name;HP;Attack;XP
        if (parts.length >= 4) {
          try {
            String name = parts[0].trim();
            int hp = Integer.parseInt(parts[1].trim());
            int atk = Integer.parseInt(parts[2].trim());
            int xp = Integer.parseInt(parts[3].trim());

            enemyTemplates.put(name.toLowerCase(), new EnemyInfo(name, hp, atk, xp));
          } catch (Exception e) {
            System.err.println("Ignored Enemy line: " + line + " -> " + e.getMessage());
          }
        }
      }
    }
  }

  /**
   * Parses a shape string into a list of positions.
   * 
   * @param shapeString String describing the shape (e.g., "0,0|0,1").
   * @return List of Positions.
   */
  private List<Position> parseShape(String shapeString) {
    List<Position> positions = new ArrayList<>();
    String[] coords = shapeString.split("\\|");
    for (String coord : coords) {
      String[] rc = coord.split(",");
      if (rc.length == 2) {
        try {
          int r = Integer.parseInt(rc[0].trim());
          int c = Integer.parseInt(rc[1].trim());
          positions.add(new Position(r, c));
        } catch (NumberFormatException e) {
          // Ignore malformed coordinate
        }
      }
    }
    return positions;
  }

  /**
   * Creates a new Item instance by name.
   * 
   * @param name Name of the item.
   * @return The new Item or null if not found.
   */
  public Item createItem(String name) {
    Objects.requireNonNull(name);
    ItemInfo info = itemTemplates.get(name.toLowerCase());
    if (info == null) {
      System.err.println("WARNING: Item not found in database: " + name);
      return null;
    }
    return new Item(info);
  }

  /**
   * Creates a random item from the loaded templates.
   * 
   * @return A random Item or null if empty.
   */
  public Item createRandomItem() {
    if (itemTemplates.isEmpty())
      return null;
    List<String> keys = new ArrayList<>(itemTemplates.keySet());
    String randomKey = keys.get(new Random().nextInt(keys.size()));
    return createItem(randomKey);
  }

  /**
   * Creates a new Enemy instance by name.
   * 
   * @param name Name of the enemy.
   * @return The new Enemy object.
   */
  public Enemy createEnemy(String name) {
    Objects.requireNonNull(name);
    EnemyInfo info = enemyTemplates.get(name.toLowerCase());
    if (info == null)
      throw new IllegalArgumentException("Unknown monster: " + name);
    return new Enemy(info.name(), info.hp(), info.attack(), info.xp());
  }
}