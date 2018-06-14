package Model;

import Client.*;
import IO.MyDecompressorInputStream;
import Server.*;
import ViewModel.MyViewModel;
import algorithms.mazeGenerators.Maze;
import algorithms.search.AState;
import algorithms.search.Solution;
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
    private ExecutorService clientThreadPool = Executors.newCachedThreadPool();

    public MyModel() {
//        maze = new Maze();
//        characterPositionRow = maze.getStartPosition().getRowIndex();
//        characterPositionColumn = maze.getStartPosition().getColumnIndex();
//        mazeSolution = new Solution();

        Server.Configurations.load("resources/config.properties");
        strategyGenerateServer = new ServerStrategyGenerateMaze();
        generateServer = new Server(5400, 7000,strategyGenerateServer);
        strategySolveServer = new ServerStrategySolveSearchProblem();
        solveServer = new Server(5401, 7000,strategySolveServer);

        generateServer.start();
        solveServer.start();
    }

    @Override
    public void generateMaze(int rows, int columns) {
        if(rows < 5 || columns < 5) {
            setChanged();
            notifyObservers("Maze dimensions must be at least 5X5");
            return;
        }
//        clientThreadPool.execute(() -> {
//            try {
                CommunicateWithServer_MazeGenerating(rows, columns);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            setChanged();
            notifyObservers(MyViewModel.EventType.MAZE);
//        });
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
    public void moveCharacter(KeyCode movement) {
        if (characterPositionRow == maze.getGoalPosition().getRowIndex() && characterPositionColumn == maze.getGoalPosition().getColumnIndex()){
            return;
        }
        switch (movement){
            case  DIGIT8:
            case NUMPAD8:
                if (!(isPass(characterPositionRow-1,characterPositionColumn))) {
                    setChanged();
                    notifyObservers(MyViewModel.EventType.INVALIDMOVEMENT);
                    return;
                }
                else characterPositionRow--;
                break;
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

        }
        if (characterPositionRow == maze.getGoalPosition().getRowIndex() && characterPositionColumn == maze.getGoalPosition().getColumnIndex()){
            setChanged();
            notifyObservers(MyViewModel.EventType.MOVEMENT);
            setChanged();
            notifyObservers(MyViewModel.EventType.VICTORY);
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
        notifyObservers(MyViewModel.EventType.SOLUTION);
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
            int column = Integer.parseInt(stepStrArr[1].substring(0,1));
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
}
