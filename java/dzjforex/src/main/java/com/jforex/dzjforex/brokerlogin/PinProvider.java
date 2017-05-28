package com.jforex.dzjforex.brokerlogin;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.Zorro;

// Code from http://www.dukascopy.com/wiki/#JForex_SDK_LIVE_mode
public class PinProvider {

    private final IClient client;
    private final String liveURL;
    private static JFrame noParentFrame = null;

    private final static Logger logger = LogManager.getLogger(PinDialog.class);

    public PinProvider(final IClient client,
                       final String liveURL) {
        this.client = client;
        this.liveURL = liveURL;
    }

    public String getPin() {
        PinDialog pd = null;
        try {
            pd = new PinDialog();
        } catch (final Exception e) {
            logger.error("getPin exc: " + e.getMessage());
            Zorro.indicateError();
        }
        return pd.pinfield.getText();
    }

    private class PinDialog extends JDialog {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        private final JTextField pinfield = new JTextField();

        public PinDialog() throws Exception {
            super(noParentFrame, "PIN Dialog", true);

            final JPanel captchaPanel = new JPanel();
            captchaPanel.setLayout(new BoxLayout(captchaPanel, BoxLayout.Y_AXIS));

            final JLabel captchaImage = new JLabel();
            captchaImage.setIcon(new ImageIcon(client.getCaptchaImage(liveURL)));
            captchaPanel.add(captchaImage);

            captchaPanel.add(pinfield);
            getContentPane().add(captchaPanel);

            final JPanel buttonPane = new JPanel();

            final JButton btnLogin = new JButton("Login");
            buttonPane.add(btnLogin);
            btnLogin.addActionListener(e -> {
                setVisible(false);
                dispose();
            });

            final JButton btnReload = new JButton("Reload");
            buttonPane.add(btnReload);
            btnReload.addActionListener(e -> {
                try {
                    captchaImage.setIcon(new ImageIcon(client.getCaptchaImage(liveURL)));
                } catch (final Exception ex) {
                    logger.error("getPin exc: " + ex.getMessage());
                    Zorro.indicateError();
                }
            });
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            pack();
            setVisible(true);
        }
    }
}
