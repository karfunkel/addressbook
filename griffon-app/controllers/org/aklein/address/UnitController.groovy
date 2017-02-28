package org.aklein.address

import ca.odell.glazedlists.BasicEventList
import com.avaje.ebean.Ebean
import com.avaje.ebean.EbeanServer
import com.avaje.ebean.FetchConfig
import griffon.transform.Threading
import org.aklein.address.db.Address
import org.aklein.address.db.Communication
import org.aklein.address.db.Relation
import org.aklein.address.db.Unit
import org.aklein.address.db.Unit_Address
import org.aklein.address.db.Unit_Category
import org.aklein.address.db.Unit_Communication
import org.aklein.dialog.DialogControllerBase

class UnitController extends DialogControllerBase<UnitModel, UnitView> {
    def categories = { evt = null ->
        def selected = new BasicEventList(model.unit.delegate.unitCategories.category)
        withMVCGroup('categories', value: selected) { m, v, c ->
            m.title = app.getMessage('categories.dialog.title', 'Selected categories')
            c.show()
            if (!m.cancelled) {
                Unit unit = model.unit.delegate
                for (def unitCategory : [] + unit.unitCategories) {
                    if (!selected.find { it.id == unitCategory.category.id }) {
                        unit.unitCategories.remove(unitCategory)
                    }
                }
                for (def category : selected) {
                    if (!unit.unitCategories.find { it.unit == unit && it.category == category }) {
                        unit.unitCategories.add(new Unit_Category(unit: unit, category: category))
                    }
                }
                view.unitCategoriesBinding.update()
            }
        }
    }

    def nations = { evt = null ->
        withMVCGroup('nationManagement', value: [] + model.nations, noCancel: true) { m, v, c ->
            m.title = app.getMessage('nationManagement.dialog.title', 'Manage nations')
            c.show()
            if (!m.cancelled) {
                for (def nation : m.list) {
                    if (!model.nations.contains(nation))
                        model.nations << nation
                }
            }
        }
    }

    def salutations = { evt = null ->
        withMVCGroup('salutationManagement', value: [] + model.salutations, noCancel: true) { m, v, c ->
            m.title = app.getMessage('salutationManagement.dialog.title', 'Manage salutations')
            c.show()
            if (!m.cancelled) {
                for (def salutation : m.list) {
                    if (!model.salutations.contains(salutation))
                        model.salutations << salutation
                }
            }
        }
    }

    @Threading(Threading.Policy.SKIP)
    def loadAddresses = { model.unit.unitAddresses }

    @Threading(Threading.Policy.SKIP)
    def createAddress = {
        def result
        withMVCGroup('addressDetail', value: new Address(nation: model.unit.delegate.nation), list: view.addressPanelGroup.model.list) { m, v, c ->
            m.title = app.getMessage("addressDetail.dialog.create.title", "Create address")
            c.show()
            if (!m.cancelled) {
                withEbean { String ebeanServerName, EbeanServer server ->
                    def address = m.address.delegate
                    def type = m.addressType
                    def note = m.note
                    server.beginTransaction()
                    def unit = model.unit.delegate
                    def entity = new Unit_Address(unit: unit, address: address, addressType: type, note: note)
                    try {
                        if(!unit.id)
                            server.save(unit)
                        server.save(address)
                        server.save(entity)
                        server.commitTransaction()
                        unit.unitAddresses << entity
                    } catch (e) {
                        server.rollbackTransaction()
                    }
                    result = server.find(entity.getClass(), entity.id)
                }
            }
        }
        return result
    }

    @Threading(Threading.Policy.SKIP)
    def editAddress = { item ->
        def result = null
        withEbean { String ebeanServerName, EbeanServer server ->
            item = server.find(item.getClass(), item.id)
        }
        withMVCGroup('addressDetail', value: item.address, list: view.addressPanelGroup.model.list, type: item.addressType, note: item.note) { m, v, c ->
            m.title = app.getMessage("addressDetail.dialog.edit.title", "Edit address")
            c.show()
            if (m.cancelled) {
                withEbean { String ebeanServerName, EbeanServer server ->
                    result = server.find(item.getClass(), item.id)
                }
            } else {
                withEbean { String ebeanServerName, EbeanServer server ->
                    item.addressType = m.addressType
                    item.note = m.note
                    server.beginTransaction()
                    try {
                        server.save(item.address)
                        server.save(item)
                        server.commitTransaction()
                    } catch (e) {
                        server.rollbackTransaction()
                    }
                }
                result = item
            }
        }
        return result
    }

