package org.aklein.address

import org.aklein.address.db.Salutation

try {
    model.salutation = value
} catch (e) {
    model.salutation = new Salutation()
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
        label(app.getMessage('salutation.label.abbrev.text', 'Abbreviation'), constraints: 'grow')
        textField(text: bind('abbrev', source: model.salutation, mutual: true), constraints: 'grow')
        label(app.getMessage('salutation.label.letter.text', 'Letter salutation'), constraints: 'grow')
        textField(text: bind('letter', source: model.salutation, mutual: true), constraints: 'grow')
        label(app.getMessage('salutation.label.address.text', 'Address salutation'), constraints: 'grow')
        textField(text: bind('address', source: model.salutation, mutual: true), constraints: 'grow')
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