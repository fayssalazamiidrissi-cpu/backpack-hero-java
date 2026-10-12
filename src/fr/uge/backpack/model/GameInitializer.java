package fr.uge.backpack.model;

import java.io.IOException;
import java.util.Objects;

/**
 * Utility class responsible for setting up a new game. Loads resources, creates
 * the floor, and gives starter items.
 */
public class GameInitializer {

  /**
   * Default constructor.
   */
  public GameInitializer() {
  }

  /**
   * Configures a new game session.
   * 
   * @param data The game data to initialize.
   */
  public static void setupNewGame(SimpleGameData data) {
    Objects.requireNonNull(data);
    
    loadResources(data);

    
    data.getDungeon().createFloor(1, data.getLoader());

    
    giveStarterItems(data);

    data.log("Game initialized!");
  }

  /**
   * Loads game resources (enemies and items).
   * 
   * @param data The game data.
   */
  private static void loadResources(SimpleGameData data) {
    try {
      data.getLoader().loadEnemies("Monstres.txt");
      data.getLoader().loadItems("Items.txt");
      data.log("Resources loaded.");
    } catch (IOException e) {
      System.err.println("CRITICAL ERROR : " + e.getMessage());
      data.log("Error loading files!");
    }
  }

  /**
   * Gives the default items to the hero.
   * 
   * @param data The game data.
   */
  private static void giveStarterItems(SimpleGameData data) {
    Hero hero = data.getHero();
    GameLoader loader = data.getLoader();
    Backpack pack = hero.getBackpack();

    
    int centerRow = (pack.getMaxRows() - 3) / 2;
    int centerCol = (pack.getMaxCols() - 3) / 2;

  
    Item sword = loader.createItem("Épée");
    if (sword != null) {
      pack.addItem(sword, centerRow, centerCol);
    } else {
      data.log("Error: Sword not found");
    }

   
    Item shield = loader.createItem("Bouclier");
    if (shield != null) {
      pack.addItem(shield, centerRow + 1, centerCol + 1);
    }

   
    Item stone = loader.createItem("Petite Pierre");
    if (stone != null) {
      pack.addItem(stone, centerRow + 2, centerCol);
    }
  }
}