    @Threading(Threading.Policy.SKIP)
    def deleteAddress = { toDelete ->
        boolean delete = view.addressPanelGroup.model.DEFAULT_DELETE_CONFIRM(toDelete)
        if (delete) {
            withEbean { String ebeanServerName, EbeanServer server ->
                toDelete.each {
                    def address = it.address
                    server.delete(it)
                    if (!server.find(it.getClass()).where().eq('address.id', address.id).findRowCount())
                        server.delete(address)
                }
            }
        }
        return delete
    }

    @Threading(Threading.Policy.SKIP)
    def linkAddress = {
        def list
        withEbean { String ebeanServerName, EbeanServer server ->
            list = server.find(Unit_Address).fetch("address").fetch("addressType").fetch("unit").findList()
        }
        withMVCGroup('addressSelection', value: list, list: view.addressPanelGroup.model.list) { m, v, c ->
            m.title = app.getMessage("addressSelection.dialog.title", "Select address")
            c.show()
            if (!m.cancelled) {
                def address = v.mainPanelGroup.model.selected[0]?.address
                def type = m.addressType
                def note = m.note
                if (address) {
                    def entity = new Unit_Address(unit: model.unit.delegate, address: address, addressType: type, note: note)
                    withEbean { String ebeanServerName, EbeanServer server ->
                        def unit = model.unit.delegate
                        if(!unit.id)
                            server.save(unit)
                        server.save(entity)
                        unit.unitAddresses << entity
                        view.addressPanelGroup.model.list.readWriteLock.writeLock().lock()
                        try {
                            view.addressPanelGroup.model.list << entity
                        }
                        finally {
                            view.addressPanelGroup.model.list.readWriteLock.writeLock().unlock()
                        }
                    }
                }
            }
        }
    }

    @Threading(Threading.Policy.SKIP)
    def loadCommunications = { model.unit.unitCommunications }

    @Threading(Threading.Policy.SKIP)
    def createCommunication = {
        def result
        withMVCGroup('communicationDetail', value: new Communication(nation: model.unit.delegate.nation), list: view.communicationPanelGroup.model.list) { m, v, c ->
            m.title = app.getMessage("communicationDetail.dialog.create.title", "Create communication")
            c.show()
            if (!m.cancelled) {
                withEbean { String ebeanServerName, EbeanServer server ->
                    def communication = m.communication.delegate
                    def type = m.communicationType
                    def note = m.note
                    def unit = model.unit.delegate
                    def entity = new Unit_Communication(unit: model.unit.delegate, communication: communication, communicationType: type, note: note)
                    server.beginTransaction()
                    try {
                        if(!unit.id)
                            server.save(unit)
                        server.save(communication)
                        server.save(entity)
                        server.commitTransaction()
                        unit.unitCommunications << entity
                    } catch (e) {
                        server.rollbackTransaction()
                    }
                    result = server.find(entity.getClass(), entity.id)
                }
            }
        }
        return result
    }

    @Threading(Threading.Policy.SKIP)
    def editCommunication = { item ->
        def result = null
        withEbean { String ebeanServerName, EbeanServer server ->
            item = server.find(item.getClass(), item.id)
        }
        withMVCGroup('communicationDetail', value: item.communication, list: view.communicationPanelGroup.model.list, type: item.communicationType, note: item.note) { m, v, c ->
            m.title = app.getMessage("communicationDetail.dialog.edit.title", "Edit communication")
            c.show()
            if (m.cancelled) {
                withEbean { String ebeanServerName, EbeanServer server ->
                    result = server.find(item.getClass(), item.id)
                }
            } else {
                withEbean { String ebeanServerName, EbeanServer server ->
                    item.communicationType = m.communicationType
                    item.note = m.note
                    server.beginTransaction()
                    try {
                        server.save(item.communication)
                        server.save(item)
                        server.commitTransaction()
                    } catch (e) {
                        server.rollbackTransaction()
                    }
                }
                result = item
            }
        }
        return result
    }

    @Threading(Threading.Policy.SKIP)
    def deleteCommunication = { toDelete ->
        boolean delete = view.communicationPanelGroup.model.DEFAULT_DELETE_CONFIRM(toDelete)
        if (delete) {
            withEbean { String ebeanServerName, EbeanServer server ->
                toDelete.each {
                    def communication = it.communication
                    server.delete(it)
                    if (!server.find(it.getClass()).where().eq('communication.id', communication.id).findRowCount())
                        server.delete(communication)
                }
            }
        }
        return delete
    }

