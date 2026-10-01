package com.example.stickmanalive

import android.graphics.Color

data class StickmanData(
    var name: String = "Bob",
    var skinColor: Int = Color.BLACK,
    var headRadius: Float = 40f,
    var torsoLength: Float = 100f,
    var armLength: Float = 70f,
    var legLength: Float = 80f
)
