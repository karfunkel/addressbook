package org.aklein.address

import org.aklein.crudlist.CrudListControllerBase

class SalutationManagementController extends CrudListControllerBase<SalutationManagementModel, SalutationManagementView> {
    SalutationManagementController() {
        super('salutationManagement', 'salutation', { SalutationModel model -> model.salutation.delegate })
    }
}
