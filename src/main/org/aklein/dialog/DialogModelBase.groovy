package org.aklein.dialog

import ca.odell.glazedlists.EventList
import groovy.beans.Bindable

class DialogModelBase {
    @Bindable String title
    @Bindable int width = 0
    @Bindable int height = 0
    @Bindable boolean resizable = true
    @Bindable boolean modal = true
    @Bindable boolean cancelled = true
}
