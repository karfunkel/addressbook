dataSource {
    driverClassName = 'org.h2.Driver'
    username = 'sa'
    password = ''
    tokenizeddl = false // set this to true if using MySQL or any other
    // RDBMS that requires execution of DDL statements
    // on separate calls
    pool {
        maxWait = 60000
        maxIdle = 5
        maxActive = 8
    }
}
environments {
    development {
        dataSource {
            dbCreate = new File(System.getProperty('user.home'), ".addressbook/database-dev.h2.db").exists() ? 'skip' : 'create' // one of ['create', 'skip']
            url = 'jdbc:h2:~/.addressbook/database-dev'
        }
    }
    test {
        dataSource {
            dbCreate = 'create'
            url = 'jdbc:h2:mem:address-test'
        }
    }
    production {
        dataSource {
            dbCreate = new File(System.getProperty('user.home'), ".addressbook/database.h2.db").exists() ? 'skip' : 'create'
            url = 'jdbc:h2:~/.addressbook/database'
        }
    }
}
