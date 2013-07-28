package org.aklein.address

import ca.odell.glazedlists.matchers.AbstractMatcherEditor
import ca.odell.glazedlists.matchers.Matcher

class SubTypeMatcherEditor extends AbstractMatcherEditor {
    Matcher matcher

    SubTypeMatcherEditor(List list, String subType) {
        super()
        List ids = list."$subType".id
        this.matcher = { def item ->
            return !ids.contains(item.id)
        } as Matcher
    }
}
