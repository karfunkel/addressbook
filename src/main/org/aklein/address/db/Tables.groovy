package org.aklein.address.db

import groovy.transform.AutoClone
import griffon.core.GriffonApplication
import griffon.util.ApplicationHolder

import javax.persistence.*
import java.sql.Timestamp


class Tables {
    static boolean importing = false
}

@Entity
@AutoClone
class Globals {
    @Id
    Integer id
    String key
    String value

    @Version
    Timestamp version

    boolean equals(o) {
        if (o == null) return false
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        if (id != o.id) return false
        return true
    }

    int hashCode() {
        return id?.hashCode() ?: 0
    }

    int eHashCode() {
        int result
        result = id.hashCode()
        result = 13 * result + (key?.hashCode() ?: 0)
        result = 13 * result + (value?.hashCode() ?: 0)
        return result
    }
}

@Entity
@AutoClone
class Address {
    @Id
    Integer id
    @ManyToOne
    @JoinColumn(name = "NATION")
    Nation nation
    String street
    String zip
    String city
    String region
    String addition

    @Version
    Timestamp version

    @OneToMany(mappedBy = "address", cascade = CascadeType.ALL)
    List<Unit_Address> unitAddresses = []

    boolean equals(o) {
        if (o == null) return false
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        if (id != o.id) return false
        return true
    }

    int hashCode() {
        return id?.hashCode() ?: 0
    }

    int eHashCode() {
        int result
        result = id.hashCode()
        result = 13 * result + (nation?.eHashCode() ?: 0)
        result = 13 * result + (street?.hashCode() ?: 0)
        result = 13 * result + (zip?.hashCode() ?: 0)
        result = 13 * result + (city?.hashCode() ?: 0)
        result = 13 * result + (region?.hashCode() ?: 0)
        result = 13 * result + (addition?.hashCode() ?: 0)
        return result
    }

    String getDisplay() {
        return [getNation()?.iso2, zip, city, street, region, addition].findAll { it }.join(' ')
    }

    String getOneliner() {
        return [street, getNation()?.iso2, zip, city, region, addition].findAll { it }.join(' ')
    }

    String getMultiliner() {
        def parts = []
        if (street)
            parts << street
        if (addition)
            parts << addition
        if (zip && city)
            parts << "$zip $city"
        else if (zip)
            parts << zip
        else if (city)
            parts << city
        if (region)
            parts << region
        if (nation?.name)
            parts << nation?.name
        return parts.join('\n')
    }
}

@Entity
@AutoClone
class AddressType {
    @Id
    Integer id
    @Column(nullable = false)
    String name
    @Column(nullable = false)
    boolean usePostalCode

    @Version
    Timestamp version

    @OneToMany(mappedBy = "addressType", cascade = CascadeType.ALL)
    List<Unit_Address> unitAddresses = []

    boolean equals(o) {
        if (o == null) return false
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        if (id != o.id) return false
        return true
    }

    int hashCode() {
        return id?.hashCode() ?: 0
    }

    int eHashCode() {
        int result
        result = id.hashCode()
        result = 13 * result + (name?.hashCode() ?: 0)
        result = 13 * result + (usePostalCode.hashCode())
        return result
    }
}

@Entity
@AutoClone
class Salutation {
    @Id
    Integer id
    String abbrev
    String letter
    String address

    @Version
    Timestamp version

    @OneToMany(mappedBy = "salutation", cascade = CascadeType.ALL)
    List<Unit> units = []

    boolean equals(o) {
        if (o == null) return false
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        if (id != o.id) return false
        return true
    }

    int hashCode() {
        return id?.hashCode() ?: 0
    }

    int eHashCode() {
        int result
        result = id.hashCode()
        result = 13 * result + (abbrev?.hashCode() ?: 0)
        result = 13 * result + (letter?.hashCode() ?: 0)
        result = 13 * result + (address?.hashCode() ?: 0)
        return result
    }
}

