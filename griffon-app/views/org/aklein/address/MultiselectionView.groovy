package org.aklein.address

import java.beans.PropertyEditor
import java.beans.PropertyEditorManager

controller.handleAttributes(builder.variables)

def createButton(String name) {
    def key = name.toLowerCase()
    def icon = app.resolveResource("${model._messagePrefix}multiselection.button.${key}.icon", (Object) app.resolveResource("multiselection.button.${key}.icon", (Object) null))
    if (icon) {
        PropertyEditor propertyEditor = PropertyEditorManager.findEditor(Icon)
        propertyEditor.setAsText(String.valueOf(icon))
        icon = propertyEditor.getValue()
    }
    noparent {
        button(
                action(
                        name: app.getMessage("${model._messagePrefix}multiselection.button.${key}.text", app.getMessage("multiselection.button.${key}.text", name)),
                        mnemonic: app.getMessage("${model._messagePrefix}multiselection.button.${key}.mnemonic", (String) app.getMessage("multiselection.button.${key}.mnemonic", (String) null)),
                        smallIcon: icon,
                        shortDescription: app.getMessage("${model._messagePrefix}multiselection.button.${key}.description", (String) app.getMessage("multiselection.button.${key}.description", (String) null)),
                        accelerator: app.getMessage("${model._messagePrefix}multiselection.button.${key}.accelerator", (String) app.getMessage("multiselection.button.${key}.accelerator", (String) null)),
                        closure: controller."${key}Action"
                )
        )
    }.last()
}

panel {
    migLayout(layoutConstraints: 'fill', columnConstraints: '[grow][][][grow]', rowConstraints: '[grow][][grow]')

    widget(id: 'availableOptions', metaComponentFix('masterlist',
            tableFormat: model._tableFormat,
            columnModel: model._columnModel,
            list: model._availableList,
            comparator: model._comparator,
            messagePrefix: model._messagePrefix,
            loadOnStartup: model._loadOnStartup,
            filter: model._filter,
            doubleclick: controller.addAction,
    ), constraints: 'grow, cell 0 0 0 3')
    availableGroup = context.mvcGroup

    widget(id: 'selectedOptions', metaComponentFix('masterlist',
            tableFormat: model._tableFormat,
            columnModel: model._columnModel,
            list: model.selected,
            comparator: model._comparator,
            messagePrefix: model._messagePrefix,
            loadOnStartup: model._loadOnStartup,
            filter: model._filter,
            doubleclick: controller.removeAction,
    ), constraints: 'grow, cell 3 0 0 3')
    selectedGroup = context.mvcGroup

    button(createButton('addall'),
            enabled: bind(source: availableGroup.model.visible, sourceEvent: 'listChanged', sourceValue: { availableGroup.model.visible.size() }),
            constraints: 'growx, cell 1 0 2 0, bottom')
    button(createButton('remove'),
            enabled: bind(source: selectedGroup.model.selected, sourceEvent: 'listChanged', sourceValue: { selectedGroup.model.selected.size() }),
            constraints: 'grow, cell 1 1')
    button(createButton('add'),
            enabled: bind(source: availableGroup.model.selected, sourceEvent: 'listChanged', sourceValue: { availableGroup.model.selected.size() }),
            constraints: 'grow, cell 2 1')
    button(createButton('removeall'),
            enabled: bind(source: selectedGroup.model.visible, sourceEvent: 'listChanged', sourceValue: { selectedGroup.model.visible.size() }),
            constraints: 'growx, cell 1 2 2 0, top')


}
