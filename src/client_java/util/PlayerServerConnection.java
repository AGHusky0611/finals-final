package client_java.util;

import Server.PlayerSide.PlayerInterface;
import Server.PlayerSide.PlayerInterfaceHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import javax.swing.*;
import java.util.Properties;

public class PlayerServerConnection {
    private static PlayerInterface playerServer;

    public static PlayerInterface getPlayerServerConnection() throws Exception {
        if (playerServer == null) {
            initializeConnection();
        }
        return playerServer;
    }

    private static void initializeConnection() throws Exception {
        Properties props = new Properties();
        props.put("org.omg.CORBA.ORBInitialHost", "192.168.1.13");
        props.put("org.omg.CORBA.ORBInitialPort", "1049");
        props.put("com.sun.CORBA.transport.ORBConnectTimeout", "10000");

        ORB orb = ORB.init(new String[0], props);
        org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
        playerServer = PlayerInterfaceHelper.narrow(ncRef.resolve_str("PlayerServer"));
    }

    public static void handleConnectionError(Exception e) {
        JOptionPane.showMessageDialog(null,
                "Connection failed: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}