package org.aklein.address

import com.avaje.ebean.EbeanServer
import com.avaje.ebean.SqlQuery
import com.healthmarketscience.jackcess.Database
import griffon.plugins.datasource.DataSourceConnector
import griffon.plugins.ebean.EbeanConnector
import griffon.transform.Threading
import groovy.sql.Sql
import org.aklein.address.db.Tables
import org.aklein.address.db.Unit

import javax.swing.*
import java.util.prefs.Preferences

class AddressController {
    // these will be injected by Griffon
    def model
    def view

    private def oldMVC

    GoogleService googleService

    void mvcGroupInit(Map<String, Object> args) {
        withEbean { String ebeanServerName, EbeanServer server ->
            SqlQuery query = server.createSqlQuery("select * from information_schema.columns where table_name = 'UNIT' AND COLUMN_NAME='VERSION';")
            if (query.findList().size() == 0) {
                server.beginTransaction()
                try {
                    server.execute(server.createCallableSql("ALTER TABLE ADDRESS ADD VERSION TIMESTAMP DEFAULT '2000-01-01 12:00:00' NOT NULL;"))
                    server.execute(server.createCallableSql("ALTER TABLE ADDRESS_TYPE ADD COLUMN VERSION TIMESTAMP DEFAULT '2000-01-01 12:00:00' NOT NULL;"))
                    server.execute(server.createCallableSql("ALTER TABLE CATEGORY ADD COLUMN VERSION TIMESTAMP DEFAULT '2000-01-01 12:00:00' NOT NULL;"))
                    server.execute(server.createCallableSql("ALTER TABLE COMMUNICATION ADD COLUMN VERSION TIMESTAMP DEFAULT '2000-01-01 12:00:00' NOT NULL;"))
                    server.execute(server.createCallableSql("ALTER TABLE COMMUNICATION_TYPE ADD COLUMN VERSION TIMESTAMP DEFAULT '2000-01-01 12:00:00' NOT NULL;"))
                    server.execute(server.createCallableSql("ALTER TABLE GLOBALS ADD COLUMN VERSION TIMESTAMP DEFAULT '2000-01-01 12:00:00' NOT NULL;"))
                    server.execute(server.createCallableSql("ALTER TABLE NATION ADD COLUMN VERSION TIMESTAMP DEFAULT '2000-01-01 12:00:00' NOT NULL;"))
                    server.execute(server.createCallableSql("ALTER TABLE RELATION ADD COLUMN VERSION TIMESTAMP DEFAULT '2000-01-01 12:00:00' NOT NULL;"))
                    server.execute(server.createCallableSql("ALTER TABLE SALUTATION ADD COLUMN VERSION TIMESTAMP DEFAULT '2000-01-01 12:00:00' NOT NULL;"))
                    server.execute(server.createCallableSql("ALTER TABLE UNIT ADD COLUMN VERSION TIMESTAMP DEFAULT '2000-01-01 12:00:00' NOT NULL;"))
                    server.execute(server.createCallableSql("ALTER TABLE UNIT_ADDRESS ADD COLUMN VERSION TIMESTAMP DEFAULT '2000-01-01 12:00:00' NOT NULL;"))
                    server.execute(server.createCallableSql("ALTER TABLE UNIT_CATEGORY ADD COLUMN VERSION TIMESTAMP DEFAULT '2000-01-01 12:00:00' NOT NULL;"))
                    server.execute(server.createCallableSql("ALTER TABLE UNIT_COMMUNICATION ADD COLUMN VERSION TIMESTAMP DEFAULT '2000-01-01 12:00:00' NOT NULL;"))
                    JOptionPane.showMessageDialog(null, app.getMessage('dbUpdate', 'Database updated - please restart'))

                } catch (e) {
                    server.rollbackTransaction()
                } finally {
                    server.commitTransaction()
                }
            }
        }
    }

    def onReadyEnd = { app ->
        edt {
            view.mainlistButton.doClick()
        }
    }

    // Navigation actions
    // Should be run in the Thread in which this method is called from
    @Threading(Threading.Policy.SKIP)
    def navigate = { mvcName ->
        model.currentElement = view."${mvcName}Button".text
        view.group.setSelected(view."${mvcName}Button".model, true)
        view.content.layout.show(view.content, mvcName)
        app.event('Navigation', [mvcName, oldMVC])
        oldMVC = mvcName
    }

    // Menu actions
    def exit = { evt = null ->
        app.shutdown()
        //def window = app.windowManager.findWindow('MainFrame')
        //window.processWindowEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
    }

    def about = { evt = null ->
        withMVCGroup('about') { m, v, c ->
            c.show()
        }
    }

    def preferences = { evt = null ->
        withMVCGroup('preferences') { m, v, c ->
            c.show()
        }
    }

    def emailExport = { evt = null ->
    }

    def envelope = { evt = null ->
    }

    def letter = { evt = null ->
    }

    def email = { evt = null ->
    }

    def labels = { evt = null ->
    }

    def reports = { evt = null ->
    }

