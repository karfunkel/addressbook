package org.aklein.address.layout

import javax.swing.JComponent
import javax.swing.table.TableColumn
import javax.swing.table.TableColumnModel
import java.awt.*

class TableFilterLayoutManager implements LayoutManager {
    def model

    TableFilterLayoutManager(def model) {
        this.model = model
    }

    void addLayoutComponent(String name, Component comp) {}

    void removeLayoutComponent(Component comp) {}

    Dimension preferredLayoutSize(Container parent) {
        return new Dimension(realModel.totalColumnWidth + (realModel.columnMargin * (realModel.columnCount - 1)), (int) parent.components.preferredSize.height.max())
    }

    Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent)
    }

    void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            def m = realModel.columnMargin
            def min = Math.min(parent.componentCount, realModel.columnCount)
            int x = 0
            int y = 0
            int h = preferredLayoutSize(parent).height
            for (int i = 0; i < min; i++) {
                JComponent child = parent.getComponent(i)
                TableColumn column = realModel.getColumn(i)
                if (i == 0)
                    h = child.preferredSize.height
                child.bounds = new Rectangle(x, y, column.width, h)
                x += column.width + m
            }
        }
    }

    TableColumnModel getRealModel() {
        if (model instanceof TableColumnModel)
            return model
        def m
        if (model instanceof Closure)
            m = model()
        if (m instanceof TableColumnModel)
            return m
        throw new IllegalArgumentException('model has to be a TableColumnModel or a Closure returning a TableColumnModel')
    }
}
