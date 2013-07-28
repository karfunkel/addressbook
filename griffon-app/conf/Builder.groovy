import griffon.swing.factory.ColumnModelFactory
import org.codehaus.griffon.runtime.builder.factory.MetaComponentFactory2

features {
    factories {
        root = [
                columnModelFix: new ColumnModelFactory(),
                metaComponentFix: new MetaComponentFactory2()
        ]
    }
}

root {
    'groovy.swing.SwingBuilder' {
        controller = ['Threading']
        view = '*'
    }
}






jx {
    'groovy.swing.SwingXBuilder' {
        view = '*'
    }
}
