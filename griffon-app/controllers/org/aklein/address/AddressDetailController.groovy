package org.aklein.address

import org.aklein.dialog.DialogControllerBase

class AddressDetailController extends DialogControllerBase<AddressDetailModel, AddressDetailView> {
    def editTypes = { evt ->
        withMVCGroup('addressTypeManagement', value: [] + model.addressTypes, noCancel: true) { m, v, c ->
            m.title = app.getMessage('addressTypeManagement.dialog.title', 'Manage address types')
            c.show()
            if (!m.cancelled) {
                for (def addressType : m.list) {
                    if (!model.addressTypes.contains(addressType))
                        model.addressTypes << addressType
                }
            }
        }
    }
}
