package com.example.space

import android.app.Activity
import android.graphics.Point
import android.os.Bundle
import android.view.WindowManager

class GameActivity : Activity() {
    private var gameView: GameView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)

        gameView = GameView(this, size)
        setContentView(gameView)
    }

    override fun onResume() {
        super.onResume()
        gameView?.resume()
    }

    override fun onPause() {
        super.onPause()
        gameView?.pause()
    }

}