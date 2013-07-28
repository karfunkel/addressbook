package org.aklein.address

import groovy.beans.Bindable
import org.aklein.address.db.Category
import org.aklein.dialog.DialogModelBase

class CategoryModel extends DialogModelBase {
    @Bindable BindDelegate<Category> category = new BindDelegate(new Category())

    void setCategory(Category category) {
        this.category = new BindDelegate<Category>(category)
    }
}