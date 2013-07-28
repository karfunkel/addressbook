package org.aklein.address

import org.aklein.address.db.Nation
import org.aklein.dialog.DialogModelBase

class NationModel extends DialogModelBase {
    @Bindable BindDelegate<Nation> nation = new BindDelegate(new Nation())

    void setNation(Nation nation) {
        this.nation = new BindDelegate<Nation>(nation)
    }
}