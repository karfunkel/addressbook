package org.aklein.address


def i18nAction = UIHelper.&i18nAction.curry(app, builder)

actions {
    i18nAction('mainlistAction', { evt = null -> controller.navigate('mainlist') })
    i18nAction('selectionAction', { evt = null -> controller.navigate('selection') })
    i18nAction('emailExportAction', controller.emailExport)
    i18nAction('envelopeAction', controller.envelope)
    i18nAction('letterAction', controller.letter)
    i18nAction('emailAction', controller.email)
    i18nAction('labelsAction', controller.labels)
    i18nAction('reportsAction', controller.reports)
    i18nAction('exitAction', controller.exit)
    i18nAction('aboutAction', controller.about)
    i18nAction('importDbAction', controller.importDb)
    i18nAction('preferencesAction', controller.preferences)
    i18nAction('googleSyncAction', controller.googleSync)
    i18nAction('googleImportAction', controller.googleImport)
    i18nAction('googleExportAction', controller.googleExport)
    i18nAction('dbExportAction', controller.dbExport)
    i18nAction('dbImportAction', controller.dbImport)
}
