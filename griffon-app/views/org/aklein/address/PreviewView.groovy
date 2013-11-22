package org.aklein.address

import net.sf.jasperreports.engine.JasperCompileManager
import net.sf.jasperreports.engine.JasperFillManager
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.JasperReport

try {
    model.parameter = params
} catch (e) {
    model.parameter = [:]
}

try {
    model.reportFile = report
} catch (e) {
    model.reportFile = new File('')
}

try {
    model.con = connection
} catch (e) {
    model.con = null
}

JasperReport jasperReport = JasperCompileManager.compileReport(model.reportFile.absolutePath)
JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, model.parameter, model.con)

def i18nAction = UIHelper.&i18nAction.curry(app, builder)

actions {
    i18nAction('cancelAction', controller.cancel)
    i18nAction('okAction', controller.ok)
}

panel(id: 'content') {
    borderLayout()

    reportViewer(constraints: CENTER, jasperPrint: jasperPrint)

    panel(constraints: SOUTH) {
        gridLayout(cols: 3, rows: 1)
        button(okAction)
    }

    keyStrokeAction(component: current,
            keyStroke: "ESCAPE",
            condition: "in focused window",
            action: okAction)

    keyStrokeAction(component: current,
            keyStroke: "ENTER",
            condition: "in focused window",
            action: okAction)
}