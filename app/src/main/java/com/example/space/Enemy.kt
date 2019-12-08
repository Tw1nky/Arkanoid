package com.example.space

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import java.util.*


class Enemy(context: Context, row: Int, column: Int, screenX: Int, screenY: Int) {
    var width = screenX / 25f
    private var height = screenY / 25f
    private val padding = screenX / 45

    var position = RectF(
        column * (width + padding),
        200 + row * (width + padding / 4),
        column * (width + padding) + width,
        100 + row * (width + padding / 4) + height
    )

    private var speed = 90f
    private val left = 1
    private val right = 2
    private var shipMoving = right

    var isVisible = true

    companion object {
        var bitmap1: Bitmap? = null
        var bitmap2: Bitmap? = null

        var numberEnemy = 0
    }

    init {
        val bitmap11 = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.invader1
        )

        val bitmap22 = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.invader2
        )

        bitmap1 = Bitmap.createScaledBitmap(
            bitmap11,
            (width.toInt()),
            (height.toInt()),
            false
        )

        bitmap2 = Bitmap.createScaledBitmap(
            bitmap22,
            (width.toInt()),
            (height.toInt()),
            false
        )

        numberEnemy++
    }

    fun update(fps: Long) {
        if (shipMoving == left) {
            position.left -= speed / fps
        }

        if (shipMoving == right) {
            position.left += speed / fps
        }

        position.right = position.left + width
    }

    fun dropDownAndReverse() {
        shipMoving = if (shipMoving == left) {
            right
        } else {
            left
        }

        position.top += height
        position.bottom += height

        if (speed < 250f)
            speed += 2.5f
    }

    fun takeAim(
        playerShipX: Float,
        playerShipLength: Float,
        waves: Int
    )
            : Boolean {

        val generator = Random()
        var randomNumber: Int

        if (playerShipX + playerShipLength > position.left &&
            playerShipX + playerShipLength < position.left + width ||
            playerShipX > position.left && playerShipX < position.left + width
        ) {


            randomNumber = generator.nextInt(100 * numberEnemy) / waves
            if (randomNumber == 0) {
                return true
            }

        }

        randomNumber = generator.nextInt(150 * numberEnemy)
        return randomNumber == 0

    }
}