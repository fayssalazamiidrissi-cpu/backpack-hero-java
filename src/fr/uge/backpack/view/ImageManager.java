package fr.uge.backpack.view;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import java.util.Objects;

/**
 * Utility class to load.
 */
public final class ImageManager {

  /** Map storing enemy images. */
  private static final Map<String, BufferedImage> ENEMIES = new HashMap<>();
  /** Hero image. */
  private static BufferedImage HERO;
  /** Merchant image. */
  private static BufferedImage MERCHANT;
  /** Dungeon map background image. */
  private static BufferedImage DUNGEON_MAP;

  static {
    loadEnemies();
    loadHero();
    loadMerchant();
    loadBackground();
  }

  /**
   * Loads all enemy images.
   */
  private static void loadEnemies() {
    loadEnemy("Ratwolf");
    loadEnemy("Small_Ratwolf");
    loadEnemy("Bee");
    loadEnemy("Slime");
    loadEnemy("Crab");
  }

  /**
   * Loads a specific enemy image.
   * 
   * @param name Name of the enemy file.
   */
  private static void loadEnemy(String name) {
    String path = "/images/ennemie/" + name + ".png";
    BufferedImage img = loadImage(path);
    if (img != null) {
      ENEMIES.put(name, img);
    }
  }

  /**
   * Gets an enemy image by name.
   * 
   * @param name Enemy name.
   * @return The image.
   */
  public static BufferedImage getEnemy(String name) {
    Objects.requireNonNull(name);
    return ENEMIES.get(name);
  }

  /**
   * Loads the hero image.
   */
  private static void loadHero() {
    HERO = loadImage("/images/hero/Satchel.png");
  }

  /**
   * Gets the hero image.
   * 
   * @return The image.
   */
  public static BufferedImage getHero() {
    return HERO;
  }

  /**
   * Loads the merchant image.
   */
  private static void loadMerchant() {
    MERCHANT = loadImage("/images/merchant/merchant.png");
  }

  /**
   * Gets the merchant image.
   * 
   * @return The image.
   */
  public static BufferedImage getMerchant() {
    return MERCHANT;
  }

  /**
   * Loads the background image.
   */
  private static void loadBackground() {
    DUNGEON_MAP = loadImage("/images/map/mapdonjon.png");
  }

  /**
   * Gets the map background.
   * 
   * @return The image.
   */
  public static BufferedImage getDungeonMap() {
    return DUNGEON_MAP;
  }

  /**
   * Loads an image from the classpath using try-with-resources.
   * 
   * @param path The resource path.
   * @return The loaded image or null.
   */
  private static BufferedImage loadImage(String path) {
    try (var is = ImageManager.class.getResourceAsStream(path)) {

      if (is == null) {
        System.err.println(" Image introuvable : " + path);
        return null;
      }
      return ImageIO.read(is);

    } catch (IOException e) {
      System.err.println(" Erreur chargement image : " + path);
      return null;
    }
  }

  /**
   * Private constructor to prevent instantiation.
   */
  private ImageManager() {
  }
}