package org.aklein.crudlist

import ca.odell.glazedlists.EventList
import groovy.beans.Bindable
import org.aklein.dialog.DialogModelBase

class CrudListModelBase<T> extends DialogModelBase {
    EventList<T> list
}
