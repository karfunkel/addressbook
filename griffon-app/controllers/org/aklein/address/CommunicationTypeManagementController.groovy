package org.aklein.address

import org.aklein.crudlist.CrudListControllerBase

class CommunicationTypeManagementController extends CrudListControllerBase<CommunicationTypeManagementModel, CommunicationTypeManagementView> {
    CommunicationTypeManagementController() {
        super('communicationTypeManagement', 'communicationType', { CommunicationTypeModel model -> model.communicationType.delegate })
    }
}
