package org.aklein.address

import groovy.beans.Bindable

import java.util.prefs.Preferences

@Bindable
class AddressModel {
    String currentElement = ' '
    String status
    String importFile = Preferences.userRoot().node('Address').get('importDB', null)
    String dbImportFolder = Preferences.userRoot().node('Address').get('dbImport', null)
    String dbExportFolder = Preferences.userRoot().node('Address').get('dbExport', null)

    void setImportFile(String importFile) {
        Preferences.userRoot().node('Address').put('importDB', importFile)
        this.importFile = importFile
    }

    void setDbImportFolder(String dbImportFolder) {
        Preferences.userRoot().node('Address').put('dbImport', dbImportFolder)
        this.dbImportFolder = dbImportFolder
    }

    void setDbExportFolder(String dbExportFolder) {
        Preferences.userRoot().node('Address').put('dbExport', dbExportFolder)
        this.dbExportFolder = dbExportFolder
    }
}