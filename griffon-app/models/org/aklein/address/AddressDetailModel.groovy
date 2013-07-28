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
import org.aklein.address.db.Unit
import org.aklein.address.db.Unit_Address
import org.aklein.dialog.DialogModelBase

class AddressDetailModel extends DialogModelBase {
    @Bindable List<Unit_Address> addressList
    @Bindable AddressType addressType
    @Bindable String note
    @Bindable BindDelegate<Address> address = new BindDelegate(new Address())

    EventList<Nation> nations = new SortedList(new BasicEventList(), { a, b -> a.name <=> b.name } as Comparator)
    EventList<AddressType> addressTypes = new SortedList(new BasicEventList(), { a, b -> a.name <=> b.name } as Comparator)
    EventList<AddressType> filteredTypes

    AddressDetailModel() {
        withEbean { String ebeanServerName, EbeanServer server ->
            nations.addAll(server.createQuery(Nation).findList())
            addressTypes.addAll(server.createQuery(AddressType).findList())
        }
    }

    void setAddress(Address address) {
        this.address = new BindDelegate<Address>(address)
    }

    void setAddressList(List<Unit_Address> addressList) {
        this.addressList = addressList
        filteredTypes = new FilterList(addressTypes, { item ->
            if (item?.id == addressType?.id)
                return true
            return !addressList.addressType.id.contains(item?.id)
        } as Matcher)
    }
}