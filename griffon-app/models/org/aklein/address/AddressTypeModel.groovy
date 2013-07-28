package org.aklein.address

import org.aklein.address.db.AddressType
import org.aklein.dialog.DialogModelBase

class AddressTypeModel extends DialogModelBase {
    @Bindable BindDelegate<AddressType> addressType = new BindDelegate(new AddressType())

    void setAddressType(AddressType addressType) {
        this.addressType = new BindDelegate<AddressType>(addressType)
    }
}