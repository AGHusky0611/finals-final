package client_java;

import client_java.controller.login.LoginController;
import client_java.model.login.LoginModel;
import client_java.view.login.Login;

public class main {
    public static void main(String[] args) {
        Login login = new Login();
        LoginModel loginModel = new LoginModel();
        LoginController loginController = new LoginController(login, loginModel);
    }
}
