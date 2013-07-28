/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package griffon.swing.factory

import javax.swing.JTable
import javax.swing.table.TableColumnModel
import groovy.util.logging.Log

/**
 * @author Alexander Klein
 * @author Hamlet D'Arcy
 */
@Log
class ColumnModelFactory extends AbstractFactory {

    Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        if (value instanceof TableColumnModel) {
            return value
        }

        Class jxTableClass = null
        try {
            jxTableClass = Class.forName("org.jdesktop.swingx.JXTable")
        } catch (ClassNotFoundException ex) {}

        if (jxTableClass != null && jxTableClass.isAssignableFrom(builder.current.getClass())) {
            return Class.forName("org.jdesktop.swingx.table.DefaultTableColumnModelExt").newInstance()
        } else {
            return new javax.swing.table.DefaultTableColumnModel()
        }
    }

    public void onNodeCompleted(FactoryBuilderSupport builder, Object parent, Object node) {
        if (!(parent instanceof JTable)) {
            log.warning("ColumnModel must be a child of a table. Found: " + parent.getClass());
        } else {
            parent.columnModel = node
        }
    }
}
