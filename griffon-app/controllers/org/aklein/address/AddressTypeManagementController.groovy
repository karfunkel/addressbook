package org.aklein.address



import org.aklein.crudlist.*

class AddressTypeManagementController extends CrudListControllerBase<AddressTypeManagementModel, AddressTypeManagementView> {
    AddressTypeManagementController() {
        super('addressTypeManagement', 'addressType', { AddressTypeModel model -> model.addressType.delegate })
    }
}
