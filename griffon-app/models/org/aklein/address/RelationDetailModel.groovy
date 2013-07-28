package org.aklein.address

import ca.odell.glazedlists.BasicEventList
import ca.odell.glazedlists.EventList
import ca.odell.glazedlists.FilterList
import ca.odell.glazedlists.SortedList
import ca.odell.glazedlists.matchers.Matcher
import com.avaje.ebean.EbeanServer
import org.aklein.address.db.Address
import org.aklein.address.db.AddressType
import org.aklein.address.db.Nation
import org.aklein.address.db.Relation
import org.aklein.address.db.Unit
import org.aklein.address.db.Unit_Address
import org.aklein.dialog.DialogModelBase

class RelationDetailModel extends DialogModelBase {
    @Bindable Unit unit
    @Bindable String description
    @Bindable String type

    EventList<Unit> units = new SortedList(new BasicEventList(), { a, b -> a.name <=> b.name } as Comparator)

    RelationDetailModel() {
        withEbean { String ebeanServerName, EbeanServer server ->
            units.addAll(server.createQuery(Unit).findList())
        }
    }
}