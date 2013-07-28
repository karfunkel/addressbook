package org.aklein.address

import org.aklein.address.db.Nation

try {
    model.nation = value
} catch (e) {
    model.nation = new Nation()
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
        label(app.getMessage('nation.label.name.text', 'Name'), constraints: 'grow')
        textField(text: bind('name', source: model.nation, mutual: true), constraints: 'grow')
        label(app.getMessage('nation.label.english.text', 'Name in english'), constraints: 'grow')
        textField(text: bind('english', source: model.nation, mutual: true), constraints: 'grow')
        label(app.getMessage('nation.label.iso2.text', 'ISO2 code'), constraints: 'grow')
        textField(text: bind('iso2', source: model.nation, mutual: true), constraints: 'grow')
        label(app.getMessage('nation.label.iso3.text', 'ISO3 code'), constraints: 'grow')
        textField(text: bind('iso3', source: model.nation, mutual: true), constraints: 'grow')
        label(app.getMessage('nation.label.tel.text', 'Telefon area code'), constraints: 'grow')
        textField(text: bind('tel', source: model.nation, mutual: true), constraints: 'grow')
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