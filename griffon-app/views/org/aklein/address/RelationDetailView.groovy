package org.aklein.address

import org.aklein.address.db.Unit

// Check if parameter noCancel is set
try {
    noCancel
} catch (e) {
    noCancel = false
}

try {
    model.unit = unit
} catch (e) {
    model.unit = new Unit()
}

def i18nAction = UIHelper.&i18nAction.curry(app, builder)

actions {
    i18nAction('cancelAction', controller.cancel)
    i18nAction('okAction', controller.ok)
}

buttonGroup(id: 'relationGroup')

panel(id: 'content') {
    borderLayout()

    panel(constraints: CENTER) {
        migLayout(layoutConstraints: 'fill, wrap 1')
        label(text: bind(sourceProperty: 'unit', source: model, converter: { it.name }), constraints: 'grow')

        panel(constraints: 'grow') {
            gridLayout(cols: 2, rows: 1)
            radioButton(
                    text: app.getMessage('unit.relation.is', 'is'),
                    buttonGroup: relationGroup,
                    selected: bind('type', source: model, mutual: true,
                            converter: { it == 'is' },
                            reverseConverter: { it ? 'is' : 'has' }
                    )
            )
            radioButton(
                    text: app.getMessage('unit.relation.has', 'has'),
                    buttonGroup: relationGroup,
                    selected: bind('type', source: model, mutual: true,
                            converter: { it == 'has' },
                            reverseConverter: { it ? 'has' : 'is' }
                    )
            )
        }

        label(app.getMessage('relationDetail.label.description.text', 'Description'), constraints: 'grow')
        scrollPane(constraints: 'grow, pushy') {
            textArea(rows: 3, text: bind('description', source: model, mutual: true))
        }

        widget(id: 'mainPanel', metaComponentFix('masterlist',
                tableFormat: defaultAdvancedTableFormat(columns: [
                        [name: 'name', class: String],
                ]),
                columnModel: columnModelFix {
                    column("name", modelIndex: 0, headerValue: app.getMessage('relationDetail.table.header.name.text', 'Name'), width: [100])
                },
                list: model.units,
                filter: [name: { Unit item, String value, String filter -> !(filter && !value?.toLowerCase()?.contains(filter.toLowerCase())) }],
                comparator: { a, b -> a.name <=> b.name },
                messagePrefix: 'relationDetail.'
        ))
        mainPanelGroup = context.mvcGroup

        try {
            context.mvcGroup.controller.selection = value
            context.mvcGroup.controller.scrollToSelection()
        } catch (e) {}
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