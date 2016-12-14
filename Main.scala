// package foo.bar

import java.io.File

import javafx.fxml.FXML
import javafx.application.{Application, Platform}
import javafx.stage.{Window, Stage, StageStyle, FileChooser}
import FileChooser.ExtensionFilter
import javafx.scene.layout.{Region, BorderPane, StackPane}
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
	val that = new FCM(null)
  def showOpenDialog(owner: Window) = that.showOpenDialog(owner)
  def showSaveDialog(owner: Window) = that.showSaveDialog(owner)
	def showSaveDialog(owner: Window, fileName: String) = that.showSaveDialog(owner, fileName)
}

class FCM private (private var initialDirectory: File) {
	private lazy val opener = {
		val fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter("Text Files", "*.txt"),
				new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
				new ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac"),
				new ExtensionFilter("All Files", "*.*")
		)
		fileChooser
	}

	def showOpenDialog(owner: Window) = {
		val fileChooser = opener
		if(initialDirectory != null) fileChooser.setInitialDirectory(initialDirectory)
		val fileOp = Option( fileChooser.showOpenDialog(owner) )
		fileOp.foreach(file => initialDirectory = file.getParentFile)
		fileOp
	}

	private lazy val saver = {
		val fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter("Text Files", "*.txt")
		)
		fileChooser
	}

	def showSaveDialog(owner: Window): Option[File] = showSaveDialog(owner, "")
	def showSaveDialog(owner: Window, fileName: String) = {
		val fileChooser = saver
		fileChooser.setInitialFileName(fileName)
		if(initialDirectory != null) fileChooser.setInitialDirectory(initialDirectory)
		val fileOp = Option( fileChooser.showSaveDialog(owner) )
		fileOp.foreach(file => initialDirectory = file.getParentFile)
		fileOp
	}
}
