package org.aklein.address

import com.avaje.ebean.EbeanServer
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.oauth2.Oauth2
import com.google.gdata.client.contacts.ContactsService
import com.google.gdata.data.DateTime
import com.google.gdata.data.PlainTextConstruct
import com.google.gdata.data.contacts.*
import com.google.gdata.data.extensions.*
import com.google.gdata.util.PreconditionFailedException
import org.aklein.address.db.*

import javax.swing.*
import java.text.ParseException

class GoogleService {
    HttpTransport httpTransport
    JsonFactory jsonFactory
    GoogleClientSecrets clientSecrets
    FileDataStoreFactory dataStoreFactory

    Map<String, ContactsService> serviceCache = [:]
    Map<String, Credential> credentialCache = [:]
    Map<String, Oauth2> oauthCache = [:]
    Map<String, ContactGroupEntry> googleGroups

    void serviceInit() {
        httpTransport = new NetHttpTransport()
        jsonFactory = new JacksonFactory()
        // load client secrets
        clientSecrets = new GoogleClientSecrets()
        def details = new GoogleClientSecrets.Details()
        details.setClientId(app.config.addressbook.clientId)
        details.setClientSecret(app.config.addressbook.clientSecret)
        details.setFactory(jsonFactory)
        clientSecrets.setInstalled(details)
        //noinspection GroovyAssignabilityCheck
        dataStoreFactory = new FileDataStoreFactory(new File(System.properties.'user.home', "${app.config.addressbook.home}"))
    }

    // void serviceDestroy() {
    //    // this method is called when the service is destroyed
    // }

