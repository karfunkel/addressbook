package org.aklein.address

import ca.odell.glazedlists.EventList
import ca.odell.glazedlists.FilterList
import ca.odell.glazedlists.event.ListEvent
import ca.odell.glazedlists.event.ListEventListener
import ca.odell.glazedlists.matchers.AbstractMatcherEditor
import ca.odell.glazedlists.matchers.Matcher

class MultiselectionModel {
    GriffonApplication app

    def _tableFormat
    def _columnModel
    def _filter
    def _comparator
    String _messagePrefix = ''
    boolean _loadOnStartup = true
    def _list
    def _loader
    FilterList _availableList
    EventList selected
}

class MultiselectionMatcherEditor extends AbstractMatcherEditor {
    MultiselectionModel model
    Matcher matcher

    MultiselectionMatcherEditor(MultiselectionModel model) {
        super()
        this.model = model
        this.matcher = { def item -> !model.selected.contains(item) } as Matcher
        model._list.addListEventListener({ ListEvent evt -> fireChanged(matcher) } as ListEventListener)
        model.selected.addListEventListener({ ListEvent evt -> fireChanged(matcher) } as ListEventListener)
    }
}