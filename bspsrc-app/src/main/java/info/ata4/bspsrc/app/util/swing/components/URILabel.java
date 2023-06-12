package info.ata4.bspsrc.app.util.swing.components;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class URILabel extends JLabel {

    private static final Logger L = LogManager.getLogger();
    private URI uri;

    public URILabel() {
        super();
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new URLOpenAdapter());
    }

    public void setURI(String text, URI uri) {
        this.uri = uri;
        super.setText("<html><body><a href=\"" + uri + "\">" + text + "</a></body></html>");
    }

    public void setURI(String text, String uriString) {
        try {
            setURI(text, new URI(uriString));
        } catch (URISyntaxException ex) {
            L.warn("Invalid URI format", ex);
            setText(text);
        }
    }

    private class URLOpenAdapter extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(uri);
                } catch (IOException ex) {
                    L.warn("Can't browse URI", ex);
                }
            }
        }
    }
}