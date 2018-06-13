package View;

import ViewModel.MyViewModel;
import algorithms.search.DepthFirstSearch;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PropertiesController {

    private MyViewModel viewModel;
    @FXML
    private Button btn_Cancel;
    public javafx.scene.control.Label lbl_propertyDescription;
    public javafx.scene.control.ChoiceBox choiceBox_generationAlgo;
    public javafx.scene.control.ChoiceBox choiceBox_solutionAlgo;
    private ObservableList<String> generationAlgorithmOptions = FXCollections.observableArrayList("MyMazeGenerator","SimpleMazeGenerator");
    private ObservableList<String> solutionAlgorithmOptions = FXCollections.observableArrayList("BestFirstSearch","BreadthFirstSearch", "DepthFirstSearch", "AStar");


    public void prepareToShow( MyViewModel viewModel){
        choiceBox_generationAlgo.setItems(generationAlgorithmOptions);
        choiceBox_solutionAlgo.setItems(solutionAlgorithmOptions);
        choiceBox_generationAlgo.setValue(MyViewModel.getConfiguration("generatorClass"));
        choiceBox_solutionAlgo.setValue(MyViewModel.getConfiguration("searchAlgorithm"));
        this.viewModel = viewModel;
    }


    public void propertiesShowGeneratorDescription(){
        lbl_propertyDescription.visibleProperty().setValue(true);
        lbl_propertyDescription.setText("Explanation:\nThis option sets the desired method for generating a new maze. Changing this will not affect your currently open maze.");
    }
    public void propertiesHideGeneratorDescription(){
        lbl_propertyDescription.visibleProperty().setValue(false);
        lbl_propertyDescription.setText("");
    }

    public void propertiesShowSolutionDescription(){
        lbl_propertyDescription.visibleProperty().setValue(true);
        lbl_propertyDescription.setText("Explanation:\nThis option sets the desired method for Solving a maze. Changing this will not affect your currently open maze.");
    }
    public void propertiesHideSolutionDescription(){
        lbl_propertyDescription.visibleProperty().setValue(false);
        lbl_propertyDescription.setText("");
    }

    public void applyProperties(){
        viewModel.changeConfiguration("generatorClass", choiceBox_generationAlgo.getValue().toString());
        viewModel.changeConfiguration("searchAlgorithm", choiceBox_solutionAlgo.getValue().toString());
    }

    public void saveAndExit(){
        applyProperties();
        exit();
    }

    public void exit(){
        Stage stage = (Stage) btn_Cancel.getScene().getWindow();
        stage.close();
    }

}
