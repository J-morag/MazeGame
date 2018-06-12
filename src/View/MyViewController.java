package View;

import ViewModel.MyViewModel;
import javafx.fxml.FXML;

import java.util.Observable;

public class MyViewController implements IView{

    @FXML
    private MyViewModel viewModel;
    public MazeDisplayer mazeDisplayer;
    public javafx.scene.control.TextField txtfld_rowsNum;
    public javafx.scene.control.TextField txtfld_columnsNum;
    public javafx.scene.control.Label lbl_rowsNum;
    public javafx.scene.control.Label lbl_columnsNum;
    public javafx.scene.control.Button btn_generateMaze;

    public void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;
        bindProperties(viewModel);
    }

    private void bindProperties(MyViewModel viewModel) {
        lbl_rowsNum.textProperty().bind(viewModel.characterPositionRow);
        lbl_columnsNum.textProperty().bind(viewModel.characterPositionColumn);
    }

    @Override
    public void update(Observable o, Object arg) {

    }

    @Override
    public void newGame() {

    }

    @Override
    public void configurationChange(String prop, String value) {

    }

    @Override
    public void solve() {

    }

    @Override
    public void exit() {

    }

    @Override
    public void saveGame() {

    }

    @Override
    public void loadGame() {

    }
}
