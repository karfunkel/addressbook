package org.aklein.address

import org.aklein.dialog.DialogControllerBase

class CommunicationDetailController extends DialogControllerBase<CommunicationDetailModel, CommunicationDetailView> {
    def editTypes = { evt ->
        withMVCGroup('communicationTypeManagement', value: [] + model.communicationTypes, noCancel: true) { m, v, c ->
            m.title = app.getMessage('communicationTypeManagement.dialog.title', 'Manage communication types')
            c.show()
            if (!m.cancelled) {
                for (def communicationType : m.list) {
                    if (!model.communicationTypes.contains(communicationType))
                        model.communicationTypes << communicationType
                }
            }
        }
    }
}
