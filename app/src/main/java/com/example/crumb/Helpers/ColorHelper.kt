package com.example.crumb.Helpers

import android.graphics.Color
import kotlin.random.Random

class ColorHelper {

    fun getColors(size: Int): IntArray {
        val colorsNeeded = size - 1
        val list = ArrayList<Int>()
            if (colorsNeeded <= 1) {
                val colors = randomColor(null)
                list.add(colors[0])
                return list.toIntArray()
            } else {
                while (list.size < colorsNeeded) {
                    val colors : IntArray
                    if (!list.isEmpty()) {
                        colors = randomColor(list.last()-1)
                    }else{
                        colors = randomColor(null)
                    }
                    list.add(colors[0])
                    if(list.size < colorsNeeded) {
                        list.add(colors[1])
                    }

                }
                while (list.size > colorsNeeded) {
                    list.remove(list.last())
                }
            }
        return list.toIntArray()
    }

    fun aRandomColor() : Int {
        val alpha = 200
        val red = Random.nextInt(25, 255)
        val green = Random.nextInt(25, 255)
        val blue = Random.nextInt(25, 255)
        return   Color.argb(alpha, red, green, blue)
    }

    fun randomColor(previousColor: Int?): IntArray {
        val colorArray = IntArray(2)
        val alpha = 200
        val red = Random.nextInt(25, 255)
        val green = Random.nextInt(25, 255)
        val blue = Random.nextInt(25, 255)
        colorArray[0] = Color.argb(alpha, red, green, blue)
        if (previousColor != null) {
           colorArray[0] = compareColors(previousColor,colorArray[0])
        }
        colorArray[1] = getCompliment(colorArray[0])
        return colorArray
    }

    fun getCompliment(col: Int): Int {
        val alpha = 200
        val red = 255 - Color.red(col)
        val green = 255 - Color.green(col)
        val blue = 255 - Color.blue(col)
        return Color.argb(alpha, red, green, blue)
    }

    fun compareColors(old : Int, new : Int) : Int{
        val alpha = 200
        val red = adjustColor(Color.red(old),Color.red(new))
        val green = adjustColor(Color.green(old),Color.green(new))
        val blue = adjustColor(Color.blue(old),Color.blue(new))
        return Color.argb(alpha,red,green,blue)
    }

    fun adjustColor(old : Int, new : Int) : Int{
        val fromMin = new
        val fromMax = 255 - new
        val min = Math.min(fromMax,fromMin)
        if(Math.abs(new - old) < 25){
            when(min){
                fromMin -> new + Random.nextInt(fromMin,fromMax)
                fromMax -> new - Random.nextInt(fromMax,fromMin)
            }
        }
        return new
    }


}