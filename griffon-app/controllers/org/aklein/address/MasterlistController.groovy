package org.aklein.address

import ca.odell.glazedlists.BasicEventList
import ca.odell.glazedlists.EventList
import ca.odell.glazedlists.FilterList
import ca.odell.glazedlists.SortedList
import griffon.transform.Threading

import javax.swing.JTable
import javax.swing.JViewport
import java.awt.*

/**
 * <p>Usage:
 *
 * <p>
 * widget(metaComponent('masterlist', [options]){ <br/>
 *     &nbsp;&nbsp;[children]<br/>
 *})
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
 *         <td>tableFormat, e.g. defined with defaultTableFormat() or defaultAdvancedTableFormat()</td>
 *     </tr>
 *     <tr><td>columnModel</td>
 *         <td>javax.swing.table.TableColumnModel</td>
 *         <td>yes</td>
 *         <td>-</td>
 *         <td>columnModel, e.g. defined with columnModel()</td>
 *     </tr>
 *     <tr><td>list</td>
 *         <td>ca.odell.glazedlists.EventList or java.util.List</td>
 *         <td>either list or loader has to be defined</td>
 *         <td>new ca.odell.glazedlists.BasicEventList()</td>
 *         <td>List-instance on which all operations base on</td>
 *     </tr>
 *     <tr><td>loader</td>
 *         <td>groovy.lang.Closure</td>
 *         <td>either list or loader has to be defined</td>
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
 *     <tr><td>create</td>
 *         <td>groovy.lang.Closure</td>
 *         <td>no</td>
 *         <td>-</td>
 *         <td>Arguments: none<br/>
 *             Returns: the new item</td>
 *     </tr>
 *     <tr><td>show</td>
 *         <td>groovy.lang.Closure</td>
 *         <td>no</td>
 *         <td>-</td>
 *         <td>Arguments: none<br/>
 *             Returns: nothing</td>
 *     </tr>
 *     <tr><td>edit</td>
 *         <td>groovy.lang.Closure</td>
 *         <td>no</td>
 *         <td>-</td>
 *         <td>Arguments: the item to edit<br/>
 *             Returns: The changed item to continue or null to cancel the processing</td>
 *     </tr>
 *     <tr><td>delete</td>
 *         <td>groovy.lang.Closure</td>
 *         <td>no</td>
 *         <td>Confirmation dialog</td>
 *         <td>Arguments: java.util.List with all items selected to delete<br/>
 *             Returns: java.util.Collection of items to really delete or an expression evaluation true to continue
 *             deleting the selected items.
 *         </td>
 *     </tr>
 *     <tr><td>doubleclick</td>
 *         <td>String or groovy.lang.Closure</td>
 *         <td>no</td>
 *         <td>edit</td>
 *         <td>Name of the button to click or a Closure with the action to call on doubleclick.<br>
 *             Arguments: the item doubleclicked to<br/>
 *             Returns: nothing</td>
 *     </tr>
 * </table>
 *
 * <p>[children]:
 * <p> If no children are defined, four buttons (Create, Show, Edit, Delete) will be created if the corresponding
 * Closure-[options] are defined. They will all be aligned to the right.
 *
 * <p> Any java.awt.Component can be used as a child.
 * <p> With the constraints argument you can define if a child should be added 'LEADING' or 'TRAILING' to the list of
 * children. The default is 'TRAILING'
 * <p> To use the default buttons, use any Component with the constraints 'CREATE', 'SHOW', 'EDIT' or 'DELETE'.
 * <p> When using any Component with the constraints 'GROW' an empty label will be created filling the available space.
 * 'GROW' can be used multiple times.
 *
 */
class MasterlistController {
    MasterlistModel model
    MasterlistView view

    def loadData = {
        if (model._loader) {
            def result = model._loader()
            if (result instanceof Collection) {
                model.list.readWriteLock.writeLock().lock()
                try {
                    model.list.addAll result
                }
                finally {
                    model.list.readWriteLock.writeLock().unlock()
                }
            }
        }
    }

    def clearData = {
        model.list.readWriteLock.writeLock().lock()
        try {
            model.list.clear()
        }
        finally {
            model.list.readWriteLock.writeLock().unlock()
        }
    }

    def reloadData() {
        model.list.readWriteLock.writeLock().lock()
        try {
            model.list.clear()
            if (model._loader) {
                def result = model._loader()
                if (result instanceof Collection) {
                    model.list.addAll result
                }
            }
        }
        finally {
            model.list.readWriteLock.writeLock().unlock()
        }
    }

    void mvcGroupInit(Map args) {
        if (!model._tableFormat)
            throw new IllegalArgumentException('tableFormat is mandatory for masterlist()')
        if (!model._columnModel)
            throw new IllegalArgumentException('columnModel is mandatory for masterlist()')

        for (int c = 0; c < model._tableFormat.columnCount; c++) {
            def name = model._tableFormat.getColumnName(c)
            model._filterMap[name] = ''
        }

        if (model._loadOnStartup)
            loadData()
    }

