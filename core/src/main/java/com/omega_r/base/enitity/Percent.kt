package com.omega_r.base.enitity

import com.omega_r.base.enitity.Percent.Companion.DEFAULT_PERCENT_MODEL
import com.omega_r.base.enitity.Percent.Companion.FLOAT_PERCENT_MODEL
import java.io.Serializable

/**
 * Created by Anton Knyazev on 20.07.2023.
 * Copyright (c) 2023 Omega https://omega-r.com
 */
@JvmInline
value class Percent private constructor(private val value: Float): Serializable {

    companion object {

        val FLOAT_PERCENT_MODEL = PercentModel(0f, 1f)

        val DEFAULT_PERCENT_MODEL = PercentModel(0, 100)

        val MAX = Percent(1.0f)
        val MIN = Percent(0.0f)

        fun calc(current: Number, max: Number): Percent = (current.toFloat() / max.toFloat()).toPercent(FLOAT_PERCENT_MODEL)

    }

    constructor(value: Float, model: PercentModel) : this(value, model.min, model.max)


    constructor(value: Number, min: Number, max: Number): this(value.toFloat(), min.toFloat(), max.toFloat())

    constructor(value: Float, min: Float, max: Float) : this(
        when {
            value < min -> MIN.value
            value > max -> MAX.value
            else -> (value - min) / (max - min)
        }
    )

    fun toInt(model: PercentModel = DEFAULT_PERCENT_MODEL): Int = toFloat(model).toInt()

    fun toInt(min: Int, max: Int): Int = toFloat(min.toFloat(), max.toFloat()).toInt()

    fun toFloat(model: PercentModel = FLOAT_PERCENT_MODEL): Float = toFloat(model.min, model.max)

    fun toFloat(min: Float, max: Float): Float = when {
        value > MAX.value -> max
        value < MIN.value -> min
        else -> value * (max - min) + min
    }

    fun isMin() = value <= MIN.value

    fun isMax() = value >= MAX.value

    operator fun compareTo(percent: Percent): Int {
        return value.compareTo(percent.value)
    }

    operator fun times(number: Number): Percent = Percent(value * number.toFloat())

    operator fun div(number: Number): Percent = Percent(value / number.toFloat())

    operator fun times(percent: Percent): Percent = Percent(value * percent.value)

    operator fun div(percent: Percent): Percent = Percent(value / percent.value)

    operator fun plus(percent: Percent): Percent = Percent(value + percent.value)

    operator fun minus(percent: Percent): Percent = Percent(value - percent.value)

    override fun toString(): String = "${value * 100}%"

}

data class PercentModel(val min: Float, val max: Float) {
    constructor(min: Number, max: Number) : this(min.toFloat(), max.toFloat())
}

fun Number.toPercent(model: PercentModel) = Percent(toFloat(), model)

fun Double.toPercent(model: PercentModel = FLOAT_PERCENT_MODEL) = Percent(toFloat(), model)

fun Int.toPercent(model: PercentModel = DEFAULT_PERCENT_MODEL) = Percent(toFloat(), model)

fun Long.toPercent(model: PercentModel = DEFAULT_PERCENT_MODEL) = Percent(toFloat(), model)

fun Number.toPercent(min: Number, max: Number) = Percent(this, min, max)

fun Float.toPercent(model: PercentModel = FLOAT_PERCENT_MODEL) = Percent(this, model)

fun Float.toPercent(min: Float, max: Float) = Percent(this, min, max)