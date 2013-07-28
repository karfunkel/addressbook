package org.aklein.address

import ca.odell.glazedlists.BasicEventList
import ca.odell.glazedlists.EventList
import ca.odell.glazedlists.SortedList
import com.avaje.ebean.EbeanServer
import groovy.beans.Bindable
import org.aklein.address.db.Category
import org.aklein.address.db.Nation
import org.aklein.address.db.Salutation
import org.aklein.address.db.Unit
import org.aklein.dialog.DialogModelBase

class UnitModel extends DialogModelBase {
    @Bindable BindDelegate<Unit> unit = new BindDelegate(new Unit())

    void setUnit(Unit unit) {
        this.unit = new BindDelegate<Unit>(unit)
    }

    EventList<Salutation> salutations = new SortedList(new BasicEventList(), { a, b -> a.address <=> b.address } as Comparator)
    EventList<Nation> nations = new SortedList(new BasicEventList(), { a, b -> a.name <=> b.name } as Comparator)

    EventList<Category> categories = new SortedList(new BasicEventList(), { a, b -> a.name <=> b.name } as Comparator)

    UnitModel() {
        width = 1280
        withEbean { String ebeanServerName, EbeanServer server ->
            salutations.addAll(server.createQuery(Salutation).findList())
            nations.addAll(server.createQuery(Nation).findList())
            categories.addAll(server.createQuery(Category).findList())
        }
    }
}