@Entity
@AutoClone
class Relation {
    @Id
    Integer id
    @ManyToOne
    @JoinColumn(name = "UNIT", nullable = false)
    Unit unit
    @ManyToOne
    @JoinColumn(name = "RELATION", nullable = false)
    Unit relation
    /*
    @ManyToOne
    @JoinColumn(name = "POSITION")
    Position position
    */
    String description

    @Version
    Timestamp version

    boolean equals(o) {
        if (o == null) return false
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        if (id != o.id) return false
        return true
    }

    int hashCode() {
        return id?.hashCode() ?: 0
    }

    int eHashCode() {
        int result
        result = id.hashCode()
        result = 13 * result + (description?.hashCode() ?: 0)
        return result
    }

    GriffonApplication getApp() {
        return ApplicationHolder.application
    }

    String getFullDisplay(Unit unit) {
        if (!unit) return ''
        if (this.getUnit() == unit) {
            return app.getMessage('unit.relation.full.has', [this.getDescription() ?: '*', getDisplay(unit)])
        } else if (this.getRelation() == unit) {
            return app.getMessage('unit.relation.full.is', [this.getDescription() ?: '*', getDisplay(unit)])
        } else
            return ''
    }

    String getTypeDisplay(Unit unit) {
        if (!unit) return ''
        if (this.getUnit() == unit) {
            return app.getMessage('unit.relation.is', 'is')
        } else if (this.getRelation() == unit) {
            return app.getMessage('unit.relation.has', 'has')
        } else
            throw new IllegalArgumentException("Relation ${this.dump()} has no relation to unit ${unit.display}")
    }

    String getDisplay(Unit unit) {
        if (!unit) return ''
        if (this.getUnit() == unit) {
            return this.getRelation()?.display ?: ''
        } else if (this.getRelation() == unit) {
            return this.getUnit()?.display ?: ''
        } else
            throw new IllegalArgumentException("Relation ${this.dump()} has no relation to unit ${unit.display}")
    }
}

@Entity
@AutoClone
class Communication {
    @Id
    Integer id
    @ManyToOne
    @JoinColumn(name = "NATION")
    Nation nation
    @Column(nullable = false)
    String text

    @Version
    Timestamp version

    @OneToMany(mappedBy = "communication", cascade = CascadeType.ALL)
    List<Unit_Communication> unitCommunications = []

    boolean equals(o) {
        if (o == null) return false
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        if (id != o.id) return false
        return true
    }

    int hashCode() {
        return id?.hashCode() ?: 0
    }

    int eHashCode() {
        int result
        result = id.hashCode()
        result = 13 * result + (nation?.eHashCode() ?: 0)
        result = 13 * result + (text?.hashCode() ?: 0)
        return result
    }

    String getDisplay(CommunicationType type) {
        return getOneliner(type)
    }

    String getOneliner(CommunicationType type) {
        return Unit_Communication.getOneliner(type, this)
    }
}

@Entity
@AutoClone
class CommunicationType {
    @Id
    Integer id
    @Column(nullable = false)
    String name
    @Column(nullable = false)
    boolean useAreaCode

    @Version
    Timestamp version

    @OneToMany(mappedBy = "communicationType", cascade = CascadeType.ALL)
    List<Unit_Communication> unitCommunications = []

    boolean equals(o) {
        if (o == null) return false
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        if (id != o.id) return false
        return true
    }

    int hashCode() {
        return id?.hashCode() ?: 0
    }

    int eHashCode() {
        int result
        result = id.hashCode()
        result = 13 * result + (name?.hashCode() ?: 0)
        result = 13 * result + (useAreaCode.hashCode() ?: 0)
        return result
    }
}

@Entity
@AutoClone
class Nation {
    @Id
    Integer id
    @Column(nullable = false, length = 2)
    String iso2
    @Column(nullable = false, length = 3)
    String iso3
    @Column(nullable = false, length = 5)
    String tel
    @Column(nullable = false)
    String name
    @Column(nullable = false)
    String english

    @Version
    Timestamp version

    @OneToMany(mappedBy = "nation", cascade = CascadeType.ALL)
    List<Address> addresses = []
    @OneToMany(mappedBy = "nation", cascade = CascadeType.ALL)
    List<Communication> communications = []
    @OneToMany(mappedBy = "nation", cascade = CascadeType.ALL)
    List<Unit> units = []

