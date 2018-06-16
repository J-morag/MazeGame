package View;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class HelpController implements Initializable {

    @FXML
    public ImageView instructionsImg;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Image img = new Image(new File("resources/Images/help1.jpg").toURI().toString());
        instructionsImg.setImage(img);
    }
}
