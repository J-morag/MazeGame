package View;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MazeDisplayer extends Canvas {

    private int[][] maze;
    private int[][] solution;
    private int characterPositionRow = 1;
    private int characterPositionColumn = 1;
    private int enemyPositionRow;
    private int enemyPositionColumn;
    private boolean solutionVisible = false;
    private boolean isGameOver = false;
    private boolean isAnimating = false;
    private double zoomMultiplier = 1;
    private double shiftX = 0;
    private double shiftY = 0;
    private double characterMinX;
    private double characterMinY;
    private double cellWidth;
    private double cellHeight;
    private Image wallImage;
    private Image floorImage;
    private Image enemyImage;
    private Image characterImage;
    private Image characterHurtImage;
    private Image solutionImage;
    private Image goalImage;
    private int themeID = 1;
    private boolean isHardMode;


    public MazeDisplayer() {
        loadImages();
    }

    public void setMaze(int[][] maze, int themeID) {
        this.themeID = themeID;
        this.maze = maze;
        isGameOver = false;
        redrawAll();
    }

    public void setTheme(int themeID){
        this.themeID = themeID;
        loadImages();
    }
    
    public void setHardMode(boolean on){
        this.isHardMode = on;
        redrawAll();
    }

    private void loadImages(){
        ImageFileNameWall = new SimpleStringProperty("resources/theme"+themeID+"/Images/wall1.jpg");
        ImageFileNameCharacter = new SimpleStringProperty("resources/theme"+themeID+"/Images/character1.png");
        ImageFileNameEnemy = new SimpleStringProperty("resources/theme"+themeID+"/Images/enemy1.png");
        imageFileNameCharacterHurt = new SimpleStringProperty("resources/theme"+themeID+"/Images/CharacterHurt1.png");
        imageFileNameFloor = new SimpleStringProperty("resources/theme"+themeID+"/Images/floor1.jpg");
        imageFileNameSolution = new SimpleStringProperty("resources/theme"+themeID+"/Images/path1.png");
        imageFileNameGoal = new SimpleStringProperty("resources/theme"+themeID+"/Images/goal1.png");
        ImageFileNameVictory = new SimpleStringProperty("resources/theme"+themeID+"/Images/victory1.jpg");
        ImageFileNameLoss = new SimpleStringProperty("resources/theme"+themeID+"/Images/loss1.png");
        try {
            wallImage = new Image(new FileInputStream(ImageFileNameWall.get()));
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        try {
            floorImage = new Image(new FileInputStream(imageFileNameFloor.get()));
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        try {
            characterImage = new Image(new FileInputStream(ImageFileNameCharacter.get()));
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        try {
            enemyImage = new Image(new FileInputStream(ImageFileNameEnemy.get()));
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        try {
            characterHurtImage = new Image(new FileInputStream(imageFileNameCharacterHurt.get()));
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        try {
            solutionImage = new Image(new FileInputStream(imageFileNameSolution.get()));
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        try {
            goalImage = new Image(new FileInputStream(imageFileNameGoal.get()));
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }



    public void setSolution(int[][] solution){
        this.solution = solution;
    }

    public void setZoomMultiplier(double zoomMultiplier){
        if(!isGameOver){
            this.zoomMultiplier = zoomMultiplier;
            redrawAll();
        }
    }

    public double getZoomMultiplier() {
        return zoomMultiplier;
    }

    public double getCharacterMinX(){
        return characterMinX;
    }

    public double getCharacterMinY(){
        return characterMinY;
    }

    public double getCellWidth() {
        return cellWidth;
    }

    public double getCellHeight() {
        return cellHeight;
    }

    public void hideSolution(){
        this.solutionVisible = false;
        redrawAll();
    }

    public void showSolution(){
        this.solutionVisible = true;
        redrawAll();
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
        isGameOver = true;
    }

    public void setLoss() {
        if(null != maze){
            double canvasHeight = Math.min(getHeight(), getWidth());
            double canvasWidth = Math.min(getHeight(), getWidth());
            try {
                Image lossImage = new Image(new FileInputStream(ImageFileNameLoss.get()));
                GraphicsContext gc = getGraphicsContext2D();
                gc.clearRect(0, 0, getWidth(), getHeight());
                gc.drawImage(lossImage, 0, 0, canvasWidth, canvasHeight);
            } catch (FileNotFoundException e) {
                //e.printStackTrace();
            }
        }
        isGameOver = true;
    }

    public void setCharacterPosition(int row, int column) {
        int prevCharacterPositionRow = characterPositionRow;
        int prevCharacterPositionColumn = characterPositionColumn;
        characterPositionRow = row;
        characterPositionColumn = column;
        if (zoomMultiplier< 1.05)
            redrawCharacter(prevCharacterPositionRow, prevCharacterPositionColumn, characterImage);
        else redrawAll();
    }

    public void setEnemyPosition(int row, int column) {
        if(isHardMode){
            int prevEnemyPositionRow = enemyPositionRow;
            int prevEnemyPositionColumn = enemyPositionColumn;
            if(enemyPositionRow == characterPositionRow && enemyPositionColumn == characterPositionColumn){
                prevEnemyPositionRow = row;
                prevEnemyPositionColumn = column;
            }
            enemyPositionRow = row;
            enemyPositionColumn = column;
            if (zoomMultiplier< 1.05)
                redrawCharacter(prevEnemyPositionRow, prevEnemyPositionColumn, enemyImage);
            else redrawAll();
        }
    }


    public void animationCharacterHurt(){
        if(!isAnimating){
            isAnimating = true;
            Task<Void> animation = new Task<Void>() {
                @Override
                protected Void call(){
                    Image tmp = characterImage;
                    for (int i = 0; i <8 ; i++) {

                        Platform.runLater(() -> {
                            characterImage = characterHurtImage;
                            redrawCharacter(characterPositionRow, characterPositionColumn, characterImage);
                        });
                        try {
                            Thread.sleep(60);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        characterImage = tmp;
                        redrawCharacter(characterPositionRow, characterPositionColumn, characterImage);
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


    public int getCharacterPositionRow() {
        return characterPositionRow;
    }

    public int getCharacterPositionColumn() {
        return characterPositionColumn;
    }

    public void redrawAll() {
        if (maze != null && !isGameOver) {
            double canvasHeight = Math.min(getHeight(), getWidth()) * zoomMultiplier;
            double canvasWidth = Math.min(getHeight(), getWidth()) * zoomMultiplier;
            this.cellHeight = canvasHeight / maze[0].length;
            this.cellWidth = canvasWidth / maze.length;
//            this.cellHeight = Math.min(canvasHeight / maze[0].length,canvasWidth / maze.length);
//            this.cellWidth = Math.min(canvasHeight / maze[0].length,canvasWidth / maze.length);

            GraphicsContext gc = getGraphicsContext2D();
            gc.clearRect(0, 0, getWidth(), getHeight());

            calcShift();

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
            characterMinX = characterPositionColumn * cellHeight +shiftX;
            characterMinY = characterPositionRow * cellWidth + shiftY;
            gc.drawImage(characterImage, characterMinX, characterMinY, cellHeight, cellWidth);

            //Draw Enemy
            if(isHardMode){
                double enemyMinX = enemyPositionColumn * cellHeight +shiftX;
                double enemyMinY = enemyPositionRow * cellWidth + shiftY;
                gc.drawImage(enemyImage, enemyMinX, enemyMinY, cellHeight, cellWidth);
            }
        }
    }

    private void redrawCharacter(int prevCharacterOrEnemyPositionRow, int prevCharacterOrEnemyPositionColumn, Image characterOrEnemyImage) {
        if (maze != null && !isGameOver) {

            GraphicsContext gc = getGraphicsContext2D();
            calcShift();
            double startX = prevCharacterOrEnemyPositionColumn * cellHeight + shiftX;
            double startY = prevCharacterOrEnemyPositionRow * cellWidth + shiftY;

            //Reform abandoned cell
            if (maze[prevCharacterOrEnemyPositionRow][prevCharacterOrEnemyPositionColumn] == 1) {
                gc.drawImage(wallImage, startX , startY, cellHeight, cellWidth);
            }
            else
                gc.drawImage(floorImage, startX ,startY, cellHeight, cellWidth);
            if (solutionVisible && solution[prevCharacterOrEnemyPositionRow][prevCharacterOrEnemyPositionColumn] == 1)
                gc.drawImage(solutionImage, startX ,startY, cellHeight, cellWidth);
            if (maze[prevCharacterOrEnemyPositionRow][prevCharacterOrEnemyPositionColumn] == 2)
                gc.drawImage(goalImage, startX, startY, cellHeight, cellWidth);

            if(characterOrEnemyImage == this.characterImage){
                //Draw Character and background for character
                characterMinX = characterPositionColumn * cellHeight +shiftX;
                characterMinY = characterPositionRow * cellWidth + shiftY;
                gc.drawImage(floorImage, characterMinX, characterMinY, cellHeight, cellWidth);
                if (solutionVisible && solution[characterPositionRow][characterPositionColumn] == 1)
                    gc.drawImage(solutionImage, characterMinX ,characterMinY, cellHeight, cellWidth);

                gc.drawImage(characterOrEnemyImage, characterMinX, characterMinY, cellHeight, cellWidth);
            }
            else{
                //Draw enemy and background for enemy
                double minX = enemyPositionColumn * cellHeight +shiftX;
                double minY = enemyPositionRow * cellWidth + shiftY;
                gc.drawImage(floorImage, minX, minY, cellHeight, cellWidth);
                if (solutionVisible && solution[enemyPositionRow][enemyPositionColumn] == 1)
                    gc.drawImage(solutionImage, minX, minY, cellHeight, cellWidth);
                if (maze[enemyPositionRow][enemyPositionColumn] == 2)
                    gc.drawImage(goalImage, minX, minY, cellHeight, cellWidth);

                gc.drawImage(characterOrEnemyImage, minX, minY, cellHeight, cellWidth);
            }
        }
    }

    private void calcShift(){
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
    private StringProperty ImageFileNameWall = new SimpleStringProperty("resources/theme"+themeID+"/Images/wall1.jpg");
    private StringProperty ImageFileNameCharacter = new SimpleStringProperty("resources/theme"+themeID+"/Images/character1.png");
    private StringProperty ImageFileNameEnemy = new SimpleStringProperty("resources/theme"+themeID+"/Images/enemy1.png");
    private StringProperty imageFileNameCharacterHurt = new SimpleStringProperty("resources/theme"+themeID+"/Images/CharacterHurt1.png");
    private StringProperty imageFileNameFloor = new SimpleStringProperty("resources/theme"+themeID+"/Images/floor1.jpg");
    private StringProperty imageFileNameSolution = new SimpleStringProperty("resources/theme"+themeID+"/Images/path1.png");
    private StringProperty imageFileNameGoal = new SimpleStringProperty("resources/theme"+themeID+"/Images/goal1.png");
    private StringProperty ImageFileNameVictory = new SimpleStringProperty("resources/theme"+themeID+"/Images/victory1.jpg");
    private StringProperty ImageFileNameLoss = new SimpleStringProperty("resources/theme"+themeID+"/Images/loss1.png");

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



}
