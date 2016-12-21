package kana.editer1

import javafx.stage.Stage
import javafx.scene.{Scene, Node, Parent}
import javafx.scene.layout.{BorderPane, AnchorPane}
import javafx.scene.control.{MenuBar, Menu}
import javafx.animation.TranslateTransition
import javafx.util.Duration
import javafx.event.ActionEvent


object StageContaner

class StageContaner(val stage: Stage) {
	protected val centerPane = new AnchorPane
	private val root = new BorderPane(centerPane)

	private val mainNodes = centerPane.getChildren
	protected val menuBarProperty = root.topProperty

	protected val defaultMenuBar = {
	  val menu = new Menu()
	  menu.setDisable(true)
	  new MenuBar(menu)
	}
	
	; {
	  menuBarProperty.set(defaultMenuBar)
	  mainNodes.add(new AnchorPane with SceneController)
	}
	; {
		// stage.initStyle(StageStyle.TRANSPARENT)  // initStyle
		// stage.setMaximized(true)
		stage.getProperties.put(StageContaner, this) // put this to stage properties
		stage.setScene(new Scene(root));
	}

	def show(firstScene: SceneController) = {
		moveScene(firstScene)
		stage.show
	}

	def moveScene(next: SceneController) = {
		// get previous before add
		val perv = getNow
		// add and expand
		addNow(next)
		// call staged
		perv.unStaged(this)
		next.staged(this)

		animateMove(next, perv)
	}

	def setMainMenuBar(nextOp: Option[MenuBar]) = {
		Option(menuBarProperty.get.asInstanceOf[MenuBar]).foreach(_.setUseSystemMenuBar(false))
		nextOp.foreach(_.setUseSystemMenuBar(true))

		nextOp match {
			case Some(menuBar) => menuBarProperty.set(menuBar)
			case None => menuBarProperty.set(defaultMenuBar)
		}
	}

	private def addNow(node: SceneController) = {
	   mainNodes.add(node)
	   expandAnchorChild(node)
	}
	
	protected def getNow = mainNodes.get(mainNodes.size-1).asInstanceOf[SceneController]

	private def expandAnchorChild(node: SceneController) = {
		AnchorPane.setBottomAnchor(node, 0)
		AnchorPane.setLeftAnchor(node, 0)
		AnchorPane.setRightAnchor(node, 0)
		AnchorPane.setTopAnchor(node, 0)
	}

	private def animateMove(next: Node, prev: Node) = {
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
		; {
			// prev.setClip(prev.getParent)
			val slidOut = new TranslateTransition(duration, prev)
			slidOut.setToX(-width)
			slidOut.setInterpolator(interpolator)
			slidOut.setOnFinished((_: ActionEvent) => mainNodes.remove(prev))
			slidOut.play
		}
	}
}


trait SceneController extends Parent with StagedNode {
	def staged(contaner: StageContaner) = {}
	def unStaged(contaner: StageContaner) = {}
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
			.getOrElse(throw new StagedNodeException("the property("+StageContaner+") isn't a StageContaner"))
	}

	def isOnScene = try {getSceneNonNull; true} catch {case e: StagedNodeException => false}
	def isOnWindow = try {getWindowNonNull; true} catch {case e: StagedNodeException => false}
	def isOnStage = try {getStageNonNull; true} catch {case e: StagedNodeException => false}
	def isOnStageContaner = try {getStageContanerNonNull; true} catch {case e: StagedNodeException => false}
}

class StagedNodeException(message: String = null, cause: Throwable = null) extends RuntimeException(message, cause)
