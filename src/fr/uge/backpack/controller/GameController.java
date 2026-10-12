package fr.uge.backpack.controller;

import java.awt.Color;
import com.github.forax.zen.Application;
import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.Event;
import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.KeyboardEvent.Action;
import com.github.forax.zen.PointerEvent;
import fr.uge.backpack.model.GameState;
import fr.uge.backpack.model.SimpleGameData;
import fr.uge.backpack.view.GameRenderer;

/**
 * Main controller class. Manages the game loop and dispatches events to
 * handlers.
 */
public class GameController {

  /**
   * Default constructor.
   */
  public GameController() {
  }

  /**
   * Main entry point of the program. Initializes data and starts the Zen
   * application loop.
   * 
   * @param args Command line arguments.
   */
  public static void main(String[] args) {
    var data = new SimpleGameData();
    var inventoryHandler = new InventoryHandler();

    Application.run(Color.BLACK, context -> {
      while (true) {

        context.renderFrame(g -> GameRenderer.render(g, context, data, inventoryHandler));

        Event event;
        while ((event = context.pollEvent()) != null) {
          boolean keepRunning = handleEvent(event, data, inventoryHandler, context);

          if (!keepRunning) {
            context.dispose();
            return;
          }
        }
      }
    });
  }

  /**
   * Dispatches events to the correct handler.
   * 
   * @param event   The event to handle.
   * @param data    The game data.
   * @param handler The inventory handler.
   * @param context The application context.
   * @return false if the game should stop, true otherwise.
   */
  private static boolean handleEvent(Event event, SimpleGameData data, InventoryHandler handler,
      ApplicationContext context) {
    if (event.getClass() == PointerEvent.class) {
      handlePointerEvent((PointerEvent) event, data, handler);
      return true;
    } else if (event.getClass() == KeyboardEvent.class) {
      return handleKeyboardEvent((KeyboardEvent) event, data, handler);
    }
    return true;
  }

  /**
   * Handles mouse clicks based on game state (Map, Shop, Inventory).
   * 
   * @param pe      The pointer event.
   * @param data    The game data.
   * @param handler The inventory handler.
   */
  private static void handlePointerEvent(PointerEvent pe, SimpleGameData data, InventoryHandler handler) {

    if (data.getState() == GameState.EXPLORATION) {
      if (pe.action() == PointerEvent.Action.POINTER_DOWN) {
        int mapCellSize = 70;
        int x = pe.location().x();
        int y = pe.location().y();

        if (x >= 400 && y >= 100) {
          int mapX = (x - 400) / mapCellSize;
          int mapY = (y - 100) / mapCellSize;
          data.movePlayerTo(mapX, mapY);
        }
      }
    }

    else if (data.getState() == GameState.SHOP) {
      handler.handleShopPointerEvent(pe, data);
    }

    else {
      handler.handlePointerEvent(pe, data);
    }
  }

  /**
   * Handles keyboard inputs.
   * 
   * @param ke      The keyboard event.
   * @param data    The game data.
   * @param handler The inventory handler.
   * @return false if Q is pressed, true otherwise.
   */
  private static boolean handleKeyboardEvent(KeyboardEvent ke, SimpleGameData data, InventoryHandler handler) {
    if (ke.action() == Action.KEY_PRESSED) {
      switch (ke.key()) {
      case E -> data.switchMode();
      case R -> handler.rotateDraggedItem();
      case SPACE -> handleSpace(data);
      case X -> {
        if (data.getState() == GameState.INVENTORY) {
          handler.handleDeleteCurse(data);
        }
      }
      case Q -> {
        return false;
      }
      default -> {
      }
      }
    }
    return true;
  }

  /**
   * Ends the turn if in combat mode.
   * 
   * @param data The game data.
   */
  private static void handleSpace(SimpleGameData data) {
    if (data.getState() == GameState.COMBAT) {
      data.endTurn();
    }
  }
}