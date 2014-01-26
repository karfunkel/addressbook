package org.aklein.address

import ca.odell.glazedlists.BasicEventList
import ca.odell.glazedlists.EventList
import ca.odell.glazedlists.FilterList
import ca.odell.glazedlists.SortedList
import griffon.transform.Threading

import javax.swing.*
import javax.swing.event.ChangeEvent
import javax.swing.event.ListSelectionEvent
import javax.swing.event.TableColumnModelEvent
import javax.swing.event.TableColumnModelListener
import javax.swing.table.TableColumn
import javax.swing.table.TableColumnModel
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.MouseListener

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

        installColumnHider()

        if (model._loadOnStartup)
            loadData()
    }

    @Threading(Threading.Policy.SKIP)
    void handleAttributes(Map attributes) {
        model._tableFormat = attributes.remove('tableFormat')
        model._columnModel = attributes.remove('columnModel')
        if (!(model._columnModel instanceof HidingTableColumnModel))
            model._columnModel = new HidingTableColumnModel(model._columnModel)
        model._columnModel.addColumnModelListener(new VisibilityListener(model._columnModel))

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

        if (filter) {
            model.matcherEditor = new MasterlistMatcherEditor(model, filter)
            model.filtered = new FilterList(model.list, model.matcherEditor)
        } else
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

    protected void installColumnHider() {
        JTable mainTable = view.mainTable
        HidingTableColumnModel columnModel = mainTable.columnModel
        def menu = view.builder.popupMenu {
            def hidden = []
            for (int i = 0; i < columnModel.columnCount; i++) {
                def index = i
                def column = columnModel.getColumn(index)
                def selected = true
                if (model.hiddenColumns.contains(column.identifier)) {
                    selected = false
                    hidden << index
                }
                checkBoxMenuItem(text: columnModel.getColumn(index).headerValue.toString(), selected: selected, actionPerformed: { ActionEvent e ->
                    columnModel.setVisible(index, e.source.selected)
                })
            }
            hidden.each { idx ->
                columnModel.setVisible(idx, false)
            }
        }

        def evaluatePopup = { e ->
            if (e.popupTrigger)
                menu.show(mainTable.tableHeader, e.x, e.y)
        }
        mainTable.tableHeader.addMouseListener([
                mousePressed: { e -> evaluatePopup(e) },
                mouseReleased: { e -> evaluatePopup(e) },
                mouseClicked: {},
                mouseEntered: {},
                mouseExited: {},
        ] as MouseListener)
    }

    class VisibilityListener implements TableColumnModelListener {
        HidingTableColumnModel model

        VisibilityListener(HidingTableColumnModel model) {
            this.model = model
        }

        @Override
        void columnAdded(TableColumnModelEvent e) {
            String columnName = e.source.getColumn(e.toIndex).getIdentifier()
            view."filter${columnName.capitalize()}".visible = true
        }

        @Override
        void columnRemoved(TableColumnModelEvent e) {
            String columnName = e.source.getColumn(e.fromIndex).getIdentifier()
            view."filter${columnName.capitalize()}".visible = false
        }

        @Override
        void columnMoved(TableColumnModelEvent e) {
        }

        @Override
        void columnMarginChanged(ChangeEvent e) {
        }

        @Override
        void columnSelectionChanged(ListSelectionEvent e) {
        }
    }
}
class HidingTableColumnModel implements TableColumnModel {
    protected TableColumnModel model
    protected Map<TableColumn, Boolean> columnVisibility
    private java.util.List<Integer> visibleColumnIndexes = null
    private java.util.List<TableColumn> visibleColumns = null
    protected java.util.List<TableColumnModelListener> listeners = []

    HidingTableColumnModel(TableColumnModel model) {
        this.model = model
        this.columnVisibility = model.getColumns().toList().inject([:]) { map, v -> map[v] = true; return map }
    }

    void dirty() {
        visibleColumnIndexes = null
        visibleColumns = null
    }

    java.util.List getVisibleColumnIndexes() {
        if (visibleColumnIndexes != null)
            return visibleColumnIndexes
        visibleColumnIndexes = []
        def columns = model.getColumns().toList()
        for (int idx = 0; idx < columns.size(); idx++) {
            TableColumn column = columns[idx]
            if (columnVisibility[column])
                visibleColumnIndexes << idx
        }
        return visibleColumnIndexes
    }

    java.util.List getVisibleColumns() {
        if (visibleColumns != null)
            return visibleColumns
        visibleColumns = getVisibleColumnIndexes().collect { model.getColumn(it) }
    }

