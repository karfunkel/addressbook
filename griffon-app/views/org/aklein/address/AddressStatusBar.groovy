package org.aklein.address

import javax.swing.SwingConstants

panel(id: 'statusPanel') {
    migLayout(layoutConstraints: 'fill', columnConstraints: '5[150][fill, right]')
    label(id: 'status',
            text: bind { model.status },
            horizontalAlignment: SwingConstants.LEADING
    )
    label(id: 'currentElement',
            text: bind { model.currentElement },
            horizontalAlignment: SwingConstants.TRAILING
    )
}