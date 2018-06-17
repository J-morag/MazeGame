package View;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
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
    private boolean solutionVisible = false;
    private boolean isVictory = false;
    private boolean isAnimating = false;
    private double zoomMultiplier = 1;
    private double shiftX = 0;
    private double shiftY = 0;

    public void setMaze(int[][] maze) {
        this.maze = maze;
        isVictory = false;
        redraw();
    }

    public void setSolution(int[][] solution){
        this.solution = solution;
    }

    public void setZoomMultiplier(double zoomMultiplier){
        if(!isVictory){
            this.zoomMultiplier = zoomMultiplier;
            redraw();
        }
    }

    public double getZoomMultiplier() {
        return zoomMultiplier;
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
//            characterPositionColumn = 0;
//            characterPositionRow = 0;
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
        isVictory = true;
    }

    public void animationCharacterHurt(){
        if(!isAnimating){
            isAnimating = true;
            Task<Void> animation = new Task<Void>() {
                @Override
                protected Void call(){
                    for (int i = 0; i <8 ; i++) {
                        Platform.runLater(() -> {
                            StringProperty tmp = ImageFileNameCharacter;
                            ImageFileNameCharacter = imageFileNameCharacterHurt;
                            redraw();
                            ImageFileNameCharacter = tmp;
                        });
                        try {
                            Thread.sleep(60);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        redraw();
                        try {
                            Thread.sleep(60);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    isAnimating = false;
                    return null;
                }
            };
            new Thread(animation).start();
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
        shiftX = 0;
        shiftY = 0;
        if (maze != null && !isVictory) {
            double canvasHeight = Math.min(getHeight(), getWidth()) * zoomMultiplier;
            double canvasWidth = Math.min(getHeight(), getWidth()) * zoomMultiplier;
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

                calcShift(cellHeight, cellWidth);

                if (!(zoomMultiplier< 1.05 && zoomMultiplier> 0.95)){ //if in zoom, draw walls underneath everything, to avoid having a big empty space where the maze isn't drawn
                    for (int i = 0; i < getHeight()/cellHeight+1; i++) {
                        for (int j = 0; j < getWidth()/cellWidth+1; j++) {
                            gc.drawImage(wallImage, i * cellHeight + shiftX%1, j * cellWidth + shiftY%1, cellHeight, cellWidth);
                        }
                    }
                }

                //Draw Maze and solution
                for (int i = 0; i < maze[0].length; i++) {
                    for (int j = 0; j < maze.length; j++) {
                        double startX = i * cellHeight + shiftX;
                        double startY = j * cellWidth + shiftY;
                        if(solutionVisible && 1 == solution[j][i] && !(maze[j][i] == 2)){//draw solution
                            gc.drawImage(floorImage, startX , startY, cellHeight, cellWidth);
                            gc.drawImage(solutionImage, startX ,startY, cellHeight, cellWidth);
                        }
                        else if (maze[j][i] == 1) {
                            //gc.fillRect(i * cellHeight, j * cellWidth, cellHeight, cellWidth);
                            gc.drawImage(wallImage, startX , startY, cellHeight, cellWidth);
                        }
                        else if (maze[j][i] == 2){
                            gc.drawImage(floorImage, startX , startY, cellHeight, cellWidth);
                            gc.drawImage(goalImage, startX , startY, cellHeight, cellWidth);
                        }
                        else gc.drawImage(floorImage, startX , startY, cellHeight, cellWidth);
                    }
                }

                //Draw Character
                gc.drawImage(characterImage, characterPositionColumn * cellHeight +shiftX, characterPositionRow * cellWidth + shiftY, cellHeight, cellWidth);
            } catch (FileNotFoundException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void calcShift(double cellHeight, double cellWidth){
        if (zoomMultiplier< 1.05){
            shiftX = 0;
            shiftY = 0;
        }
        else if(zoomMultiplier>1.05 && zoomMultiplier<2.05) { //will be added to position of all maze elements
//                double centerPointX = getHeight()/2/*shift to middle*/ - cellHeight/*character height*/;
//                double centerPointY = getWidth()/2/*shift to middle*/ - cellWidth/*character width*/;
            shiftX = (-characterPositionColumn * cellHeight) * (zoomMultiplier - 1)/*center on character*/;
            shiftY = (-characterPositionRow * cellWidth) * (zoomMultiplier - 1)/*center on character*/;
        }
        else {
            shiftX = (-characterPositionColumn*cellHeight)/*center on character*/;
            shiftY = (-characterPositionRow*cellWidth)/*center on character*/;
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
