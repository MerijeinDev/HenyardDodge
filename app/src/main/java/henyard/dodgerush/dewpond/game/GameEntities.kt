package henyard.dodgerush.dewpond.game

import android.graphics.Bitmap

/** High-level behaviour category of a spawned entity. */
enum class EntityCategory { OBSTACLE, COLLECTIBLE, POWERUP }

/** Concrete entity kinds mapped to art. */
enum class EntityKind(val category: EntityCategory) {
    BARREL(EntityCategory.OBSTACLE),
    HAY(EntityCategory.OBSTACLE),
    TOOL_1(EntityCategory.OBSTACLE),
    TOOL_2(EntityCategory.OBSTACLE),
    FOX(EntityCategory.OBSTACLE),

    COIN(EntityCategory.COLLECTIBLE),
    GRAIN(EntityCategory.COLLECTIBLE),

    SHIELD(EntityCategory.POWERUP),
    MAGNET(EntityCategory.POWERUP),
    HEART(EntityCategory.POWERUP),
    FEATHER(EntityCategory.POWERUP),
}

/**
 * A single moving thing in the arena. Positions are in view pixels and refer
 * to the entity centre. Collision uses a simple circle of [radius].
 */
class Entity(
    val kind: EntityKind,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float,
    val bitmap: Bitmap,
) {
    var alive: Boolean = true
    var rotation: Float = 0f
    var rotationSpeed: Float = 0f
    /** Telegraph timer (seconds) before an obstacle becomes active/visible falling. */
    var telegraph: Float = 0f

    val category: EntityCategory get() = kind.category
}
