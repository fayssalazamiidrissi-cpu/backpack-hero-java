package fr.uge.backpack.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Main game data class. Holds the game state, hero, dungeon, and logic.
 */
public class SimpleGameData {
  private final Hero hero;
  private final ArrayList<Enemy> enemies;
  private GameState state;
  private final Dungeon dungeon;
  private final List<String> logs = new ArrayList<>();
  private final List<Item> itemsOut = new ArrayList<>();

  private int curseRefusalCount = 0;
  private boolean cursePenaltyActive = false;

  private final GameLoader loader;

  /**
   * Default constructor. Initializes the game.
   */
  public SimpleGameData() {
    this.loader = new GameLoader();
    this.hero = new Hero();
    this.enemies = new ArrayList<>();
    this.dungeon = new Dungeon();
    this.state = GameState.EXPLORATION;

    GameInitializer.setupNewGame(this);
  }

  private Item generateRandomLoot() {
    Random rand = new Random();
    int roll = rand.nextInt(100);

    if (roll < 20)
      return createRandomCurse();
    if (roll < 60)
      return loader.createRandomItem();
    return null;
  }

  private Item createRandomCurse() {
    Random rand = new Random();
    int shapeType = rand.nextInt(3);
    List<Position> shape;

    switch (shapeType) {
    case 0:
      shape = List.of(new Position(0, 0), new Position(0, 1), new Position(1, 1), new Position(1, 2));
      break;
    case 1:
      shape = List.of(new Position(0, 0), new Position(0, 1), new Position(1, 0), new Position(1, 1));
      break;
    case 2:
      shape = List.of(new Position(0, 0), new Position(1, 0), new Position(2, 0));
      break;
    default:
      shape = List.of(new Position(0, 0));
    }
    return Item.create("Malédiction", ItemType.CURSE, 0, 0, 0, 0, shape);
  }

  /**
   * Refuses a curse and applies penalty.
   * 
   * @param curse The curse item.
   */
  public void refuseCurse(Item curse) {
    Objects.requireNonNull(curse);
    curseRefusalCount++;
    int damage = curseRefusalCount * 5;
    log("Refus n°" + curseRefusalCount + " : -" + damage + " PV !");
    hero.takeDamage(damage);
    itemsOut.remove(curse);
  }

  /**
   * Removes a curse from bag.
   * 
   * @param curse The curse item.
   */
  public void removeCurseFromBag(Item curse) {
    Objects.requireNonNull(curse);
    cursePenaltyActive = true;
    log("Malédiction jetée : -1 Énergie au prochain combat !");
  }

  /**
   * Logs a message.
   * 
   * @param message The message.
   */
  public void log(String message) {
    Objects.requireNonNull(message);
    logs.add(message);
    if (logs.size() > 5)
      logs.remove(0);
  }

  /**
   * Moves the player on the map.
   * 
   * @param x X coordinate.
   * @param y Y coordinate.
   */
  public void movePlayerTo(int x, int y) {
    if (state != GameState.EXPLORATION)
      return;

    if (hasPendingCurse()) {
      log("Une malédiction vous bloque ! (Ouvrez l'inventaire)");
      return;
    }

    if (!isValidMove(x, y)) {
      log("Déplacement impossible !");
      return;
    }
    dungeon.movePlayer(x, y);
    handleRoomInteraction(dungeon.getRoom(x, y));
  }

  private boolean isValidMove(int x, int y) {
    Room room = dungeon.getRoom(x, y);
    if (room == null)
      return false;
    if (room.getType() == RoomType.VOID)
      return false;

    int diffX = Math.abs(x - dungeon.getPlayerX());
    int diffY = Math.abs(y - dungeon.getPlayerY());
    if (diffX + diffY != 1)
      return false;
    return true;
  }

  private void handleRoomInteraction(Room room) {
    switch (room.getType()) {
    case ENEMY -> startCombat(room.getEnemies());
    case HEALER -> processHealer(room);
    case TREASURE -> lootTreasure(room);
    case MERCHANT -> {
      log("Bienvenue au magasin !");
      state = GameState.SHOP;
    }
    case EXIT -> nextFloor();
    default -> {
    }
    }
  }

