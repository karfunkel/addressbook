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

    }
}
