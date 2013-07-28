import javax.swing.JOptionPane

onBootstrapEnd = { app ->
  app.addShutdownHandler([
          canShutdown: { a ->
            return JOptionPane.showConfirmDialog(
                    app.windowManager.windows.find {it.focused},
                    app.getMessage('application.confirm.shutdown.message', 'Do you really want to exit?'),
                    app.getMessage('application.confirm.shutdown.title','Exit'),
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION
          },
          onShutdown: { a -> }
  ] as griffon.core.ShutdownHandler)
}