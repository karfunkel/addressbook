package org.aklein.address

import ca.odell.glazedlists.BasicEventList
import ca.odell.glazedlists.EventList
import ca.odell.glazedlists.FilterList
import ca.odell.glazedlists.SortedList
import ca.odell.glazedlists.matchers.Matcher
import com.avaje.ebean.EbeanServer
import org.aklein.address.db.AddressType
import org.aklein.address.db.Unit_Address
import org.aklein.dialog.DialogModelBase

class AddressSelectionModel extends DialogModelBase {
    @Bindable List<Unit_Address> addressList
    EventList<Unit_Address> list
    @Bindable AddressType addressType
    @Bindable String note
    EventList<AddressType> addressTypes = new SortedList(new BasicEventList(), { a, b -> a.name <=> b.name } as Comparator)
    EventList<AddressType> filteredTypes

    AddressSelectionModel() {
        withEbean { String ebeanServerName, EbeanServer server ->
            addressTypes.addAll(server.createQuery(AddressType).findList())
        }
    }

    void setAddressList(List<Unit_Address> addressList) {
        this.addressList = addressList
        filteredTypes = new FilterList(addressTypes, { item ->
            return !addressList.addressType.id.contains(item?.id)
        } as Matcher)
    }
}

