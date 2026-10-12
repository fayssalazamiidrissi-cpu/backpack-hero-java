package fr.uge.backpack.model;

/**
 * Enumeration representing the different states of the game.
 */
public enum GameState {
  /** State when the player is exploring the map. */
  EXPLORATION,
  /** State when the player is fighting enemies. */
  COMBAT,
  /** State when the player dies. */
  GAME_OVER,
  /** State when the player finishes the game. */
  VICTORY,
  /** State when the player is buying items. */
  SHOP,
  /** State when the player is managing the inventory. */
  INVENTORY
}