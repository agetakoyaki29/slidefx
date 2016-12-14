// package foo.bar

import javafx.stage.Stage
import javafx.scene.{Scene, Node, Parent}
import javafx.scene.layout.{BorderPane, AnchorPane}
import javafx.scene.control.MenuBar

import javafx.event.{ActionEvent, EventHandler}
import javafx.animation.TranslateTransition
import javafx.util.Duration


object StageContaner

class StageContaner(val stage: Stage) {
	private val centerPane = new AnchorPane
	private val root = new BorderPane(centerPane)

	private val mainNodes = centerPane.getChildren
	private val menuBarProperty = root.topProperty

	; {
		// stage.initStyle(StageStyle.TRANSPARENT)
		// stage.setMaximized(true)
		stage.getProperties.put(StageContaner, this)
		stage.setScene(new Scene(root));
	}

	def show(firstScene: SceneController) = {
		moveScene(firstScene)
		stage.show
	}

	def moveScene(next: SceneController) = {
		addMainPane(next)
		setMainMenuBar(next.createMainMenu)
		next.init

		val prevOp = if(mainNodes.size <= 1) None
								 else Some(mainNodes.get(mainNodes.size-2))

		animateMove(next, prevOp)
	}

	private def addMainPane(node: SceneController) = {
		mainNodes.add(node)
		AnchorPane.setBottomAnchor(node, 0)
		AnchorPane.setLeftAnchor(node, 0)
		AnchorPane.setRightAnchor(node, 0)
		AnchorPane.setTopAnchor(node, 0)
	}

	def setMainMenuBar(nextOp: Option[MenuBar]) = {
		Option(menuBarProperty.get.asInstanceOf[MenuBar]).foreach(_.setUseSystemMenuBar(false))
		nextOp.foreach(_.setUseSystemMenuBar(true))

		nextOp match {
			case Some(menuBar) => menuBarProperty.set(menuBar)
			case None => menuBarProperty.set(null)
		}
	}

	private def animateMove(next: Node, prevOp: Option[Node]) = {
		val duration = Duration.seconds(1)
		val interpolator = new SineInterpolator(.3)
		val width = centerPane.getLayoutBounds.getWidth

		; {
			val slidIn = new TranslateTransition(duration, next)
			slidIn.setFromX(width)
			slidIn.setToX(0)
			slidIn.setInterpolator(interpolator)
			slidIn.play
		}
		prevOp.foreach(prev => {
			// prev.setClip(prev.getParent)
			val slidOut = new TranslateTransition(duration, prev)
			slidOut.setToX(-width)
			slidOut.setInterpolator(interpolator)
			slidOut.setOnFinished(_ => mainNodes.remove(prev))
			slidOut.play
		})
	}
}


trait SceneController extends Parent with RootedController with StagedNode {
	def createMainMenu: Option[MenuBar] = None
	def init = {}
}


trait StagedNode extends Node {
	def getSceneNonNull = Option(getScene)
		.getOrElse(throw new StagedNodeException("this node("+this+") isn't part of a scene"))

	def getWindowNonNull = Option(getSceneNonNull.getWindow)
		.getOrElse(throw new StagedNodeException("this scene("+getSceneNonNull+") isn't part of a window"))

	def getStageNonNull = Option(getWindowNonNull.asInstanceOf[Stage])
		.getOrElse(throw new StagedNodeException("this window("+getWindowNonNull+") isn't a Stage"))

	def getStageContanerNonNull = {
		val value = Option(getWindowNonNull.getProperties.get(StageContaner))
			.getOrElse(throw new StagedNodeException("this window("+getWindowNonNull+") isn't a StageContaner"))
		Option(value.asInstanceOf[StageContaner])
			.getOrElse(throw new StagedNodeException("this window("+getWindowNonNull+") isn't a StageContaner"))
	}

	def isOnScene = try {getSceneNonNull; true} catch {case e: StagedNodeException => false}
	def isOnWindow = try {getWindowNonNull; true} catch {case e: StagedNodeException => false}
	def isOnStage = try {getStageNonNull; true} catch {case e: StagedNodeException => false}
	def isOnStageContaner = try {getStageContanerNonNull; true} catch {case e: StagedNodeException => false}
}

class StagedNodeException(message: String = null, cause: Throwable = null) extends RuntimeException(message, cause)