  /**
   * Starts a combat.
   * 
   * @param roomEnemies List of enemies.
   */
  public void startCombat(List<Enemy> roomEnemies) {
    Objects.requireNonNull(roomEnemies);
    log("Combat lancé !");
    state = GameState.COMBAT;
    enemies.clear();
    if (roomEnemies != null)
      enemies.addAll(roomEnemies);

    if (cursePenaltyActive) {
      hero.payEnergy(1);
      log("Malédiction : -1 Énergie (Fatigue)");
      cursePenaltyActive = false;
    }
  }

  private void processHealer(Room room) {
    log("Guérisseur : PV restaurés.");
    hero.heal(hero.getMaxPv());
    room.setType(RoomType.EMPTY);
  }

  /**
   * Loots a treasure room.
   * 
   * @param room The room.
   */
  public void lootTreasure(Room room) {
    Item loot = loader.createRandomItem();
    if (loot == null)
      loot = createRandomCurse();

    loot.setPixelPos(1500, 100);
    itemsOut.add(loot);
    log("Trésor : " + loot.getName());
    room.setType(RoomType.EMPTY);
  }

  /**
   * Uses an item.
   * 
   * @param item   The item.
   * @param target The enemy target.
   */
  public void playerUseItem(Item item, Enemy target) {
    Objects.requireNonNull(item);
    if (!validateItemUsage(item, target))
      return;

    hero.payEnergy(item.getEnergyCost());

    if (item.getType() == ItemType.MAGIC_WEAPON) {
      hero.payMana(item.getManaComponent());
    }

    applyItemEffect(item, target);
    item.setUsedThisTurn(true);

    if (target != null && !target.isAlive()) {
      handleEnemyDeath(target);
    }
    enemies.removeIf(e -> !e.isAlive());
    checkFightStatus();
  }

  private boolean validateItemUsage(Item item, Enemy target) {
    if (item.getType() == ItemType.MANA_SOURCE && item.isUsedThisTurn()) {
      log("Déjà utilisé ce tour !");
      return false;
    }
    if (hero.getStamina() < item.getEnergyCost()) {
      log("Pas assez d'énergie !");
      return false;
    }
    if (item.getType() == ItemType.MAGIC_WEAPON && hero.getMana() < item.getManaComponent()) {
      log("Pas assez de Mana ! (" + item.getManaComponent() + " requis)");
      return false;
    }

    boolean needsTarget = (item.getType() == ItemType.MELEE_WEAPON || item.getType() == ItemType.MAGIC_WEAPON);

    if (needsTarget && (target == null || !target.isAlive())) {
      log("Cible invalide !");
      return false;
    }
    return true;
  }

  private void applyItemEffect(Item item, Enemy target) {
    switch (item.getType()) {
    case MANA_SOURCE -> {
      int gain = item.getValue();
      hero.addMana(gain);
      log("Mana +" + gain);
    }
    case MAGIC_WEAPON -> {
      target.takeDamage(item.getValue());
      log("Magie : " + item.getValue() + " dégâts");
    }
    case MELEE_WEAPON -> {
      target.takeDamage(item.getValue());
      log("Attaque : " + item.getValue());
    }
    case SHIELD -> {
      hero.addArmor(item.getValue());
      log("Armure : +" + item.getValue());
    }
    case MISC -> {
      hero.heal(item.getValue());
      log("Soin : +" + item.getValue());
      hero.getBackpack().remove(item);
    }
    default -> {
    }
    }
  }

  private void handleEnemyDeath(Enemy target) {
    log(target.getName() + " vaincu !");
    hero.addXp(target.getXp());

    Item loot = generateRandomLoot();
    if (loot != null) {
      int offset = itemsOut.size() * 80;
      loot.setPixelPos(1500, 100 + offset);
      itemsOut.add(loot);

      if (loot.getType() == ItemType.CURSE)
        log("Maudit ! (Malédiction)");
      else
        log("Drop : " + loot.getName());
    }

    Random rand = new Random();
    int goldAmount = 5 + rand.nextInt(11);
    hero.addGold(goldAmount);
    log("Gain : +" + goldAmount + " Or");
  }

