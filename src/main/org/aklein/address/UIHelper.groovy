package org.aklein.address

import ca.odell.glazedlists.EventList
import ca.odell.glazedlists.TextFilterator
import ca.odell.glazedlists.swing.EventComboBoxModel

import javax.swing.Action
import javax.swing.JComboBox
import javax.swing.KeyStroke
import griffon.core.GriffonApplication

import java.beans.PropertyChangeSupport
import java.text.FieldPosition
import java.text.Format
import java.text.ParsePosition

class UIHelper {
    static Action i18nAction(GriffonApplication app, def builder, String id, Closure closure) {
        def key = "application.action.${id[0..-7].capitalize()}"
        def map = [
                id: id,
                name: app.getMessage("${key}.name"),
                shortDescription: app.getMessage("${key}.shortDescription", (String) null),
                mnemonic: app.getMessage("${key}.mnemonic", (String) null),
                accelerator: KeyStroke.getKeyStroke(app.getMessage("${key}.accelerator", (String) null)),
                closure: closure
        ]

        try {
            map.smallIcon = builder.crystalIcon(icon: app.getMessage("${key}.icon"), size: 16, category: 'actions')
        } catch (e) {}

        try {
            map.swingLargeIconKey = builder.crystalIcon(icon: app.getMessage("${key}.icon"), size: 32, category: 'actions')
        } catch (e) {}


        return builder.action(map)
    }

    static String withSeparator(String target, def separator = ' ', def value) {
        target ? target << separator << value : value
    }

    static boolean checkBoolean(GriffonApplication app, Boolean value, String filter) {
        if (!filter)
            return true

        if (value == null)
            return !filter

        if (value)
            return app.getMessage('application.checkBoolean.true', 't,true').toLowerCase().split(',').contains(filter.toLowerCase())
        else
            return app.getMessage('application.checkBoolean.false', 'f,false').toLowerCase().split(',').contains(filter.toLowerCase())
    }

    static def createComboBox(builder, EventList itemList, String displayProperty, String constraints = 'grow, pushy') {
        builder.with {
            def cb
            cb = comboBox(
                    model: new EventComboBoxModel(itemList),
                    renderer: listCellRenderer {
                        label()
                        onRender { children[0].text = value?."$displayProperty" ?: '' }
                    },
                    constraints: constraints
            ) {
                def support = installComboBoxAutoCompleteSupport(
                        items: itemList,
                        textFilterator: { list, item -> if (item?."$displayProperty") list << item."$displayProperty" } as TextFilterator,
                        format: new ItemPropertyFormat(itemList, displayProperty)
                )
                support.strict = true
            }
            return cb
        }
    }
}

class BindDelegate<T> {
    @Delegate
    private PropertyChangeSupport pcs

    T delegate

    BindDelegate(T delegate) {
        this.delegate = delegate
        this.pcs = new PropertyChangeSupport(delegate)
    }

    def methodMissing(String name, def args) {
        if (name.startsWith('set') && name[3] == name[3].toUpperCase() && args.size() == 1) {
            def property
            if (name[4] == name[4].toUpperCase())
                property = name[3..<name.size()]
            else
                property = name[3].toLowerCase() + name[4..<name.size()]
            def old = delegate[property]
            delegate[property] = args[0]
            pcs.firePropertyChange(property, old, args[0])
        } else
            return delegate.invokeMethod(name, args)
    }

    def propertyMissing(String name) {
        return delegate[name]
    }

    def propertyMissing(String name, def arg) {
        def old = delegate[name]
        delegate[name] = arg
        pcs.firePropertyChange(name, old, arg)
    }
}

class ItemPropertyFormat extends Format {
    List list
    String property

    ItemPropertyFormat(List list, String property) {
        this.list = list
        this.property = property
    }

    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        if (obj != null)
            toAppendTo << obj."$property"
        return toAppendTo
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        list.find { it."$property" == source.substring(pos.getIndex()) }
    }
}