    @Threading(Threading.Policy.SKIP)
    def linkCommunication = {
        def list
        withEbean { String ebeanServerName, EbeanServer server ->
            list = server.find(Unit_Communication).fetch("communication").fetch("communicationType").fetch("unit").findList()
        }
        withMVCGroup('communicationSelection', value: list, list: view.communicationPanelGroup.model.list) { m, v, c ->
            m.title = app.getMessage("communicationSelection.dialog.title", "Select communication")
            c.show()
            if (!m.cancelled) {
                def communication = v.mainPanelGroup.model.selected[0]?.communication
                def type = m.communicationType
                def note = m.note
                if (communication) {
                    def entity = new Unit_Communication(unit: model.unit.delegate, communication: communication, communicationType: type, note: note)
                    withEbean { String ebeanServerName, EbeanServer server ->
                        def unit = model.unit.delegate
                        if(!unit.id)
                            server.save(unit)
                        server.save(entity)
                        unit.unitCommunications << entity
                        view.communicationPanelGroup.model.list.readWriteLock.writeLock().lock()
                        try {
                            view.communicationPanelGroup.model.list << entity
                        }
                        finally {
                            view.communicationPanelGroup.model.list.readWriteLock.writeLock().unlock()
                        }
                    }
                }
            }
        }
    }

    @Threading(Threading.Policy.SKIP)
    def loadRelations = {
        List<Relation> relations = Ebean.find(Relation).fetch('unit', new FetchConfig().query()).fetch('relation', new FetchConfig().query()).where().eq('unit.id', model.unit.id).findList()
        relations += Ebean.find(Relation).fetch('unit', new FetchConfig().query()).fetch('relation', new FetchConfig().query()).where().eq('relation.id', model.unit.id).findList()
        return relations
    }

    @Threading(Threading.Policy.SKIP)
    def createRelation = {
        def result
        withMVCGroup('relationDetail', unit: model.unit.delegate) { m, v, c ->
            m.title = app.getMessage("relationDetail.dialog.create.title", "Create relation")
            c.show()
            if (!m.cancelled) {
                withEbean { String ebeanServerName, EbeanServer server ->
                    def unit = model.unit.delegate
                    def entity
                    server.beginTransaction()
                    try {
                        if(!unit.id)
                            server.save(unit)
                        if (m.type == 'has') {
                            entity = new Relation(unit: m.unit, relation: v.mainPanelGroup.model.selected[0], description: m.description)
                            unit.sourceRelations << entity
                        } else {
                            entity = new Relation(relation: m.unit, unit: v.mainPanelGroup.model.selected[0], description: m.description)
                            unit.targetRelations << entity
                        }
                        server.save(entity)
                        server.commitTransaction()
                    } catch (e) {
                        server.rollbackTransaction()
                    }
                    result = server.find(entity.getClass(), entity.id)
                }
            }
        }
        return result
    }

    @Threading(Threading.Policy.SKIP)
    def editRelation = { item ->
        def result = null
        withEbean { String ebeanServerName, EbeanServer server ->
            item = server.find(item.getClass(), item.id)
        }
        def unit = model.unit.delegate
        def type
        def relation
        if (item.unit == model.unit.delegate) {
            type = 'has'
            relation = item.relation
        } else {
            type = 'is'
            relation = item.unit
        }
        withMVCGroup('relationDetail', value: relation, type: type, unit: unit, description: item.description) { m, v, c ->
            m.title = app.getMessage("relationDetail.dialog.edit.title", "Edit relation")
            c.show()
            if (m.cancelled) {
                withEbean { String ebeanServerName, EbeanServer server ->
                    result = server.find(item.getClass(), item.id)
                }
            } else {
                withEbean { String ebeanServerName, EbeanServer server ->
                    item.description = m.description
                    if (m.type == 'has') {
                        item.unit = m.unit
                        item.relation = v.mainPanelGroup.model.selected[0]
                    } else {
                        item.relation = m.unit
                        item.unit = v.mainPanelGroup.model.selected[0]
                    }
                    server.beginTransaction()
                    try {
                        server.save(item)
                        server.commitTransaction()
                    } catch (e) {
                        server.rollbackTransaction()
                    }
                }
                result = item
            }
        }
        return result
    }

    @Threading(Threading.Policy.SKIP)
    def deleteRelation = { toDelete ->
        boolean delete = view.relationPanelGroup.model.DEFAULT_DELETE_CONFIRM(toDelete)
        if (delete) {
            withEbean { String ebeanServerName, EbeanServer server ->
                toDelete.each {
                    server.delete(it)
                }
            }
        }
        return delete
    }
}
