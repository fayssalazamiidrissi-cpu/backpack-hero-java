package fr.uge.backpack.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Represents a room in the dungeon.
 */
public class Room {

  private RoomType type;
  private boolean visited;
  private final List<Enemy> enemies = new ArrayList<>();
  private final List<Item> shopItems = new ArrayList<>();

  private final GameLoader loader;

  /**
   * Constructor.
   * 
   * @param type   Room type.
   * @param loader Game loader.
   */
  public Room(RoomType type, GameLoader loader) {
    Objects.requireNonNull(loader);
    Objects.requireNonNull(type);
    this.type = type;
    this.loader = loader;
    this.visited = false;
    enemyRoom();
  }

  /**
   * Generates room content.
   */
  public void enemyRoom() {
    enemies.clear();
    shopItems.clear();

    if (type == RoomType.ENEMY) {
      var rand = new Random();
      int nbEnemies = 1 + rand.nextInt(2);

      String[] possibleEnemies = { "Ratwolf", "Small_Ratwolf", "Bee", "Slime", "Crab" };

      for (int i = 0; i < nbEnemies; i++) {
        String randomName = possibleEnemies[rand.nextInt(possibleEnemies.length)];
        enemies.add(loader.createEnemy(randomName));
      }
    } else if (type == RoomType.MERCHANT) {
      setupMerchant();
    }
  }

  /**
   * Setups the merchant items.
   */
  public void setupMerchant() {
    int itemsToSell = 3;
    int attempts = 0;

    while (shopItems.size() < itemsToSell && attempts < 15) {
      attempts++;

      Item item = loader.createRandomItem();

      if (item == null || item.getType() == ItemType.CURSE)
        continue;

      applyMerchantPricing(item);

      shopItems.add(item);
    }
  }

  private void applyMerchantPricing(Item item) {
    int basePrice = item.getPrice();
    Random rand = new Random();

    double factor = 1.0 + (rand.nextDouble() * 0.4 - 0.2);

    int finalPrice = (int) (basePrice * factor);
    if (finalPrice < 1)
      finalPrice = 1;

    item.setPrice(finalPrice);
  }

  // --- GETTERS / SETTERS ---

  public void setVisited(boolean visited) {
    this.visited = visited;
  }

  public boolean isVisited() {
    return visited;
  }

  public RoomType getType() {
    return type;
  }

  public void setType(RoomType type) {
    Objects.requireNonNull(type);
    this.type = type;
    enemyRoom();
  }

  public List<Enemy> getEnemies() {
    return enemies;
  }

  public List<Item> getShopItems() {
    return shopItems;
  }
}