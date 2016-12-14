// package foo.bar

import java.util.logging.Logger
import java.io.IOException

import javafx.fxml.FXMLLoader
import javafx.stage.Stage
import javafx.scene.{Scene, Node, Parent}
import javafx.scene.layout.{BorderPane, AnchorPane}
import javafx.scene.control.MenuBar

import javafx.event.{ActionEvent, EventHandler}
import javafx.animation.TranslateTransition
import javafx.util.Duration


object StageContaner
class StageContaner(val stage: Stage) {
	val rootPane = new BorderPane
	val mainPane = new AnchorPane

		// stage.initStyle(StageStyle.TRANSPARENT)
	stage.getProperties.put(StageContaner, this)
	// stage.setMaximized(true)

	stage.setScene(new Scene(rootPane))
	rootPane.setCenter(mainPane)

	def show(firstScene: SceneController) = {
		moveScene(firstScene)
		stage.show
	}

	def moveScene(next: SceneController) = {
		addMainPane(next)
		setMainMenuBar(next.createMainMenu)
		next.init

		val prevOp = if(mainPane.getChildren.size <= 1) None
								 else Some(mainPane.getChildren.get(mainPane.getChildren.size-2))

		animateMove(next, prevOp)
	}

	private def addMainPane(node: SceneController) = {
		mainPane.getChildren.add(node)
		AnchorPane.setBottomAnchor(node, 0)
		AnchorPane.setLeftAnchor(node, 0)
		AnchorPane.setRightAnchor(node, 0)
		AnchorPane.setTopAnchor(node, 0)
	}

	private def animateMove(next: Node, prevOp: Option[Node]) = {
		val duration = Duration.seconds(1)
		val width = mainPane.getLayoutBounds.getWidth
		; {
			val slidIn = new TranslateTransition(duration, next)
			slidIn.setFromX(width)
			slidIn.setToX(0)
			slidIn.play
		}
		prevOp.foreach(prev => {
			val slidOut = new TranslateTransition(duration, prev)
			slidOut.setToX(-width)
			slidOut.setOnFinished(_ => mainPane.getChildren.remove(prev))
			slidOut.play
		})
	}

	def setMainMenuBar(nextOp: Option[MenuBar]) = {
		Option(rootPane.getTop.asInstanceOf[MenuBar]).foreach(_.setUseSystemMenuBar(false))
		nextOp.foreach(_.setUseSystemMenuBar(true))

		nextOp match {
			case Some(menuBar) => rootPane.setTop(menuBar)
			case None => rootPane.setTop(null)
		}
	}
}

trait RootedController {
	val loader = new FXMLLoader(location)
	loader.setRoot(this)
	loader.setController(this)
	try loader.load
	catch { case e: IOException =>
		Logger.getAnonymousLogger.warning("check fx:root, fx:controller attribute")
		throw new RuntimeException("fxml loader can't load with location("+location+").", e)
	}

	def prefix = "C"

	def fileName = {
		val className = getClass.getSimpleName
		val lastIndex = className.lastIndexOf(prefix)
		if(lastIndex < 0) throw new RuntimeException("class name("+className+") doesn't include prefix("+prefix+").")
		val fileName = className.substring(0, lastIndex) + ".fxml"
		fileName
	}

	def location = {
		val location = getClass.getResource(fileName)
		if(location == null) throw new RuntimeException("class loader("+getClass+") can't find file with name ("+fileName+").")
		location
	}
}

trait SceneController extends Parent with RootedController with StagedNode {
	def createMainMenu: Option[MenuBar] = None
	def init = {}
}

trait StagedNode extends Node {
	def getSceneNonNull = Option(getScene)
		.getOrElse(throw new RuntimeException("this node("+this+") isn't part of a scene"))

	def getWindowNonNull = Option(getSceneNonNull.getWindow)
		.getOrElse(throw new RuntimeException("this scene("+getSceneNonNull+") isn't part of a window"))

	def getStageNonNull = Option(getWindowNonNull.asInstanceOf[Stage])
		.getOrElse(throw new RuntimeException("this window("+getWindowNonNull+") isn't a Stage"))

	def getStageContaner = {
		val value = Option(getWindowNonNull.getProperties.get(StageContaner))
			.getOrElse(throw new RuntimeException("this window("+getWindowNonNull+") isn't a StageContaner"))
		Option(value.asInstanceOf[StageContaner])
			.getOrElse(throw new RuntimeException("this window("+getWindowNonNull+") isn't a StageContaner"))
	}
}
