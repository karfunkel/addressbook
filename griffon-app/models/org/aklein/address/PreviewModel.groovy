package org.aklein.address

import org.aklein.dialog.DialogModelBase

import java.sql.Connection

class PreviewModel extends DialogModelBase {
    @Bindable int width = 800
    @Bindable int height = 500
    @Bindable Map parameter
    @Bindable File reportFile
    @Bindable Connection con
}