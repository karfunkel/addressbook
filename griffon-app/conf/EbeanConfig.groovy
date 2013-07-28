ebeanServer {
    // specify any properties from com.avaje.ebean.config.ServerConfig
    debugSql = true
    debugLazyLoad = true
}

/*
ebeanServers {
    someName {
        someConfigurationProperty = someValue
    }
}
*/

environments {
    development {
        ebeanServer {
            // someConfigurationProperty = someValue
        }
    }
    test {
        ebeanServer {
            // someConfigurationProperty = someValue
        }
    }
    production {
        ebeanServer {
            searchJars=['address.jar']
            debugSql = false
            debugLazyLoad = false
        }
    }
}
