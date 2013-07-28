application {
    title = 'Addressbook'
    startupGroups = ['mainlist', 'selection', 'address']

    // Should Griffon exit when no Griffon created frames are showing?
    autoShutdown = true

    // If you want some non-standard application class, apply it here
    //frameClass = 'javax.swing.JFrame'
}
mvcGroups {
    // MVC Group for "nationDetail"
    'nation' {
        model      = 'org.aklein.address.NationModel'
        view       = 'org.aklein.address.NationView'
        controller = 'org.aklein.address.NationController'
    }

    // MVC Group for "nationManagement"
    'nationManagement' {
        model      = 'org.aklein.address.NationManagementModel'
        view       = 'org.aklein.address.NationManagementView'
        controller = 'org.aklein.address.NationManagementController'
    }

    // MVC Group for "salutationDetail"
    'salutation' {
        model      = 'org.aklein.address.SalutationModel'
        view       = 'org.aklein.address.SalutationView'
        controller = 'org.aklein.address.SalutationController'
    }

    // MVC Group for "salutationManagement"
    'salutationManagement' {
        model      = 'org.aklein.address.SalutationManagementModel'
        view       = 'org.aklein.address.SalutationManagementView'
        controller = 'org.aklein.address.SalutationManagementController'
    }

    // MVC Group for "relationDetail"
    'relationDetail' {
        model      = 'org.aklein.address.RelationDetailModel'
        view       = 'org.aklein.address.RelationDetailView'
        controller = 'org.aklein.address.RelationDetailController'
    }

    // MVC Group for "communicationType"
    'communicationType' {
        model      = 'org.aklein.address.CommunicationTypeModel'
        view       = 'org.aklein.address.CommunicationTypeView'
        controller = 'org.aklein.address.CommunicationTypeController'
    }

    // MVC Group for "communicationTypeManagement"
    'communicationTypeManagement' {
        model      = 'org.aklein.address.CommunicationTypeManagementModel'
        view       = 'org.aklein.address.CommunicationTypeManagementView'
        controller = 'org.aklein.address.CommunicationTypeManagementController'
    }

    // MVC Group for "communicationSelection"
    'communicationSelection' {
        model      = 'org.aklein.address.CommunicationSelectionModel'
        view       = 'org.aklein.address.CommunicationSelectionView'
        controller = 'org.aklein.address.CommunicationSelectionController'
    }

    // MVC Group for "communicationDetail"
    'communicationDetail' {
        model      = 'org.aklein.address.CommunicationDetailModel'
        view       = 'org.aklein.address.CommunicationDetailView'
        controller = 'org.aklein.address.CommunicationDetailController'
    }

    // MVC Group for "addressType"
    'addressType' {
        model      = 'org.aklein.address.AddressTypeModel'
        view       = 'org.aklein.address.AddressTypeView'
        controller = 'org.aklein.address.AddressTypeController'
    }

    // MVC Group for "addressTypeManagement"
    'addressTypeManagement' {
        model      = 'org.aklein.address.AddressTypeManagementModel'
        view       = 'org.aklein.address.AddressTypeManagementView'
        controller = 'org.aklein.address.AddressTypeManagementController'
    }

    // MVC Group for "addressSelection"
    'addressSelection' {
        model      = 'org.aklein.address.AddressSelectionModel'
        view       = 'org.aklein.address.AddressSelectionView'
        controller = 'org.aklein.address.AddressSelectionController'
    }

    // MVC Group for "addressDetail"
    'addressDetail' {
        model      = 'org.aklein.address.AddressDetailModel'
        view       = 'org.aklein.address.AddressDetailView'
        controller = 'org.aklein.address.AddressDetailController'
    }

    // MVC Group for "googleSync"
    'googleSync' {
        model      = 'org.aklein.address.GoogleSyncModel'
        view       = 'org.aklein.address.GoogleSyncView'
        controller = 'org.aklein.address.GoogleSyncController'
    }

    // MVC Group for "category"
    'category' {
        model      = 'org.aklein.address.CategoryModel'
        view       = 'org.aklein.address.CategoryView'
        controller = 'org.aklein.address.CategoryController'
    }

    // MVC Group for "categoryManagement"
    'categoryManagement' {
        model      = 'org.aklein.address.CategoryManagementModel'
        view       = 'org.aklein.address.CategoryManagementView'
        controller = 'org.aklein.address.CategoryManagementController'
    }

    // MVC Group for "categories"
    'categories' {
        model      = 'org.aklein.address.CategoriesModel'
        view       = 'org.aklein.address.CategoriesView'
        controller = 'org.aklein.address.CategoriesController'
    }

    // MVC Group for "multiselection"
    'multiselection' {
        model      = 'org.aklein.address.MultiselectionModel'
        view       = 'org.aklein.address.MultiselectionView'
        controller = 'org.aklein.address.MultiselectionController'
        config {
            component = true
        }
    }

    // MVC Group for "masterlist"
    'masterlist' {
        model      = 'org.aklein.address.MasterlistModel'
        view       = 'org.aklein.address.MasterlistView'
        controller = 'org.aklein.address.MasterlistController'
        config {
            component = true
        }
    }

    // MVC Group for "unit"
    'unit' {
        model      = 'org.aklein.address.UnitModel'
        view       = 'org.aklein.address.UnitView'
        controller = 'org.aklein.address.UnitController'
    }

    // MVC Group for "mainlist"
    'mainlist' {
        model = 'org.aklein.address.MainlistModel'
        view = 'org.aklein.address.MainlistView'
        controller = 'org.aklein.address.MainlistController'
    }

    // MVC Group for "selection"
    'selection' {
        model = 'org.aklein.address.SelectionModel'
        view = 'org.aklein.address.SelectionView'
        controller = 'org.aklein.address.SelectionController'
    }

    // MVC Group for "preferences"
    'preferences' {
        model = 'org.aklein.address.PreferencesModel'
        view = 'org.aklein.address.PreferencesView'
        controller = 'org.aklein.address.DialogController'
    }

    // MVC Group for "license"
    'license' {
        model = 'org.aklein.address.LicenseModel'
        view = 'org.aklein.address.LicenseView'
        controller = 'org.aklein.address.DialogController'
    }

    // MVC Group for "credits"
    'credits' {
        model = 'org.aklein.address.CreditsModel'
        view = 'org.aklein.address.CreditsView'
        controller = 'org.aklein.address.DialogController'
    }

    // MVC Group for "about"
    'about' {
        model = 'org.aklein.address.AboutModel'
        view = 'org.aklein.address.AboutView'
        controller = 'org.aklein.address.DialogController'
    }

    // MVC Group for "address"
    'address' {
        model = 'org.aklein.address.AddressModel'
        view = 'org.aklein.address.AddressView'
        controller = 'org.aklein.address.AddressController'
    }
}
