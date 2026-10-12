package fr.uge.backpack.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;

/**
 * Represents the dungeon map. Manages the grid of rooms, the current floor, and
 * the player's position. Handles procedural generation of the level layout.
 */
public class Dungeon {
  /** Number of columns in the dungeon grid. */
  private final int columns = 11;
  /** Number of rows in the dungeon grid. */
  private final int rows = 5;

  /** The grid of rooms. */
  private ArrayList<ArrayList<Room>> map;

  /** Current floor level. */
  private int currentFloor = 1;
  /** Player's X coordinate. */
  private int playerX;
  /** Player's Y coordinate. */
  private int playerY;

  /**
   * Default constructor. Initializes the map list.
   */
  public Dungeon() {
    map = new ArrayList<>();
  }

  /**
   * Initializes the grid with VOID rooms.
   * 
   * @param loader The game loader used to instantiate rooms.
   */
  public void initializeGrid(GameLoader loader) {
    Objects.requireNonNull(loader);
    map.clear();
    for (int i = 0; i < rows; i++) {
      ArrayList<Room> line = new ArrayList<>();
      for (int j = 0; j < columns; j++) {
        line.add(new Room(RoomType.VOID, loader));
      }
      map.add(line);
    }
  }

  /**
   * Distributes enemies, treasures, and special rooms on the generated floor.
   * 
   * @param floor The current floor level.
   */
  private void populateFloor(int floor) {

    ArrayList<Room> emptyRooms = new ArrayList<>();
    for (int y = 0; y < rows; y++) {
      for (int x = 0; x < columns; x++) {
        if (map.get(y).get(x).getType() == RoomType.EMPTY) {
          emptyRooms.add(map.get(y).get(x));
        }
      }
    }

    Collections.shuffle(emptyRooms);

    int nbEnemies = 3;
    int nbTreasures = 2;

    // Enemies
    for (int i = 0; i < nbEnemies && !emptyRooms.isEmpty(); i++)
      emptyRooms.remove(0).setType(RoomType.ENEMY);

    // Treasures
    for (int i = 0; i < nbTreasures && !emptyRooms.isEmpty(); i++)
      emptyRooms.remove(0).setType(RoomType.TREASURE);

    // Healer
    if (!emptyRooms.isEmpty()) {
      emptyRooms.remove(0).setType(RoomType.HEALER);
    }

    // Merchant
    if (!emptyRooms.isEmpty()) {
      Room merchantRoom = emptyRooms.remove(0);
      merchantRoom.setType(RoomType.MERCHANT);
    }
  }

  /**
   * Generates a new floor layout procedurally. Uses a snake-like path generation
   * followed by random branching.
   * 
   * @param floor  The floor number to generate.
   * @param loader The loader for game assets.
   */
  public void createFloor(int floor, GameLoader loader) {
    if (floor <= 0)
      throw new IllegalArgumentException("Negative floor");
    Objects.requireNonNull(loader);
    this.currentFloor = floor;

    initializeGrid(loader);

    Random rand = new Random();

    int y = rand.nextInt(rows);
    int x = 0;

    map.get(y).get(x).setType(RoomType.START);
    map.get(y).get(x).setVisited(true);
    playerX = x;
    playerY = y;

    int roomCount = 1;

    while (x < columns - 1) {
      if (rand.nextBoolean()) {
        x++;
      } else {
        int dir = rand.nextBoolean() ? 1 : -1;
        y = Math.max(0, Math.min(rows - 1, y + dir));
      }

      RoomType type = (x == columns - 1) ? RoomType.EXIT : RoomType.EMPTY;
      map.get(y).get(x).setType(type);
      roomCount++;
    }

    int targetRooms = 18;

    while (roomCount < targetRooms) {
      int ry = rand.nextInt(rows);
      int rx = rand.nextInt(columns);

      if (map.get(ry).get(rx).getType() != RoomType.VOID) {
        int dir = rand.nextInt(4);
        int nx = rx, ny = ry;

        if (dir == 0)
          ny--; // Up
        else if (dir == 1)
          ny++; // Down
        else if (dir == 2)
          nx--; // Left
        else if (dir == 3)
          nx++; // Right

        if (nx >= 0 && nx < columns && ny >= 0 && ny < rows && map.get(ny).get(nx).getType() == RoomType.VOID) {

          map.get(ny).get(nx).setType(RoomType.EMPTY);
          roomCount++;
        }
      }
    }

    populateFloor(floor);
  }

  // --- GETTERS & SETTERS ---

  /**
   * Gets the room at specific coordinates.
   * 
   * @param x X coordinate.
   * @param y Y coordinate.
   * @return The Room object or null if out of bounds.
   */
  public Room getRoom(int x, int y) {
    if (x < 0 || x >= columns || y < 0 || y >= rows) {
      return null;
    }
    return map.get(y).get(x);
  }

  /**
   * Updates player coordinates.
   * 
   * @param x New X coordinate.
   * @param y New Y coordinate.
   */
  public void movePlayer(int x, int y) {
    if (x < 0 || x >= columns || y < 0 || y >= rows) {
      return;
    }
    // Variable removed to avoid warning
    playerX = x;
    playerY = y;
  }

  /**
   * Gets the number of columns in the dungeon.
   * 
   * @return The number of columns.
   */
  public int getColumns() {
    return columns;
  }

  /**
   * Gets the number of rows in the dungeon.
   * 
   * @return The number of rows.
   */
  public int getRows() {
    return rows;
  }

  /**
   * Gets the player's X coordinate.
   * 
   * @return X coordinate.
   */
  public int getPlayerX() {
    return playerX;
  }

  /**
   * Gets the player's Y coordinate.
   * 
   * @return Y coordinate.
   */
  public int getPlayerY() {
    return playerY;
  }

  /**
   * Gets the current floor level.
   * 
   * @return The floor number.
   */
  public int getCurrentFloor() {
    return currentFloor;
  }

  /**
   * Sets the current floor level.
   * 
   * @param currentFloor The new floor number.
   */
  public void setCurrentFloor(int currentFloor) {
    this.currentFloor = currentFloor;
  }
}