package org.aklein.address

import org.aklein.crudlist.CrudListControllerBase

class CategoryManagementController extends CrudListControllerBase<CategoryManagementModel, CategoryManagementView>{
    CategoryManagementController() {
        super('categoryManagement', 'category', { CategoryModel model -> model.category.delegate })
    }
}
