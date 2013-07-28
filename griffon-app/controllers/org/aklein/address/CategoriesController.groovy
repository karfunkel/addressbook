package org.aklein.address

import griffon.transform.Threading
import org.aklein.dialog.DialogControllerBase

class CategoriesController extends DialogControllerBase<CategoriesModel, CategoriesView> {
    @Threading(Threading.Policy.SKIP)
    def manage = { evt = null ->
        withMVCGroup('categoryManagement', value: [] + model.available, noCancel: true) { m, v, c ->
            m.title = app.getMessage('categoryManagement.dialog.title', 'Manage categories')
            c.show()
            if (!m.cancelled) {
                for (def category : m.list) {
                    if (!model.available.contains(category))
                        model.available << category
                }
            }
        }
    }
}