    boolean equals(o) {
        if (o == null) return false
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        if (id != o.id) return false
        return true
    }

    int hashCode() {
        return id?.hashCode() ?: 0
    }

    int eHashCode() {
        int result
        result = id.hashCode()
        result = 13 * result + (iso2?.hashCode() ?: 0)
        result = 13 * result + (iso3?.hashCode() ?: 0)
        result = 13 * result + (tel?.hashCode() ?: 0)
        result = 13 * result + (name?.hashCode() ?: 0)
        result = 13 * result + (english?.hashCode() ?: 0)
        return result
    }
}

/*
@Entity
@AutoClone
class Position {
    @Id
    Integer id
    @Column(nullable = false)
    String name
    @Column(nullable = false, length = 4)
    String abbrev
    String description

    @OneToMany(mappedBy = "position", cascade = CascadeType.ALL)
    List<Relation> relations = []

    boolean equals(o) {
        if (o == null) return false
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        if (id != o.id) return false
        return true
    }

    int hashCode() {
        return id?.hashCode() ?: 0
    }
}
*/

@Entity
@AutoClone
class Category {
    @Id
    Integer id
    @Column(nullable = false)
    String name

    @Version
    Timestamp version

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    List<Unit_Category> unitCategories = []

    boolean equals(o) {
        if (o == null) return false
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        if (id != o.id) return false
        return true
    }

    int hashCode() {
        return id?.hashCode() ?: 0
    }

    int eHashCode() {
        int result
        result = id.hashCode()
        result = 13 * result + (name?.hashCode() ?: 0)
        return result
    }
}

@Entity
@AutoClone
class Unit {
    static final String PERSON = 'N'
    static final String ORGANISATION = 'J'

    @Id
    Integer id
    @ManyToOne
    @JoinColumn(name = "NATION")
    Nation nation
    @ManyToOne
    @JoinColumn(name = "SALUTATION")
    Salutation salutation
    String note
    String type = PERSON

    // Person
    String lastname
    String firstname
    String namePart
    String position
    String organisation
    String department
    String title
    @Temporal(TemporalType.DATE)
    Date birthday

    // Organisation
    String name
    String vatId

    @Version
    Timestamp version

    Date editDate
    String googleId

    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL)
    List<Relation> sourceRelations = []

    @OneToMany(mappedBy = "relation", cascade = CascadeType.ALL)
    List<Relation> targetRelations = []

    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL)
    List<Unit_Address> unitAddresses = []

    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL)
    List<Unit_Communication> unitCommunications = []

    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL)
    List<Unit_Category> unitCategories = []

    String getName() {
        def parts = new StringBuilder()
        if (type == PERSON) {
            if (lastname)
                parts << lastname << ', '
            if (title)
                parts << title << ' '
            if (firstname)
                parts << firstname << ' '
            if (namePart)
                parts << namePart << ' '
            return parts.toString()
        } else
            return name
    }

    String getLetterName() {
        def parts = []
        if (type == PERSON) {
            if (title)
                parts << title
            if (firstname)
                parts << firstname
            if (namePart)
                parts << namePart
            if (lastname)
                parts << lastname
            return parts.join(' ')
        } else
            return name
    }

    boolean equals(o) {
        if (o == null) return false
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        if (id != o.id) return false
        return true
    }

    int hashCode() {
        return id?.hashCode() ?: 0
    }

    int eHashCode() {
        int result
        result = id.hashCode()
        result = 13 * result + (nation?.eHashCode() ?: 0)
        result = 13 * result + (salutation?.eHashCode() ?: 0)
        result = 13 * result + (note?.hashCode() ?: 0)
        result = 13 * result + (type?.hashCode() ?: 0)
        result = 13 * result + (lastname?.hashCode() ?: 0)
        result = 13 * result + (firstname?.hashCode() ?: 0)
        result = 13 * result + (namePart?.hashCode() ?: 0)
        result = 13 * result + (position?.hashCode() ?: 0)
        result = 13 * result + (organisation?.hashCode() ?: 0)
        result = 13 * result + (department?.hashCode() ?: 0)
        result = 13 * result + (title?.hashCode() ?: 0)
        result = 13 * result + (birthday?.hashCode() ?: 0)
        result = 13 * result + (name?.hashCode() ?: 0)
        result = 13 * result + (vatId?.hashCode() ?: 0)
        unitAddresses.sort { a, b -> a.id <=> b.id }.each {
            result = 13 * result + (it?.eHashCode() ?: 0)
        }
        unitCommunications.sort { a, b -> a.id <=> b.id }.each {
            result = 13 * result + (it?.eHashCode() ?: 0)
        }
        unitCategories.sort { a, b -> a.id <=> b.id }.each {
            result = 13 * result + (it?.eHashCode() ?: 0)
        }
        sourceRelations.sort { a, b -> a.id <=> b.id }.each {
            result = 13 * result + (it?.eHashCode() ?: 0)
        }
        targetRelations.sort { a, b -> a.id <=> b.id }.each {
            result = 13 * result + (it?.eHashCode() ?: 0)
        }
        return result
    }

    String getDisplay() {
        return name ?: ''
    }
}

