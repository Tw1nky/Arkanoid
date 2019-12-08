package com.example.space

import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceView

class GameView(context: Context, private val size: Point) : SurfaceView(context), Runnable {

    private val soundPlayer = Sounds(context)

    private val gameThread = Thread(this)

    private var playing = false

    private var paused = true

    private var canvas: Canvas = Canvas()
    private val paint: Paint = Paint()

    private var ship: Ship = Ship(context, size.x, size.y)

    private val enemy = ArrayList<Enemy>()
    private var numEnemy = 0

    private val bricks = ArrayList<Bricks>()
    private var numBricks: Int = 0

    private var playerBullet = Bullet(size.y, 1200f, 40f)

    private val enemyBullets = ArrayList<Bullet>()
    private var nextBullet = 0
    private val maxEnemyBullets = 10

    private var score = 0

    private var waves = 1

    private var lives = 3

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "Kotlin Invaders",
        Context.MODE_PRIVATE
    )

    var highScore = prefs.getInt("highScore", 0)

    private var menaceInterval: Long = 1000

    private var uhOrOh: Boolean = false
    private var lastMenaceTime = System.currentTimeMillis()


    private fun prepareLevel() {
        Enemy.numberEnemy = 0
        numEnemy = 0
        for (column in 0..6) {
            for (row in 0..3) {
                enemy.add(
                    Enemy(
                        context,
                        row,
                        column,
                        size.x,
                        size.y
                    )
                )

                numEnemy++
            }
        }

        numBricks = 0
        for (shelterNumber in 0..4) {
            for (column in 0..18) {
                for (row in 0..8) {
                    bricks.add(
                        Bricks(
                            row,
                            column,
                            shelterNumber,
                            size.x,
                            size.y
                        )
                    )

                    numBricks++
                }
            }
        }

        for (i in 0 until maxEnemyBullets) {
            enemyBullets.add(Bullet(size.y))
        }
    }

    override fun run() {
        var fps: Long = 0

        while (playing) {

            val startFrameTime = System.currentTimeMillis()

            if (!paused) {
                update(fps)
            }

            draw()

            val timeThisFrame = System.currentTimeMillis() - startFrameTime
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame
            }

            if (!paused && ((startFrameTime - lastMenaceTime) > menaceInterval))
                menacePlayer()
        }
    }

    private fun menacePlayer() {
        if (uhOrOh) {
            soundPlayer.playSound(Sounds.uhID)

        } else {
            soundPlayer.playSound(Sounds.ohID)
        }

        lastMenaceTime = System.currentTimeMillis()
        uhOrOh = !uhOrOh

    }

    private fun update(fps: Long) {
        ship.update(fps)

        var bumped = false

        var lost = false

        for (i in enemy) {
            if (i.isVisible) {
                i.update(fps)
                if (i.takeAim(
                        ship.position.left,
                        ship.width,
                        waves
                    )
                ) {

                    if (enemyBullets[nextBullet].shoot(
                            i.position.left
                                    + i.width / 2,
                            i.position.top,
                            playerBullet.down
                        )
                    ) {

                        nextBullet++
                        if (nextBullet == maxEnemyBullets) {

                            nextBullet = 0
                        }
                    }
                }

                if (i.position.left > size.x - i.width
                    || i.position.left < 0
                ) {

                    bumped = true

                }
            }
        }

        if (playerBullet.isActive) {
            playerBullet.update(fps)
        }

        for (bullet in enemyBullets) {
            if (bullet.isActive) {
                bullet.update(fps)
            }
        }

        if (bumped) {

            for (i in enemy) {
                i.dropDownAndReverse()
                if (i.position.bottom >= size.y && i.isVisible) {
                    lost = true
                }
            }
        }

        if (playerBullet.position.bottom < 0) {
            playerBullet.isActive = false
        }

        for (bullet in enemyBullets) {
            if (bullet.position.top > size.y) {
                bullet.isActive = false
            }
        }

        if (playerBullet.isActive) {
            for (i in enemy) {
                if (i.isVisible) {
                    if (RectF.intersects(playerBullet.position, i.position)) {
                        i.isVisible = false

                        soundPlayer.playSound(Sounds.invaderExplodeID)
                        playerBullet.isActive = false
                        Enemy.numberEnemy--
                        score += 10
                        if (score > highScore) {
                            highScore = score
                        }

                        if (Enemy.numberEnemy == 0) {
                            paused = true
                            lives++
                            enemy.clear()
                            bricks.clear()
                            enemyBullets.clear()
                            prepareLevel()
                            waves++
                            break
                        }

                        break
                    }
                }
            }
        }

        for (bullet in enemyBullets) {
            if (bullet.isActive) {
                for (brick in bricks) {
                    if (brick.isVisible) {
                        if (RectF.intersects(bullet.position, brick.position)) {
                            bullet.isActive = false
                            brick.isVisible = false
                            soundPlayer.playSound(Sounds.damageShelterID)
                        }
                    }
                }
            }

        }

        if (playerBullet.isActive) {
            for (brick in bricks) {
                if (brick.isVisible) {
                    if (RectF.intersects(playerBullet.position, brick.position)) {
                        playerBullet.isActive = false
                        brick.isVisible = false
                        soundPlayer.playSound(Sounds.damageShelterID)
                    }
                }
            }
        }

        for (bullet in enemyBullets) {
            if (bullet.isActive) {
                if (RectF.intersects(ship.position, bullet.position)) {
                    bullet.isActive = false
                    lives--
                    soundPlayer.playSound(Sounds.playerExplodeID)

                    if (lives == 0) {
                        lost = true
                        break
                    }
                }
            }
        }

        if (lost) {
            paused = true
            lives = 3
            score = 0
            waves = 1
            enemy.clear()
            bricks.clear()
            enemyBullets.clear()
            prepareLevel()
        }
    }

    private fun draw() {
        if (holder.surface.isValid) {
            canvas = holder.lockCanvas()

            canvas.drawColor(
                Color.argb(
                    255,
                    153,
                    50,
                    204
                )
            )

            paint.color = Color.argb(
                255,
                0,
                255,
                0
            )

            canvas.drawBitmap(
                ship.bitmap, ship.position.left,
                ship.position.top
                , paint
            )


            for (i in enemy) {
                if (i.isVisible) {
                    if (uhOrOh) {
                        canvas.drawBitmap(
                            Enemy.bitmap1 ?: return,
                            i.position.left,
                            i.position.top,
                            paint
                        )
                    } else {
                        canvas.drawBitmap(
                            Enemy.bitmap2 ?: return,
                            i.position.left,
                            i.position.top,
                            paint
                        )
                    }
                }
            }

            for (brick in bricks) {
                if (brick.isVisible) {
                    canvas.drawRect(brick.position, paint)
                }
            }

            if (playerBullet.isActive) {
                canvas.drawRect(playerBullet.position, paint)
            }

            for (bullet in enemyBullets) {
                if (bullet.isActive) {
                    canvas.drawRect(bullet.position, paint)
                }
            }

            paint.color = Color.argb(255, 255, 255, 255)
            paint.textSize = 70f
            canvas.drawText("Score: $score   Lives: $lives Wave: $waves", 20f, 75f, paint)
            canvas.drawText("High score: $highScore", 20f, 150f, paint)
            holder.unlockCanvasAndPost(canvas)
        }
    }

    fun pause() {
        playing = false
        try {
            gameThread.join()
        } catch (e: InterruptedException) {
            Log.e("Error:", "joining thread")
        }

        val prefs = context.getSharedPreferences(
            "Kotlin Invaders",
            Context.MODE_PRIVATE
        )

        val oldHighScore = prefs.getInt("highScore", 0)

        if (highScore > oldHighScore) {
            val editor = prefs.edit()

            editor.putInt(
                "highScore", highScore
            )

            editor.apply()
        }
    }

    fun resume() {
        playing = true
        prepareLevel()
        gameThread.start()
    }

    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        when (motionEvent.action and MotionEvent.ACTION_MASK) {

            MotionEvent.ACTION_POINTER_DOWN,
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                paused = false

                if (motionEvent.y > size.y - size.y / 8) {
                    if (motionEvent.x > size.x / 2) {
                        ship.moving = Ship.right
                    } else {
                        ship.moving = Ship.left
                    }

                }

                if (motionEvent.y < size.y - size.y / 8) {
                    if (playerBullet.shoot(
                            ship.position.left + ship.width / 2f,
                            ship.position.top,
                            playerBullet.up
                        )
                    ) {

                        soundPlayer.playSound(Sounds.shootID)
                    }
                }
            }
            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_UP -> {
                if (motionEvent.y > size.y - size.y / 10) {
                    ship.moving = Ship.stopped
                }
            }

        }
        return true
    }

}