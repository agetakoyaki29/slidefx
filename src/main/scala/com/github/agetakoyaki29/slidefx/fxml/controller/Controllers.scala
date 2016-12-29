package com.github.agetakoyaki29.slidefx.fxml.controller

import java.io.IOException
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Tab


trait FXMLController {
	val prefix = "C"

	val filename = {
		val className = getClass.getSimpleName
		val lastIndex = className.lastIndexOf(prefix)
		if(lastIndex < 0) throw new IllegalStateException("class name("+className+") doesn't include prefix("+prefix+").")
		className.substring(0, lastIndex) + ".fxml"
	}

	val location = Option(getClass.getResource(filename))
			.getOrElse(throw new IllegalStateException("class loader("+getClass+") can't find file with name("+filename+")."))

	protected val loader = new FXMLLoader(location)
}


trait RootedController extends FXMLController {
	; {
		loader.setController(this)
		loader.setRoot(this)
		loader.load
	}
}


object LoadedController

trait LoadedController extends FXMLController {
	val root: Any = {
		loader.setController(this)
		// load
		try loader.load
		catch { case e: IOException =>
			throw new LoadedControllerException("fxml loader can't load with location("+location+").", e)
		}
		// get root
		loader.getRoot
	}
	; {
		// put controller to root
		(root match {
		  case node: Node => node.getProperties
		  case tab: Tab => tab.getProperties
		  case _ => ???
		}).put(LoadedController, this)
	}
}

class LoadedControllerException(message: String = null, cause: Throwable = null) extends RuntimeException(message, cause)

