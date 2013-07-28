package org.aklein.address

import org.aklein.address.db.CommunicationType
import org.aklein.dialog.DialogModelBase

class CommunicationTypeModel extends DialogModelBase {
    @Bindable BindDelegate<CommunicationType> communicationType = new BindDelegate(new CommunicationType())

    void setCommunicationType(CommunicationType communicationType) {
        this.communicationType = new BindDelegate<CommunicationType>(communicationType)
    }
}