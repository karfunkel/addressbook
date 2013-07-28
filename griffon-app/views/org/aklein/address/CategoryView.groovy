package org.aklein.address

import org.aklein.address.db.Category

try {
    model.category = value
} catch (e) {
    model.category = new Category()
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
        label(app.getMessage('category.label.name.text', 'Name'), constraints: 'grow')
        textField(text: bind('name', source: model.category, mutual: true), constraints: 'grow')
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