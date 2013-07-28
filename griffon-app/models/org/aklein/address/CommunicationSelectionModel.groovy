package org.aklein.address

import ca.odell.glazedlists.BasicEventList
import ca.odell.glazedlists.EventList
import ca.odell.glazedlists.FilterList
import ca.odell.glazedlists.SortedList
import ca.odell.glazedlists.matchers.Matcher
import com.avaje.ebean.EbeanServer
import org.aklein.address.db.CommunicationType
import org.aklein.address.db.Unit_Communication
import org.aklein.dialog.DialogModelBase

class CommunicationSelectionModel extends DialogModelBase {
    @Bindable List<Unit_Communication> communicationList
    EventList<Unit_Communication> list
    @Bindable CommunicationType communicationType
    @Bindable String note
    EventList<CommunicationType> communicationTypes = new SortedList(new BasicEventList(), { a, b -> a.name <=> b.name } as Comparator)
    EventList<CommunicationType> filteredTypes

    CommunicationSelectionModel() {
        withEbean { String ebeanServerName, EbeanServer server ->
            communicationTypes.addAll(server.createQuery(CommunicationType).findList())
        }
    }

    void setCommunicationList(List<Unit_Communication> communicationList) {
        this.communicationList = communicationList
        filteredTypes = new FilterList(communicationTypes, { item ->
            return !communicationList.communicationType.id.contains(item?.id)
        } as Matcher)
    }
}

