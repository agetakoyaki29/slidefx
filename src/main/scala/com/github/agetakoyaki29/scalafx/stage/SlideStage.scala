package com.github.agetakoyaki29.scalafx.stage

import javafx.stage.Stage
import javafx.scene.{Scene, Node}
import javafx.scene.layout.{BorderPane, AnchorPane}
import javafx.scene.control.{MenuBar, Menu}
import javafx.animation.TranslateTransition
import javafx.util.Duration
import javafx.event.ActionEvent

import com.github.agetakoyaki29.scalafx.animation.SineInterpolator


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
	  defaultMenuBar.setUseSystemMenuBar(true)
	  
	  mainNodes.add(new AnchorPane with SlideController)
	}
	; {
		// stage.initStyle(StageStyle.TRANSPARENT)  // initStyle
		// stage.setMaximized(true)
		stage.getProperties.put(StageContaner, this) // put this to stage properties
		stage.setScene(new Scene(root));
	}

	def show(firstSlide: SlideController) = {
		moveSlide(firstSlide)
		stage.show
	}

	def setMainMenuBar(nextOp: Option[MenuBar]) = {
		Option(menuBarProperty.get.asInstanceOf[MenuBar]).foreach(_.setUseSystemMenuBar(false))
		nextOp.foreach(_.setUseSystemMenuBar(true))

		nextOp match {
			case Some(menuBar) => menuBarProperty.set(menuBar)
			case None => menuBarProperty.set(defaultMenuBar)
		}
	}

	def moveSlide(next: SlideController) = {
		// get previous before add
		val perv = getNow
		// add and expand
		addNow(next)
		// call staged
		perv.unstaged(this)
		next.staged(this)

		animateAndRemove(next, perv)
	}

	private def addNow(slide: SlideController) = {
	   mainNodes.add(slide)
	   expandAnchorChild(slide)
	}
	
	protected def getNow = mainNodes.get(mainNodes.size-1).asInstanceOf[SlideController]

	private def expandAnchorChild(slide: SlideController) = {
		AnchorPane.setBottomAnchor(slide, 0)
		AnchorPane.setLeftAnchor(slide, 0)
		AnchorPane.setRightAnchor(slide, 0)
		AnchorPane.setTopAnchor(slide, 0)
	}

	private def animateAndRemove(next: SlideController, prev: SlideController) = {
		val duration = Duration.seconds(1)
		val interpolator = new SineInterpolator(.3)
		val width = centerPane.getLayoutBounds.getWidth
		; {
			val slidIn = new TranslateTransition(duration, next)
			slidIn.setFromX(width)    // width to 0
			slidIn.setToX(0)
			slidIn.setInterpolator(interpolator)
			slidIn.play
		}
		; {
			// prev.setClip(prev.getParent)
			val slidOut = new TranslateTransition(duration, prev)
			slidOut.setToX(-width)    // 0 to -width
			slidOut.setInterpolator(interpolator)
			slidOut.setOnFinished((_: ActionEvent) => mainNodes.remove(prev))  // remove on finish
			slidOut.play
		}
	}
}


trait SlideController extends Node with StagedNode {
	def staged(contaner: StageContaner) = {}
	def unstaged(contaner: StageContaner) = {}
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
