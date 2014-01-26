package org.aklein.address

import ca.odell.glazedlists.FilterList
import ca.odell.glazedlists.matchers.MatcherEditor
import com.avaje.ebean.EbeanServer
import griffon.plugins.datasource.DataSourceHolder
import griffon.transform.Threading
import net.sf.jasperreports.engine.JREmptyDataSource
import net.sf.jasperreports.engine.JasperCompileManager
import net.sf.jasperreports.engine.JasperExportManager
import net.sf.jasperreports.engine.JasperFillManager
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.JasperReport
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
import net.sf.jasperreports.swing.JRViewer
import org.aklein.address.db.Address
import org.aklein.address.db.Unit
import org.aklein.address.db.Unit_Address
import org.aklein.address.db.Unit_Communication

import javax.swing.JFileChooser
import java.awt.Window
import java.util.prefs.Preferences

class SelectionController {
    // these will be injected by Griffon
    def model
    def view

    @Threading(Threading.Policy.SKIP)
    def loader = {
        def result
        withEbean { String ebeanServerName, EbeanServer server ->
            result = server.createQuery(Unit).findList()
        }
        return result
    }

    def print = {
        def window = Window.windows.find { it.focused }
        Preferences preferences = Preferences.userRoot().node('/org/aklein/addressbook')
        String file = preferences.get('report/last', null)

        JFileChooser fc = view.sourceDialog
        if (file)
            fc.selectedFile = new File(file)
        if (fc.showOpenDialog() != JFileChooser.APPROVE_OPTION)
            return

        def selected = fc.selectedFile
        preferences.put('report/last', selected.absolutePath)
        preferences.flush()

        if (!selected.name.endsWith('.jrxml'))
            selected = new File(selected.parentFile, selected.name + '.jrxml')

        if (selected.exists() && selected.file) {
            Map<String, String> parameter = [:]
            parameter.unit_list = (model._list.unit.id as Set).join(',')
            parameter.unit_address_list = model._list.id.join(',')
            withMVCGroup('preview', report: selected, params: parameter, connection: DataSourceHolder.instance.getDataSource().connection) { m, v, c ->
                m.title = app.getMessage("preview.dialog.title", "Report preview")
                c.show(window)
            }
        }
    }

    def select = {
        def toAdd = view.listGroup.model.selected

        model._list.readWriteLock.writeLock().lock()
        try {
            for (Unit person : toAdd) {
                for (Unit_Address ua : person.unitAddresses) {
                    if (!model._list.contains(ua))
                        model._list.add(ua)
                }
            }
        }
        finally {
            model._list.readWriteLock.writeLock().unlock()
        }
    }

    def doublets = { evt ->
        model.filterDoublets = evt.source.selected
        MasterlistMatcherEditor editor = view.selectionModel.matcherEditor
        if(editor)
            editor.fireChanged()
    }

    @Threading(Threading.Policy.SKIP)
    def delete = {
        def toRemove = view.selectionGroup.model.selected
        model._list.readWriteLock.writeLock().lock()
        try {
            model._list.removeAll toRemove
        }
        finally {
            model._list.readWriteLock.writeLock().unlock()
        }
    }
}
