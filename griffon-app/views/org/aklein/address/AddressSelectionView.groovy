package org.aklein.address

import ca.odell.glazedlists.BasicEventList
import ca.odell.glazedlists.swing.EventComboBoxModel
import org.aklein.address.db.Address
import org.aklein.address.db.Unit
import org.aklein.address.db.Unit_Address

// Check if parameter noCancel is set
try {
    noCancel
} catch (e) {
    noCancel = false
}

try {
    model.addressList = list
} catch (e) {
    model.addressList = []
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

def addressDisplay = { Address address ->
    return new UnitView().addressDisplay(address)
}

actions {
    i18nAction('cancelAction', controller.cancel)
    i18nAction('okAction', controller.ok)
}

panel(id: 'content') {
    borderLayout()

    panel(constraints: CENTER) {
        def cb
        migLayout(layoutConstraints: 'fill, wrap 1')
        label(app.getMessage('address.label.type.text', 'Type'), constraints: 'grow')
        cb = UIHelper.createComboBox(builder, model.filteredTypes, 'name', 'grow, pushy, split 2')
        bind(sourceProperty: 'addressType', source: model, target: cb, targetProperty: 'selectedItem', mutual: true)
        button(app.getMessage('address.label.type.edit.text', 'Edit'), actionPerformed: controller.editTypes)

        label(app.getMessage('address.label.note.text', 'Note'), constraints: 'grow')
        scrollPane(constraints: 'grow, pushy') {
            textArea(rows: 5, text: bind('note', source: model, mutual: true))
        }

        widget( id: 'mainPanel',
                metaComponentFix('masterlist',
                        tableFormat: defaultAdvancedTableFormat(columns: [
                                [
                                        name: 'unit',
                                        comparator: { a, b -> a.name <=> b.name } as Comparator,
                                        class: String
                                ],
                                [
                                        name: 'address',
                                        comparator: { a, b -> addressDisplay(a) <=> addressDisplay(b) } as Comparator,
                                        class: String
                                ]
                        ]),
                        columnModel: columnModelFix {
                            column("unit",
                                    modelIndex: 0,
                                    headerValue: app.getMessage('unit.addresses.header.unit.text'),
                                    width: [150]) {
                                cellRenderer {
                                    onRender {
                                        children[0].text = value?.name ?: ''
                                    }
                                }
                            }
                            column("address",
                                    modelIndex: 1,
                                    headerValue: app.getMessage('unit.addresses.header.address.text'),
                                    width: [400]) {
                                cellRenderer {
                                    onRender {
                                        children[0].text = addressDisplay(value)
                                    }
                                }
                            }
                        },
                        list: model.list,
                        filter: [
                                unit: { Unit_Address item, Unit value, String filter -> !(filter && !value?.name?.toLowerCase()?.contains(filter.toLowerCase())) },
                                address: { Unit_Address item, Address value, String filter -> !(filter && !addressDisplay(value).toLowerCase().contains(filter.toLowerCase())) },
                        ],
                        comparator: { a, b -> a.addressType.name <=> b.addressType.name ?: addressDisplay(a.address) <=> addressDisplay(b.address) },
                        messagePrefix: 'mainlist.',
                ) {
                    label(constraints: 'GROW')
                },
                constraints: 'grow, pushy'
        )
        mainPanelGroup = context.mvcGroup
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