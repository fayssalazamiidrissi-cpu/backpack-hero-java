package fr.uge.backpack.model;

/**
 * Sealed interface representing a cell in the backpack grid. Can be a
 * BlockFree, BlockInactive, or an Item.
 */
public sealed interface Case permits BlockFree, BlockInactive, Item {
}