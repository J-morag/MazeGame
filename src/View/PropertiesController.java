package View;

import ViewModel.MyViewModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PropertiesController {

    public javafx.scene.control.Label lbl_propertyDescription;
    public javafx.scene.control.ChoiceBox choiceBox_generationAlgo;
    public javafx.scene.control.ChoiceBox choiceBox_solutionAlgo;
    private ObservableList<String> generationAlgorithmOptions = FXCollections.observableArrayList("MyMazeGenerator","SimpleMazeGenerator");


    public void prepareToShow(){
        choiceBox_generationAlgo.setItems(generationAlgorithmOptions);
        choiceBox_generationAlgo.accessibleTextProperty().setValue(MyViewModel.getConfiguration("generatorClass"));
    }


    public void propertiesShowGeneratorDescription(){
        lbl_propertyDescription.visibleProperty().setValue(true);
        lbl_propertyDescription.setText("Explanation:\nThis option sets the desired method for generating a new maze. Changing this will not effect your current open maze.");
    }
    public void propertiesHideGeneratorDescription(){
        lbl_propertyDescription.visibleProperty().setValue(false);
        lbl_propertyDescription.setText("");
    }

    public void propertiesShowSolutionDescription(){
        lbl_propertyDescription.visibleProperty().setValue(true);
        lbl_propertyDescription.setText("Explanation:\nThis option sets the desired method for Solving amaze.");
    }
    public void propertiesHideSolutionDescription(){
        lbl_propertyDescription.visibleProperty().setValue(false);
        lbl_propertyDescription.setText("");
    }

    public void applyProperties(){
        //TODO implement
    }

    public void generationConfigurationChange() {
        //TODO implement

    }
}
