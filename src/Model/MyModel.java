package Model;

import Client.*;
import IO.MyDecompressorInputStream;
import Server.*;
import ViewModel.MyViewModel;
import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.Position;
import algorithms.search.AState;
import algorithms.search.MazeState;
import algorithms.search.Solution;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyModel  extends Observable implements IModel {

    Server generateServer;
    Server solveServer;
    ServerStrategyGenerateMaze strategyGenerateServer;
    ServerStrategySolveSearchProblem strategySolveServer;
    private Maze maze;
    private int characterPositionRow;
    private int characterPositionColumn;
    private Solution mazeSolution;
    private Enemy enemy;
    private boolean isHardMode;

    public MyModel() {
        Server.Configurations.load("resources/config.properties");
        strategyGenerateServer = new ServerStrategyGenerateMaze();
        generateServer = new Server(5400, 7000,strategyGenerateServer);
        strategySolveServer = new ServerStrategySolveSearchProblem();
        solveServer = new Server(5401, 7000,strategySolveServer);

        generateServer.start();
        solveServer.start();
    }

    public void setHardMode(boolean on){
        isHardMode = on;
    }

    @Override
    public void generateMaze(int rows, int columns) {
        if(rows < 5 || columns < 5) {
            setChanged();
            notifyObservers("Maze dimensions must be at least 5X5");
            return;
        }
        CommunicateWithServer_MazeGenerating(rows, columns);
        setChanged();

        enemy = new Enemy(maze);

        Platform.runLater(() -> notifyObservers(MyViewModel.EventType.MAZE));
    }

    private void CommunicateWithServer_MazeGenerating(int rows, int columns) {
        try {
            Client client = new Client(InetAddress.getLocalHost(), 5400, new IClientStrategy() {
                public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                    try {
                        ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                        ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                        toServer.flush();
                        int[] mazeDimensions = new int[]{rows, columns};
                        toServer.writeObject(mazeDimensions);
                        toServer.flush();
                        byte[] compressedMaze = (byte[])fromServer.readObject();
                        InputStream is = new MyDecompressorInputStream(new ByteArrayInputStream(compressedMaze));
                        byte[] decompressedMaze = new byte[rows*columns + 24];
                        is.read(decompressedMaze);
                        maze = new Maze(decompressedMaze);
                        characterPositionRow = maze.getStartPosition().getRowIndex();
                        characterPositionColumn = maze.getStartPosition().getColumnIndex();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            client.communicateWithServer();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int[][] getMaze() {
        //create a copy of the maze map
        int[][] mazeMap = new int[maze.getMazeMap().length][maze.getMazeMap()[0].length];
        for (int i = 0; i <maze.getMazeMap().length ; i++) {  //TODO put the deep copy in Maze. fix getAllPossibleStates accordingly
            mazeMap[i] = maze.getMazeMap()[i].clone();
        }
        //mark the goal position
        mazeMap[maze.getGoalPosition().getRowIndex()][maze.getGoalPosition().getColumnIndex()] = 2;
        return mazeMap;
    }

    @Override
    public int getCharacterPositionRow() {
        return characterPositionRow;
    }

    @Override
    public int getCharacterPositionColumn() {
        return characterPositionColumn;
    }

    @Override
    public int getEnemyPositionRow() {
        return enemy.row;
    }

    @Override
    public int getEnemyPositionColumn() {
        return enemy.column;
    }

    @Override
    public void moveCharacter(KeyCode movement) {
        if (characterPositionRow == maze.getGoalPosition().getRowIndex() && characterPositionColumn == maze.getGoalPosition().getColumnIndex()){
            return;
        }
        switch (movement){
            case UP:
            case  DIGIT8:
            case NUMPAD8:
                if (!(isPass(characterPositionRow-1,characterPositionColumn))) {
                    setChanged();
                    notifyObservers(MyViewModel.EventType.INVALIDMOVEMENT);
                    return;
                }
                else
                    characterPositionRow--;

                break;
            case DOWN:
            case DIGIT2:
            case NUMPAD2:
                if (!(isPass(characterPositionRow+1,characterPositionColumn))) {
                    setChanged();
                    notifyObservers(MyViewModel.EventType.INVALIDMOVEMENT);
                    return;
                }
                else
                    characterPositionRow++;
                break;
            case RIGHT:
            case DIGIT6:
            case NUMPAD6:
                if (!(isPass(characterPositionRow,characterPositionColumn+1))) {
                    setChanged();
                    notifyObservers(MyViewModel.EventType.INVALIDMOVEMENT);
                    return;
                }
                else
                    characterPositionColumn++;
                break;
            case LEFT:
            case DIGIT4:
            case NUMPAD4:
                if (!(isPass(characterPositionRow,characterPositionColumn-1))) {
                    setChanged();
                    notifyObservers(MyViewModel.EventType.INVALIDMOVEMENT);
                    return;
                }
                else
                    characterPositionColumn--;
                break;
            case  DIGIT1:
            case NUMPAD1:
                if (!(isPass(characterPositionRow+1,characterPositionColumn-1))) {
                    setChanged();
                    notifyObservers(MyViewModel.EventType.INVALIDMOVEMENT);
                    return;
                }
                else{
                    characterPositionRow++;
                    characterPositionColumn--;
                }
                break;
            case  DIGIT3:
            case NUMPAD3:
                if (!(isPass(characterPositionRow+1,characterPositionColumn+1))) {
                    setChanged();
                    notifyObservers(MyViewModel.EventType.INVALIDMOVEMENT);
                    return;
                }
                else{
                    characterPositionRow++;
                    characterPositionColumn++;
                }
                break;
            case  DIGIT7:
            case NUMPAD7:
                if (!(isPass(characterPositionRow-1,characterPositionColumn-1))) {
                    setChanged();
                    notifyObservers(MyViewModel.EventType.INVALIDMOVEMENT);
                    return;
                }
                else{
                    characterPositionRow--;
                    characterPositionColumn--;
                }
                break;
            case  DIGIT9:
            case NUMPAD9:
                if (!(isPass(characterPositionRow-1,characterPositionColumn+1))) {
                    setChanged();
                    notifyObservers(MyViewModel.EventType.INVALIDMOVEMENT);
                    return;
                }
                else{
                    characterPositionRow--;
                    characterPositionColumn++;
                }
                break;


//            case HOME:
//                characterPositionRow = maze.getStartPosition().getRowIndex();
//                characterPositionColumn = maze.getStartPosition().getColumnIndex();
        }
        if (characterPositionRow == maze.getGoalPosition().getRowIndex() && characterPositionColumn == maze.getGoalPosition().getColumnIndex()){
            setChanged();
            notifyObservers(MyViewModel.EventType.MOVEMENT);
            setChanged();
            notifyObservers(MyViewModel.EventType.VICTORY);
        }
        else if (isHardMode && enemy.touchingPlayer(characterPositionRow, characterPositionColumn)){
            setChanged();
            notifyObservers(MyViewModel.EventType.LOSS);
        }
        if (isHardMode)
            enemy.move(characterPositionRow, characterPositionColumn);
        if (isHardMode && enemy.touchingPlayer(characterPositionRow, characterPositionColumn)){
            setChanged();
            notifyObservers(MyViewModel.EventType.LOSS);
        }
        else{
            setChanged();
            notifyObservers(MyViewModel.EventType.MOVEMENT);
        }
    }

    private boolean isPass (int row, int column){
        //check if position is out of bounds of the maze
        if(row < 0 || column < 0 || row >= maze.getMazeMap().length || column >= (maze.getMazeMap())[0].length) {
            return false;
        }
        //check if position is wall
        if(maze.getMazeMap()[row][column] == 1){
            return false;
        }
        return true;
    }

    private void CommunicateWithServer_SolveSearchProblem() {
        try {
            Client client = new Client(InetAddress.getLocalHost(), 5401, new IClientStrategy() {
                public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                    try {
                        ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                        ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                        toServer.flush();
                        toServer.writeObject(maze);
                        toServer.flush();
                        mazeSolution = (Solution)fromServer.readObject();
                        if(mazeSolution == null || mazeSolution.toString() == ""){
                            setChanged();
                            notifyObservers("Solution does not exist");
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            client.communicateWithServer();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void solve() {
        CommunicateWithServer_SolveSearchProblem();
        setChanged();
        Platform.runLater(() -> notifyObservers(MyViewModel.EventType.SOLUTION));
    }




    /**
     * Creates a new map (with sizes of the maze), and put '1' in the position of each step in the solution.
     */
    @Override
    public int[][] solutionOnMap() {
        ArrayList<AState> mazeSolutionPath = mazeSolution.getSolutionPath();
        int[][] map = new int[maze.getMazeMap().length][(maze.getMazeMap())[0].length];
        for (AState step:mazeSolutionPath) {
            String[] stepStrArr = step.toString().split(",");
            int row = Integer.parseInt(stepStrArr[0].substring(1));
            int column = Integer.parseInt(stepStrArr[1].substring(0,stepStrArr[1].length()-1));
            map[row][column] = 1;
        }
        return map;
    }

    @Override
    public void save(String pathFile) {
        try {
            FileOutputStream outFile = new FileOutputStream(pathFile);
            ObjectOutputStream outFileObj = new ObjectOutputStream(outFile);
            outFileObj.writeObject(maze);
            outFile.flush();
            outFileObj.flush();
            outFile.close();
            outFileObj.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setChanged();
        notifyObservers(MyViewModel.EventType.MESSAGE);
    }

    @Override
    public void load(String pathFile) {
        try {
            FileInputStream inputFile = new FileInputStream(pathFile);
            ObjectInputStream inFileObj = new ObjectInputStream(inputFile);
            maze = (Maze) inFileObj.readObject();
            characterPositionRow = maze.getStartPosition().getRowIndex();
            characterPositionColumn = maze.getStartPosition().getColumnIndex();
            inputFile.close();
            inFileObj.close();
            setChanged();
            notifyObservers(MyViewModel.EventType.MAZE);
        } catch (IOException e) {
            e.printStackTrace();
            setChanged();
            notifyObservers("Error: cannot open file");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e){
            setChanged();
            notifyObservers("Error: corrupted maze file");
        }
    }

    @Override
    public void stopServers() {
        generateServer.stop();
        solveServer.stop();
    }

    @Override
    public void changeConfiguration(String prop, String value) {
        Server.Configurations.setProperty(prop, value);
    }


    @Override
    public void storeConfigurations() {
        Server.Configurations.store("resources/config.properties");
    }

    private class Enemy{
        int row;
        int column;
        int prevCharacterRow;
        int prevCharacterColumn;
        Maze reversedMaze;
        Solution pathToTake;
        public Enemy(Maze maze){
            //create a copy of the maze map
            int[][] mazeMap = new int[maze.getMazeMap().length][maze.getMazeMap()[0].length];
            for (int i = 0; i <maze.getMazeMap().length ; i++) {
                mazeMap[i] = maze.getMazeMap()[i].clone();
            }
            //create maze with same map, and start and end reversed
            reversedMaze = new Maze(mazeMap, new Position(maze.getGoalPosition()), new Position(maze.getStartPosition()));
            //initialize prev character positions to initial character position
            prevCharacterRow = reversedMaze.getGoalPosition().getRowIndex();
            prevCharacterColumn = reversedMaze.getGoalPosition().getColumnIndex();

            row = reversedMaze.getStartPosition().getRowIndex();
            column = reversedMaze.getStartPosition().getColumnIndex();
        }

        protected void move(int characterPositionRow, int characterPositionColumn){
            reversedMaze = new Maze(reversedMaze.getMazeMap(), new Position(row, column), new Position(prevCharacterRow, prevCharacterColumn));
            //get path to space before character
            CommunicateWithServer_SolveSearchProblem();

            if (pathToTake != null){
                ArrayList<AState> path = pathToTake.getSolutionPath();
                if(null != path.get(1))
                row = ((MazeState)path.get(1)).getPosition().getRowIndex();
                column = ((MazeState)path.get(1)).getPosition().getColumnIndex();
            }
            prevCharacterRow = characterPositionRow;
            prevCharacterColumn = characterPositionColumn;
        }

        protected boolean touchingPlayer(int characterPositionRow, int characterPositionColumn){
            return (this.row == characterPositionRow && this.column == characterPositionColumn);
        }

        private void CommunicateWithServer_SolveSearchProblem() {
            try {
                Client client = new Client(InetAddress.getLocalHost(), 5401, new IClientStrategy() {
                    public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                        try {
                            ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                            ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                            toServer.flush();
                            toServer.writeObject(reversedMaze);
                            toServer.flush();
                            pathToTake = (Solution)fromServer.readObject();
                            if(pathToTake == null || pathToTake.toString() == ""){
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                client.communicateWithServer();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }
}
