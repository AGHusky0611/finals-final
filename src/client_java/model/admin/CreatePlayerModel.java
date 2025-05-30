package client_java.model.admin;

import Server.PlayerSide.PlayerInterface;
import Server.PlayerSide.PlayerInterfaceHelper;
import Server.Exceptions.LostConnectionException;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

public class CreatePlayerModel {
    private PlayerInterface playerServer;

    public CreatePlayerModel() {
        try {
            ORB orb = ORB.init(new String[0], null);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            playerServer = PlayerInterfaceHelper.narrow(ncRef.resolve_str("PlayerServer"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public String[] getRegisteredPlayers() throws LostConnectionException {
//        if (playerServer == null) {
//            throw new LostConnectionException("Player server not available");
//        }
//        return playerServer.getRegisteredPlayers();
//    }
//
//    public String[] getConnectedPlayers() throws LostConnectionException {
//        if (playerServer == null) {
//            throw new LostConnectionException("Player server not available");
//        }
//        return playerServer.getConnectedPlayers();
//    }
}
