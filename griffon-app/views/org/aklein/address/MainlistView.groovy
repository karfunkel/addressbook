package org.aklein.address

import org.aklein.address.db.Unit

import java.awt.Color

String orga = app.getMessage('application.flag.organisation').toLowerCase()
String pers = app.getMessage('application.flag.person').toLowerCase()

panel(id: 'mainPanel') {
    migLayout(layoutConstraints: 'wrap 1, fill')
    widget( constraints: 'grow, pushy',
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
                                    name: 'category',
                                    comparator: { a, b -> a?.name?.join(', ') ?: '' <=> b?.name?.join(', ') ?: '' } as Comparator,
                                    read: { obj, columns, idx ->
                                        obj.unitCategories?.category
                                    },
                                    class: String
                            ],
                    ]
            ),
            columnModel: columnModelFix {
                column("name", modelIndex: 0, headerValue: app.getMessage('mainlist.table.header.name.text'), width: [50, 100])
                column("nation", modelIndex: 1, headerValue: app.getMessage('mainlist.table.header.nation.text'), width: [200]) {
                    cellRenderer {
                        onRender {
                            children[0].text = value?.iso3 ?: ''
                            children[0].toolTipText = value?.name ?: ''
                        }
                    }
                }
                column("type", modelIndex: 2, headerValue: app.getMessage('mainlist.table.header.type.text'), width: [50]) {
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
                column("category", modelIndex: 3, headerValue: app.getMessage('mainlist.table.header.category.text'), width: [50, 100]) {
                    cellRenderer {
                        onRender {
                            children[0].text = value?.name?.join(', ') ?: ''
                        }
                    }
                }
            },
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
                if (filter.Category && !item?.unitCategories?.category?.name?.join('_')?.toLowerCase()?.contains(filter.Category.toLowerCase()))
                    return false
                if (filter.Nation && !item?.nation?.iso3?.toLowerCase()?.contains(filter.Nation.toLowerCase()))
                    return false
                return true
            },
            comparator: { a, b -> a.name <=> b.name },
            messagePrefix: 'mainlist.',
            create: controller.create,
            edit: controller.edit,
            delete: controller.delete,
    ) {
        masterGroup = parentContext.mvcGroup
        masterModel = parentContext.mvcGroup.model
        label(text: bind(
                source: masterModel.selected,
                sourceEvent: 'listChanged',
                sourceValue: { masterModel.selected.size() },
                converter: { it ? app.getMessage('mainlist.label.visibleLines.text', [it], '{0} entries') : '' }),
                visible: bind(source: masterModel.selected, sourceEvent: 'listChanged', sourceValue: { masterModel.selected }),
                background: Color.RED,
                constraints: 'TRAILING'
        )
        label(constraints: 'GROW')
        button(constraints: 'CREATE')
        button(constraints: 'EDIT')
        button(constraints: 'DELETE')
    })
    mainPanelGroup = masterGroup
}