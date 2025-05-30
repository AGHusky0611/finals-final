package serverMain;

import Server.AdminSide.AdminInterface;
import Server.AdminSide.AdminInterfaceHelper;
import Server.PlayerSide.PlayerInterface;
import Server.PlayerSide.PlayerInterfaceHelper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HangmanServer {
    private static final Logger logger = Logger.getLogger(HangmanServer.class.getName());
    private static final String ANSI_GREEN = "\u001B[32m";

    public static void main(String[] args) {
        try {
            logger.info(ANSI_GREEN);
            // Configure ORB properties
            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBInitialPort", "1049");
            props.put("org.omg.CORBA.ORBInitialHost", "localhost");

            logger.info("[SERVER]: Starting Hangman Server");
            ORB orb = ORB.init(args, props);

            // Get root POA and activate
            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();

            // Get naming context
            org.omg.CORBA.Object nameServiceObj = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(nameServiceObj);
            if (ncRef == null) {
                throw new RuntimeException("[SERVER]: Failed to narrow NameService");
            }

            // Initialize and register services
            logger.info("[SERVER]: Registering services");
            registerPlayerService(rootPOA, orb, ncRef);
            registerAdminService(rootPOA, orb, ncRef);

            logger.info("[SERVER]: Hangman Server ready and waiting for connections");
            orb.run();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[SERVER]: Server error: " + e.getMessage(), e);
            System.exit(1);
        }
    }

    private static void registerPlayerService(POA rootPOA, ORB orb, NamingContextExt ncRef)
            throws ServantNotActive, WrongPolicy, InvalidName, org.omg.CosNaming.NamingContextPackage.InvalidName, CannotProceed, NotFound {
        PlayerImpl playerImpl = new PlayerImpl();
        org.omg.CORBA.Object ref = rootPOA.servant_to_reference(playerImpl);
        PlayerInterface playerRef = PlayerInterfaceHelper.narrow(ref);

        NameComponent[] path = ncRef.to_name("PlayerServer");
        ncRef.rebind(path, playerRef);

        logger.info("[SERVER]: Player service registered successfully");
    }

    private static void registerAdminService(POA rootPOA, ORB orb, NamingContextExt ncRef)
            throws ServantNotActive, WrongPolicy, InvalidName, org.omg.CosNaming.NamingContextPackage.InvalidName, CannotProceed, NotFound {
        AdminImpl adminImpl = new AdminImpl();
        org.omg.CORBA.Object ref = rootPOA.servant_to_reference(adminImpl);
        AdminInterface adminRef = AdminInterfaceHelper.narrow(ref);

        NameComponent[] path = ncRef.to_name("AdminServer");
        ncRef.rebind(path, adminRef);

        logger.info("[SERVER]: Admin service registered successfully");
    }
}