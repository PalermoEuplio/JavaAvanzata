package controller;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.utility.Amministratore;
import model.db.DBConnector;
import model.connection.Sessione;
import model.Main;

/**
 * Controller per la pagina di login.
 */
public class LoginController implements Initializable {

    // Collegamenti agli elementi della pagina
    @FXML
    private TextField fieldUsername;

    @FXML
    private TextField fieldPassword;

    @FXML
    private Button enter;

    @FXML
    private Label errormsg;

    /**
     * Inizializza il controller.
     * 
     * @param location L'URL di location.
     * @param resources Le risorse.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Disabilito il pulsante di login se username o password sono vuoti
        enter.disableProperty().bind(fieldUsername.textProperty().isEmpty().or(fieldPassword.textProperty().isEmpty()));

    }

    // ------------- Metodi per l'interfaccia grafica ------------------

    /**
     * Comportamento del pulsante di login.
     * 
     * @throws IOException In caso di errori I/O.
     */
    @FXML
    private void login() throws IOException {

        errormsg.setVisible(false); // Disattivo l'eventuale messaggio d'errore che può essere comparso in
                                    // precedenza

        // Impedisco all'admin di loggarsi come ND
        if (fieldUsername.getText().equals("ND")) {
            errormsg.setText("* Errore: Utente non valido"); // Inserisco il relativo messaggio d'errore
            errormsg.setVisible(true);
            return;
        }

        try {
            // Mi collego al database per la verifica delle credenziali
            Amministratore adminlog = (Amministratore) new DBConnector()
                    .cerca(new Amministratore(fieldUsername.getText(), -1), fieldPassword.getText());

            // Se non viene sollevata un'eccezzione vuol dire che sono riuscito a collegarmi
            // e setto l'amministratore della sessione corrente
            Sessione.setAdmin(adminlog);
            Main.setRoot("adminDashboard"); // Mi sposto alla dashboardAmministratore

        } catch (SQLException e) { // In caso d'errore significa che o c'è stato un'errore o una delle credenziali
                                   // era errata
            errormsg.setText("* Errore: " + e.getMessage()); // Mostro il messaggio d'errore a video
            errormsg.setVisible(true);
        } catch (IllegalArgumentException e) {
            System.out.println("Parametri non gestiti: " + e.getMessage());
        } // Caso in cui è stato inserito un tipo non gestito nel metodo del DBController

    }
}
