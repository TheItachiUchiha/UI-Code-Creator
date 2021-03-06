package com.example.demo.views

import com.example.demo.model.GridScope
import javafx.geometry.HPos
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.layout.*
import tornadofx.*
import javafx.scene.layout.ColumnConstraints

class MyTiles : View() {
    /***** Global Variables *****/
    override val scope = super.scope as GridScope
    private val model = scope.model
    private val gridInfo = model.item

    /***** View *****/
    override val root = gridpane {

        alignment = Pos.CENTER
        val colConstraints = columnConstraints
        colConstraints.clear()
        val columns = gridInfo.columns
        for (i in 0 until columns) {
            val c = ColumnConstraints()
            c.halignment = HPos.CENTER
            c.hgrow = Priority.NEVER
            c.minWidth = 100.0
            c.maxWidth = 100.0
            colConstraints.add(c)
        }

        val rowConstraints = rowConstraints
        rowConstraints.clear()
        val rows = gridInfo.rows
        for (i in 0 until rows) {
            val r = RowConstraints()
            r.valignment = VPos.CENTER
            r.vgrow = Priority.NEVER
            r.minHeight = 100.0
            r.maxHeight = 100.0
            rowConstraints.add(r)
        }

        hgap = 5.0
        vgap = 5.0

    }

    init {
        for (i in gridInfo.moduleTiles) {
            root.add(i.tile, i.colIndex, i.rowIndex, i.colSpan, i.rowSpan)
        }
    }
}

