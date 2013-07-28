package org.aklein.address

import org.aklein.address.db.CommunicationType

try {
    model.communicationType = value
} catch (e) {
    model.communicationType = new CommunicationType()
}

def i18nAction = UIHelper.&i18nAction.curry(app, builder)

actions {
    i18nAction('cancelAction', controller.cancel)
    i18nAction('okAction', controller.ok)
}

panel(id: 'content') {
    migLayout(layoutConstraints: 'fill, wrap 1')

    panel(constraints: 'grow, pushy') {
        migLayout(layoutConstraints: 'fill, wrap 1')
        label(app.getMessage('communicationType.label.name.text', 'Name'), constraints: 'grow')
        textField(text: bind('name', source: model.communicationType, mutual: true), constraints: 'grow')
        label(app.getMessage('communicationType.label.useAreaCode.text', 'Use postal code'), constraints: 'grow, split 2')
        checkBox(selected: bind('useAreaCode', source: model.communicationType, mutual: true))
    }

    panel(constraints: 'south, grow') {
        gridLayout(cols: 3, rows: 1)
        button(cancelAction)
        button(okAction)
    }

    keyStrokeAction(component: current,
            keyStroke: "ESCAPE",
            condition: "in focused window",
            action: cancelAction)

    keyStrokeAction(component: current,
            keyStroke: "ENTER",
            condition: "in focused window",
            action: okAction)
}