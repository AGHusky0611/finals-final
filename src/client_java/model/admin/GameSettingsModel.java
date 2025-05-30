package client_java.model.admin;

import Server.AdminSide.AdminInterface;
import Server.AdminSide.AdminInterfaceHelper;
import Server.CommonObjects.GameRules;
import Server.Exceptions.LostConnectionException;
import Server.Exceptions.NotLoggedInException;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

public class GameSettingsModel {
    private AdminInterface adminServer;

    public GameSettingsModel() {
        try {
            ORB orb = ORB.init(new String[0], null);
            NamingContextExt nc = NamingContextExtHelper.narrow(
                    orb.resolve_initial_references("NameService"));
            adminServer = AdminInterfaceHelper.narrow(nc.resolve_str("AdminService"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Update waitTime & roundDuration (in seconds).
     * Passing (0,0) simply fetches current settings.
     */
    public GameRules changeRules(int waitTime, int roundDuration)
            throws LostConnectionException, NotLoggedInException {
        return adminServer.changeRules(waitTime, roundDuration);
    }
}
