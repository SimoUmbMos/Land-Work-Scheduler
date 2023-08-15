package com.simosc.landworkscheduler

import androidx.compose.ui.graphics.Color
import com.simosc.landworkscheduler.domain.extension.invert
import com.simosc.landworkscheduler.domain.extension.parseColor
import com.simosc.landworkscheduler.domain.extension.parseColorAbgr
import com.simosc.landworkscheduler.domain.extension.toAbgrString
import com.simosc.landworkscheduler.domain.extension.toArgbString
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ColorUnitTest {
    @Test
    fun parseColor_isCorrect() {
        Color.Red.let { color ->
            val string1 = color.toArgbString()
            val string2 = color.toAbgrString()
            val color1 = Color.parseColor(string1)
            val color2 = Color.parseColorAbgr(string2)
            val color3 = Color.parseColor("ffff0000")
            val color4 = Color.parseColor("ff0000")
            val color5 = Color.parseColor("ff00")
            val color6 = Color.parseColor("f00")

            println("")
            println("Test: parseColor_isCorrect")
            println("color: $color")
            println("color1: $color1")
            println("color2: $color2")
            println("color3: $color3")
            println("color4: $color4")
            println("color5: $color5")
            println("color6: $color6")
            println("")
            println("string1: $string1")
            println("string2: $string2")
            println("")

            assertEquals(color, color1)
            assertEquals(color, color2)
            assertEquals(color, color3)
            assertEquals(color, color4)
            assertEquals(color, color5)
            assertEquals(color, color6)
        }
    }

    @Test
    fun invertColor_isCorrect(){
        val colorSeed = Color(red = 1f, green = 0f, blue = 0f)
        val colorSeedInverted = Color(red = 0f, green = 1f, blue = 1f)

        val colorInverted = colorSeed.invert()
        val colorNormalized = colorInverted.invert()

        println("")
        println("Test: invertColor_isCorrect")
        println("colorSeed: $colorSeed")
        println("colorSeedInverted: $colorSeedInverted")
        println("colorInverted: $colorInverted")
        println("colorNormalized: $colorNormalized")
        println("")

        assertNotEquals(colorSeed,colorInverted)
        assertNotEquals(colorSeedInverted,colorNormalized)

        assertEquals(colorSeed,colorNormalized)
        assertEquals(colorSeedInverted,colorInverted)
    }
}