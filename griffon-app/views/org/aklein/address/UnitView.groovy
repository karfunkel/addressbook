package org.aklein.address

import org.aklein.address.db.*

try {
    model.unit = value
} catch (e) {
    model.unit = new Unit()
}

def i18nAction = UIHelper.&i18nAction.curry(app, builder)

actions {
    i18nAction('cancelAction', controller.cancel)
    i18nAction('okAction', controller.ok)
}

def unitDisplay(Unit unit) {
    if (!unit) return ''
    return unit.display
}

def addressDisplay(Address address) {
    if (!address) return ''
    return address.oneliner
}

def unitCommunicationDisplay(Unit_Communication uCom) {
    return uCom.oneliner
}

def communicationDisplay(CommunicationType type, Communication com) {
    return Unit_Communication.getOneliner(type, com)
}

def relationFullDisplay(Relation relation, Unit unit) {
    if (!relation) return ''
    return relation.getFullDisplay(unit)
}

def relationTypeDisplay(Relation relation, Unit unit) {
    if (!relation) return ''
    return relation.getTypeDisplay(unit)
}

def relationDisplay(Relation relation, Unit unit) {
    if (!relation) return ''
    return relation.getDisplay(unit)

}

buttonGroup(id: 'unitGroup')
panel(id: 'content') {
    borderLayout()
    panel(constraints: CENTER) {
        migLayout(layoutConstraints: 'fill, wrap 2')
        panel(constraints: 'grow') {
            gridLayout(cols: 2, rows: 1)
            radioButton(
                    text: app.getMessage('unit.option.person.text', 'Person'),
                    buttonGroup: unitGroup,
                    selected: bind('type', source: model.unit, mutual: true,
                            converter: { it == 'N' },
                            reverseConverter: { it ? 'N' : 'J' }
                    )
            )
            radioButton(
                    text: app.getMessage('unit.option.organisation.text', 'Organisation'),
                    buttonGroup: unitGroup,
                    selected: bind('type', source: model.unit, mutual: true,
                            converter: { it == 'J' },
                            reverseConverter: { it ? 'J' : 'N' }
                    )
            )
        }

        // Person
        panel(
                visible: bind('type', source: model.unit,
                        converter: { it == 'N' }
                ),
                constraints: 'grow, pushy, hidemode 3'
        ) {
            migLayout(layoutConstraints: 'wrap 4, fill')
            label(app.getMessage('unit.label.firstname.text', 'Firstname'), constraints: 'grow')
            label(app.getMessage('unit.label.namePart.text', 'Name part'), constraints: 'grow')
            label(app.getMessage('unit.label.lastname.text', 'Lastname'), constraints: 'grow')
            label(app.getMessage('unit.label.birthday.text', 'Birthday'), constraints: 'grow')

            textField(text: bind('firstname', source: model.unit, mutual: true), constraints: 'grow')
            textField(text: bind('namePart', source: model.unit, mutual: true), constraints: 'grow')
            textField(text: bind('lastname', source: model.unit, mutual: true), constraints: 'grow')
            jxdatePicker(date: bind('birthday', source: model.unit, mutual: true), constraints: 'grow')

            label(app.getMessage('unit.label.title.text', 'Title'), constraints: 'grow')
            label(app.getMessage('unit.label.position.text', 'Position'), constraints: 'grow')
            label(app.getMessage('unit.label.organisation.text', 'Organisation'), constraints: 'grow')
            label(app.getMessage('unit.label.department.text', 'Department'), constraints: 'grow')

            textField(text: bind('title', source: model.unit, mutual: true), constraints: 'grow')
            textField(text: bind('position', source: model.unit, mutual: true), constraints: 'grow')
            textField(text: bind('organisation', source: model.unit, mutual: true), constraints: 'grow')
            textField(text: bind('department', source: model.unit, mutual: true), constraints: 'grow')
        }
        // Organisation
        panel(
                visible: bind('type', source: model.unit,
                        converter: { it == 'J' }
                ),
                constraints: 'grow, pushy, hidemode 3'
        ) {
            migLayout(layoutConstraints: 'wrap 1, fill')

            label(app.getMessage('unit.label.name.text', 'Name'), constraints: 'grow, pushy')
            textField(text: bind('name', source: model.unit, mutual: true), constraints: 'grow, pushy')

            label(app.getMessage('unit.label.vatId.text', 'VAT ID'), constraints: 'grow, pushy')
            textField(text: bind('vatId', source: model.unit, mutual: true), constraints: 'grow, pushy')
        }

        panel(constraints: 'grow, pushy, spanx 2, wrap') {
            migLayout(layoutConstraints: 'fill')

            label(app.getMessage('unit.label.salutation.text', 'Salutation'), constraints: 'grow, pushy')
            label(app.getMessage('unit.label.nation.text', 'Nation'), constraints: 'grow, pushy')
            label(app.getMessage('unit.label.categories.text', 'Categories'), constraints: 'grow, pushy')
            label('', constraints: 'grow, pushy, wrap')

            def cb = UIHelper.createComboBox(builder, model.salutations, 'abbrev', 'grow, pushy, split 2')
            bind(sourceProperty: 'salutation', source: model.unit, target: cb, targetProperty: 'selectedItem', mutual: true)
            button(constraints: 'grow, pushy',
                    text: app.getMessage('unit.label.salutation.edit', 'Manage'),
                    actionPerformed: controller.salutations
            )

            cb = UIHelper.createComboBox(builder, model.nations, 'name', 'grow, pushy, split 2')
            bind(sourceProperty: 'nation', source: model.unit, target: cb, targetProperty: 'selectedItem', mutual: true)
            button(constraints: 'grow, pushy',
                    text: app.getMessage('unit.label.nation.edit', 'Manage'),
                    actionPerformed: controller.nations
            )

            button(constraints: 'grow, pushy',
                    text: bind('unitCategories',
                            id: 'unitCategoriesBinding',
                            source: model.unit,
                            converter: { unitCategories ->
                                unitCategories?.category?.name?.join(', ')
                            }),
                    actionPerformed: controller.categories
            )
        }

        label(app.getMessage('unit.label.addresses.text', 'Addresses'), constraints: 'grow, pushy')
        label(app.getMessage('unit.label.communications.text', 'Communications'), constraints: 'grow, pushy')

        widget(
                metaComponentFix('masterlist',
                        tableFormat: defaultAdvancedTableFormat(columns: [
                                [
                                        name: 'addressType',
                                        comparator: { a, b -> a.name <=> b.name } as Comparator,
                                        class: String
                                ],
                                [
                                        name: 'address',
                                        comparator: { a, b -> addressDisplay(a) <=> addressDisplay(b) } as Comparator,
                                        class: String
                                ],
                                [
                                        name: 'note',
                                        class: String
                                ],
                        ]),
                        columnModel: columnModelFix {
                            column("addressType",
                                    modelIndex: 0,
                                    headerValue: app.getMessage('unit.addresses.header.addressType.text'),
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
                            column("note",
                                    modelIndex: 2,
                                    headerValue: app.getMessage('unit.addresses.header.note.text'),
                                    width: [100]
                            )
                        },
                        loader: controller.loadAddresses,
                        filter: [
                                addressType: { Unit_Address item, AddressType value, String filter -> !(filter && !value?.name?.toLowerCase()?.contains(filter.toLowerCase())) },
                                address: { Unit_Address item, Address value, String filter -> !(filter && !addressDisplay(value).toLowerCase().contains(filter.toLowerCase())) },
                                note: { Unit_Address item, String value, String filter -> !(filter && !value?.toLowerCase()?.contains(filter.toLowerCase())) },
                        ],
                        comparator: { a, b -> a.addressType?.name <=> b.addressType?.name ?: addressDisplay(a.address) <=> addressDisplay(b.address) },
                        messagePrefix: 'mainlist.',
                        create: controller.createAddress,
                        edit: controller.editAddress,
                        delete: controller.deleteAddress,
                ) {
                    masterModel = parentContext.mvcGroup.model
                    masterView = parentContext.mvcGroup.view
                    label(constraints: 'GROW')
                    button(masterView.createButton('Link', controller.linkAddress), constraints: 'TRAILING')
                    button(constraints: 'CREATE')
                    button(constraints: 'EDIT')
                    button(constraints: 'DELETE')
                },
                constraints: 'grow, pushy'
        )
        addressPanelGroup = context.mvcGroup

        widget(
                metaComponentFix('masterlist',
                        tableFormat: defaultAdvancedTableFormat(columns: [
                                [
                                        name: 'communicationType',
                                        comparator: { a, b -> a.name <=> b.name } as Comparator,
                                        class: String
                                ],
                                [
                                        name: 'communication',
                                        comparator: { a, b -> unitCommunicationDisplay(a) <=> unitCommunicationDisplay(b) } as Comparator,
                                        read: { obj, columns, idx -> obj },
                                        class: String
                                ],
                                [
                                        name: 'note',
                                        class: String
                                ],
                        ]),
                        columnModel: columnModelFix {
                            column("communicationType",
                                    modelIndex: 0,
                                    headerValue: app.getMessage('unit.communications.header.communicationType.text'),
                                    width: [150]) {
                                cellRenderer {
                                    onRender {
                                        children[0].text = value?.name ?: ''
                                    }
                                }
                            }
                            column("communication",
                                    modelIndex: 1,
                                    headerValue: app.getMessage('unit.communications.header.communication.text'),
                                    width: [250]) {
                                cellRenderer {
                                    onRender {
                                        children[0].text = unitCommunicationDisplay(value)
                                    }
                                }
                            }
                            column("note",
                                    modelIndex: 2,
                                    headerValue: app.getMessage('unit.communications.header.note.text'),
                                    width: [200]
                            )
                        },
                        loader: controller.loadCommunications,
                        filter: [
                                communicationType: { Unit_Communication item, CommunicationType value, String filter -> !(filter && !value?.name?.toLowerCase()?.contains(filter.toLowerCase())) },
                                communication: { Unit_Communication item, Unit_Communication value, String filter -> !(filter && !unitCommunicationDisplay(item).toLowerCase().contains(filter.toLowerCase())) },
                                note: { Unit_Communication item, String value, String filter -> !(filter && !value?.toLowerCase()?.contains(filter.toLowerCase())) },
                        ],
                        comparator: { a, b -> a.communicationType.name <=> b.communicationType.name ?: unitCommunicationDisplay(a) <=> unitCommunicationDisplay(b) },
                        messagePrefix: 'mainlist.',
                        create: controller.createCommunication,
                        edit: controller.editCommunication,
                        delete: controller.deleteCommunication,
                ) {
                    label(constraints: 'GROW')
                    button(masterView.createButton('Link', controller.linkCommunication), constraints: 'TRAILING')
                    button(constraints: 'CREATE')
                    button(constraints: 'EDIT')
                    button(constraints: 'DELETE')
                },
                constraints: 'grow, pushy'
        )
        communicationPanelGroup = context.mvcGroup

        label(app.getMessage('unit.label.relations.text', 'Relations'), constraints: 'grow, pushy')
        label(app.getMessage('unit.label.note.text', 'Notes'), constraints: 'grow, pushy')

        widget(
                metaComponentFix('masterlist',
                        tableFormat: defaultAdvancedTableFormat(columns: [
                                [
                                        name: 'relation',
                                        comparator: { a, b -> relationDisplay(a, model.unit.delegate) <=> relationDisplay(b, model.unit.delegate) } as Comparator,
                                        read: { obj, columns, idx -> obj },
                                        class: String
                                ],
                                [
                                        name: 'type',
                                        comparator: { a, b -> relationTypeDisplay(a, model.unit.delegate) <=> relationTypeDisplay(b, model.unit.delegate) } as Comparator,
                                        read: { obj, columns, idx -> obj },
                                        class: String
                                ],
                                [
                                        name: 'description',
                                        class: String
                                ]
                        ]),
                        columnModel: columnModelFix {
                            column("relation",
                                    modelIndex: 0,
                                    headerValue: app.getMessage('unit.relations.header.relation.text'),
                                    width: [250]) {
                                cellRenderer {
                                    onRender {
                                        children[0].text = relationDisplay(value, model.unit.delegate)
                                    }
                                }
                            }
                            column("type",
                                    modelIndex: 1,
                                    headerValue: app.getMessage('unit.relations.header.type.text'),
                                    width: [70]) {
                                cellRenderer {
                                    onRender {
                                        children[0].text = relationTypeDisplay(value, model.unit.delegate)
                                    }
                                }
                            }
                            column("description",
                                    modelIndex: 2,
                                    headerValue: app.getMessage('unit.relations.header.description.text'),
                                    width: [150]) {
                            }
                        },
                        loader: controller.loadRelations,
                        filter: [
                                relation: { Relation item, Relation value, String filter -> !(filter && !relationDisplay(item, value.unit).toLowerCase().contains(filter.toLowerCase())) },
                                type: { Relation item, Relation value, String filter -> !(filter && !relationTypeDisplay(item, value.unit).toLowerCase().contains(filter.toLowerCase())) },
                                description: { Relation item, String value, String filter -> !(filter && !value?.toLowerCase()?.contains(filter.toLowerCase())) },
                        ],
                        comparator: { a, b -> relationTypeDisplay(a, model.unit.delegate) <=> relationTypeDisplay(b, model.unit.delegate) ?: relationDisplay(a, model.unit.delegate) <=> relationDisplay(b, model.unit.delegate) ?: a.description <=> b.description },
                        messagePrefix: 'mainlist.',
                        create: controller.createRelation,
                        edit: controller.editRelation,
                        delete: controller.deleteRelation,
                ) {
                    label(constraints: 'GROW')
                    button(constraints: 'CREATE')
                    button(constraints: 'EDIT')
                    button(constraints: 'DELETE')
                },
                constraints: 'grow, pushy'
        )
        relationPanelGroup = context.mvcGroup

        scrollPane(constraints: 'grow, pushy') {
            textArea(rows: 5, text: bind('note', source: model.unit, mutual: true))
        }
    }
    panel(constraints: SOUTH) {
        gridLayout(cols: 2, rows: 1)
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
