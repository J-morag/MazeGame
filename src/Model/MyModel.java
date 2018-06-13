package Model;

import Client.*;
import IO.MyDecompressorInputStream;
import Server.*;
import algorithms.mazeGenerators.Maze;
import algorithms.search.AState;
import algorithms.search.Solution;
import javafx.scene.input.KeyCode;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Observable;

public class MyModel  extends Observable implements IModel {

    Server generateServer;
    Server solveServer;
    ServerStrategyGenerateMaze strategyGenerateServer;
    ServerStrategySolveSearchProblem strategySolveServer;
    private Maze maze;
    private int characterPositionRow;
    private int characterPositionColumn;
    private Solution mazeSolution;

    public MyModel() {
        maze = new Maze();
        characterPositionRow = maze.getStartPosition().getRowIndex();
        characterPositionColumn = maze.getStartPosition().getColumnIndex();
        mazeSolution = new Solution();

        strategyGenerateServer = new ServerStrategyGenerateMaze();
        generateServer = new Server(5400, 7000,strategyGenerateServer);
        strategySolveServer = new ServerStrategySolveSearchProblem();
        solveServer = new Server(5401, 7000,strategySolveServer);

        generateServer.start();
        solveServer.start();
    }

    @Override
    public void generateMaze(int rows, int columns) {
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
        setChanged();
        notifyObservers();
    }

    @Override
    public int[][] getMaze() {
        return maze.getMazeMap();
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
        switch (movement){
            case UP:
                characterPositionRow--;
                break;
            case DOWN:
                characterPositionRow++;
                break;
            case RIGHT:
                characterPositionColumn++;
                break;
            case LEFT:
                characterPositionColumn--;
                break;
        }
        setChanged();
        notifyObservers();
    }

    @Override
    public void solve() {
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
                        characterPositionRow = maze.getGoalPosition().getRowIndex();
                        characterPositionColumn = maze.getGoalPosition().getColumnIndex();
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
    public Solution getSolution() {
        return mazeSolution;
    }



    @Override
    public void solutionOn2DArr() {

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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
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
    public void exit() {

    }

    @Override
    public void storeConfigurations() {
        Server.Configurations.store("resources/config.properties");
    }
}
