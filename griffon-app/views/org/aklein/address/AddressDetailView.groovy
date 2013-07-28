package org.aklein.address

import ca.odell.glazedlists.swing.EventComboBoxModel
import org.aklein.address.db.Address
import org.aklein.address.db.Unit

// Check if parameter noCancel is set
try {
    noCancel
} catch (e) {
    noCancel = false
}

try {
    model.address = value
} catch (e) {
    model.address = new Address()
}

try {
    model.addressType = type
} catch (e) {
    model.addressType = null
}

try {
    model.note = note
} catch (e) {
    model.note = null
}

try {
    model.addressList = list
} catch (e) {
    model.addressList = []
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
        label(app.getMessage('address.label.type.text', 'Type'), constraints: 'grow')
        cb = UIHelper.createComboBox(builder, model.filteredTypes, 'name', 'grow, pushy, split 2')
        bind(sourceProperty: 'addressType', source: model, target: cb, targetProperty: 'selectedItem', mutual: true)
        button(app.getMessage('address.label.type.edit.text', 'Edit'), actionPerformed: controller.editTypes)

        label(app.getMessage('address.label.note.text', 'Note'), constraints: 'grow')
        scrollPane(constraints: 'grow, pushy') {
            textArea(rows: 5, text: bind('note', source: model, mutual: true))
        }

        label(app.getMessage('address.label.nation.text', 'Nation'), constraints: 'grow')
        cb = UIHelper.createComboBox(builder, model.nations, 'name')
        bind(sourceProperty: 'nation', source: model.address, target: cb, targetProperty: 'selectedItem', mutual: true)

        label(app.getMessage('address.label.street.text', 'Street'), constraints: 'grow')
        textField(text: bind('street', source: model.address, mutual: true), constraints: 'grow')
        label(app.getMessage('address.label.zip.text', 'ZIP'), constraints: 'grow')
        textField(text: bind('zip', source: model.address, mutual: true), constraints: 'grow')
        label(app.getMessage('address.label.city.text', 'City'), constraints: 'grow')
        textField(text: bind('city', source: model.address, mutual: true), constraints: 'grow')
        label(app.getMessage('address.label.region.text', 'Region'), constraints: 'grow')
        textField(text: bind('region', source: model.address, mutual: true), constraints: 'grow')
        label(app.getMessage('address.label.addition.text', 'Addition'), constraints: 'grow')
        textField(text: bind('addition', source: model.address, mutual: true), constraints: 'grow')
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