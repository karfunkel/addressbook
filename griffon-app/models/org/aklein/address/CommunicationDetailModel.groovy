package org.aklein.address

import ca.odell.glazedlists.BasicEventList
import ca.odell.glazedlists.EventList
import ca.odell.glazedlists.FilterList
import ca.odell.glazedlists.SortedList
import ca.odell.glazedlists.matchers.Matcher
import com.avaje.ebean.EbeanServer
import org.aklein.address.db.Communication
import org.aklein.address.db.CommunicationType
import org.aklein.address.db.Nation
import org.aklein.address.db.Unit_Communication
import org.aklein.dialog.DialogModelBase

class CommunicationDetailModel extends DialogModelBase {
    @Bindable List<Unit_Communication> communicationList
    @Bindable CommunicationType communicationType
    @Bindable String note
    @Bindable BindDelegate<Communication> communication = new BindDelegate(new Communication())

    EventList<Nation> nations = new SortedList(new BasicEventList(), { a, b -> a.name <=> b.name } as Comparator)
    EventList<CommunicationType> communicationTypes = new SortedList(new BasicEventList(), { a, b -> a.name <=> b.name } as Comparator)
    EventList<CommunicationType> filteredTypes

    CommunicationDetailModel() {
        withEbean { String ebeanServerName, EbeanServer server ->
            nations.addAll(server.createQuery(Nation).findList())
            communicationTypes.addAll(server.createQuery(CommunicationType).findList())
        }
    }

    void setCommunication(Communication communication) {
        this.communication = new BindDelegate<Communication>(communication)
    }

    void setCommunicationList(List<Unit_Communication> communicationList) {
        this.communicationList = communicationList
        filteredTypes = new FilterList(communicationTypes, { item ->
            if (item?.id == communicationType?.id)
                return true
            return !communicationList.communicationType.id.contains(item?.id)
        } as Matcher)
    }
}