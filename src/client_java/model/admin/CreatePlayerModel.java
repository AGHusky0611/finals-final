package client_java.model.admin;

import Server.AdminSide.AdminInterface;
import Server.AdminSide.AdminInterfaceHelper;
import Server.CommonObjects.User;
import Server.Exceptions.LostConnectionException;
import Server.Exceptions.NotLoggedInException;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

public class CreatePlayerModel {
    private AdminInterface adminServer;

    public CreatePlayerModel() {
        try {
            ORB orb = ORB.init(new String[0], null);
            NamingContextExt nc = NamingContextExtHelper.narrow(
                    orb.resolve_initial_references("NameService"));
            adminServer = AdminInterfaceHelper.narrow(nc.resolve_str("AdminService"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createPlayer(String username, String password)
            throws LostConnectionException, NotLoggedInException {
        adminServer.createPlayer(username, password);
    }

    public String[] getRegisteredPlayers()
            throws LostConnectionException, NotLoggedInException {
        User[] users = adminServer.getUserList();
        String[] ids = new String[users.length];
        for (int i = 0; i < users.length; i++) {
            ids[i] = users[i].userId;
        }
        return ids;
    }
}