    def dbExport = { evt = null ->
        EbeanConnector con = EbeanConnector.instance
        try {
            withEbean { String ebeanServerName, EbeanServer server ->
                server.execute(server.createCallableSql('CHECKPOINT SYNC'))
            }
        } catch (e) {}

        ConfigObject dsConfig = DataSourceConnector.instance.createConfig(app)
        String url = dsConfig.dataSource.url.substring(8)
        String homeDir = System.properties['user.home'].replaceAll('\\\\', '/')
        String dbName = new File(url).name
        url = url.replace('~', homeDir)
        File dbFile = new File(url + '.h2.db')

        String now = new Date().format('yyyy.MM.dd-HH:mm:ss')
        JFileChooser fc = view.dbExportDialog
        fc.selectedFile = new File(model.dbExportFolder ?: '.', "${dbName}_${now}.h2.db")
        if (fc.showSaveDialog() != JFileChooser.APPROVE_OPTION)
            return

        def selected = fc.selectedFile
        model.dbExportFolder = selected.parentFile.absolutePath
        if (!selected.name.endsWith('.h2.db'))
            selected = new File(selected.parentFile, selected.name + '.h2.db')

        if (selected.exists() && JOptionPane.showConfirmDialog(null, app.getMessage('exportExists.message', [selected.absolutePath], 'Do you want to overwrite {0}?'), app.getMessage('exportExists.dialog.title', 'Databasefile already exists'), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return

        copy(dbFile, selected)
    }

    def dbImport = { evt = null ->
        EbeanConnector con = EbeanConnector.instance
        try {
            try {
                withEbean { String ebeanServerName, EbeanServer server ->
                    server.execute(server.createCallableSql('SHUTDOWN COMPACT'))
                }
                con.disconnect(app)
            } catch (e) {}

            ConfigObject dsConfig = DataSourceConnector.instance.createConfig(app)
            String url = dsConfig.dataSource.url.substring(8)
            String homeDir = System.properties['user.home'].replaceAll('\\\\', '/')
            url = url.replace('~', homeDir)
            File dbFile = new File(url + '.h2.db')

            JFileChooser fc = view.dbImportDialog
            fc.currentDirectory = new File(model.dbImportFolder ?: '.')
            if (fc.showOpenDialog() != JFileChooser.APPROVE_OPTION)
                return

            def selected = fc.selectedFile
            model.dbImportFolder = selected.parentFile.absolutePath
            if (selected.exists())
                if (JOptionPane.showConfirmDialog(null, app.getMessage('importConfirmation.message', [selected.absolutePath], 'All data will be replaced. Are you sure?'), app.getMessage('importConfirmation.dialog.title', 'Import complete database'), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    copy(dbFile, new File(dbFile.absolutePath + '.sav'))
                    copy(selected, dbFile)
                }

        } finally {
            con.connect(app)
        }
    }

    protected def copy(File src, File dest) {
        def input = src.newInputStream()
        def output = dest.newOutputStream()
        output << input
        input.close()
        output.close()
    }

    def importDb = { evt = null ->
        def fc = view.openImportDialog
        fc.currentDirectory = new File(model.importFile ?: '.')
        if (fc.showOpenDialog() != JFileChooser.APPROVE_OPTION)
            return

        try {
            Tables.importing = true
            def selected = fc.selectedFile
            model.importFile = selected.parentFile.absolutePath

            withEbean { String ebeanServerName, EbeanServer server ->
                Database db = Database.open(selected)

                def tables = app.config.database.importData
                server.beginTransaction()
                try {
                    for (String tableNames : tables.keySet()) {
                        def (oldTable, newTable) = tableNames.split('__')
                        def t = db.getTable(oldTable)
                        int s = t.rowCount
                        int c = 0
                        for (Map<String, Object> row : t) {
                            model.status = app.getMessage('application.status.ImportDb.table', [newTable, ++c, s])
                            def instance = Class.forName("org.aklein.address.db.$newTable").newInstance()
                            tables[tableNames].each { k, v ->
                                def targetCls = instance.metaClass.properties.find { it.name == k }.type
                                def val = row[v]
                                if (v.toLowerCase().endsWith('id') && v != 'O_UStId') {
                                    if (val == 0)
                                        val = -6666
                                    else if (val == -1)
                                        val = -9999
                                    else if (val instanceof Number)
                                        val = -val
                                }
                                if (val instanceof Date)
                                    val = new Date(val.time)
                                if (targetCls?.package?.name == 'org.aklein.address.db')
                                    if (val == null)
                                        instance[k] = val
                                    else
                                        instance[k] = server.find(targetCls, val)
                                else
                                    instance[k] = val
                            }
                            if (instance instanceof Unit)
                                instance.editDate = new Date()
                            server.save(instance)
                        }
                    }
                    server.commitTransaction()
                } catch (e) {
                    server.rollbackTransaction()
                    throw e
                }
                model.status = app.getMessage('application.status.ImportDb.finished')
                app.mvcGroupManager.views.mainlist.masterGroup.controller.loadData()
                db.close()
            }
        } finally {
            Tables.importing = false
        }
    }

    def onOSXAbout = { app ->
        withMVCGroup('about') { m, v, c ->
            c.show()
        }
    }

    def onOSXQuit = { app ->
        exit()
    }

    def onOSXPrefs = { app ->
        withMVCGroup('preferences') { m, v, c ->
            c.show()
        }
    }

    def googleSync = { evt = null ->
        openSync app.getMessage('googleSync.dialog.title', 'Synchronize with Google'), true, true
    }

    def googleImport = { evt = null ->
        openSync app.getMessage('googleImport.dialog.title', 'Import from Google'), true, false
    }

    def googleExport = { evt = null ->
        openSync app.getMessage('googleExport.dialog.title', 'Export to Google'), false, true
    }

    protected openSync = { title, importGoogle, exportGoogle ->
        Preferences preferences = Preferences.userRoot().node('/org/aklein/addressbook')
        String userId = preferences.get('google/userId', null)
        withMVCGroup('googleSync', value: userId) { m, v, c ->
            m.title = title
            c.show()
            if (!m.cancelled) {
                preferences.put('google/userId', m.userId)
                preferences.flush()
                googleService.sync m.userId, importGoogle, exportGoogle
            }
        }
    }
}