  /**
   * Ends the player turn.
   */
  public void endTurn() {
    for (Enemy enemy : enemies) {
      if (enemy.isAlive())
        applyEnemyAction(enemy);
    }

    hero.startTurn();

    Backpack pack = hero.getBackpack();
    for (int r = 0; r < pack.getMaxRows(); r++) {
      for (int c = 0; c < pack.getMaxCols(); c++) {
        Case cell = pack.get(r, c);
        if (cell instanceof Item item) {
          item.setUsedThisTurn(false);
        }
      }
    }

    for (Enemy enemy : enemies) {
      if (enemy.isAlive())
        enemy.planNextMove();
    }
    checkFightStatus();
  }

  private void applyEnemyAction(Enemy enemy) {
    int damage = enemy.getNextMoveDamage();
    if (damage > 0) {
      log(enemy.getName() + " attaque (" + damage + ")");
      hero.takeDamage(damage);
    } else {
      log(enemy.getName() + " se protège.");
    }
  }

  /**
   * Checks win/lose condition.
   */
  public void checkFightStatus() {
    enemies.removeIf(e -> !e.isAlive());
    if (hero.isDead()) {
      state = GameState.GAME_OVER;
      log("GAME OVER");
    } else if (enemies.isEmpty() && state == GameState.COMBAT) {
      state = GameState.INVENTORY;
      log("Victoire ! Récupérez votre butin.");

      hero.startTurn();
      Room room = dungeon.getRoom(dungeon.getPlayerX(), dungeon.getPlayerY());
      if (room != null)
        room.setType(RoomType.EMPTY);
    }
  }

  /**
   * Switches game mode.
   */
  public void switchMode() {
    if (state == GameState.EXPLORATION) {
      state = GameState.INVENTORY;
      log("Inventaire");
    } else if (state == GameState.INVENTORY) {
      tryExitInventory();
    } else if (state == GameState.SHOP) {
      exitShop();
    }
  }

  private void tryExitInventory() {
    if (hasPendingCurse()) {
      log("Ramassez la malédiction !");
      return;
    }
    state = GameState.EXPLORATION;
    if (!itemsOut.isEmpty()) {
      itemsOut.clear();
      log("Objets perdus.");
    }
    log("Retour carte.");
  }

  private boolean hasPendingCurse() {
    for (Item item : itemsOut) {
      if (item.getType() == ItemType.CURSE)
        return true;
    }
    return false;
  }

  private void exitShop() {
    state = GameState.EXPLORATION;
    log("Sortie magasin.");
  }

  /**
   * Moves to next floor.
   */
  public void nextFloor() {
    int next = dungeon.getCurrentFloor() + 1;
    if (next > 3) {
      state = GameState.VICTORY;
      log("VICTOIRE !");
    } else {
      log("ÉTAGE " + next);
      dungeon.createFloor(next, loader);
      dungeon.setCurrentFloor(next);
      dungeon.movePlayer(dungeon.getPlayerX(), dungeon.getPlayerY());
    }
  }

  /**
   * Buys an item from shop.
   * 
   * @param item The item.
   */
  public void buyItem(Item item) {
    Objects.requireNonNull(item);
    if (hero.getGold() < item.getPrice()) {
      log("Pas assez d'or !");
      return;
    }
    hero.removeGold(item.getPrice());
    Room room = dungeon.getRoom(dungeon.getPlayerX(), dungeon.getPlayerY());
    if (room != null && room.getType() == RoomType.MERCHANT) {
      room.getShopItems().remove(item);
    }
    int offset = itemsOut.size() * 70;
    item.setPixelPos(1200, 200 + offset);
    itemsOut.add(item);
    log("Acheté : " + item.getName());
  }

  public List<Item> getItemsOut() {
    return itemsOut;
  }

  public Hero getHero() {
    return hero;
  }

  public ArrayList<Enemy> getEnemies() {
    return enemies;
  }

  public GameState getState() {
    return state;
  }

  public Dungeon getDungeon() {
    return dungeon;
  }

  public List<String> getLogs() {
    return logs;
  }

  public GameLoader getLoader() {
    return loader;
  }
}