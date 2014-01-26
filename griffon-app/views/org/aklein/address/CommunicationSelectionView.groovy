package org.aklein.address

import ca.odell.glazedlists.BasicEventList
import ca.odell.glazedlists.swing.EventComboBoxModel
import org.aklein.address.db.Communication
import org.aklein.address.db.Unit
import org.aklein.address.db.Unit_Communication

import java.beans.PropertyChangeListener

// Check if parameter noCancel is set
try {
    noCancel
} catch (e) {
    noCancel = false
}

try {
    model.communicationList = list
} catch (e) {
    model.communicationList = []
}

try {
    if (value instanceof BasicEventList)
        model.list = value
    else
        model.list = new BasicEventList(value)
} catch (e) {
    model.list = new BasicEventList()
}

def i18nAction = UIHelper.&i18nAction.curry(app, builder)

def communicationDisplayCache = [:]

def communicationDisplay = { Communication communication ->
    if (communicationDisplayCache[communication.id])
        return communicationDisplayCache[communication.id]
    def d = new UnitView().communicationDisplay(model.communicationType, communication)
    communicationDisplayCache[communication.id] = d
    return d
}

actions {
    i18nAction('cancelAction', controller.cancel)
    i18nAction('okAction', controller.ok)
}

panel(id: 'content') {
    borderLayout()

    panel(constraints: CENTER) {
        def cb
        migLayout(layoutConstraints: 'fill, wrap 1')
        label(app.getMessage('communication.label.type.text', 'Type'), constraints: 'grow')
        cb = UIHelper.createComboBox(builder, model.filteredTypes, 'name', 'grow, pushy, split 2')
        bind(sourceProperty: 'communicationType', source: model, target: cb, targetProperty: 'selectedItem', mutual: true)
        button(app.getMessage('communication.label.type.edit.text', 'Edit'), actionPerformed: controller.editTypes)

        label(app.getMessage('communication.label.note.text', 'Note'), constraints: 'grow')
        scrollPane(constraints: 'grow, pushy') {
            textArea(rows: 5, text: bind('note', source: model, mutual: true))
        }

        model.addPropertyChangeListener('communicationType', { evt ->
            communicationDisplayCache.clear()
            mainPanelGroup.view.mainTable.repaint()
        } as PropertyChangeListener)

        widget(id: 'mainPanel',
                metaComponentFix('masterlist',
                        tableFormat: defaultAdvancedTableFormat(columns: [
                                [
                                        name: 'unit',
                                        comparator: { a, b -> a.name <=> b.name } as Comparator,
                                        class: String
                                ],
                                [
                                        name: 'communication',
                                        comparator: { a, b -> communicationDisplay(a) <=> communicationDisplay(b) } as Comparator,
                                        class: String
                                ]
                        ]),
                        columnModel: columnModelFix {
                            column("unit",
                                    modelIndex: 0,
                                    headerValue: app.getMessage('unit.communications.header.unit.text'),
                                    width: [150]) {
                                cellRenderer {
                                    onRender {
                                        children[0].text = value?.name ?: ''
                                    }
                                }
                            }
                            column("communication",
                                    modelIndex: 1,
                                    headerValue: app.getMessage('unit.communications.header.communication.text'),
                                    width: [400]) {
                                cellRenderer {
                                    onRender {
                                        children[0].text = communicationDisplay(value)
                                    }
                                }
                            }
                        },
                        list: model.list,
                        filter: [
                                unit: { Unit_Communication item, Unit value, String filter -> !(filter && !value?.name?.toLowerCase()?.contains(filter.toLowerCase())) },
                                communication: { Unit_Communication item, Communication value, String filter -> !(filter && !communicationDisplay(value).toLowerCase().contains(filter.toLowerCase())) },
                        ],
                        comparator: { a, b -> a.communicationType.name <=> b.communicationType.name ?: communicationDisplay(a.communication) <=> communicationDisplay(b.communication) },
                        messagePrefix: 'mainlist.',
                ) {
                    label(constraints: 'GROW')
                },
                constraints: 'grow, pushy'
        )
        mainPanelGroup = context.mvcGroup
    }

    panel(constraints: SOUTH) {
        gridLayout(cols: 3, rows: 1)
        if (!noCancel)
            button(cancelAction)
        button(okAction)
    }

    keyStrokeAction(component: current,
            keyStroke: "ESCAPE",
            condition: "in focused window",
            action: noCancel ? okAction : cancelAction)

    keyStrokeAction(component: current,
            keyStroke: "ENTER",
            condition: "in focused window",
            action: okAction)
}