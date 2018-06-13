package View;

import ViewModel.MyViewModel;
import ViewModel.MyViewModel.EventType;
import algorithms.search.Solution;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Observable;

public class MyViewController implements IView{

    @FXML
    private MyViewModel viewModel;
    public javafx.scene.control.TextField txtfld_rowsNum;
    public javafx.scene.control.TextField txtfld_columnsNum;
    public javafx.scene.control.Label lbl_rowsNum;
    public javafx.scene.control.Label lbl_columnsNum;
    public javafx.scene.control.Button btn_newMaze;
    public javafx.scene.control.Button btn_flashSolution;
    public javafx.scene.control.ToggleButton tglbtn_showSolution;
    public Label lbl_statusText;
    public BorderPane bdpn_background;

    public MazeDisplayer mazeDisplayer = new MazeDisplayer();
    private String invalidRowsOrColumnsMessage = "Rows and Columns must be numbers, equal to or greater than 5.";

    public void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;
        bindProperties(viewModel);
    }

    private void bindProperties(MyViewModel viewModel) {
        lbl_rowsNum.textProperty().bind(viewModel.characterPositionRow);
        lbl_columnsNum.textProperty().bind(viewModel.characterPositionColumn);
    }

    public void setMaze(int[][] maze) {
        mazeDisplayer.setMaze(maze);
    }
    public void setSolution(int[][] solution) {
        mazeDisplayer.setSolution(solution);
    }

    private void positionCharacter(){
        int characterPositionRow = viewModel.getCharacterPositionRow();
        int characterPositionColumn = viewModel.getCharacterPositionColumn();
        mazeDisplayer.setCharacterPosition(characterPositionRow, characterPositionColumn);
        this.characterPositionRow.set(characterPositionRow + "");
        this.characterPositionColumn.set(characterPositionColumn + "");
    }

    public void toggleSolutionVisibility(ActionEvent actionEvent){
        if(tglbtn_showSolution.isSelected()) {
            displaySolution();
            btn_flashSolution.setDisable(true);
        }
        else {
            hideSolution();
            btn_flashSolution.setDisable(false);
        }
    }
    public void displaySolution(){
        mazeDisplayer.showSolution();
    }
    public void hideSolution(){
        mazeDisplayer.hideSolution();
    }
    private void animationInvalidMovement(){
//        final Timeline timeline = new Timeline();
//        timeline.setCycleCount(Timeline.INDEFINITE);
//        timeline.setAutoReverse(true);
//        final KeyValue kv = new KeyValue(bdpn_background.setStyle("-fx-background-color: #800000;"));
//        final KeyFrame kf = new KeyFrame(Duration.millis(250), kv);
//        timeline.getKeyFrames().add(kf);
//        timeline.play();
//        bdpn_background.setStyle("-fx-background-color: #800000;");
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        bdpn_background.setStyle("-fx-background-color: #d5e8ff;");

    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == viewModel) {
            if (arg == EventType.MAZE){
                setMaze(viewModel.getMaze());
                positionCharacter();
                btn_newMaze.setDisable(false);
                lbl_statusText.setText("Ready");
            }
            else if (arg == EventType.MOVEMENT ){
                positionCharacter();
            }
            else if (arg == EventType.SOLUTION){
                setSolution(viewModel.getSolution());
                lbl_statusText.setText("Ready");
                btn_flashSolution.setDisable(false);
                tglbtn_showSolution.setDisable(false);
            }
            else if (arg == EventType.INVALIDMOVEMENT){
                animationInvalidMovement();
            }
            else if (arg == EventType.ERRORMESSAGE){

            }
            else if (arg instanceof String){

            }
        }
    }

    @Override
    public void newGame() {
        btn_newMaze.setDisable(true);
        try{
            int rows = Integer.valueOf(txtfld_rowsNum.getText());
            int columns = Integer.valueOf(txtfld_columnsNum.getText());
            lbl_statusText.setText("Generating maze...");
            mazeDisplayer.hideSolution();
            viewModel.generateMaze(rows, columns);
            lbl_statusText.setText("Solving maze...");
            viewModel.generateSolution();
        }
        catch(NumberFormatException e){
//            e.printStackTrace();
            showAlert(invalidRowsOrColumnsMessage);
            btn_newMaze.setDisable(false);
        }
    }



    private void showAlert(String alertMessage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(alertMessage);
        alert.show();
    }

    public void KeyPressed(KeyEvent keyEvent) {
        if(keyEvent.getCode().isDigitKey()){
            viewModel.moveCharacter(keyEvent.getCode());
        }
        keyEvent.consume();
    }

    //region String Property for Binding
    public StringProperty characterPositionRow = new SimpleStringProperty();
    public StringProperty characterPositionColumn = new SimpleStringProperty();


    public String getCharacterPositionRow() {
        return characterPositionRow.get();
    }

    public StringProperty characterPositionRowProperty() {
        return characterPositionRow;
    }

    public String getCharacterPositionColumn() {
        return characterPositionColumn.get();
    }

    public StringProperty characterPositionColumnProperty() {
        return characterPositionColumn;
    }

    //end region String Property for Binding

    public void setResizeEvent(Scene scene) {
        long width = 0;
        long height = 0;
        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
//                System.out.println("Width: " + newSceneWidth); //TODO resize maze
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
//                System.out.println("Height: " + newSceneHeight); //TODO resize maze
            }
        });
    }

    public void About(ActionEvent actionEvent) {
        try {
            Stage stage = new Stage();
            stage.setTitle("AboutController");
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent root = fxmlLoader.load(getClass().getResource("About.fxml").openStream());
            Scene scene = new Scene(root, 400, 350);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); //Lock the window until it closes
            stage.show();
        } catch (Exception e) {

        }
    }

    public void PropertiesStage(ActionEvent actionEvent) {
        try {
            Stage stage = new Stage();
            stage.setTitle("Properties");
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent root = fxmlLoader.load(getClass().getResource("Properties.fxml").openStream());
            Scene scene = new Scene(root, 600, 350);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); //Lock the window until it closes

            PropertiesController propWindow = fxmlLoader.getController();
            propWindow.prepareToShow(viewModel);

            stage.show();
        } catch (Exception e) {

        }
    }






    @Override
    public void solve() {

    }

    @Override
    public void exit() {
        viewModel.exit();
    }

    @Override
    public void saveGame() {

    }

    @Override
    public void loadGame() {

    }
}
