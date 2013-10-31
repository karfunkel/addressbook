package org.aklein.address

import org.aklein.address.db.Unit

import javax.swing.JSplitPane
import java.text.ParseException

String orga = app.getMessage('application.flag.organisation').toLowerCase()
String pers = app.getMessage('application.flag.person').toLowerCase()

def unitView = new UnitView()
unitView.app = app

panel(id: 'mainPanel') {
    migLayout(columnConstraints: '[fill][200:200:200]', layoutConstraints: 'fill')
    widget(constraints: 'grow, pushy',
            metaComponentFix('masterlist',
                    tableFormat: defaultAdvancedTableFormat(
                            columns: [
                                    [
                                            name: 'name',
                                            class: String
                                    ],
                                    [
                                            name: 'nation',
                                            comparator: { a, b -> a?.iso3 <=> b?.iso3 } as Comparator,
                                            class: String
                                    ],
                                    [
                                            name: 'type',
                                            class: String
                                    ],
                                    [
                                            name: 'birthday',
                                            comparator: { a, b -> a <=> b } as Comparator,
                                            class: Date
                                    ],
                                    [
                                            name: 'note',
                                            class: String
                                    ],
                                    [
                                            name: 'salutation',
                                            comparator: { a, b -> a.abbrev <=> b.abbrev } as Comparator,
                                            read: { obj, columns, idx -> obj.salutation },
                                            class: String
                                    ],
                                    [
                                            name: 'category',
                                            comparator: { a, b -> a?.name?.join(', ') ?: '' <=> b?.name?.join(', ') ?: '' } as Comparator,
                                            read: { obj, columns, idx -> obj.unitCategories?.category },
                                            class: String
                                    ],
                                    [
                                            name: 'address',
                                            comparator: { a, b -> a?.collect { unitView.addressDisplay(it) }?.join(', ') ?: '' <=> b?.collect { unitView.addressDisplay(it) }?.join(', ') ?: '' } as Comparator,
                                            read: { obj, columns, idx -> obj.unitAddresses?.address },
                                            class: String
                                    ],
                                    [
                                            name: 'communication',
                                            comparator: { a, b -> a?.collect { unitView.unitCommunicationDisplay(it) }?.join(', ') ?: '' <=> b?.collect { unitView.unitCommunicationDisplay(it) }?.join(', ') ?: '' } as Comparator,
                                            read: { obj, columns, idx -> obj.unitCommunications },
                                            class: String
                                    ],
                                    [
                                            name: 'relation',
                                            read: { obj, columns, idx ->
                                                (obj.sourceRelations + obj.targetRelations)?.collect { unitView.relationFullDisplay(it, obj) }?.join(', ')
                                            },
                                            class: String
                                    ],
                            ]
                    ),
                    columnModel: columnModelFix {
                        column("name", modelIndex: 0, headerValue: app.getMessage('selectionlist.table.header.name.text'), width: [50, 200])
                        column("nation", modelIndex: 1, headerValue: app.getMessage('selectionlist.table.header.nation.text'), width: [50, 50]) {
                            cellRenderer {
                                onRender {
                                    children[0].text = value?.iso3 ?: ''
                                    children[0].toolTipText = value?.name ?: ''
                                }
                            }
                        }
                        column("type", modelIndex: 2, headerValue: app.getMessage('selectionlist.table.header.type.text'), width: [50, 50]) {
                            cellRenderer {
                                onRender {
                                    if (value == Unit.ORGANISATION)
                                        children[0].text = app.getMessage('application.flag.organisation')
                                    else if (value == Unit.PERSON)
                                        children[0].text = app.getMessage('application.flag.person')
                                    else
                                        children[0].text = ''
                                }
                            }
                        }
                        def dateFormat = app.getMessage('selectionlist.table.header.birthday.format')
                        column("birthday", modelIndex: 3, headerValue: app.getMessage('selectionlist.table.header.birthday.text'), width: [50, 100]) {
                            cellRenderer {
                                onRender {
                                    children[0].text = value?.format(dateFormat) ?: ''
                                }
                            }
                        }
                        column("note", modelIndex: 4, headerValue: app.getMessage('selectionlist.table.header.note.text'), width: [50, 200])
                        column("salutation", modelIndex: 5, headerValue: app.getMessage('selectionlist.table.header.salutation.text'), width: [50, 100]) {
                            cellRenderer {
                                onRender {
                                    children[0].text = value?.abbrev ?: ''
                                }
                            }
                        }
                        column("category", modelIndex: 6, headerValue: app.getMessage('selectionlist.table.header.category.text'), width: [50, 100]) {
                            cellRenderer {
                                onRender {
                                    children[0].text = value?.name?.join(', ') ?: ''
                                }
                            }
                        }
                        column("address", modelIndex: 7, headerValue: app.getMessage('selectionlist.table.header.address.text'), width: [50, 200]) {
                            cellRenderer {
                                onRender {
                                    children[0].text = value?.collect { unitView.addressDisplay(it) }?.join(', ') ?: ''
                                }
                            }
                        }
                        column("communication", modelIndex: 8, headerValue: app.getMessage('selectionlist.table.header.communication.text'), width: [50, 100]) {
                            cellRenderer {
                                onRender {
                                    children[0].text = value?.collect { unitView.unitCommunicationDisplay(it) }?.join(', ') ?: ''
                                }
                            }
                        }
                        column("relation", modelIndex: 9, headerValue: app.getMessage('selectionlist.table.header.relation.text'), width: [50, 200])
                    },
                    hiddenColumns: ["nation", "type", "note", "salutation", "relation"],
                    loader: controller.loader,
                    filter: { Unit item, Map filter ->
                        if (filter.Name && !item?.name?.toLowerCase()?.contains(filter.Name.toLowerCase()))
                            return false

                        if (filter.Type) {
                            if (item.type == Unit.ORGANISATION && filter.Type.toLowerCase() != orga)
                                return false
                            if (item.type == Unit.PERSON && filter.Type.toLowerCase() != pers)
                                return false
                        }

                        if (filter.Nation && !item?.nation?.iso3?.toLowerCase()?.contains(filter.Nation.toLowerCase()))
                            return false

                        if (filter.Category && !item?.unitCategories?.category?.name?.join('_')?.toLowerCase()?.contains(filter.Category.toLowerCase()))
                            return false

                        if (filter.Note && !item?.note?.toLowerCase()?.contains(filter.Note.toLowerCase()))
                            return false

                        if (filter.Salutation && !item?.salutation?.abbrev?.toLowerCase()?.contains(filter.Salutation.toLowerCase()))
                            return false

                        if (filter.Birthday) {
                            def parts = filter.Birthday.split('-').collect { it.trim() }
                            Date from, to
                            if (parts.size() == 1) {
                                try {
                                    from = Date.parse(app.getMessage('selectionlist.table.filter.birthday.format'), parts[0])
                                    if (item?.birthday != from)
                                        return false
                                } catch (ParseException e) {
                                }
                            } else if (parts.size() > 1) {
                                try {
                                    from = Date.parse(app.getMessage('selectionlist.table.filter.birthday.format'), parts[0])
                                    to = Date.parse(app.getMessage('selectionlist.table.filter.birthday.format'), parts[1])
                                    if (item?.birthday < from || item?.birthday > to)
                                        return false
                                } catch (ParseException e) {
                                }
                            }
                        }
                        def a = item?.unitAddresses?.address?.collect { unitView.addressDisplay(it) }?.join(', ') ?: ''
                        if (filter.Address && !a?.toLowerCase()?.contains(filter.Address.toLowerCase()))
                            return false

                        def c = item?.unitCommunications?.collect { unitView.unitCommunicationDisplay(it) }?.join(', ') ?: ''
                        if (filter.Communication && !c?.toLowerCase()?.contains(filter.Communication.toLowerCase()))
                            return false

                        def r = (item.sourceRelations + item.targetRelations)?.collect { "${unitView.relationTypeDisplay(it, item)} ${unitView.relationDisplay(it, item)}" }?.join(', ')
                        if (filter.Relation && !r?.toLowerCase()?.contains(filter.Relation.toLowerCase()))
                            return false
                        return true
                    },
                    comparator: { a, b -> a.name <=> b.name },
                    messagePrefix: 'selectionlist.',
            ) {
                listGroup = parentContext.mvcGroup
                listModel = parentContext.mvcGroup.model
                listView = parentContext.mvcGroup.view
                label(constraints: 'GROW')
                button(listView.createButton('Select', controller.select), constraints: 'TRAILING')
            }
    )

    widget(constraints: 'grow, pushy',
            metaComponentFix('masterlist',
                    tableFormat: defaultAdvancedTableFormat(
                            columns: [
                                    [
                                            name: 'name',
                                            class: String
                                    ],
                                    /*
                                    [
                                            name: 'nation',
                                            comparator: { a, b -> a?.iso3 <=> b?.iso3 } as Comparator,
                                            class: String
                                    ],
                                    [
                                            name: 'type',
                                            class: String
                                    ],
                                    */
                            ]
                    ),
                    columnModel: columnModelFix {
                        column("name", modelIndex: 0, headerValue: app.getMessage('selectionlist.table.header.name.text'), width: [50, 200])
                        /*
                        column("nation", modelIndex: 1, headerValue: app.getMessage('selectionlist.table.header.nation.text'), width: [200]) {
                            cellRenderer {
                                onRender {
                                    children[0].text = value?.iso3 ?: ''
                                    children[0].toolTipText = value?.name ?: ''
                                }
                            }
                        }
                        column("type", modelIndex: 2, headerValue: app.getMessage('selectionlist.table.header.type.text'), width: [50]) {
                            cellRenderer {
                                onRender {
                                    if (value == Unit.ORGANISATION)
                                        children[0].text = app.getMessage('application.flag.organisation')
                                    else if (value == Unit.PERSON)
                                        children[0].text = app.getMessage('application.flag.person')
                                    else
                                        children[0].text = ''
                                }
                            }
                        }
                        */
                    },
                    list: model._list,
                    filter: { Unit item, Map filter ->
                        if (filter.Name && !item?.name?.toLowerCase()?.contains(filter.Name.toLowerCase()))
                            return false
                        /*
                        if (filter.Type) {
                            if (item.type == Unit.ORGANISATION && filter.Type.toLowerCase() != orga)
                                return false
                            if (item.type == Unit.PERSON && filter.Type.toLowerCase() != pers)
                                return false
                        }
                        if (filter.Nation && !item?.nation?.iso3?.toLowerCase()?.contains(filter.Nation.toLowerCase()))
                            return false
                        */
                        return true
                    },
                    comparator: { a, b -> a.name <=> b.name },
                    messagePrefix: 'selectionlist.',
                    delete: controller.delete,
            ) {
                selectionGroup = parentContext.mvcGroup
                selectionModel = parentContext.mvcGroup.model
                selectionView = parentContext.mvcGroup.view
                label(constraints: 'GROW')
                button(constraints: 'DELETE')
                button(selectionView.createButton('Print', controller.print), constraints: 'TRAILING')
            }
    )
}