    boolean isVisible(int index, boolean onlyVisible = true) {
        return isVisible(getColumn(index, onlyVisible))
    }

    boolean isVisible(TableColumn column) {
        columnVisibility[column]
    }

    void setVisible(int index, boolean visible) {
        int idx
        if (visible) {
            columnVisibility[model.getColumn(index)] = visible
            dirty()
            idx = getColumnIndexByModelIndex(index)
            fireVisibiltyChanged(idx, visible)
        } else {
            idx = getColumnIndexByModelIndex(index)
            fireVisibiltyChanged(idx, visible)
            columnVisibility[model.getColumn(index)] = visible
            dirty()
        }
    }

    protected fireVisibiltyChanged(int index, boolean visible) {
        for (TableColumnModelListener listener : listeners) {
            if (visible)
                listener.columnAdded(new TableColumnModelEvent(this, -1, index))
            else
                listener.columnRemoved(new TableColumnModelEvent(this, index, -1))
        }
    }

    int getColumnIndexByVisibleIndex(int columnIndex) {
        if (columnIndex < 0)
            return columnIndex
        getVisibleColumnIndexes()[columnIndex]
    }

    int getColumnIndexByModelIndex(int columnIndex) {
        if (columnIndex < 0)
            return columnIndex
        try {
            TableColumn column = model.getColumn(columnIndex)
            return getVisibleColumns().indexOf(column)
        } catch (ArrayIndexOutOfBoundsException e) {
            return getVisibleColumns().size()
        }
    }

    @Override
    void addColumn(TableColumn column) {
        dirty()
        model.addColumn(column)
        columnVisibility[column] = true
    }

    @Override
    void removeColumn(TableColumn column) {
        dirty()
        model.removeColumn(column)
        columnVisibility[column] = false
    }

    @Override
    void moveColumn(int columnIndex, int newIndex, boolean onlyVisible = true) {
        dirty()
        model.moveColumn(columnIndex, newIndex)
    }

    @Override
    void setColumnMargin(int newMargin) {
        model.setColumnMargin(newMargin)
    }

    @Override
    int getColumnCount(boolean onlyVisible = true) {
        return onlyVisible ? getVisibleColumnIndexes().size() : model.getColumnCount()
    }

    @Override
    Enumeration<TableColumn> getColumns(boolean onlyVisible = true) {
        return onlyVisible ? Collections.enumeration(getVisibleColumns()) : model.columns
    }

    @Override
    int getColumnIndex(Object columnIdentifier, boolean onlyVisible = true) {
        int idx = model.getColumnIndex(columnIdentifier)
        return onlyVisible ? getColumnIndexByModelIndex(idx) : idx
    }

    @Override
    TableColumn getColumn(int columnIndex, boolean onlyVisible = true) {
        return onlyVisible ? getVisibleColumns()[columnIndex] : model.getColumn(columnIndex)
    }

    @Override
    int getColumnMargin() {
        return model.getColumnMargin()
    }

    @Override
    int getColumnIndexAtX(int x) {
        if (x < 0)
            return -1
        for (int column = 0; column < getColumnCount(true); column++) {
            x = x - getColumn(column, true).width
            if (x < 0)
                return column
        }
        return -1
    }

    @Override
    int getTotalColumnWidth(boolean onlyVisible = true) {
        return onlyVisible ? getVisibleColumns().sum { TableColumn column -> column.width } : model.getTotalColumnWidth()
    }

    @Override
    void setColumnSelectionAllowed(boolean flag) {
        model.setColumnSelectionAllowed(false)
    }

    @Override
    boolean getColumnSelectionAllowed() {
        return model.getColumnSelectionAllowed()
    }

    @Override
    int[] getSelectedColumns(boolean onlyVisible = true) {
        return onlyVisible ? (model.getSelectedColumns().inject([]) { list, idx -> if (isVisible(idx, false)) list << getColumnIndexByModelIndex(idx); return list }) as int[] : model.getSelectedColumns()
    }

    @Override
    int getSelectedColumnCount(boolean onlyVisible = true) {
        return getSelectedColumns(onlyVisible).size()
    }

    @Override
    void setSelectionModel(ListSelectionModel newModel) {
        model.setSelectionModel(newModel)
    }

    @Override
    ListSelectionModel getSelectionModel() {
        return model.getSelectionModel()
    }

    @Override
    void addColumnModelListener(TableColumnModelListener x) {
        listeners << x
        model.addColumnModelListener(x)
    }

    @Override
    void removeColumnModelListener(TableColumnModelListener x) {
        listeners.remove(x)
        model.removeColumnModelListener(x)
    }
}