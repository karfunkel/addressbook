package org.aklein.address

import ca.odell.glazedlists.BasicEventList
import org.aklein.address.db.Category

try {
    model.selected = new BasicEventList(value)
} catch (e) {
    model.selected = new BasicEventList()
}

def i18nAction = UIHelper.&i18nAction.curry(app, builder)

actions {
    i18nAction('cancelAction', controller.cancel)
    i18nAction('manageAction', controller.manage)
    i18nAction('okAction', controller.ok)
}

panel(id: 'content') {
    borderLayout()
    widget(metaComponentFix('multiselection',
            tableFormat: defaultAdvancedTableFormat(columns: [
                    [name: 'name', class: String],
            ]),
            columnModel: columnModelFix {
                column("name",
                        modelIndex: 0,
                        headerValue: app.getMessage('categories.dialog.name.text'),
                )
            },
            selected: model.selected,
            available: model.available,
            filter: [name: { Category item, String value, String filter -> !(filter && !value?.toLowerCase()?.contains(filter.toLowerCase())) }],
            comparator: { a, b -> a.name <=> b.name },
            messagePrefix: 'categories.dialog.',

    ), constraints: CENTER)
    panel(constraints: SOUTH) {
        gridLayout(cols: 3, rows: 1)
        button(cancelAction)
        button(manageAction)
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