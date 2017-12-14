package com.example.demo.views
import com.example.demo.app.Styles
import com.example.demo.app.Styles.Companion.metroTileHomepageGUI
import com.example.demo.controllers.LoginController
import com.example.demo.controllers.MetroTileHomepageController
import com.example.demo.controllers.WorkbenchController
import com.example.demo.model.DragTile
import com.example.demo.model.DragTileScope
import com.example.demo.model.GridInfo
import com.example.demo.model.GridScope
import eu.hansolo.tilesfx.Tile
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.geometry.Side
import javafx.scene.Node
import javafx.scene.input.*
import javafx.scene.layout.*
import javafx.scene.text.Font
import tornadofx.*
import javafx.scene.shape.Circle
import javafx.fxml.FXML





class MetroTileHomepage : Fragment() {
    private val loginController: LoginController by inject()
    private val workbenchController: WorkbenchController by inject()
    private val controller: MetroTileHomepageController by inject()
    private val paginator = DataGridPaginator(controller.smallTiles, itemsPerPage = 8)
    lateinit var tile: Tile
    lateinit var leavingTile: Tile
    lateinit var grid: GridPane
    private var offset = Point2D(0.0, 0.0)
    private var movingPiece = false
    val dragTileScope = DragTileScope()

    companion object {
        private var TILES: DataFormat = DataFormat("eu.hansolo.tilesfx.Tile")
    }

    override val root = borderpane {
        val gridInfo = GridInfo(controller.useTileGrid(workbenchController.metroTile))
        grid = passGridInfo(gridInfo)
        addClass(metroTileHomepageGUI)
        setPrefSize(1000.0, 750.0)

        flowpane {
            top {
                label(title) {
                    font = Font.font(22.0)
                }
                menubar {
                    menu("File") {
                        item("Logout").action {
                            loginController.logout()
                        }
                        item("Quit").action {
                            Platform.exit()
                        }
                    }
                }
            }

            center  = grid

                //grid.addEventFilter(MouseEvent.MOUSE_EXITED_TARGET, this::leaveGrid)
                //grid.addEventFilter(MouseEvent.MOUSE_RELEASED, this::checkReleaseOutOfGrid)
                grid.addClass(Styles.grid)
                grid.setOnDragOver { event ->
                    if (event.dragboard.hasContent(TILES)) event.acceptTransferModes(TransferMode.COPY)
                    event.consume()
                }

                grid.setOnDragDropped { event ->
                    if (event.dragboard.hasContent(TILES)) {
                        event.isDropCompleted = false
                        val db: Dragboard = event.dragboard
                        val node: Node = event.pickResult.intersectedNode
                        if (node != grid && db.hasContent(TILES) && db.getContent(TILES) is DragTile) {
                            val columnIndex = GridPane.getColumnIndex(node)
                            val rowIndex = GridPane.getRowIndex(node)
                            val x = if (columnIndex == null) 0 else columnIndex
                            val y = if (rowIndex == null) 0 else rowIndex
                            dragTileScope.model.item = (db.getContent(TILES) as? DragTile)
                            grid.add(dragTileScope.model.item.tile, x, y,
                                    dragTileScope.model.item.colSpan,
                                    dragTileScope.model.item.rowSpan)
                        }
                    }
                    event.isDropCompleted = true
                    event.consume()
                }


            right {
                vbox {
                    maxWidth = 300.0

                    drawer(side = Side.RIGHT) {
                        item("Small Modules", expanded = true) {
                            datagrid(paginator.items) {
                                maxCellsInRow = 2

                                cellWidth = 100.0
                                cellHeight = 100.0
                                paddingLeft = 35.0
                                minWidth = 300.0

                                cellFormat {
                                    graphic = cache {
                                        it
                                    }

                                    graphic.setOnDragDetected { event ->
                                        startDragAndDrop(TransferMode.MOVE).apply {
                                            tile = it
                                            startMovingPiece(event)
                                            movePiece(event)
                                            event.consume()
                                        }
                                    }
                                }
                            }
                        }
                        item("Large Modules") {
                            datagrid(paginator.items) {
                                maxCellsInRow = 2

                                cellWidth = 100.0
                                cellHeight = 100.0
                                paddingLeft = 35.0
                                minWidth = 300.0

                                cellFormat {
                                    graphic = cache {
                                        it
                                    }
                                }
                            }
                        }

                    }

                    form {
                        paddingLeft = 20.0
                        paddingTop = 20.0
                        hbox(20) {
                            fieldset("Customize Module") {
                                hbox(20) {
                                    vbox {
                                        field("Color") { textfield("#fffffff") }
                                        field("HoverColor") { textfield("#fffffff") }
                                        field("Image Source") { textfield("") }
                                        field("Label") { textfield("Label") }
                                    }
                                }
                            }
                        }
                    }
                    hbox {
                        hboxConstraints {
                            alignment = Pos.BASELINE_RIGHT
                        }
                        button("Return to Workbench") {
                            setOnAction {
                                workbenchController.returnToWorkbench(this@MetroTileHomepage)
                            }

                            hboxConstraints {
                                marginLeftRight(10.0)
                                marginBottom = 20.0
                            }
                        }
                        button("Save") {
                            isDefaultButton = true

                            setOnAction {
                                // Save
                            }

                            hboxConstraints {
                                marginLeftRight(10.0)
                                marginBottom = 20.0
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startMovingPiece(evt: MouseEvent) {
        tile.opacity = 0.4
        offset = Point2D(evt.x, evt.y)

        leavingTile.opacity = 1.0
        leavingTile.layoutX = tile.layoutX
        leavingTile.layoutY = tile.layoutY

        movingPiece = true
    }

    private fun movePiece(evt: MouseEvent) {

        val mousePoint = Point2D(evt.x, evt.y)
        val mousePointS = Point2D(evt.sceneX, evt.sceneY)

        if (!inGrid(mousePointS)) {
            return   // don't relocate() b/c will resize Pane
        }

        val mousePointP = tile.localToParent(mousePoint)
        tile.relocate(mousePointP.x - offset.x, mousePointP.y - offset.y)
    }

    private fun inGrid(pt: Point2D): Boolean {
        val gridPt = grid.sceneToLocal(pt)
        return (gridPt.x - offset.x >= 0.0
                && gridPt.y - offset.y >= 0.0
                && gridPt.x <= grid.width
                && gridPt.y <= grid.height)
    }

}

private fun passGridInfo(gridInfo: GridInfo): GridPane {
    val metroScope = GridScope()
    metroScope.model.item = gridInfo
    return find<MyTiles>(metroScope).root
}

/*private fun checkReleaseOutOfGrid(event: MouseEvent) {
    val mousePoint_s: Point2D = Point2D(event.sceneX, event.sceneY)
    if (!inGrid(event)) {
        leaveGrid(event)
        event.consume()
    }
}

private fun leaveGrid(event: MouseEvent) {
    if (movingTile) {
        val timeline: Timeline = Timeline()

        movingTile = false

        timeline.keyFrames.add(KeyFrame(Duration(200.0)),
                KeyValue(tile.))
    }
}*/

