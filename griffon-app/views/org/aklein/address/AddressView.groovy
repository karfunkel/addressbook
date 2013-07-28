package org.aklein.address

import javax.swing.JFileChooser
import javax.swing.filechooser.FileFilter

build(AddressActions)

optionPane(id: 'popup')
fileChooser(id: "openImportDialog",
        dialogTitle: app.getMessage('application.dialog.openImport.title', "Please choose a source filename"),
        fileSelectionMode: JFileChooser.FILES_ONLY
)
fileChooser(id: "dbImportDialog",
        dialogTitle: app.getMessage('application.dialog.dbImport.title', "Please choose a database to import"),
        fileSelectionMode: JFileChooser.FILES_ONLY,
        fileFilter: [getDescription: {-> "*.h2.db"}, accept:{file-> file ==~ /.*?\.h2.db/ || file.isDirectory() }] as FileFilter
)
fileChooser(id: "dbExportDialog",
        dialogTitle: app.getMessage('application.dialog.dbExport.title', "Please choose a location to save the database to"),
        fileSelectionMode: JFileChooser.FILES_ONLY,
        fileFilter: [getDescription: {-> "*.h2.db"}, accept:{file-> file ==~ /.*?\.h2.db/ || file.isDirectory() }] as FileFilter
)

application(title: GriffonNameUtils.capitalize(app.getMessage('application.title', app.config.application.title)),
        name: 'MainFrame',
        preferredSize: [1000, 700],
        pack: true,
        //location: [50,50],
        locationByPlatform: true,
        iconImage: imageIcon('/griffon-icon-48x48.png').image,
        iconImages: [imageIcon('/griffon-icon-48x48.png').image,
                imageIcon('/griffon-icon-32x32.png').image,
                imageIcon('/griffon-icon-16x16.png').image]) {
    widget(build(AddressMenu))
    migLayout(layoutConstraints: 'fill')
    buttonGroup(id: 'group')
    panel(id: 'navigation', constraints: 'dock west, width 150') {
        migLayout(layoutConstraints: 'wrap 1, fill', columnConstraints: 'fill', rowConstraints: 'fill')
        toggleButton(id: 'mainlistButton', action: mainlistAction, buttonGroup: group)
        toggleButton(id: 'selectionButton', action: selectionAction, buttonGroup: group)
    }
    panel(id: 'content', constraints: 'dock center') {
        cardLayout()
        widget(app.views.mainlist.mainPanel, constraints: 'mainlist')
        widget(app.views.selection.mainPanel, constraints: 'selection')
    }
    controller.navigate('mainlist')
    panel(build(AddressStatusBar), constraints: 'dock south')
}
