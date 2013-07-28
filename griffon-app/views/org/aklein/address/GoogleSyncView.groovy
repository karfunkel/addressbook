package org.aklein.address

// Check if parameter noCancel is set
try {
    noCancel
} catch (e) {
    noCancel = false
}

try {
    model.userId = value
} catch (e) {
    model.userId = ''
}

def i18nAction = UIHelper.&i18nAction.curry(app, builder)

actions {
    i18nAction('cancelAction', controller.cancel)
    i18nAction('okAction', controller.ok)
}

panel(id: 'content') {
    borderLayout()

    panel(constraints: CENTER) {
        migLayout(layoutConstraints: 'fill, wrap 1')
        label(app.getMessage('googleSync.label.name.text', 'Google user'), constraints: 'grow')
        textField(text: bind('userId', source: model, mutual: true), constraints: 'grow')
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