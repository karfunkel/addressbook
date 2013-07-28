package org.aklein.address

import ca.odell.glazedlists.BasicEventList
import ca.odell.glazedlists.EventList
import com.avaje.ebean.EbeanServer
import groovy.beans.Bindable

import org.aklein.address.db.Category
import org.aklein.dialog.DialogModelBase

class CategoriesModel extends DialogModelBase {
    EventList<Category> selected
    EventList<Category> available = new BasicEventList<Category>()

    CategoriesModel() {
        withEbean { String ebeanServerName, EbeanServer server ->
            available.addAll(server.createQuery(Category).findList())
        }
    }
}