package org.aklein.address

import ca.odell.glazedlists.BasicEventList
import org.aklein.address.db.Salutation

try {
    noCancel
} catch (e) {
    noCancel = false
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

actions {
    i18nAction('cancelAction', controller.cancel)
    i18nAction('okAction', controller.ok)
}

panel(id: 'content') {
    migLayout(layoutConstraints: 'fill, wrap 1, ins n')

    widget(id: 'mainPanel', metaComponentFix('masterlist',
            tableFormat: defaultAdvancedTableFormat(columns: [
                    [name: 'abbrev', class: String],
            ]),
            columnModel: columnModelFix {
                column("abbrev", modelIndex: 0, headerValue: app.getMessage('salutationManagement.table.header.abbrev.text', 'Abbreviation'), width: [100])
            },
            list: model.list,
            filter: [abbrev: { Salutation item, String value, String filter -> !(filter && !value?.toLowerCase()?.contains(filter.toLowerCase())) }],
            comparator: { a, b -> a.abbrev <=> b.abbrev },
            messagePrefix: 'salutationManagement.',
            create: controller.create,
            edit: controller.edit,
            delete: controller.delete,
    ))
    mainPanelGroup = context.mvcGroup

    panel(constraints: 'south') {
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
