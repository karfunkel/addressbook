package org.aklein.address

import org.aklein.address.db.Salutation
import org.aklein.dialog.DialogModelBase

class SalutationModel extends DialogModelBase {
    @Bindable BindDelegate<Salutation> salutation = new BindDelegate(new Salutation())

    void setSalutation(Salutation salutation) {
        this.salutation = new BindDelegate<Salutation>(salutation)
    }
}