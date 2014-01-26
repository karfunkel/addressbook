package org.aklein.address

import ca.odell.glazedlists.BasicEventList
import ca.odell.glazedlists.EventList
import ca.odell.glazedlists.FilterList
import ca.odell.glazedlists.gui.TableFormat
import ca.odell.glazedlists.matchers.AbstractMatcherEditor
import ca.odell.glazedlists.matchers.Matcher
import ca.odell.glazedlists.matchers.MatcherEditor
import ca.odell.glazedlists.swing.EventSelectionModel

import java.util.Timer
import javax.swing.*
import javax.swing.table.TableColumnModel

class MasterlistModel {
    GriffonApplication app

    final Closure DEFAULT_DELETE_CONFIRM = { toDelete ->
        return JOptionPane.showConfirmDialog(
                null,
                app.getMessage("${_messagePrefix}masterlist.confirm.delete.message", [toDelete.size()], app.getMessage('masterlist.confirm.delete.message', [toDelete.size()], 'Do you want to delete {0} entries?')),
                app.getMessage("${_messagePrefix}masterlist.confirm.delete.title", app.getMessage('masterlist.confirm.delete.title', 'Delete entries')),
                JOptionPane.YES_NO_OPTION
        ) == JOptionPane.YES_OPTION
    }

    TableFormat _tableFormat
    TableColumnModel _columnModel
    ObservableMap _filterMap = [:]
    FilterList filtered
    MasterlistMatcherEditor matcherEditor
    EventList list = new BasicEventList()
    EventList visible
    EventList selected
    EventSelectionModel selectionModel

    boolean _loadOnStartup = true
    boolean customComponents = false
    Closure _loader
    Closure _create
    Closure _show
    Closure _edit
    Closure _delete
    Closure _dclick
    String _messagePrefix = ''
    Collection hiddenColumns = []

    void set_messagePrefix(String messagePrefix) {
        if (!messagePrefix)
            this._messagePrefix = ''
        else
            this._messagePrefix = messagePrefix + '.'
    }
}

class MasterlistMatcherEditor extends AbstractMatcherEditor {
    MasterlistModel model
    def filter
    Matcher matcher

    MasterlistMatcherEditor(MasterlistModel model, def filter) {
        super()
        this.model = model
        this.filter = filter

        if (filter instanceof Closure)
            this.matcher = { def item ->
                filter(item, model._filterMap)
            } as Matcher
        else if ((filter instanceof Collection) || (filter.getClass().isArray())) {
            this.matcher = { def item ->
                for (def f : filter) {
                    if (!(f instanceof Closure))
                        throw new IllegalArgumentException("filter may only contain elements of type $Closure for masterlist()")
                    if (!f(item, model._filterMap))
                        return false
                }
                return true
            } as Matcher
        } else if ((filter instanceof Map)) {
            this.matcher = { def item ->
                for (def key : filter.keySet()) {
                    def f = filter[key]
                    if (!(f instanceof Closure))
                        throw new IllegalArgumentException("filter may only contain elements of type $Closure for masterlist()")
                    try {
                        def v = model._tableFormat.getColumnValue(item, model._columnModel.getColumnIndex(key))
                        //def v = item[key]
                        def fv = model._filterMap[key.capitalize()]
                        if (!f(item, v, fv))
                            return false
                    } catch (MissingPropertyException e) {
                        throw new IllegalArgumentException("Map-key $key for filter have to map with properties of the item for masterlist()", e)
                    } catch (MissingMethodException e) {
                        throw new IllegalArgumentException("Filter for key $key does not exist or its parameters do not map for masterlist()", e)
                    }
                }
                return true
            } as Matcher
        } else {
            throw new IllegalArgumentException("filter has to be of type $Closure, a $Collection of Closures or an Array of Closures for masterlist()")
        }

        Timer timer = new Timer()
        TimerTask task
        model._filterMap.addPropertyChangeListener({ PropertyChangeEvent evt ->
            if (task)
                task.cancel()
            task = new TimerTask() {
                @Override
                void run() {
                    fireChanged(matcher)
                    task = null
                }
            }
            timer.schedule(task, 250)
            timer.purge()
        } as PropertyChangeListener)
    }

    void fireChanged() {
        fireChanged(matcher)
    }
}