@Entity
@AutoClone
class Unit_Address {
    @Id
    Integer id
    @ManyToOne
    @JoinColumn(name = "UNIT", nullable = false)
    Unit unit
    @ManyToOne
    @JoinColumn(name = "ADDRESS", nullable = false)
    Address address
    @ManyToOne
    @JoinColumn(name = "ADDRESS_TYPE", nullable = false)
    AddressType addressType
    String note

    @Version
    Timestamp version

    boolean equals(o) {
        if (o == null) return false
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        if (id != o.id) return false
        return true
    }

    int hashCode() {
        return id?.hashCode() ?: 0
    }

    int eHashCode() {
        int result
        result = id.hashCode()
        result = 13 * result + (note?.hashCode() ?: 0)
        result = 13 * result + (address?.eHashCode() ?: 0)
        result = 13 * result + (addressType?.eHashCode() ?: 0)
        return result
    }
}

@Entity
@AutoClone
class Unit_Communication {
    @Id
    Integer id
    @ManyToOne
    @JoinColumn(name = "UNIT", nullable = false)
    Unit unit
    @ManyToOne
    @JoinColumn(name = "COMMUNICATION", nullable = false)
    Communication communication
    @ManyToOne
    @JoinColumn(name = "COMMUNICATION_TYPE", nullable = false)
    CommunicationType communicationType
    String note

    @Version
    Timestamp version

    boolean equals(o) {
        if (o == null) return false
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        if (id != o.id) return false
        return true
    }

    int hashCode() {
        return id?.hashCode() ?: 0
    }

    int eHashCode() {
        int result
        result = id.hashCode()
        result = 13 * result + (note?.hashCode() ?: 0)
        result = 13 * result + (communication?.eHashCode() ?: 0)
        result = 13 * result + (communicationType?.eHashCode() ?: 0)
        return result
    }

    String getDisplay() {
        return getOneliner()
    }

    String getOneliner() {
        return getOneliner(communicationType, communication)
    }

    static String getOneliner(CommunicationType type, Communication com) {
        if (!com) return ''
        def l = []
        if (type?.useAreaCode)
            l << "+${com.nation?.tel}"
        l << com.text
        l.findAll { it }.join(' ')
        return l
    }
}

@Entity
@AutoClone
class Unit_Category {
    @Id
    Integer id
    @ManyToOne
    @JoinColumn(name = "UNIT", nullable = false)
    Unit unit
    @ManyToOne
    @JoinColumn(name = "CATEGORY", nullable = false)
    Category category

    @Version
    Timestamp version

    boolean equals(o) {
        if (o == null) return false
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        if (id != o.id) return false
        return true
    }

    int hashCode() {
        return id?.hashCode() ?: 0
    }

    int eHashCode() {
        int result
        result = id.hashCode()
        result = 13 * result + (category?.eHashCode() ?: 0)
        return result
    }
}
