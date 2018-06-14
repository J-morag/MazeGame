package View;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;

public class MazeDisplayer extends Canvas {

    private int[][] maze;
    private int[][] solution;
    private int characterPositionRow = 1;
    private int characterPositionColumn = 1;
    boolean solutionVisible = false;

    public void setMaze(int[][] maze) {
        this.maze = maze;
        redraw();
    }

    public void setSolution(int[][] solution){
        this.solution = solution;
    }


    public void hideSolution(){
        this.solutionVisible = false;
        redraw();
    }

    public void showSolution(){
        this.solutionVisible = true;
        redraw();
    }

    public void setVictory(){
        if(null != maze){
            double canvasHeight = Math.min(getHeight(), getWidth());
            double canvasWidth = Math.min(getHeight(), getWidth());
            try {
                Image victoryImage = new Image(new FileInputStream(ImageFileNameVictory.get()));
                GraphicsContext gc = getGraphicsContext2D();
                gc.clearRect(0, 0, getWidth(), getHeight());
                gc.drawImage(victoryImage, 0, 0, canvasWidth, canvasHeight);
            } catch (FileNotFoundException e) {
                //e.printStackTrace();
            }
        }
    }

    public void animationCharacterHurt(){
        try {
            StringProperty tmp = ImageFileNameCharacter;
            ImageFileNameCharacter = imageFileNameCharacterHurt;
            redraw();
            ImageFileNameCharacter = tmp;
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void setCharacterPosition(int row, int column) {
        characterPositionRow = row;
        characterPositionColumn = column;
        redraw();
    }

    public int getCharacterPositionRow() {
        return characterPositionRow;
    }

    public int getCharacterPositionColumn() {
        return characterPositionColumn;
    }

    public void redraw() {
        if (maze != null) {
            double canvasHeight = Math.min(getHeight(), getWidth());
            double canvasWidth = Math.min(getHeight(), getWidth());
            double cellHeight = canvasHeight / maze[0].length;
            double cellWidth = canvasWidth / maze.length;

            try {
                Image wallImage = new Image(new FileInputStream(ImageFileNameWall.get()));
                Image floorImage = new Image(new FileInputStream(imageFileNameFloor.get()));
                Image characterImage = new Image(new FileInputStream(ImageFileNameCharacter.get()));
                Image solutionImage = new Image(new FileInputStream(imageFileNameSolution.get()));
                Image goalImage = new Image(new FileInputStream(imageFileNameGoal.get()));

                GraphicsContext gc = getGraphicsContext2D();
                gc.clearRect(0, 0, getWidth(), getHeight());

                //Draw Maze
                for (int i = 0; i < maze[0].length; i++) {
                    for (int j = 0; j < maze.length; j++) {
                        if(solutionVisible && 1 == solution[j][i] && !(maze[j][i] == 2)){//draw solution
                            gc.drawImage(floorImage, i * cellHeight, j * cellWidth, cellHeight, cellWidth);
                            gc.drawImage(solutionImage, i * cellHeight, j * cellWidth, cellHeight, cellWidth);
                        }
                        else if (maze[j][i] == 1) {
                            //gc.fillRect(i * cellHeight, j * cellWidth, cellHeight, cellWidth);
                            gc.drawImage(wallImage, i * cellHeight, j * cellWidth, cellHeight, cellWidth);
                        }
                        else if (maze[j][i] == 2){
                            gc.drawImage(floorImage, i * cellHeight, j * cellWidth, cellHeight, cellWidth);
                            gc.drawImage(goalImage, i * cellHeight, j * cellWidth, cellHeight, cellWidth);
                        }
                        else gc.drawImage(floorImage, i * cellHeight, j * cellWidth, cellHeight, cellWidth);
                    }
                }

                //Draw Character
                gc.drawImage(characterImage, characterPositionColumn * cellHeight, characterPositionRow * cellWidth, cellHeight, cellWidth);
            } catch (FileNotFoundException e) {
                //e.printStackTrace();
            }
        }
    }

    // Properties
    private StringProperty ImageFileNameWall = new SimpleStringProperty("resources/Images/wall1.jpg");
    private StringProperty ImageFileNameCharacter = new SimpleStringProperty("resources/Images/character1.png");
    private StringProperty imageFileNameCharacterHurt = new SimpleStringProperty("resources/Images/CharacterHurt1.png");
    private StringProperty imageFileNameFloor = new SimpleStringProperty("resources/Images/floor1.jpg");
    private StringProperty imageFileNameSolution = new SimpleStringProperty("resources/Images/path1.png");
    private StringProperty imageFileNameGoal = new SimpleStringProperty("resources/Images/goal1.png");
    private StringProperty ImageFileNameVictory = new SimpleStringProperty("resources/Images/victory1.jpg");

    public String getImageFileNameWall() {
        return ImageFileNameWall.get();
    }

    public void setImageFileNameWall(String imageFileNameWall) {
        this.ImageFileNameWall.set(imageFileNameWall);
    }

    public String getImageFileNameCharacter() {
        return ImageFileNameCharacter.get();
    }

    public void setImageFileNameCharacter(String imageFileNameCharacter) {
        this.ImageFileNameCharacter.set(imageFileNameCharacter);
    }
    //endregion

}
