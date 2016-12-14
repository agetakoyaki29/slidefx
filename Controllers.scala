// package foo.bar

import java.io.IOException
import java.util.logging.Logger

import javafx.fxml.FXMLLoader


trait RootedController {
	val loader = new FXMLLoader(location)
	loader.setRoot(this)
	loader.setController(this)
	try loader.load
	catch { case e: IOException =>
		Logger.getAnonymousLogger.warning("check fx:root, fx:controller attribute")
		throw new RootedControllerException("fxml loader can't load with location("+location+").", e)
	}

	def prefix = "C"

	def fileName = {
		val className = getClass.getSimpleName
		val lastIndex = className.lastIndexOf(prefix)
		if(lastIndex < 0) throw new RootedControllerException("class name("+className+") doesn't include prefix("+prefix+").")
		val fileName = className.substring(0, lastIndex) + ".fxml"
		fileName
	}

	def location = {
		val location = getClass.getResource(fileName)
		if(location == null) throw new RootedControllerException("class loader("+getClass+") can't find file with name ("+fileName+").")
		location
	}
}

class RootedControllerException(message: String = null, cause: Throwable = null) extends RuntimeException(message, cause)
