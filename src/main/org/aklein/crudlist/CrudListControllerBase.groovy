package org.aklein.crudlist

import com.avaje.ebean.EbeanServer
import griffon.transform.Threading
import org.aklein.dialog.DialogControllerBase

import java.awt.Window

class CrudListControllerBase<M extends CrudListModelBase, V> extends DialogControllerBase<M, V> {
    String artifactName
    String calledMvcGroup
    Closure entityAccessor

    CrudListControllerBase(String artifactName, String calledMvcGroup, Closure entityAccessor) {
        this.artifactName = artifactName
        this.calledMvcGroup = calledMvcGroup
        this.entityAccessor = entityAccessor
    }

    @Threading(Threading.Policy.SKIP)
    def create = {
        def result
        withMVCGroup(calledMvcGroup) { m, v, c ->
            m.title = app.getMessage("${artifactName}.dialog.${calledMvcGroup}.create.title", "Create ${calledMvcGroup}")
            c.show()
            if (!m.cancelled) {
                withEbean { String ebeanServerName, EbeanServer server ->
                    def entity = entityAccessor(m)
                    server.insert(entity)
                    result = server.find(entity.getClass(), entity.id)
                }
            }
        }
        return result
    }

    @Threading(Threading.Policy.SKIP)
    def edit = { item ->
        def result = null
        withEbean { String ebeanServerName, EbeanServer server ->
            item = server.find(item.getClass(), item.id)
        }
        withMVCGroup(calledMvcGroup, value: item) { m, v, c ->
            m.title = app.getMessage("${artifactName}.dialog.${calledMvcGroup}.edit.title", "Edit ${calledMvcGroup}")
            c.show()
            if (m.cancelled) {
                withEbean { String ebeanServerName, EbeanServer server ->
                    result = server.find(item.getClass(), item.id)
                }
            } else {
                withEbean { String ebeanServerName, EbeanServer server ->
                    server.save(item)
                }
                result = item
            }
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
