package View;

import ViewModel.MyViewModel;
import ViewModel.MyViewModel.EventType;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.Observable;
import java.util.ResourceBundle;

public class MyViewController implements IView, Initializable{

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
    public Slider masterVolume;

    public MazeDisplayer mazeDisplayer = new MazeDisplayer();
    private final String invalidRowsOrColumnsMessage = "Rows and Columns must be numbers, equal to or greater than 5.";
    final Media track1 = new Media(new File("resources/Sounds/track1.mp3").toURI().toString());
    final MediaPlayer backgroundMusic = new MediaPlayer(track1);
    private boolean BGMisPlaying = false;
    final Media victoryMusic1 = new Media(new File("resources/Sounds/victory1.mp3").toURI().toString());
    private MediaPlayer victoryMusic = new MediaPlayer(victoryMusic1);
    final Media ouch1 = new Media(new File("resources/Sounds/ouch1.wav").toURI().toString());
    private MediaPlayer characterHurtSound = new MediaPlayer(ouch1);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        masterVolume.valueProperty().addListener((observable, oldValue, newValue) -> {
            backgroundMusic.setVolume(masterVolume.getValue()*1.0);
        });
    }

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


    @Override
    public void update(Observable o, Object arg) {
        if (o == viewModel) {
            if (arg == EventType.MAZE){ //new maze to display
                lbl_statusText.setText("Drawing maze...");//update status indicator

                //draw maze after ui updates status indicator
                Task<Void> drawMaze = new Task<Void>() {
                    @Override
                    public Void call() {
                        //create a delay for status text to update
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        //draw maze in javaFX thread
                        Platform.runLater(() -> {
                            setMaze(viewModel.getMaze());
                            positionCharacter();

                            //solve maze in separate task. will result in another update() with EventType.SOLUTION as arg.
                            lbl_statusText.setText("Solving maze...");
                            Task<Void> solve = new Task<Void>() {
                                @Override
                                public Void call() {
                                    viewModel.generateSolution();
                                    return null ;
                                }
                            };
                            new Thread(solve).start();
                        });

                        return null ;
                    }
                };
                new Thread(drawMaze).start();
            }
            else if (arg == EventType.MOVEMENT ){
                positionCharacter();
            }
            else if (arg == EventType.SOLUTION){
                setSolution(viewModel.getSolution());
                lbl_statusText.setText("Ready");
                btn_newMaze.setDisable(false);
                btn_newMaze.requestFocus();
                btn_flashSolution.setDisable(false);
                tglbtn_showSolution.setDisable(false);
            }
            else if (arg == EventType.INVALIDMOVEMENT){
                characterHurtSound.stop();
                characterHurtSound.setVolume(masterVolume.getValue()*0.5);
                characterHurtSound.play();
                mazeDisplayer.animationCharacterHurt();
            }
            else if (arg == EventType.ERRORMESSAGE){

            }
            else if (arg == EventType.VICTORY){
                backgroundMusic.stop();
                BGMisPlaying = false;
                victoryMusic.stop();
                victoryMusic.setVolume(masterVolume.getValue()*0.7);
                victoryMusic.play();
                tglbtn_showSolution.setDisable(true);
                btn_flashSolution.setDisable(true);
                mazeDisplayer.setVictory();
            }
            else if (arg instanceof String){
                showAlert((String)arg);
            }
        }
    }

    /**
     * make a new maze, display it. when the new maze is displayed, a solution will also be generated (not in this method - will be run
     * in a separate Task, created from update())
     */
    @Override
    public void newGame() {

        btn_newMaze.setDisable(true);
        tglbtn_showSolution.setSelected(false);
        tglbtn_showSolution.setDisable(true);
        btn_flashSolution.setDisable(true);
        try{
            int rows = Integer.valueOf(txtfld_rowsNum.getText());
            int columns = Integer.valueOf(txtfld_columnsNum.getText());
            if( rows < 5 || columns < 5) throw new NumberFormatException();
            mazeDisplayer.hideSolution();
            lbl_statusText.setText("Generating maze...");

            //generate in a separate task, to avoid freezing the GUI.
            Task<Void> generate = new Task<Void>() {
                @Override
                public Void call() {
                    viewModel.generateMaze(rows, columns);
                    return null ;
                }
            };
            playBGM();
            new Thread(generate).start();
        }
        catch(NumberFormatException e){
            showAlert(invalidRowsOrColumnsMessage);
            btn_newMaze.setDisable(false);
            tglbtn_showSolution.setDisable(false);
            btn_flashSolution.setDisable(false);
        }
    }

    private void playBGM() {
        if(!BGMisPlaying){
            victoryMusic.stop();

            backgroundMusic.setOnEndOfMedia(() -> BGMisPlaying = false );
            backgroundMusic.setVolume(masterVolume.getValue()*1.0);
            backgroundMusic.setAutoPlay(true);
            backgroundMusic.play();
            BGMisPlaying = true;
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
        else if(keyEvent.getCode() == KeyCode.ENTER){
            newGame();
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
                mazeDisplayer.setHeight(newSceneWidth.intValue()-200);
                mazeDisplayer.redraw();
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                mazeDisplayer.setWidth(newSceneHeight.intValue()-50);
                mazeDisplayer.redraw();
            }
        });
    }

    public void About(ActionEvent actionEvent) {
        try {
            Stage stage = new Stage();
            stage.setTitle("About");
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent root = fxmlLoader.load(getClass().getResource("About.fxml").openStream());
            Scene scene = new Scene(root, 650, 450);
            stage.setMinHeight(450);
            stage.setMinWidth(650);
            stage.setMaxHeight(450);
            stage.setMaxWidth(650);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); //Lock the window until it closes
            stage.show();
        } catch (Exception e) {

        }
    }

    public void Help(ActionEvent actionEvent) {
        try {
            Stage stage = new Stage();
            stage.setTitle("Instructions");
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent root = fxmlLoader.load(getClass().getResource("Help.fxml").openStream());
            Scene scene = new Scene(root, 1000, 590);
            stage.setMinHeight(590);
            stage.setMinWidth(1000);
            stage.setMaxHeight(590);
            stage.setMaxWidth(1000);
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
    public void exit() {
        viewModel.exit();
        Stage stage = (Stage) btn_newMaze.getScene().getWindow();
        stage.close();
    }

    @Override
    public void saveGame() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Maze");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File maze = fileChooser.showSaveDialog(btn_newMaze.getScene().getWindow());
        if (maze != null)
            viewModel.save(maze.toString());
    }

    @Override
    public void loadGame() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Maze File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File maze = fileChooser.showOpenDialog( btn_newMaze.getScene().getWindow());
        if(null != maze){
            lbl_statusText.setText("Loading maze...");
            viewModel.load(maze.toString());
        }
        lbl_statusText.setText("Solving maze...");
        viewModel.generateSolution();
    }
}
