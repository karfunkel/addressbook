package org.aklein.address

import ca.odell.glazedlists.ListSelection
import org.aklein.address.layout.TableFilterLayoutManager

import javax.swing.Icon
import javax.swing.ListSelectionModel
import javax.swing.event.ChangeEvent
import javax.swing.event.TableColumnModelListener
import java.awt.event.ComponentListener
import java.awt.event.MouseEvent
import java.beans.PropertyEditor
import java.beans.PropertyEditorManager

controller.handleAttributes(builder.variables)

def createButton(String name, Closure buttonAction = null) {
    def key = name.toLowerCase()
    def icon = app.resolveResource("${model._messagePrefix}masterlist.button.${key}.icon", (Object) app.resolveResource("masterlist.button.${key}.icon", (Object) null))
    if (icon) {
        PropertyEditor propertyEditor = PropertyEditorManager.findEditor(Icon)
        propertyEditor.setAsText(String.valueOf(icon))
        icon = propertyEditor.getValue()
    }
    noparent {
        button( id: key,
                action(
                        name: app.getMessage("${model._messagePrefix}masterlist.button.${key}.text", app.getMessage("masterlist.button.${key}.text", name)),
                        mnemonic: app.getMessage("${model._messagePrefix}masterlist.button.${key}.mnemonic", (String) app.getMessage("masterlist.button.${key}.mnemonic", (String) null)),
                        smallIcon: icon,
                        shortDescription: app.getMessage("${model._messagePrefix}masterlist.button.${key}.description", (String) app.getMessage("masterlist.button.${key}.description", (String) null)),
                        accelerator: app.getMessage("${model._messagePrefix}masterlist.button.${key}.accelerator", (String) app.getMessage("masterlist.button.${key}.accelerator", (String) null)),
                        closure: buttonAction ?: controller."${key}Action"
                )
        )
    }.last()
}

panel {
    migLayout(layoutConstraints: 'wrap 1, fill, ins 0')

    panel(constraints: 'grow, pushy') {
        migLayout(layoutConstraints: 'wrap 1, fill, ins 0')
        panel(id: 'filterRow', layout: new TableFilterLayoutManager({ mainTable.columnModel })) {
            for (int i = 0; i < model._tableFormat.columnCount; i++)
                textField(text: bind(source: model._filterMap, sourceProperty: model._tableFormat.getColumnName(i), mutual: true))
            // TODO: Filter for boolean usw.
        }
        scrollPane(constraints: 'grow, pushy') {
            table(id: 'mainTable') {
                eventTableModel(source: model.visible, format: model._tableFormat)
                installTableComparatorChooser(source: model.visible)
                model.selectionModel = installEventSelectionModel(source: model.visible, selectionMode: ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
                // TODO: Fix installEventSelectionModel to allow ListSelect.* as selectionMode
                model.selectionModel.selectionMode = ListSelection.MULTIPLE_INTERVAL_SELECTION_DEFENSIVE
                model.selected = model.selectionModel.selected
            }
            mainTable.columnModel = model._columnModel
            mainTable.mouseClicked = { MouseEvent evt->
                if (evt.clickCount == 2) {
                    controller.dclickAction()
                }
            }
        }
    }

    mainTable.columnModel.addColumnModelListener([
            columnMarginChanged: { ChangeEvent evt ->
                filterRow.revalidate()
                filterRow.repaint()
            },
            columnSelectionChanged: {},
            columnMoved: {},
            columnRemoved: {},
            columnAdded: {}
    ] as TableColumnModelListener)

    mainTable.addComponentListener([
            componentResized: {
                filterRow.revalidate()
                filterRow.repaint()
            },
            componentMoved: {},
            componentShown: {},
            componentHidden: {}
    ] as ComponentListener)

    defaultComponents = [
            CREATE: createButton('Create'),
            EDIT: createButton('Edit'),
            DELETE: createButton('Delete'),
            SHOW: createButton('Show'),
            GROW: noparent { label('GROW') }.last()
    ]

    panel(id: 'tools', constraints: 'grow') {
        migLayout(layoutConstraints: 'fill, ins 0')
        label(constraints: 'grow, pushx')
        if (model._create)
            widget(defaultComponents.CREATE, constraints: 'right')
        if (model._show)
            widget(defaultComponents.SHOW, constraints: 'right')
        if (model._edit)
            widget(defaultComponents.EDIT, constraints: 'right')
        if (model._delete)
            widget(defaultComponents.DELETE, constraints: 'right')
    }
}