    Credential authorize(String userId) throws Exception {
        if (credentialCache[userId])
            return credentialCache[userId]

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, app.config.addressbook.scopes).
                setDataStoreFactory(dataStoreFactory).build()
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize(userId)
        credential.refreshToken()
        credentialCache[userId] = credential
        return credential
    }

    Oauth2 oauth(String userId) {
        Credential credential = authorize(userId)
        if (oauthCache[userId])
            return oauthCache[userId]
        Oauth2 oauth = new Oauth2.Builder(httpTransport, jsonFactory, credential).setApplicationName(app.config.addressbook.name).build()
        oauthCache[userId] = oauth
        return oauth
    }

    ContactsService getService(String userId) {
        if (serviceCache[userId])
            return serviceCache[userId]
        ContactsService service = new ContactsService(app.config.addressbook.name)
        Credential credential = authorize(userId)
        service.setOAuth2Credentials(credential)
        serviceCache[userId] = service
        return service
    }

    void sync(String userId, boolean importGoogle = true, boolean exportGoogle = true) {
        AddressModel addressModel = app.models.address
        googleGroups = null
        ContactsService service = getService(userId)
        DateTime lastSync = getLastSync()
        withEbean { String ebeanServerName, EbeanServer server ->
            try {
                server.beginTransaction()
                Map<Integer, Unit> units = loadLocal()
                def max = units.size()
                def c = 0
                def googleContacts = loadGoogle(userId)
                googleContacts.each { entry ->
                    addressModel.status = app.getMessage('application.status.GoogleSync.contact', [++c, max])
                    def addressbookId = entry.userDefinedFields?.find { it.key == 'addressbookId' }?.value?.toInteger()
                    def ehash = entry.userDefinedFields?.find { it.key == 'ehash' }?.value?.toInteger()
                    Unit unit = units.remove(addressbookId)
                    if (!unit) {
                        if (addressbookId) {
                            // Removed locally
                            if (exportGoogle)
                                entry.delete()
                        } else if (importGoogle) {
                            // Created at Google
                            unit = createUnit(entry)
                            server.save(unit)
                            entry.addUserDefinedField(new UserDefinedField('addressbookId', unit.id.toString()))
                            entry.addUserDefinedField(new UserDefinedField('ehash', unit.eHashCode().toString()))
                            save(userId, service, entry)
                        }
                    } else {
                        if (entry.hasDeleted()) {
                            // Removed at Google
                            if (importGoogle)
                                server.delete(unit)
                        } else {
                            int uehash = unit.eHashCode()
                            DateTime googleChange = entry.edited
                            boolean remoteChange = lastSync ? googleChange.compareTo(lastSync) > 0 : true
                            boolean localChange = lastSync ? uehash != ehash : true

                            if (remoteChange && localChange) {
                                // Conflict
                                if (importGoogle && exportGoogle) {
                                    if (JOptionPane.showConfirmDialog(null, app.getMessage('conflictDialog.message', 'Do you want to overwrite your local changes with the remote ones?'), app.getMessage('conflictDialog.dialog.title', 'Conflict while syncing with Google'), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                                        localChange = false
                                        remoteChange = true
                                    } else {
                                        localChange = true
                                        remoteChange = false
                                    }
                                } else if (importGoogle) {
                                    localChange = false
                                    remoteChange = true
                                } else if (exportGoogle) {
                                    localChange = true
                                    remoteChange = false
                                }
                            }
                            if (localChange) {
                                // Local change
                                if (exportGoogle) {
                                    entry = createContactEntry(userId, unit, entry)
                                    save(userId, service, entry)
                                }
                            }
                            if (remoteChange) {
                                // Change at Google
                                if (importGoogle) {
                                    unit = createUnit(entry)
                                    server.save(unit)
                                }
                            }
                        }
                    }
                }

                units.each { Integer id, Unit unit ->
                    addressModel.status = app.getMessage('application.status.GoogleSync.contact', [++c, max])
                    if (importGoogle && unit.googleId) {
                        // Removed at Google
                        server.delete(unit)
                    } else {
                        // Created locally
                        if (exportGoogle) {
                            com.google.gdata.data.contacts.ContactEntry entry = createContactEntry(userId, unit)
                            entry = save(userId, service, entry)
                            unit.googleId = entry.id
                            //println debug(unit)
                            try {
                                server.save(unit)
                            } catch (e) {
                                //println(debug(unit))
                                e.printStackTrace()
                            }
                        }
                    }
                }
                server.commitTransaction()
                setLastSync()
                addressModel.status = app.getMessage('application.status.GoogleSync.finished')
            } catch (e) {
                try {
                    server.rollbackTransaction()
                } catch (e1) {}
                addressModel.status = app.getMessage('application.status.GoogleSync.failed')
                throw e
            }
        }
    }

    com.google.gdata.data.contacts.ContactEntry save(String userId, ContactsService service, com.google.gdata.data.contacts.ContactEntry entry) throws PreconditionFailedException {
        try {
            if (entry.editLink) {
                URL editUrl = new URL(entry.editLink.href)
                return service.update(editUrl, entry)
            } else {
                URL feedUrl = new URL("https://www.google.com/m8/feeds/contacts/${encode(userId)}/full")
                return service.insert(feedUrl, entry)
            }
        } catch (e) {
            log.error(debug(entry), e)
            throw e
        }
    }

    DateTime getLastSync() {
        withEbean { String ebeanServerName, EbeanServer server ->
            def r = server.createQuery(Globals).where().eq('key', 'lastSync').findList()
            return (r) ? DateTime.parseDateTime(r.first().value) : null
        }
    }

    def setLastSync() {
        withEbean { String ebeanServerName, EbeanServer server ->
            server.beginTransaction()
            try {
                def r = server.createQuery(Globals).where().eq('key', 'lastSync').findList()
                Globals g
                if (r)
                    g = r.first()
                else
                    g = new Globals(key: 'lastSync')
                g.value = DateTime.now().toString()
                server.save(g)
                server.commitTransaction()
            } catch (e) {
                server.rollbackTransaction()
                throw e
            }
        }
    }

    List<com.google.gdata.data.contacts.ContactEntry> loadGoogle(String userId) {
        ContactsService service = getService(userId)
        URL url = new URL("https://www.google.com/m8/feeds/contacts/${encode(userId)}/full")
        List<com.google.gdata.data.contacts.ContactEntry> result = []
        com.google.gdata.data.contacts.ContactFeed resultFeed
        while (true) {
            resultFeed = service.getFeed(url, com.google.gdata.data.contacts.ContactFeed)
            result.addAll resultFeed.entries
            if (resultFeed.nextLink == null)
                break;
            url = new URL(resultFeed.nextLink.href)
        }
        return result
    }

    Map<Integer, Unit> loadLocal() {
        withEbean { String ebeanServerName, EbeanServer server ->
            server.createQuery(Unit).findList().inject([:]) { Map map, Unit unit ->
                map[unit.id] = unit
                return map
            }
        }
    }

    Unit createUnit(com.google.gdata.data.contacts.ContactEntry entry) {
        Name name = entry.name
        Birthday birthday = entry.birthday
        List<Organization> organisations = entry.organizations ?: []
        Map<String, String> custom = entry.userDefinedFields?.inject([:], { Map map, UserDefinedField field -> map[field.key] = field.value; return map }) ?: [:]

        Unit unit = new Unit()
        unit.id = custom.addressbookId?.toInteger()
        unit.googleId = entry.getId()
        unit.nation = loadNation(custom.nation)
        unit.salutation = loadSalutation(custom.salutation)
        unit.note = custom.note
        unit.type = custom.type == 'J' ? Unit.ORGANISATION : Unit.PERSON

        if (unit.type == Unit.PERSON) {
            unit.lastname = name.familyName?.value
            unit.firstname = name.givenName?.value
            unit.namePart = name.additionalName?.value
            unit.position = organisations.collect { it.orgTitle }.join(app.config.addressbook.organisationSeparator ?: '|')
            unit.organisation = organisations.collect { it.orgName ?: it.label ?: it.rel }.join(app.config.addressbook.organisationSeparator ?: '|')
            unit.department = organisations.collect { it.orgDepartment }.join(app.config.addressbook.organisationSeparator ?: '|')
            unit.title = name.nameSuffix
            unit.birthday = parseDate(birthday?.when)
        } else {
            unit.name = name.fullName?.value
            unit.vatId = custom.vatId
        }

        List<GroupMembershipInfo> groupInfos = entry.groupMembershipInfos ?: []
        unit.unitCategories.addAll groupInfos.collect { new Unit_Category(unit: unit, category: getCategory(it)) }

        List<StructuredPostalAddress> addresses = entry.structuredPostalAddresses ?: []
        unit.unitAddresses.addAll addresses.collect { StructuredPostalAddress address ->
            new Unit_Address(unit: unit,
                    addressType: loadAddressType(address.label),
                    address: new Address(nation: loadNation(address.country?.code),
                            street: address.street?.value,
                            zip: address.postcode?.value,
                            city: address.city?.value,
                            region: address.region?.value,
                            addition: address.pobox?.value
                    )
            )
        }

        List<Email> emails = entry.emailAddresses ?: []
        unit.unitCommunications.addAll emails.collect { Email email ->
            CommunicationType type = loadEmailType(email.label, email.rel)
            new Unit_Communication(unit: unit,
                    communicationType: type,
                    communication: parseCommunication(email.address)
            )
        }

        List<PhoneNumber> phoneNumbers = entry.phoneNumbers ?: []
        unit.unitCommunications.addAll phoneNumbers.collect { PhoneNumber number ->
            CommunicationType type = loadPhoneType(number.label, number.rel)
            new Unit_Communication(unit: unit,
                    communicationType: type,
                    communication: parseCommunication(number.uri)
            )
        }

        List<Im> instantMessaging = entry.imAddresses ?: []
        unit.unitCommunications.addAll instantMessaging.collect { Im im ->
            CommunicationType type = loadImType(im.label, im.rel)
            new Unit_Communication(unit: unit,
                    communicationType: type,
                    communication: parseCommunication(im.address)
            )
        }

        List<Website> websites = entry.websites ?: []
        unit.unitCommunications.addAll websites.collect { Website website ->
            CommunicationType type = loadWebType(website.label, website.rel?.toString())
            new Unit_Communication(unit: unit,
                    communicationType: type,
                    communication: parseCommunication(website.href)
            )
        }
        return unit
    }

    String debug(com.google.gdata.data.contacts.ContactEntry entry) {
        Map<String, String> custom = entry.userDefinedFields.inject([:], { Map map, UserDefinedField field -> map[field.key] = field.value; return map })
        StringBuffer sb = new StringBuffer()
        sb << "contact:\n"
        sb << "id: $entry.id\n"
        sb << "addressbookId: $custom.addressbookId\n"
        sb << "ehash: $custom.ehash\n"
        sb << "type: $custom.type\n"
        sb << "fullName: ${entry.name?.fullName?.value}\n"
        sb << "givenName: ${entry.name?.givenName?.value}\n"
        sb << "additionalName: ${entry.name?.additionalName?.value}\n"
        sb << "familyName: ${entry.name?.familyName?.value}\n"
        sb << "nation: $custom.nation\n"
        sb << "salutation: $custom.salutation\n"
        sb << "organizations: ${entry.organizations.collect { ['orgName: ' + it.orgName, 'orgDepartment: ' + it.orgDepartment, 'orgTitle: ' + it.orgTitle].join(', ') }.join(' | ')}\n"
        sb << "birthday: ${entry.birthday?.when}\n"
        sb << "vatId: $custom.vatId\n"
        sb << "groups: ${entry.groupMembershipInfos.collect { it.href }}\n"
        sb << "note: $custom.note\n"
        sb << "addresses: \n"
        entry.structuredPostalAddresses.each { a ->
            sb << "label: $a.label\n"
            sb << "country: ${a.country?.code}\n"
            sb << "street: ${a.street?.value}\n"
            sb << "postcode: ${a.postcode?.value}\n"
            sb << "city: ${a.city?.value}\n"
            sb << "region: ${a.region?.value}\n"
            sb << "pobox: ${a.pobox?.value}\n"
        }
        sb << "emails: \n"
        entry.emailAddresses.each { e ->
            sb << "label: $e.label\n"
            sb << "rel: $e.rel\n"
            sb << "address: $e.address\n"
        }
        sb << "phoneNumbers: \n"
        entry.phoneNumbers.each { e ->
            sb << "label: $e.label\n"
            sb << "rel: $e.rel\n"
            sb << "address: $e.phoneNumber\n"
        }
        sb << "ims: \n"
        entry.imAddresses.each { e ->
            sb << "label: $e.label\n"
            sb << "rel: $e.rel\n"
            sb << "address: $e.address\n"
        }
        sb << "websites: \n"
        entry.websites.each { e ->
            sb << "label: $e.label\n"
            sb << "rel: $e.rel\n"
            sb << "address: $e.href\n"
        }
    }

    String debug(Unit unit) {
        StringBuffer sb = new StringBuffer()
        sb << "contact:\n"
        sb << "id: $unit.id\n"
        sb << "googleId: $unit.googleId\n"
        sb << "ehash: ${unit.eHashCode()}\n"
        sb << "type: $unit.type\n"
        sb << "name: $unit.name\n"
        sb << "firstName: $unit.firstname\n"
        sb << "namePart: $unit.namePart\n"
        sb << "lastname: $unit.lastname\n"
        sb << "nation: $unit.nation?.iso2\n"
        sb << "salutation: $unit.salutation?.abbrev\n"
        sb << "position: $unit.position\n"
        sb << "organisation: $unit.organisation\n"
        sb << "department: $unit.department\n"
        sb << "title: $unit.title\n"
        sb << "birthday: $unit.birthday\n"
        sb << "vatId: $unit.vatId\n"
        sb << "note: $unit.note\n"
        return sb.toString()
    }

    com.google.gdata.data.contacts.ContactEntry createContactEntry(String userId, Unit unit, com.google.gdata.data.contacts.ContactEntry entry = new com.google.gdata.data.contacts.ContactEntry()) {
        List<Organization> organisations = entry.organizations
        Map<String, String> custom = entry.userDefinedFields.inject([:], { Map map, UserDefinedField field -> map[field.key] = field.value; return map })

        entry.addUserDefinedField(new UserDefinedField('addressbookId', unit.id.toString()))
        entry.addUserDefinedField(new UserDefinedField('ehash', unit.eHashCode().toString()))
        if (unit.googleId) entry.setId(unit.googleId)
        if (unit.nation) {
            entry.userDefinedFields.remove(entry.userDefinedFields.find { it.key == 'nation' })
            entry.addUserDefinedField(new UserDefinedField('nation', unit.nation?.iso2))
        } else if (custom.nation) {
            entry.userDefinedFields.remove(entry.userDefinedFields.find { it.key == 'nation' })
        }
        if (unit.salutation) {
            entry.userDefinedFields.remove(entry.userDefinedFields.find { it.key == 'salutation' })
            entry.addUserDefinedField(new UserDefinedField('salutation', unit.salutation?.abbrev))
        } else if (custom.salutation) {
            entry.userDefinedFields.remove(entry.userDefinedFields.find { it.key == 'salutation' })
        }
        if (unit.note) {
            entry.userDefinedFields.remove(entry.userDefinedFields.find { it.key == 'note' })
            entry.addUserDefinedField(new UserDefinedField('note', unit.note))
        } else if (custom.salutation) {
            entry.userDefinedFields.remove(entry.userDefinedFields.find { it.key == 'note' })
        }
        entry.userDefinedFields.remove(entry.userDefinedFields.find { it.key == 'type' })
        entry.addUserDefinedField(new UserDefinedField('type', unit.type))

        if (unit.type == Unit.PERSON) {
            if (!entry.name)
                entry.name = new Name()
            entry.name.fullName = optionalSet(entry.name?.fullName ?: new FullName(), [unit.firstname, unit.namePart, unit.lastname].findAll { it }.join(' '))
            entry.name.familyName = optionalSet(entry.name?.familyName ?: new FamilyName(), unit.lastname)
            entry.name.givenName = optionalSet(entry.name?.givenName ?: new GivenName(), unit.firstname)
            entry.name.additionalName = optionalSet(entry.name?.additionalName ?: new AdditionalName(), unit.namePart)
            organisations.clear()
            if (unit.organisation || unit.department || unit.position) {
                def orga = new Organization([unit.organisation, unit.department, unit.position].findAll { it }.join(' '), false, null)
                if (unit.position)
                    orga.orgTitle = new OrgTitle(unit.position)
                if (unit.organisation)
                    orga.orgName = new OrgName(unit.organisation)
                if (unit.department)
                    orga.orgDepartment = new OrgDepartment(unit.department)
                organisations << orga
            }
            entry.name.nameSuffix = optionalSet(entry.name.nameSuffix ?: new NameSuffix(), unit.title)
            DateTime bDay = unit.birthday ? new DateTime(unit.birthday + 1) : null
            bDay?.dateOnly = true
            entry.birthday = optionalSet(entry.birthday ?: new Birthday(), bDay?.toString(), 'when')
        } else {
            if (!entry.name)
                entry.name = new Name()
            entry.name.fullName = optionalSet(entry.name.fullName ?: new FullName(), unit.name)
            entry.name.familyName = optionalSet(entry.name?.familyName ?: new FamilyName(), unit.name)
            entry.name.givenName = null
            entry.name.additionalName = null
            entry.name.namePrefix = null
            organisations.clear()
            entry.birthday = null
            entry.userDefinedFields.remove(entry.userDefinedFields.find { it.key == 'vatId' })
            if (unit.vatId)
                entry.addUserDefinedField(new UserDefinedField('vatId', unit.vatId))
        }

        entry.groupMembershipInfos.clear()
        ContactGroupEntry defaultGroup = getContactGroupEntry(userId, 'System Group: My Contacts')
        entry.addGroupMembershipInfo(new GroupMembershipInfo(false, defaultGroup.id))
        unit.unitCategories.each { uc ->
            if (uc?.category) {
                ContactGroupEntry group = getContactGroupEntry(userId, uc.category.name)
                entry.addGroupMembershipInfo(new GroupMembershipInfo(false, group.id))
            }
        }

        entry.structuredPostalAddresses.clear()
        unit.unitAddresses.each { ua ->
            Address a = ua.address
            StructuredPostalAddress address = new StructuredPostalAddress()
            address.label = ua.addressType.name
            address.country = new Country(a.nation?.iso2, a.nation?.english)
            address.street = optionalSet(address.street ?: new Street(), a.street)
            address.postcode = optionalSet(address.postcode ?: new PostCode(), a.zip)
            address.city = optionalSet(address.city ?: new City(), a.city)
            address.region = optionalSet(address.region ?: new Region(), a.region)
            address.pobox = optionalSet(address.pobox ?: new PoBox(), a.addition)
            entry.structuredPostalAddresses << address
        }

        entry.emailAddresses.clear()
        entry.phoneNumbers.clear()
        entry.imAddresses.clear()
        unit.unitCommunications.each { uc ->
            String type = uc.communicationType.name
            Communication c = uc.communication

            if (isEmailType(type)) {
                Email email = new Email()
                email.rel = Email.Rel.fields.find { it.name == app.config.addressbook.emailTypeMapping.find { k, v -> v == type } }
                if (!email.rel)
                    email.label = type
                email.address = prepareCommunication(uc.communicationType, c)
                entry.emailAddresses << email
            } else if (isPhoneType(type)) {
                PhoneNumber number = new PhoneNumber()
                number.rel = PhoneNumber.Rel.fields.find { it.name == app.config.addressbook.phoneTypeMapping.find { k, v -> v == type } }
                if (!number.rel)
                    number.label = type
                number.phoneNumber = prepareCommunication(uc.communicationType, c)
                entry.phoneNumbers << number
            } else if (isImType(type)) {
                Im im = new Im()
                im.rel = Im.Rel.fields.find { it.name == app.config.addressbook.imTypeMapping.find { k, v -> v == type } }
                if (!im.rel)
                    im.label = type
                im.address = prepareCommunication(uc.communicationType, c)
                entry.imAddresses << im
            } else if (isWebType(type)) {
                Website web = new Website()
                web.rel = Website.Rel.fields.find { it.name == app.config.addressbook.webTypeMapping.find { k, v -> v == type } }
                if (!web.rel)
                    web.label = type
                web.href = prepareCommunication(uc.communicationType, c)
                entry.websites << web
            }
        }
        return entry
    }

    def optionalSet(def obj, String value, String property = 'value') {
        if (value?.trim()) {
            obj."$property" = value.trim()
            return obj
        } else
            return null
    }

    String prepareCommunication(CommunicationType type, Communication communication) {
        String text = communication.text?.trim() ?: ''
        if (isWebType(type.name) && !(text.startsWith('http')))
            text = "http://$text"
        if (isPhoneType(type.name))
            text = text.replaceAll(/(?i)\((.*)\)/, { it[1] })
        if (type.useAreaCode)
            text = "+${communication.nation?.tel} $text"
        if (isPhoneType(type.name))
            text = text.replaceAll(/\s+/, '-').replaceAll('\\\\', '-').replaceAll('/', '-')
        return text
    }

    Communication parseCommunication(String text) {
        Communication com = new Communication()
        text = text.trim()
        if (text.startsWith('+')) {
            int pos = text.indexOf('-')
            if (pos >= 0) {
                def areaCode = text[1..<pos]
                com.nation = loadNationByAreaCode(areaCode)
                if (com.nation)
                    text = text.substring(pos + 1)
            }
        }
        com.text = text
        return com
    }

    AddressType loadAddressType(String type) {
        withEbean { String ebeanServerName, EbeanServer server ->
            def addressTypes = server.createQuery(AddressType).where().ilike('name', type).findList()
            def r = addressTypes ? addressTypes.first() : null
            if (r) return r
            if (type != app.config.addressbook.defaultAddressType)
                return loadAddressType(app.config.addressbook.defaultAddressType)
            else
                return null
        }
    }

    CommunicationType loadPhoneType(String label, String type) {
        if (label)
            type = label
        else
            type = app.config.addressbook.phoneTypeMapping[type.toUpperCase()] ?: app.config.addressbook.phoneTypeMapping.DEFAULT

        withEbean { String ebeanServerName, EbeanServer server ->
            def communicationTypes = server.createQuery(CommunicationType).where().ilike('name', type).findList()
            return communicationTypes ? communicationTypes.first() : null
        }
    }

    boolean isPhoneType(String type) {
        return type.matches(app.config.addressbook.phoneTypePattern)
    }

    CommunicationType loadEmailType(String label, String type) {
        if (label)
            type = label
        else
            type = app.config.addressbook.emailTypeMapping[type.toUpperCase()] ?: app.config.addressbook.emailTypeMapping.DEFAULT

        withEbean { String ebeanServerName, EbeanServer server ->
            def communicationTypes = server.createQuery(CommunicationType).where().ilike('name', type).findList()
            return communicationTypes ? communicationTypes.first() : null
        }
    }

    boolean isEmailType(String type) {
        return type.matches(app.config.addressbook.emailTypePattern)
    }

    CommunicationType loadImType(String label, String type) {
        if (label)
            type = label
        else
            type = app.config.addressbook.imTypeMapping[type.toUpperCase()] ?: app.config.addressbook.imTypeMapping.DEFAULT

        withEbean { String ebeanServerName, EbeanServer server ->
            def communicationTypes = server.createQuery(CommunicationType).where().ilike('name', type).findList()
            return communicationTypes ? communicationTypes.first() : null
        }

    }

    boolean isImType(String type) {
        return type.matches(app.config.addressbook.imTypePattern)
    }

    CommunicationType loadWebType(String label, String type) {
        if (label)
            type = label
        else
            type = app.config.addressbook.webTypeMapping[type.toUpperCase()] ?: app.config.addressbook.webTypeMapping.DEFAULT

        withEbean { String ebeanServerName, EbeanServer server ->
            def communicationTypes = server.createQuery(CommunicationType).where().ilike('name', type).findList()
            return communicationTypes ? communicationTypes.first() : null
        }
    }

    boolean isWebType(String type) {
        return type.matches(app.config.addressbook.webTypePattern)
    }

    String getGroupId(GroupMembershipInfo group) {
        return group.href.split('/').last()
    }

    Category getCategory(GroupMembershipInfo group) {
        String groupId = getGroupId(group)
        withEbean { String ebeanServerName, EbeanServer server ->
            def categories = server.createQuery(Category).where().eq('name', groupId).findList()
            return categories ? categories.first() : null
        }
    }

    ContactGroupEntry getContactGroupEntry(String userId, String group) {
        ContactsService service = getService(userId)
        Map<String, ContactGroupEntry> groups = getGoogleGroups(userId)
        if (groups[group])
            return groups[group]

        ContactGroupEntry groupEntry = new ContactGroupEntry()
        groupEntry.title = new PlainTextConstruct(group)
        groupEntry = service.insert(new URL("https://www.google.com/m8/feeds/groups/${encode(userId)}/full"), groupEntry)
        groups[group] = groupEntry
        return groupEntry
    }

    Map<String, ContactGroupEntry> getGoogleGroups(String userId) {
        if (!googleGroups) {
            ContactsService service = getService(userId)
            URL feedUrl = new URL("https://www.google.com/m8/feeds/groups/${encode(userId)}/full")
            ContactGroupFeed resultFeed = service.getFeed(feedUrl, ContactGroupFeed)
            googleGroups = resultFeed.entries.collectEntries { ContactGroupEntry e -> [e.title.plainText, e] }
        }
        return googleGroups
    }

    String encode(String value) {
        return URLEncoder.encode(value, 'UTF-8')
    }

    Date parseDate(String date) {
        if (date == null)
            return null
        List<String> formats = app.config.addressbook.dateFormats ?: ['yyyy-MM-dd']
        for (String format : formats) {
            try {
                return Date.parse(format, date) - 1
            } catch (ParseException e) {}
        }
    }

    Nation loadNation(String iso2) {
        if (iso2 == null)
            return null
        withEbean { String ebeanServerName, EbeanServer server ->
            def nations = server.createQuery(Nation).where().eq('iso2', iso2.toUpperCase()).findList()
            return nations ? nations.first() : null
        }
    }

    Nation loadNationByAreaCode(String areaCode) {
        if (areaCode == null)
            return null
        withEbean { String ebeanServerName, EbeanServer server ->
            def nations = server.createQuery(Nation).where().eq('tel', areaCode).findList()
            return nations ? nations.first() : null
        }
    }


    Salutation loadSalutation(String abbrev) {
        if (abbrev == null)
            return null
        withEbean { String ebeanServerName, EbeanServer server ->
            def salutations = server.createQuery(Salutation).where().eq('abbrev', abbrev).findList()
            return salutations ? salutations.first() : null
        }

    }

}
