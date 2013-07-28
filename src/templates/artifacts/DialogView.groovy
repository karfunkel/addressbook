package org.aklein.address

// Check if parameter noCancel is set
try {
    noCancel
} catch (e) {
    noCancel = false
}

def i18nAction = UIHelper.&i18nAction.curry(app, builder)

actions {
    i18nAction('cancelAction', controller.cancel)
    i18nAction('okAction', controller.ok)
}

panel(id: 'content') {
    borderLayout()

    panel(constraints: CENTER) {
        borderLayout()
        // Enter dialog content here
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