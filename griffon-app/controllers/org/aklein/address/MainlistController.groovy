package org.aklein.address

import com.avaje.ebean.EbeanServer
import griffon.transform.Threading
import org.aklein.address.db.Unit

class MainlistController {
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

    @Threading(Threading.Policy.SKIP)
    def create = {
        def result
        withMVCGroup('unit') { m, v, c ->
            m.title = app.getMessage('mainlist.dialog.unit.create.title', 'Create unit')
            c.show()
            if (!m.cancelled) {
                withEbean { String ebeanServerName, EbeanServer server ->
                    m.unit.delegate.editDate = new Date()
                    server.save(m.unit.delegate)
                    result = server.find(m.unit.delegate.getClass(), m.unit.delegate.id)
                }
            }
        }
        return result
    }

    @Threading(Threading.Policy.SKIP)
    def edit = { item ->
        withEbean { String ebeanServerName, EbeanServer server ->
            item = server.find(item.getClass(), item.id)
        }
        return openUnitDialog(item)
    }

    @Threading(Threading.Policy.SKIP)
    def openUnitDialog = { item ->
        def result = null
        try {
            withMVCGroup('unit', value: item) { m, v, c ->
                m.title = app.getMessage('mainlist.dialog.unit.edit.title', 'Edit unit')
                c.show()
                if (m.cancelled) {
                    withEbean { String ebeanServerName, EbeanServer server ->
                        result = server.find(item.getClass(), item.id)
                    }
                } else {
                    withEbean { String ebeanServerName, EbeanServer server ->
                        item.editDate = new Date()
                        server.save(item)
                        result = item
                    }
                }
            }
        } catch(e) {
            if(app.groups['unit'])
                destroyMVCGroup('unit')
        }
        return result
    }

    @Threading(Threading.Policy.SKIP)
    def delete = { toDelete ->
        boolean delete = view.mainPanelGroup.model.DEFAULT_DELETE_CONFIRM(toDelete)
        if (delete) {
            withEbean { String ebeanServerName, EbeanServer server ->
                server.delete(toDelete)
            }
        }
        return delete
    }

}
