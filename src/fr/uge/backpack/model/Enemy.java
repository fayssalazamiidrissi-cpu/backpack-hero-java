package fr.uge.backpack.model;

import java.util.Objects;
import java.util.Random;

/**
 * Represents an enemy in the game.
 */
public class Enemy {
  /** Name of the enemy. */
  private final String name;
  /** Maximum HP. */
  private final int maxPv;
  /** Current HP. */
  private int currentPv;
  /** Experience points given. */
  private final int xp;
  /** Attack damage. */
  private int attack;
  /** Armor value. */
  private int armor;
  /** Damage planned for next move. */
  private int nextMoveDamage;
  /** Random generator. */
  private final Random random = new Random();

  /**
   * Constructor.
   * 
   * @param name   Enemy name.
   * @param maxPv  Max HP.
   * @param attack Attack power.
   * @param xp     XP reward.
   */
  public Enemy(String name, int maxPv, int attack, int xp) {
    this.name = Objects.requireNonNull(name);
    this.maxPv = maxPv;
    this.currentPv = maxPv;
    this.attack = attack;
    this.xp = xp;
    planNextMove();
  }

  /**
   * Decides the next move (Attack or Defend).
   */
  public void planNextMove() {
    this.nextMoveDamage = 0;
    this.armor = 0;

    boolean willAttack = random.nextBoolean();

    if (willAttack) {
      this.nextMoveDamage = this.attack;
    } else {
      this.armor = 5;
    }
  }

  /**
   * Applies damage to the enemy.
   * 
   * @param damage Damage amount.
   */
  public void takeDamage(int damage) {
    if (damage < 0)
      return;
    if (armor >= damage) {
      armor -= damage;
      return;
    } else {
      damage -= armor;
      armor = 0;
    }
    this.currentPv -= damage;
    if (this.currentPv < 0)
      this.currentPv = 0;
  }

  /**
   * Checks if the enemy is alive.
   * 
   * @return true if alive.
   */
  public boolean isAlive() {
    return currentPv > 0;
  }

  /**
   * Gets enemy name.
   * 
   * @return Name.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets current PV.
   * 
   * @return PV.
   */
  public int getCurrentPv() {
    return currentPv;
  }

  /**
   * Gets max PV.
   * 
   * @return Max PV.
   */
  public int getMaxPv() {
    return maxPv;
  }

  /**
   * Gets XP reward.
   * 
   * @return XP.
   */
  public int getXp() {
    return xp;
  }

  /**
   * Gets planned damage.
   * 
   * @return Damage.
   */
  public int getNextMoveDamage() {
    return nextMoveDamage;
  }

  /**
   * Gets current shield/armor.
   * 
   * @return Shield.
   */
  public int getShield() {
    return armor;
  }
}