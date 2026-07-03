package henyard.dodgerush.dewpond.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import henyard.dodgerush.dewpond.R
import kotlin.math.hypot
import kotlin.random.Random

/**
 * Core arcade surface for Henyard Dodge.
 *
 * The player chicken follows the finger inside the pen. Obstacles fall from the
 * top and cost a life on contact; coins/grain add score; shield/magnet/heart are
 * power-ups. Survive the timer to win.
 */
class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    interface Listener {
        fun onScoreChanged(score: Int)
        fun onLivesChanged(lives: Int)
        fun onTimeChanged(secondsLeft: Int)
        fun onGameOver(result: GameResult)
    }

    var listener: Listener? = null

    // ---- Configurable level params ----
    var level: Int = 1
    var levelDurationMs: Long = 45_000L
    var heroDrawableRes: Int = R.drawable.hen_run

    /** Difficulty tuning for the current [level]; set by the host before start. */
    var tuning: Levels.Tuning = Levels.tuningFor(1)

    /** Ability granted by the equipped skin. */
    var skinAbility: SkinAbility = SkinAbility.NONE

    // ---- Runtime state ----
    @Volatile private var running = false
    @Volatile private var paused = false
    @Volatile private var finished = false
    private var thread: Thread? = null
    private var lastSecondShown = -1

    private var score = 0
    private var coins = 0
    private var lives = 3
    private var timeLeftMs = levelDurationMs

    // Player
    private var px = 0f
    private var py = 0f
    private var pr = 0f
    private var targetX = 0f
    private var targetY = 0f
    private var iframeMs = 0f
    private var shieldMs = 0f
    private var magnetMs = 0f
    private var slowMs = 0f

    // Skin-derived modifiers (resolved in setupWorld)
    private var hitboxScale = 1f
    private var toughCharge = false

    // Run stats (for achievements / result popup)
    private var grainCollected = 0
    private var hazardsDodged = 0
    private var foxDodged = 0
    private var shieldBlocks = 0
    private var hits = 0
    private var noHitMs = 0L
    private var longestNoHitMs = 0L

    // World
    private val playfield = RectF()
    private val entities = ArrayList<Entity>()
    private var elapsedMs = 0L

    // Spawn accumulators (seconds)
    private var obstacleTimer = 0f
    private var collectibleTimer = 0f
    private var powerupTimer = 0f

    // ---- Bitmaps ----
    private lateinit var bgBitmap: Bitmap
    private lateinit var playerBitmap: Bitmap
    private lateinit var spawnZone: Bitmap
    private lateinit var shieldBitmap: Bitmap
    private val kindBitmaps = HashMap<EntityKind, Bitmap>()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
    private val srcRect = Rect()
    private val dstRect = RectF()

    private var initialized = false

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    // region lifecycle
    override fun surfaceCreated(holder: SurfaceHolder) {
        startLoop()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (width == 0 || height == 0) return
        setupWorld(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopLoop()
    }

    fun pauseGame() { paused = true }
    fun resumeGame() { if (!finished) paused = false }

    private fun startLoop() {
        if (running) return
        running = true
        thread = Thread({ loop() }, "HenyardGameLoop").also { it.start() }
    }

    private fun stopLoop() {
        running = false
        try { thread?.join(500) } catch (_: InterruptedException) {}
        thread = null
    }
    // endregion

    private fun setupWorld(w: Int, h: Int) {
        val reqW = w
        bgBitmap = decodeScaledToWidth(R.drawable.bg_4, reqW)
        spawnZone = decodeScaledToWidth(R.drawable.spawn_zone, (w * 0.18f).toInt())
        shieldBitmap = decodeScaledToWidth(R.drawable.powerup_shield, (w * 0.26f).toInt())

        pr = w * 0.085f
        playerBitmap = decodeScaledToWidth(heroDrawableRes, (pr * 2.1f).toInt())

        loadKind(EntityKind.BARREL, R.drawable.obstacle_barrel, w * 0.17f)
        loadKind(EntityKind.HAY, R.drawable.obstacle_hay, w * 0.19f)
        loadKind(EntityKind.TOOL_1, R.drawable.obstacle_tool_1, w * 0.14f)
        loadKind(EntityKind.TOOL_2, R.drawable.obstacle_tool_2, w * 0.14f)
        loadKind(EntityKind.FOX, R.drawable.obstacle_fox, w * 0.34f)
        loadKind(EntityKind.COIN, R.drawable.coin, w * 0.11f)
        loadKind(EntityKind.GRAIN, R.drawable.collect_grain, w * 0.14f)
        loadKind(EntityKind.SHIELD, R.drawable.powerup_shield, w * 0.13f)
        loadKind(EntityKind.MAGNET, R.drawable.powerup_magnet, w * 0.13f)
        loadKind(EntityKind.HEART, R.drawable.heart_active, w * 0.12f)
        kindBitmaps[EntityKind.FEATHER] = bitmapFromVector(R.drawable.powerup_slow, (w * 0.13f).toInt())

        // Skin-derived modifiers.
        hitboxScale = if (skinAbility == SkinAbility.SLIM) 0.75f else 1f
        toughCharge = skinAbility == SkinAbility.TOUGH

        playfield.set(w * 0.10f, h * 0.14f, w * 0.90f, h * 0.94f)
        if (!initialized) {
            px = w / 2f
            py = h * 0.6f
            targetX = px
            targetY = py
            timeLeftMs = levelDurationMs
            initialized = true
            listener?.let { l ->
                post {
                    l.onScoreChanged(score); l.onLivesChanged(lives)
                    l.onTimeChanged((timeLeftMs / 1000).toInt())
                }
            }
        }
    }

    private fun loadKind(kind: EntityKind, res: Int, targetWidth: Float) {
        kindBitmaps[kind] = decodeScaledToWidth(res, targetWidth.toInt())
    }

    /** Rasterizes a vector drawable to a bitmap of [targetWidth], preserving aspect. */
    private fun bitmapFromVector(res: Int, targetWidth: Int): Bitmap {
        val drawable = androidx.core.content.ContextCompat.getDrawable(context, res)!!
        val w = targetWidth.coerceAtLeast(1)
        val ratio = if (drawable.intrinsicWidth > 0)
            drawable.intrinsicHeight.toFloat() / drawable.intrinsicWidth else 1f
        val h = (w * ratio).toInt().coerceAtLeast(1)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        drawable.setBounds(0, 0, w, h)
        drawable.draw(c)
        return bmp
    }

    private fun decodeScaledToWidth(res: Int, targetWidth: Int): Bitmap {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(resources, res, opts)
        var sample = 1
        if (targetWidth > 0 && opts.outWidth > targetWidth) {
            var half = opts.outWidth / 2
            while (half >= targetWidth) { sample *= 2; half /= 2 }
        }
        val decoded = BitmapFactory.decodeResource(
            resources, res, BitmapFactory.Options().apply { inSampleSize = sample },
        )
        if (targetWidth <= 0 || decoded.width == targetWidth) return decoded
        val ratio = targetWidth.toFloat() / decoded.width
        val targetHeight = (decoded.height * ratio).toInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(decoded, targetWidth, targetHeight, true)
        if (scaled != decoded) decoded.recycle()
        return scaled
    }

    // region loop
    private fun loop() {
        var last = System.nanoTime()
        while (running) {
            val now = System.nanoTime()
            var dt = (now - last) / 1_000_000_000f
            last = now
            if (dt > 0.05f) dt = 0.05f

            if (!paused && !finished && initialized) update(dt)

            val canvas = holder.lockCanvas() ?: continue
            try {
                synchronized(holder) { drawGame(canvas) }
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }
    // endregion

    private fun update(dt: Float) {
        val dtMs = (dt * 1000).toLong()
        elapsedMs += dtMs
        timeLeftMs -= dtMs

        // timers
        if (iframeMs > 0) iframeMs -= dt * 1000
        if (shieldMs > 0) shieldMs -= dt * 1000
        if (magnetMs > 0) magnetMs -= dt * 1000
        if (slowMs > 0) slowMs -= dt * 1000

        // longest no-hit streak
        noHitMs += dtMs
        if (noHitMs > longestNoHitMs) longestNoHitMs = noHitMs

        // player follows finger with easing
        px += (targetX - px) * (12f * dt).coerceAtMost(1f)
        py += (targetY - py) * (12f * dt).coerceAtMost(1f)
        px = px.coerceIn(playfield.left + pr, playfield.right - pr)
        py = py.coerceIn(playfield.top + pr, playfield.bottom - pr)

        spawnTick(dt)
        updateEntities(dt)
        checkTime()

        val sec = (timeLeftMs.coerceAtLeast(0) / 1000).toInt()
        if (sec != lastSecondShown) {
            lastSecondShown = sec
            listener?.let { l -> post { l.onTimeChanged(sec) } }
        }
    }

    private fun difficulty(): Float {
        val t = (elapsedMs / 1000f)
        return (t / 30f).coerceIn(0f, 1.5f)
    }

    private fun spawnTick(dt: Float) {
        val diff = difficulty()
        obstacleTimer -= dt
        collectibleTimer -= dt
        powerupTimer -= dt

        if (obstacleTimer <= 0f) {
            val base = (1.25f - 0.45f * diff).coerceAtLeast(0.55f) + Random.nextFloat() * 0.3f
            obstacleTimer = base / tuning.densityMul
            spawnObstacle(diff)
        }
        if (collectibleTimer <= 0f) {
            collectibleTimer = 0.85f + Random.nextFloat() * 0.6f
            spawnCollectible()
        }
        if (powerupTimer <= 0f) {
            powerupTimer = (7.5f + Random.nextFloat() * 4f) * tuning.bonusRarityMul
            spawnPowerup()
        }
    }

    private fun randomX(inset: Float): Float =
        Random.nextFloat() * (playfield.width() - 2 * inset) + playfield.left + inset

    private fun spawnObstacle(diff: Float) {
        val kind = tuning.hazards.randomOrNull() ?: EntityKind.HAY
        val bmp = kindBitmaps[kind] ?: return
        val r = bmp.width * 0.42f
        val speed = height * (0.42f + 0.18f * diff) * tuning.speedMul
        if (kind == EntityKind.FOX) {
            // Fox dashes horizontally across the pen.
            val fromLeft = Random.nextBoolean()
            val y = randomYBand()
            val e = Entity(
                kind = kind,
                x = if (fromLeft) -r else width + r,
                y = y,
                vx = if (fromLeft) speed * 1.4f else -speed * 1.4f,
                vy = 0f,
                radius = r * 0.7f,
                bitmap = bmp,
            )
            entities.add(e)
        } else {
            val x = randomX(r)
            val e = Entity(kind, x, -r, 0f, speed, r, bmp)
            e.spin = (Random.nextFloat() - 0.5f) * 120f
            e.telegraph = 0.5f
            entities.add(e)
        }
    }

    private fun randomYBand(): Float =
        Random.nextFloat() * (playfield.height() * 0.5f) + playfield.top + playfield.height() * 0.15f

    private fun spawnCollectible() {
        val kind = if (Random.nextInt(100) < 70) EntityKind.COIN else EntityKind.GRAIN
        val bmp = kindBitmaps[kind] ?: return
        val r = bmp.width * 0.42f
        val x = randomX(r)
        val speed = height * (0.30f + 0.06f * difficulty())
        entities.add(Entity(kind, x, -r, 0f, speed, r, bmp))
    }

    private fun spawnPowerup() {
        val roll = Random.nextInt(4)
        val kind = when (roll) {
            0 -> EntityKind.SHIELD
            1 -> EntityKind.MAGNET
            2 -> EntityKind.FEATHER
            else -> EntityKind.HEART
        }
        val bmp = kindBitmaps[kind] ?: return
        val r = bmp.width * 0.42f
        val x = randomX(r)
        entities.add(Entity(kind, x, -r, 0f, height * 0.28f, r, bmp))
    }

    private fun updateEntities(dt: Float) {
        // Slow-motion feather halves entity speed (the player still follows normally).
        val moveDt = if (slowMs > 0) dt * 0.5f else dt
        val powerMagnet = magnetMs > 0
        val passiveMagnet = skinAbility == SkinAbility.MAGNET
        val it = entities.iterator()
        while (it.hasNext()) {
            val e = it.next()
            if (e.telegraph > 0f) {
                e.telegraph -= dt
                continue
            }

            val isCollectible = e.category == EntityCategory.COLLECTIBLE
            val distNow = hypot(px - e.x, py - e.y)
            val magnetPulling = isCollectible &&
                (powerMagnet || (passiveMagnet && distNow < height * 0.33f))
            if (magnetPulling) {
                val dx = px - e.x
                val dy = py - e.y
                val d = distNow.coerceAtLeast(1f)
                val pull = if (powerMagnet) height * 0.9f else height * 0.55f
                e.x += dx / d * pull * moveDt
                e.y += dy / d * pull * moveDt
            } else {
                e.x += e.vx * moveDt
                e.y += e.vy * moveDt
            }
            e.rotation += e.spin * moveDt

            // off-screen cull (an obstacle that leaves the pen was dodged)
            if (e.y - e.radius > height || e.x < -e.radius * 2 || e.x > width + e.radius * 2) {
                if (e.category == EntityCategory.OBSTACLE) {
                    hazardsDodged++
                    if (e.kind == EntityKind.FOX) foxDodged++
                }
                it.remove()
                continue
            }

            // collision with player
            val dist = hypot(px - e.x, py - e.y)
            if (dist < pr * hitboxScale + e.radius * 0.8f) {
                when (e.category) {
                    EntityCategory.OBSTACLE -> {
                        if (shieldMs > 0) {
                            shieldBlocks++
                            it.remove()
                        } else if (iframeMs <= 0) {
                            if (toughCharge) {
                                // Tough Hat absorbs the first hit each level.
                                toughCharge = false
                                iframeMs = 1200f
                                it.remove()
                            } else {
                                lives -= 1
                                hits++
                                noHitMs = 0
                                iframeMs = 1200f
                                listener?.let { l -> post { l.onLivesChanged(lives) } }
                                it.remove()
                                if (lives <= 0) endGame(false)
                            }
                        }
                    }
                    EntityCategory.COLLECTIBLE -> {
                        if (e.kind == EntityKind.COIN) {
                            score += 10
                            coins++
                        } else {
                            score += 5
                            grainCollected++
                        }
                        listener?.let { l -> post { l.onScoreChanged(score) } }
                        it.remove()
                    }
                    EntityCategory.POWERUP -> {
                        when (e.kind) {
                            EntityKind.SHIELD ->
                                shieldMs = if (skinAbility == SkinAbility.LONG_SHIELD) 12_000f else 6_000f
                            EntityKind.MAGNET -> magnetMs = 6_000f
                            EntityKind.FEATHER -> slowMs = 2_500f
                            EntityKind.HEART -> if (lives < 3) {
                                lives += 1
                                listener?.let { l -> post { l.onLivesChanged(lives) } }
                            }
                            else -> {}
                        }
                        it.remove()
                    }
                }
            }
        }
    }

    private fun checkTime() {
        if (timeLeftMs <= 0 && !finished) endGame(true)
    }

    private fun endGame(won: Boolean) {
        if (finished) return
        finished = true
        val stars = when {
            !won -> 0
            score >= 400 -> 3
            score >= 200 -> 2
            else -> 1
        }
        val result = GameResult(
            score = score,
            coinsCollected = coins,
            won = won,
            stars = stars,
            grain = grainCollected,
            hazardsDodged = hazardsDodged,
            foxDodged = foxDodged,
            shieldBlocks = shieldBlocks,
            hits = hits,
            survivedSeconds = (elapsedMs / 1000).toInt(),
            longestNoHitSeconds = (longestNoHitMs / 1000).toInt(),
        )
        listener?.let { l -> post { l.onGameOver(result) } }
    }

    // region draw
    private fun drawGame(canvas: Canvas) {
        canvas.drawColor(Color.rgb(0xF0, 0xC9, 0x7A))
        if (!initialized) return

        // background stretched to fill
        srcRect.set(0, 0, bgBitmap.width, bgBitmap.height)
        dstRect.set(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawBitmap(bgBitmap, srcRect, dstRect, paint)

        // telegraphs first
        for (e in entities) {
            if (e.telegraph > 0f) {
                val a = ((e.telegraph.coerceIn(0f, 0.5f) / 0.5f) * 200).toInt()
                paint.alpha = 255 - a
                drawCentered(canvas, spawnZone, e.x, playfield.top + spawnZone.height / 2f)
                paint.alpha = 255
            }
        }

        // entities
        for (e in entities) {
            if (e.telegraph > 0f) continue
            if (e.rotation != 0f) {
                canvas.save()
                canvas.rotate(e.rotation, e.x, e.y)
                drawCentered(canvas, e.bitmap, e.x, e.y)
                canvas.restore()
            } else {
                drawCentered(canvas, e.bitmap, e.x, e.y)
            }
        }

        // player (blink during i-frames)
        val blink = iframeMs > 0 && ((elapsedMs / 100) % 2 == 0L)
        if (!blink) {
            drawCentered(canvas, playerBitmap, px, py)
        }
        if (shieldMs > 0) {
            paint.alpha = 150
            drawCentered(canvas, shieldBitmap, px, py)
            paint.alpha = 255
        }

        // Slow-motion: cool blue wash over the whole pen.
        if (slowMs > 0) {
            canvas.drawColor(Color.argb(60, 40, 120, 220))
        }
    }

    private fun drawCentered(canvas: Canvas, bmp: Bitmap, cx: Float, cy: Float) {
        canvas.drawBitmap(bmp, cx - bmp.width / 2f, cy - bmp.height / 2f, paint)
    }
    // endregion

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                targetX = event.x
                targetY = event.y
            }
        }
        return true
    }
}

/** Snapshot of a finished run, delivered to the host for scoring/persistence. */
data class GameResult(
    val score: Int,
    val coinsCollected: Int,
    val won: Boolean,
    val stars: Int,
    val grain: Int,
    val hazardsDodged: Int,
    val foxDodged: Int,
    val shieldBlocks: Int,
    val hits: Int,
    val survivedSeconds: Int,
    val longestNoHitSeconds: Int,
)
