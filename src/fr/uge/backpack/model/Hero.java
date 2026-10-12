package fr.uge.backpack.model;

/**
 * Represents the Hero character controlled by the player. Manages statistics
 * like health, energy, mana, gold, and the backpack.
 */
public class Hero {
  /** Current health points of the hero. */
  private int currentPv;
  /** Maximum health points. */
  private final int maxPv;
  /** Current energy/stamina points. */
  private int stamina;
  /** Current armor points. */
  private int armor;
  /** Current mana points. */
  private int mana;
  /** Current level of the hero. */
  private int level;
  /** Current amount of gold. */
  private int gold;
  /** The backpack inventory of the hero. */
  private final Backpack backpack;

  /** Current experience points. */
  private int currentXp = 0;
  /** XP required to reach the next level. */
  private int xpToNextLevel = 10;
  /** Points available to unlock backpack slots. */
  private int unlockPoints = 0;

  /**
   * Default constructor. Initializes hero with default stats.
   */
  public Hero() {
    this.maxPv = 40;
    this.currentPv = maxPv;
    this.stamina = 3;
    this.armor = 0;
    this.mana = 0;
    this.gold = 10;
    this.backpack = new Backpack(8, 10);
    this.level = 1;
  }

  /**
   * Adds experience points to the hero. Triggers level up if threshold is
   * reached.
   * 
   * @param amount Amount of XP to add.
   */
  public void addXp(int amount) {
    if (amount <= 0)
      return;
    this.currentXp += amount;

    while (currentXp >= xpToNextLevel) {
      levelUp();
    }
  }

  /**
   * Handles the level up process.
   */
  private void levelUp() {
    currentXp -= xpToNextLevel;
    level++;
    xpToNextLevel += 15;
    this.unlockPoints += 3;
  }

  /**
   * Gets the available points to unlock backpack slots.
   * 
   * @return Unlock points.
   */
  public int getUnlockPoints() {
    return unlockPoints;
  }

  /**
   * Uses an unlock point.
   */
  public void useUnlockPoint() {
    if (unlockPoints > 0) {
      unlockPoints--;
    }
  }

  /**
   * Gets current XP.
   * 
   * @return Current XP.
   */
  public int getCurrentXp() {
    return currentXp;
  }

  /**
   * Gets XP required for next level.
   * 
   * @return XP target.
   */
  public int getXpToNextLevel() {
    return xpToNextLevel;
  }

  /**
   * Gets current level.
   * 
   * @return Level.
   */
  public int getLevel() {
    return level;
  }

  /**
   * Sets current level.
   * 
   * @param level New level.
   */
  public void setLevel(int level) {
    if (level < 1) {
      throw new IllegalArgumentException("Invalid level");
    }
    this.level = level;
  }

  /**
   * Resets turn-based stats (energy, armor).
   */
  public void startTurn() {
    this.stamina = 3;
    this.armor = 0;
  }

  /**
   * Pays an energy cost.
   * 
   * @param amount Energy to pay.
   * @return true if successful, false otherwise.
   */
  public boolean payEnergy(int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Negative cost");
    }
    if (this.stamina >= amount) {
      this.stamina -= amount;
      return true;
    }
    return false;
  }

  /**
   * Adds armor to the hero.
   * 
   * @param amount Armor amount.
   */
  public void addArmor(int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Negative armor");
    }
    armor += amount;
  }

  /**
   * Applies damage to the hero, reducing armor first then health.
   * 
   * @param damage Damage amount.
   */
  public void takeDamage(int damage) {
    if (damage < 0)
      throw new IllegalArgumentException("Negative damage");
    if (damage == 0)
      return;

    if (armor >= damage) {
      armor -= damage;
      return;
    } else {
      damage -= armor;
      armor = 0;
    }

    currentPv -= damage;
    if (currentPv < 0)
      currentPv = 0;
  }

  /**
   * Heals the hero.
   * 
   * @param amount HP to restore.
   */
  public void heal(int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Negative heal");
    }
    if (currentPv + amount > maxPv) {
      currentPv = maxPv;
      return;
    }
    currentPv += amount;
  }

  /**
   * Adds mana.
   * 
   * @param amount Mana to add.
   */
  public void addMana(int amount) {
    if (amount > 0) {
      this.mana += amount;
    }
  }

  /**
   * Pays a mana cost.
   * 
   * @param amount Mana to pay.
   */
  public void payMana(int amount) {
    if (this.mana < amount) {
      throw new IllegalStateException("Not enough mana!");
    }
    this.mana -= amount;
  }

  /**
   * Checks if hero is dead.
   * 
   * @return true if HP is less than or equal to 0.
   */
  public boolean isDead() {
    return currentPv <= 0;
  }

  /**
   * Gets current HP.
   * 
   * @return Current HP.
   */
  public int getCurrentPv() {
    return currentPv;
  }

  /**
   * Adds gold.
   * 
   * @param amount Gold to add.
   */
  public void addGold(int amount) {
    if (amount > 0) {
      this.gold += amount;
    }
  }

  /**
   * Removes gold.
   * 
   * @param amount Gold to remove.
   */
  public void removeGold(int amount) {
    if (amount > 0) {
      this.gold -= amount;
      if (this.gold < 0) {
        this.gold = 0;
      }
    }
  }

  /**
   * Gets current stamina/energy.
   * 
   * @return Stamina.
   */
  public int getStamina() {
    return stamina;
  }

  /**
   * Gets current armor.
   * 
   * @return Armor.
   */
  public int getArmor() {
    return armor;
  }

  /**
   * Gets max HP.
   * 
   * @return Max HP.
   */
  public int getMaxPv() {
    return maxPv;
  }

  /**
   * Gets current mana.
   * 
   * @return Mana.
   */
  public int getMana() {
    return mana;
  }

  /**
   * Gets the backpack.
   * 
   * @return The Backpack object.
   */
  public Backpack getBackpack() {
    return backpack;
  }

  /**
   * Gets current gold.
   * 
   * @return Gold.
   */
  public int getGold() {
    return gold;
  }
}