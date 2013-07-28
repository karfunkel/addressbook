package org.aklein.address

import ca.odell.glazedlists.BasicEventList
import ca.odell.glazedlists.EventList
import ca.odell.glazedlists.FilterList
import ca.odell.glazedlists.event.ListEvent
import ca.odell.glazedlists.event.ListEventListener
import griffon.transform.Threading

/**
 * <p>Usage:
 *
 * <p>
 * widget(metaComponent('multiselection', [options]))
 *
 * <p>[options]:
 * <table>
 *     <tr><th>Name</th>
 *         <th>type</th>
 *         <th>mandatory</th>
 *         <th>default</th>
 *         <th>description</th>
 *     </tr>
 *     <tr><td>tableFormat</td>
 *         <td>ca.odell.glazedlists.gui.TableFormat</td>
 *         <td>yes</td>
 *         <td>-</td>
 *         <td>tableFormat for both tables, e.g. defined with defaultTableFormat() or defaultAdvancedTableFormat()</td>
 *     </tr>
 *     <tr><td>columnModel</td>
 *         <td>javax.swing.table.TableColumnModel</td>
 *         <td>yes</td>
 *         <td>-</td>
 *         <td>columnModel for both tables, e.g. defined with columnModel()</td>
 *     </tr>
 *     <tr><td>available</td>
 *         <td>ca.odell.glazedlists.EventList or java.util.List</td>
 *         <td>either available or loader has to be defined</td>
 *         <td>new ca.odell.glazedlists.BasicEventList()</td>
 *         <td>List-instance for all the available options on which all operations base on</td>
 *     </tr>
 *     <tr><td>selected</td>
 *         <td>ca.odell.glazedlists.EventList or java.util.List</td>
 *         <td>no</td>
 *         <td>new ca.odell.glazedlists.BasicEventList()</td>
 *         <td>List-instance for all selected options</td>
 *     </tr>
 *     <tr><td>loader</td>
 *         <td>groovy.lang.Closure</td>
 *         <td>either available or loader has to be defined</td>
 *         <td>{}</td>
 *         <td>Arguments: none<br/>
 *             Returns: java.util.Collection with the data to populate the list<br/>
 *             Closure to populate list with data</td>
 *     </tr>
 *     <tr><td>filter</td>
 *         <td>groovy.lang.Closure or java.util.Collection&lt;groovy.lang.Closure&gt;</td>
 *         <td>no</td>
 *         <td>unfiltered list</td>
 *         <td>Closure or Collection of Closure to define if an item is part of the filtered list. If one Closures in
 *         the Collection returns false, the item is not part of the list.</td>
 *     </tr>
 *     <tr><td>comparator</td>
 *         <td>groovy.lang.Closure or java.util.Comparator</td>
 *         <td>no</td>
 *         <td>unsorted list</td>
 *         <td>comparator to define the base-sorting of the list</td>
 *     </tr>
 *     <tr><td>messagePrefix</td>
 *         <td>java.lang.String</td>
 *         <td>no</td>
 *         <td>-</td>
 *         <td>Prefix to the keys to lookup in the i18n-files. If the key with prefix does not result a value, it tries
 *         to find a value with out the prefix before using the defaults.</td>
 *     </tr>
 *     <tr><td>loadOnStartup</td>
 *         <td>boolean</td>
 *         <td>no</td>
 *         <td>true</td>
 *         <td>Defines if the list should be populated automatically at startup. If loader is not defined, this has no
 *         effect.</td>
 *     </tr>
 * </table>
 */
class MultiselectionController {
    MultiselectionModel model
    MultiselectionView view

    def loadData = {
        if (model._loader) {
            def result = model._loader()
            if (result instanceof Collection) {
                model._list.readWriteLock.writeLock().lock()
                try {
                    model._list.addAll result
                }
                finally {
                    model._list.readWriteLock.writeLock().unlock()
                }
            }
        }
    }

    def clearData = {
        model._list.readWriteLock.writeLock().lock()
        try {
            model._list.clear()
        }
        finally {
            model._list.readWriteLock.writeLock().unlock()
        }
    }

    def reloadData() {
        model._list.readWriteLock.writeLock().lock()
        try {
            model._list.clear()
            if (model._loader) {
                def result = model._loader()
                if (result instanceof Collection) {
                    model._list.addAll result
                }
            }
        }
        finally {
            model._list.readWriteLock.writeLock().unlock()
        }
    }

    void mvcGroupInit(Map args) {
        if (!model._tableFormat)
            throw new IllegalArgumentException('tableFormat is mandatory for masterlist()')
        if (!model._columnModel)
            throw new IllegalArgumentException('columnModel is mandatory for masterlist()')

        if (model._loadOnStartup)
            loadData()
    }

    @Threading(Threading.Policy.SKIP)
    void handleAttributes(Map attributes) {
        model._tableFormat = attributes.remove('tableFormat')
        model._columnModel = attributes.remove('columnModel')
        model._comparator = attributes.remove('comparator')
        model._filter = attributes.remove('filter')
        model._messagePrefix = attributes.remove('messagePrefix') ?: ''
        model._loadOnStartup = attributes.remove('loadOnStartup') ?: true
        model._loader = attributes.remove('loader')

        def list = attributes.remove('available')

        if (model._loader != null && list != null)
            throw new IllegalArgumentException('either list or loader is mandatory for multiselection()')

        if (list instanceof EventList)
            model._list = list
        else if (!list)
            model._list = new BasicEventList()
        else
            model._list = new BasicEventList(list)

        def selected = attributes.remove('selected')
        if (selected instanceof EventList)
            model.selected = selected
        else if (!selected)
            model.selected = new BasicEventList()
        else
            model.selected = new BasicEventList(selected)
        model._availableList = new FilterList(model._list, new MultiselectionMatcherEditor(model))
    }

    def addAction = {
        def toAdd = view.availableGroup.model.selected
        model.selected.readWriteLock.writeLock().lock()
        try {
            model.selected.addAll toAdd
        }
        finally {
            model.selected.readWriteLock.writeLock().unlock()
        }
    }

    def addallAction = {
        def toAdd = model._availableList
        model.selected.readWriteLock.writeLock().lock()
        try {
            model.selected.addAll toAdd
        }
        finally {
            model.selected.readWriteLock.writeLock().unlock()
        }
    }

    def removeAction = {
        def toRemove = view.selectedGroup.model.selected
        model.selected.readWriteLock.writeLock().lock()
        try {
            model.selected.removeAll toRemove
        }
        finally {
            model.selected.readWriteLock.writeLock().unlock()
        }
    }

    def removeallAction = {
        model.selected.readWriteLock.writeLock().lock()
        try {
            model.selected.clear()
        }
        finally {
            model.selected.readWriteLock.writeLock().unlock()
        }
    }
}
