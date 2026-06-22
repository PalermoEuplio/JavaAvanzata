package controller;


import java.io.IOException;
import javafx.scene.control.Button;
import javafx.fxml.*;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import model.Main;

public class PrimaryController {

    
    @FXML
    Button btnSi;
    
    @FXML
    TextArea areaId;
    
    @FXML
    TextField fieldId;
    
    
    @FXML
    private void createServer() throws IOException {
        

    }

    
    @FXML
    private void createClient() throws IOException {

    }
    
    
    
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }
    
    
    @FXML
private void sendMsg() throws IOException {
   
}
    
}
