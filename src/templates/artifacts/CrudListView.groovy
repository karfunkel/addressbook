package org.aklein.address

import ca.odell.glazedlists.BasicEventList
import org.aklein.address.db.@artifact.name.plain@

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
    borderLayout()

    widget(id: 'mainPanel', metaComponentFix('masterlist',
            tableFormat: defaultAdvancedTableFormat(columns: [
                    [name: 'name', class: String],
            ]),
            columnModel: columnModelFix {
                column("name", modelIndex: 0, headerValue: app.getMessage('@artifact.name.plain@.table.header.name.text', 'Name'), width: [100])
            },
            list: model.list,
            filter: [name: { @artifact.name.plain@ item, String value, String filter -> !(filter && !value?.toLowerCase()?.contains(filter.toLowerCase())) }],
            comparator: { a, b -> a.name <=> b.name },
            messagePrefix: '@artifact.name.plain@.',
            create: controller.create,
            edit: controller.edit,
            delete: controller.delete,
    ), constraints: CENTER)
    mainPanelGroup = context.mvcGroup

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
