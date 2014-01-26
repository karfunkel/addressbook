package org.aklein.address

import org.aklein.address.db.Unit
import org.aklein.address.db.Unit_Address

import javax.swing.JFileChooser
import java.text.ParseException

fileChooser(id: "sourceDialog",
        dialogTitle: app.getMessage('application.dialog.sourceDialog.title', "Please choose a report"),
        fileSelectionMode: JFileChooser.FILES_ONLY,
        fileFilter: [getDescription: {-> "*.jrxml" }, accept: { file -> file ==~ /.*?\.jrxml/ || file.isDirectory() }] as javax.swing.filechooser.FileFilter
)

String orga = app.getMessage('application.flag.organisation').toLowerCase()
String pers = app.getMessage('application.flag.person').toLowerCase()

panel(id: 'mainPanel') {
    migLayout(columnConstraints: '[fill][400:400:400]', layoutConstraints: 'fill')
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
                                            comparator: { a, b -> a?.collect { it.display }?.join(', ') ?: '' <=> b?.collect { it.display }?.join(', ') ?: '' } as Comparator,
                                            read: { obj, columns, idx ->
                                                obj.unitAddresses?.address
                                            },
                                            class: String
                                    ],
                                    [
                                            name: 'communication',
                                            comparator: { a, b -> a?.collect { it.display() }?.join(', ') ?: '' <=> b?.collect { it.display }?.join(', ') ?: '' } as Comparator,
                                            read: { obj, columns, idx -> obj.unitCommunications },
                                            class: String
                                    ],
                                    [
                                            name: 'relation',
                                            read: { obj, columns, idx ->
                                                (obj.sourceRelations + obj.targetRelations)?.collect { it.getFullDisplay(obj) }?.join(', ')
                                            },
                                            constraints: 'right', class: String
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
                                    children[0].text = value?.collect { it.display }?.join(', ') ?: ''
                                }
                            }
                        }
                        column("communication", modelIndex: 8, headerValue: app.getMessage('selectionlist.table.header.communication.text'), width: [50, 100]) {
                            cellRenderer {
                                onRender {
                                    children[0].text = value?.collect { it.display }?.join(', ') ?: ''
                                }
                            }
                        }
                        column("relation", modelIndex: 9, headerValue: app.getMessage('selectionlist.table.header.relation.text'), width: [50, 200])
                    },
                    hiddenColumns: ["nation", "type", "note", "salutation", "relation", "communication"],
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
                        def a = item?.unitAddresses?.address?.collect { it.display }?.join(', ') ?: ''
                        if (filter.Address && !a?.toLowerCase()?.contains(filter.Address.toLowerCase()))
                            return false

                        def c = item?.unitCommunications?.collect { it.display }?.join(', ') ?: ''
                        if (filter.Communication && !c?.toLowerCase()?.contains(filter.Communication.toLowerCase()))
                            return false

                        def r = (item.sourceRelations + item.targetRelations)?.collect { "${it.getTypeDisplay(item)} ${it.getDisplay(item)}" }?.join(', ')
                        if (filter.Relation && !r?.toLowerCase()?.contains(filter.Relation.toLowerCase()))
                            return false
                        return true
                    },
                    comparator: { a, b -> a.name <=> b.name },
                    messrelationagePrefix: 'selectionlist.',
                    doubleclick: controller.select
            ) {
                listGroup = parentContext.mvcGroup
                listModel = parentContext.mvcGroup.model
                listView = parentContext.mvcGroup.view
                label(constraints: 'GROW')
                button(listView.createButton(app.getMessage('selectionlist.button.select.text'), controller.select), constraints: 'TRAILING')
            }
    )
    widget(constraints: 'grow, pushy',
            metaComponentFix('masterlist',
                    tableFormat: defaultAdvancedTableFormat(
                            columns: [
                                    [
                                            name: 'name',
                                            comparator: { a, b -> a.unit.name <=> b.unit.name } as Comparator,
                                            read: { obj, columns, idx -> obj.unit.name },
                                            class: String
                                    ],
                                    [
                                            name: 'display',
                                            comparator: { a, b -> a.address.display <=> b.address.display } as Comparator,
                                            read: { obj, columns, idx -> obj.address.display },
                                            class: String
                                    ]
                            ]
                    ),
                    columnModel: columnModelFix {
                        column("name", modelIndex: 0, headerValue: app.getMessage('selectionlist.table.header.name.text'), width: [50, 200]) {
                            cellRenderer {
                                onRender {
                                    children[0].text = value ?: ''
                                    children[0].toolTipText = value ?: ''
                                }
                            }
                        }
                        column("display", modelIndex: 1, headerValue: app.getMessage('selectionlist.table.header.address.text'), width: [50, 400]) {
                            cellRenderer {
                                onRender {
                                    children[0].text = value ?: ''
                                    children[0].toolTipText = value ?: ''
                                }
                            }
                        }
                    },
                    list: model._list,
                    filter: { Unit_Address item, Map filter ->
                        if (filter.Name && !item?.unit?.name?.toLowerCase()?.contains(filter.Name.toLowerCase()))
                            return false
                        if (filter.Display && !item?.address?.display?.toLowerCase()?.contains(filter.Display.toLowerCase()))
                            return false
                        if (model.filterDoublets && item?.unit?.unitAddresses?.size() <= 1)
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
                    comparator: { a, b -> a.unit.name <=> b.unit.name ?: a.address.display <=> b.address.display },
                    messagePrefix: 'selectionlist.',
                    delete: controller.delete,
                    doubleclick: controller.delete
            ) {
                selectionGroup = parentContext.mvcGroup
                selectionModel = parentContext.mvcGroup.model
                selectionView = parentContext.mvcGroup.view
                toggleButton(selectionView.createToggleButton(app.getMessage('selectionlist.button.doublets.text'), controller.doublets), constraints: 'TRAILING')
                label(constraints: 'GROW')
                button(constraints: 'DELETE')
                button(selectionView.createButton(app.getMessage('selectionlist.button.print.text'), controller.print), constraints: 'TRAILING')
            }
    )
}