package org.aklein.address

import ca.odell.glazedlists.swing.EventComboBoxModel
import org.aklein.address.db.Communication

// Check if parameter noCancel is set
try {
    noCancel
} catch (e) {
    noCancel = false
}

try {
    model.communication = value
} catch (e) {
    model.communication = new Communication()
}

try {
    model.communicationType = type
} catch (e) {
    model.communicationType = null
}

try {
    model.note = note
} catch (e) {
    model.note = null
}

try {
    model.communicationList = list
} catch (e) {
    model.communicationList = []
}

def i18nAction = UIHelper.&i18nAction.curry(app, builder)

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

        label(app.getMessage('communication.label.nation.text', 'Nation'), constraints: 'grow')
        cb = UIHelper.createComboBox(builder, model.nations, 'name')
        bind(sourceProperty: 'nation', source: model.communication, target: cb, targetProperty: 'selectedItem', mutual: true)

        label(app.getMessage('communication.label.text.text', 'Text'), constraints: 'grow')
        textField(text: bind('text', source: model.communication, mutual: true), constraints: 'grow')
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