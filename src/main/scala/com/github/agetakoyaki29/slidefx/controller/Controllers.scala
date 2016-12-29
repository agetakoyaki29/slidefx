package com.github.agetakoyaki29.slidefx.controller

import java.io.IOException
import javafx.fxml.FXMLLoader


trait RootedController {
	; {
		val loader = new FXMLLoader(getLocation)
		loader.setRoot(this)
		loader.setController(this)
		try loader.load
		catch { case e: IOException =>
			throw new RootedControllerException("fxml loader can't load with location("+getLocation+").", e)
		}
	}

	def getPrefix = "C"

	def getFileName = {
		val className = getClass.getSimpleName
		val lastIndex = className.lastIndexOf(getPrefix)
		if(lastIndex < 0) throw new RootedControllerException("class name("+className+") doesn't include prefix("+getPrefix+").")
		className.substring(0, lastIndex) + ".fxml"
	}

	def getLocation = Option(getClass.getResource(getFileName))
			.getOrElse(throw new RootedControllerException("class loader("+getClass+") can't find file with name("+getFileName+")."))
}

class RootedControllerException(message: String = null, cause: Throwable = null) extends RuntimeException(message, cause)
