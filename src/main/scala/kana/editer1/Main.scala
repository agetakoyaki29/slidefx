package kana.editer1

import java.io.File
import javafx.fxml.FXML
import javafx.application.Application
import javafx.stage.{Window, Stage, FileChooser}
import javafx.stage.FileChooser.ExtensionFilter
import javafx.scene.layout.{BorderPane, StackPane}
import javafx.scene.control.{TabPane, Tab, MenuBar}
import javafx.event.ActionEvent


object Main {
	def main(args: Array[String]) {
		println("helo")
		Application.launch(classOf[MyApplication], args:_*)
	}
}

class MyApplication extends Application {
	override def start(stage: Stage) = new StageContaner(stage).show(new TopController)
}


class TopController extends StackPane with SceneController {
	@FXML def onAction(event: ActionEvent) =  getStageContanerNonNull.moveScene(new EditerController)
}


class EditerController extends BorderPane with SceneController {
	@FXML var mainTabPane: TabPane = _

	mainTabPane.setFocusTraversable(false)

	override def createMainMenu = Some(new EditerMenuBarController(this))

	def addAndSelectTab(tab: TextTabController) = {
		mainTabPane.getTabs.add(tab)
		val sm = mainTabPane.getSelectionModel
		sm.select(tab)
	}

	def toClosetab = {
		val index = mainTabPane.getSelectionModel.getSelectedIndex
		if(index >= 0) mainTabPane.getTabs.remove(index)
		else getSceneNonNull.getWindow.hide
	}

	def getSelectedItem = mainTabPane.getSelectionModel.getSelectedItem.asInstanceOf[TextTabController]
}


class EditerMenuBarController(val editerController: EditerController) extends MenuBar with RootedController with StagedNode {
	@FXML def onFileNew(event: ActionEvent) = editerController.addAndSelectTab(new TextTabController(None))
	@FXML def onFileOpen(event: ActionEvent) = editerController.addAndSelectTab(new TextTabController( FCM.showOpenDialog(getSceneNonNull.getWindow) ))
	@FXML def onFileReopen(event: ActionEvent) = println(getSceneNonNull.getFocusOwner)
	@FXML def onFileSave(event: ActionEvent) = Option(editerController.getSelectedItem).foreach(tab => { tab.fileOp match {
			case None => editerController.addAndSelectTab(new TextTabController( FCM.showSaveDialog(getSceneNonNull.getWindow) ))
			case Some(file) => tab.save
	}})
	@FXML def onFileSaveAs(event: ActionEvent) = println( FCM.showSaveDialog(getSceneNonNull.getWindow) )
	@FXML def onFileCloseTab(event: ActionEvent) = editerController.toClosetab

	@FXML def onFileExit(event: ActionEvent) = getSceneNonNull.getWindow.hide

	@FXML def onSceneTop(event: ActionEvent) = getStageContanerNonNull.moveScene(new TopController)
	@FXML def onSceneEditer(event: ActionEvent) = getStageContanerNonNull.moveScene(new EditerController)

	@FXML def onHelpHelp(event: ActionEvent) = ???
	@FXML def onHelpAbout(event: ActionEvent) = ???
}


object TextTabController

class TextTabController(val fileOp: Option[File]) extends Tab with RootedController {
	this.getProperties.put(TextTabController, this)

	fileOp.foreach(file => setText(file.getName))

	def save = println("saved")
}

// ----

object FCM {
	val that = new FCM(None)
  def showOpenDialog(owner: Window) = that.showOpenDialog(owner)
  def showSaveDialog(owner: Window) = that.showSaveDialog(owner)
	def showSaveDialog(owner: Window, fileName: String) = that.showSaveDialog(owner, fileName)
}

class FCM private (var workDirOp: Option[File]) {
	private lazy val opener = {
		val fc = new FileChooser();
		fc.getExtensionFilters().addAll(
				new ExtensionFilter("Text Files", "*.txt"),
				new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
				new ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac"),
				new ExtensionFilter("All Files", "*.*")
		)
		fc
	}

	private lazy val saver = {
		val fc = new FileChooser();
		fc.getExtensionFilters().addAll(
				new ExtensionFilter("Text Files", "*.txt")
		)
		fc
	}

	def showOpenDialog(owner: Window) = {
		val fc = opener
		workDirOp.foreach(fc.setInitialDirectory(_))

		val fileOp = Option( fc.showOpenDialog(owner) )
		fileOp.foreach(file => workDirOp = Option(file.getParentFile))
		fileOp
	}

	def showSaveDialog(owner: Window, fileName: String = "") = {
		val fc = saver
		workDirOp.foreach(fc.setInitialDirectory(_))
		fc.setInitialFileName(fileName)

		val fileOp = Option( fc.showSaveDialog(owner) )
		fileOp.foreach(file => workDirOp = Option(file.getParentFile))
		fileOp
	}
}
