package fr.uge.backpack.model;

/**
 * Represents an inactive or locked cell in the backpack grid. These cells
 * cannot hold items unless unlocked.
 */
public record BlockInactive() implements Case {
}