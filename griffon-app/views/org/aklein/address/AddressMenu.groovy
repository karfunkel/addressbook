package org.aklein.address

import static griffon.util.GriffonApplicationUtils.isMacOSX

menuBar = menuBar {
    menu(text: app.getMessage('application.menu.File.name', 'File'), mnemonic: app.getMessage('application.menu.File.mnemonic', 'F')) {
        menuItem(mainlistAction)
        menuItem(selectionAction)
        if (!isMacOSX) {
            separator()
            menuItem(exitAction)
        }
    }
    menu(text: app.getMessage('application.menu.Actions.name', 'Actions'), mnemonic: app.getMessage('application.menu.Actions.mnemonic', 'A')) {
        menuItem(emailExportAction)
        separator(visible: bind { model.currentElement == 'Selection' })
        menuItem(envelopeAction, visible: bind { model.currentElement == 'Selection' })
        menuItem(letterAction, visible: bind { model.currentElement == 'Selection' })
        menuItem(emailAction, visible: bind { model.currentElement == 'Selection' })
        menuItem(labelsAction, visible: bind { model.currentElement == 'Selection' })
        menuItem(reportsAction, visible: bind { model.currentElement == 'Selection' })
    }
    menu(text: app.getMessage('application.menu.Extra.name', 'Extra'), mnemonic: app.getMessage('application.menu.Extra.mnemonic', 'E')) {
        menuItem(importDbAction)
        separator()
        menuItem(googleSyncAction)
        menuItem(googleExportAction)
        menuItem(googleImportAction)
        /*
        separator()
        menuItem(dbExportAction)
        menuItem(dbImportAction)
        */
    }
    glue()
    menu(text: app.getMessage('application.menu.Help.name', 'Help'), mnemonic: app.getMessage('application.menu.Help.mnemonic', 'H')) {
        menuItem(aboutAction)
        menuItem(preferencesAction)
    }
}
return menuBar

