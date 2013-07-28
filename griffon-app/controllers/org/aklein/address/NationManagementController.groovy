package org.aklein.address

import org.aklein.crudlist.CrudListControllerBase

class NationManagementController extends CrudListControllerBase<NationManagementModel, NationManagementView> {
    NationManagementController() {
        super('nationManagement', 'nation', { NationModel model -> model.nation.delegate })
    }
}
