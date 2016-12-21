package com.github.agetakoyaki29.scalafx.animation

import javafx.animation.Interpolator


class SineInterpolator(power: Double = 0.2) extends Interpolator {
	override def curve(t: Double) = t - power*Math.sin(2*Math.PI*t)
}
