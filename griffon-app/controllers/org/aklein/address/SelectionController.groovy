package org.aklein.address

import com.avaje.ebean.EbeanServer
import griffon.transform.Threading
import org.aklein.address.db.Unit

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

    }

    def select = {
        def toAdd = view.listGroup.model.selected
        model._list.readWriteLock.writeLock().lock()
        try {
            for(def add: toAdd) {
                if(!model._list.contains(add))
                    model._list.addAll add
            }
        }
        finally {
            model._list.readWriteLock.writeLock().unlock()
        }
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