    @Threading(Threading.Policy.SKIP)
    void handleAttributes(Map attributes) {
        model._tableFormat = attributes.remove('tableFormat')
        model._columnModel = attributes.remove('columnModel')
        def list = attributes.remove('list')
        model._loader = attributes.remove('loader')
        if (model._loader != null && list != null)
            throw new IllegalArgumentException('either list or loader is mandatory for masterlist()')

        def filter = attributes.remove('filter')
        def comparator = attributes.remove('comparator')

        if (comparator instanceof Closure)
            comparator = comparator as Comparator
        else if (comparator && !(comparator instanceof Comparator))
            throw new IllegalArgumentException("comparator has to be of type $Closure or $Comparator for masterlist()")

        if (list instanceof EventList)
            model.list = list
        else if (!list)
            model.list = new BasicEventList()
        else
            model.list = new BasicEventList(list)

        if (filter)
            model.filtered = new FilterList(model.list, new MasterlistMatcherEditor(model, filter))
        else
            model.filtered = model.list

        if (comparator)
            model.visible = new SortedList(model.filtered, comparator)
        else
            model.visible = model.filtered

        model._messagePrefix = attributes.remove('messagePrefix') ?: ''
        model._loadOnStartup = attributes.remove('loadOnStartup') ?: true
        model._create = attributes.remove('create') ?: model._create
        model._show = attributes.remove('show') ?: model._edit
        model._edit = attributes.remove('edit') ?: model._show
        model._delete = attributes.remove('delete') ?: model._delete
        def dclick = attributes.remove('doubleclick') ?: 'edit'
        if (dclick instanceof String)
            model._dclick = { item -> view."$dclick".doClick() }
        else if (dclick instanceof Closure)
            model._dclick = dclick
    }

    Component setChild(FactoryBuilderSupport builder, def parent, def child) {
        if (!(child instanceof Component))
            return null
        def constraints = builder.context.constraints ?: 'TRAILING'
        if (constraints instanceof CharSequence)
            constraints = constraints.toString().toUpperCase()
        def tools = view.tools
        if (!model.customComponents) {
            model.customComponents = true
            tools.removeAll()
        }
        switch (constraints) {
            case 'LEADING':
                tools.add(child, 0)
                break
            case 'CREATE':
                child = view.defaultComponents.CREATE
                tools.add child
                break
            case 'EDIT':
                child = view.defaultComponents.EDIT
                tools.add child
                view.builder.bind(source: model.selected, sourceEvent: 'listChanged', sourceValue: { model.selected.size() == 1 }, target: child.action, targetProperty: 'enabled')
                break
            case 'DELETE':
                child = view.defaultComponents.DELETE
                tools.add child
                view.builder.bind(source: model.selected, sourceEvent: 'listChanged', sourceValue: { model.selected.size() }, target: child.action, targetProperty: 'enabled')
                break
            case 'SHOW':
                child = view.defaultComponents.SHOW
                tools.add child
                view.builder.bind(source: model.selected, sourceEvent: 'listChanged', sourceValue: { model.selected.size() == 1 }, target: child.action, targetProperty: 'enabled')
                break
            case 'GROW':
                child = builder.noparent { label('') }.last()
                tools.add(child, 'grow, pushx')
                break
            case 'TRAILING':
            default:
                tools.add child
        }
        return child
    }

    def createAction = {
        def item
        if (model._create)
            item = model._create()
        else
            return
        if (item != null) {
            model.list.readWriteLock.writeLock().lock()
            try {
                model.list << item
            }
            finally {
                model.list.readWriteLock.writeLock().unlock()
            }
        }
    }

    def showAction = {
        if (model._show)
            model._show(model.selected[0])
    }

    def editAction = {
        def item = model.selected[0]
        if (item != null) {
            int index = model.list.indexOf(item)
            if (!model._edit)
                return
            def result = model._edit(item)
            if (!result)
                return
            model.list.readWriteLock.writeLock().lock();
            try {
                model.list.set(index, result)
            }
            finally {
                model.list.readWriteLock.writeLock().unlock();
            }
        }
    }

    def deleteAction = {
        def toDelete = new ArrayList(model.selected)
        def result
        if (model._delete)
            result = model._delete(toDelete)
        else
            return
        if (result) {
            if (result instanceof Collection)
                toDelete = result
            try {
                model.list.readWriteLock.writeLock().lock()
                model.list.removeAll toDelete
            }
            finally {
                model.list.readWriteLock.writeLock().unlock()
            }
        }
    }

    def dclickAction = {
        def item = model.selected[0]
        if (item != null && model._dclick) {
            model._dclick(item)
        }
    }

    def addSelection(def elements) {
        elements.each {
            int idx = model.visible.indexOf(it)
            model.selectionModel.addSelectionInterval(idx, idx)
        }
    }

    def setSelection(def elements) {
        model.selectionModel.clearSelection()
        addSelection(elements)
    }

    def scrollToSelection() {
        int rowIdx = model.selectionModel.getMinSelectionIndex()
        int colIdx = 0

        JTable table = view.mainTable
        if (!(table.parent instanceof JViewport))
            return
        if (table.rowCount < 1)
            return
        JViewport viewport = table.parent
        // view dimension
        Dimension dim = viewport.extentSize
        // cell dimension
        Dimension dimOne = new Dimension(0, 0)

        // This rectangle is relative to the table where the
        // northwest corner of cell (0,0) is always (0,0).
        Rectangle rect = table.getCellRect(rowIdx, colIdx, true)
        Rectangle rectOne
        if (rowIdx + 1 < table.rowCount) {
            if (colIdx + 1 < table.columnCount)
                colIdx++
            rectOne = table.getCellRect(rowIdx + 1, colIdx, true)
            dimOne.width = rectOne.x - rect.x
            dimOne.height = rectOne.y - rect.y
        }

        // '+ view dimension - cell dimension' to set first selected row on the top
        rect.setLocation((int) (rect.x + dim.width - dimOne.width), ((int) rect.y + dim.height - dimOne.height))
        table.scrollRectToVisible(rect)
    }
}
