log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    appenders {
        console name: 'stdout', layout: pattern(conversionPattern: '%d [%t] %-5p %c - %m%n')
    }

    error 'org.codehaus.griffon'

    info 'griffon.util',
            'griffon.core',
            'griffon.swing',
            'griffon.app',
            'com.healthmarketscience.jackcess'
}

splash.image = 'Book.png'

i18n.basename = 'messages'

database {
    importData {
        Nation__Nation {
            id = 'N_Id'
            iso2 = 'N_ISO2'
            iso3 = 'N_ISO3'
            tel = 'N_Tel'
            name = 'N_Name'
            english = 'N_Englisch'
        }
        Adresse__Address {
            id = 'A_Id'
            nation = 'A_N_Id'
            street = 'A_Strasse'
            zip = 'A_PLZ'
            city = 'A_Ort'
            addition = 'A_Zusatz'
            region = 'A_Region'
        }
        AdressTyp__AddressType {
            id = 'AT_Id'
            name = 'AT_Name'
            usePostalCode = 'AT_UsePostalCode'
        }
        Anrede__Salutation {
            id = 'AR_Id'
            abbrev = 'AR_Kurz'
            letter = 'AR_Brief'
            address = 'AR_Adresse'
        }
        Kommunikation__Communication {
            id = 'K_Id'
            nation = 'K_N_Id'
            text = 'K_Text'
        }
        KommunikationsTyp__CommunicationType {
            id = 'KT_Id'
            name = 'KT_Name'
            useAreaCode = 'KT_UseVorwahl'
        }
        /*
        Position__Position {
            id = 'PO_Id'
            name = 'PO_Name'
            abbrev = 'PO_Krz'
            description = 'PO_Beschr'
        }
        */
        Rubrik__Category {
            id = 'R_Id'
            name = 'R_Name'
        }
        Unit__Unit {
            id = 'U_Id'
            nation = 'U_N_Id'
            salutation = 'U_AR_Id'
            note = 'U_Notiz'
            type = 'U_Typ'
            lastname = 'P_Nachname'
            firstname = 'P_Vorname'
            namePart = 'P_NameBest'
            position = 'P_Beruf'
            organisation = 'P_Orga'
            department = 'P_Abteilung'
            title = 'P_Titel'
            birthday = 'P_GebDat'
            name = 'O_Name'
            vatId = 'O_UStId'
        }
        Beziehung__Relation {
            id = 'B_Id'
            unit = 'U_Id'
            relation = 'U_Bez_Id'
            //position = 'PO_Id'
            description = 'B_Beschr'
        }
        Unit_Adresse__Unit_Address {
            id = 'ID'
            unit = 'U_Id'
            address = 'A_Id'
            addressType = 'AT_Id'
            note = 'UA_Notiz'
        }
        Unit_Kommunikation__Unit_Communication {
            id = 'ID'
            unit = 'U_Id'
            communication = 'K_Id'
            communicationType = 'KT_Id'
            note = 'UK_Notiz'
        }
        Unit_Rubrik__Unit_Category {
            id = 'ID'
            unit = 'U_Id'
            category = 'R_Id'
        }
    }
}

griffon.ebean.injectInto = ['controller', 'service', 'model']

addressbook {
    name = 'Addressbook'
    clientId = '989344344561.apps.googleusercontent.com'
    clientSecret = 'a_nShCQWECuNrbZUApAS2_-X'
    home = '.addressbook'
    oauth = 'oauth2.json'
    scopes = ['https://www.google.com/m8/feeds']
    dateFormats = ['MM/dd/yyyy', 'MM/dd/yy', 'dd.MM.yy', 'dd.MM.yyyy', 'yy-MM-dd', 'yyyy-MM-dd']
    organisationSeparator = '|'
    defaultAddressType = 'Büro'

    phoneTypePattern = ~/(?i).*(Telefon|Fax).*/
    phoneTypeMapping = [
            ASSISTANT: null,
            CALLBACK: null,
            CAR: null,
            FAX: 'Fax, Büro',
            GENERAL: 'Telefon, Büro',
            HOME: 'Telefon, Privat',
            HOME_FAX: 'Fax, Privat',
            INTERNAL_EXTENSION: null,
            ISDN: null,
            MAIN: 'Telefon, Büro',
            MOBILE: 'Telefon, Mobil',
            OTHER: 'Telefon, Andere',
            OTHER_FAX: null,
            PAGER: null,
            RADIO: null,
            SATELLITE: null,
            TELEX: null,
            TTY_TDD: null,
            WORK_FAX: 'Fax, Büro',
            WORK_MOBILE: 'Telefon, Mobil2',
            WORK_PAGER: null,
            COMPANY_MAIN: 'Telefon, Büro',
            DEFAULT: 'Telefon, Andere'
    ]

    emailTypePattern = ~/(?i).*Email.*/
    emailTypeMapping = [
            GENERAL: 'Email, Büro/Privat',
            HOME: 'Email, Büro/Privat',
            OTHER: 'Email, Andere',
            WORK: 'Email, Büro/Privat',
            DEFAULT: 'Email, Sonstige'
    ]

    imTypePattern = ~/(?i).*Messaging.*/
    imTypeMapping = [
            HOME: null,
            WORK: null,
            OTHER: null,
            DEFAULT: 'Email, Sonstige'
    ]

    webTypePattern = ~/(?i).*WWW.*/
    webTypeMapping = [
            BLOG: null,
            FTP: null,
            HOME: 'WWW, Privat',
            HOME_PAGE: 'WWW, Privat',
            OTHER: null,
            PROFILE: null,
            WORK: 'WWW, Büro',
            DEFAULT: 'WWW, Büro'
    ]
}
