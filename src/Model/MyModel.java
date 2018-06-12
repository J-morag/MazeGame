package Model;

import Client.Client;
import IO.MyDecompressorInputStream;
import Server.Server;
import Server.ServerStrategyGenerateMaze;
import Server.ServerStrategySolveSearchProblem;
import algorithms.mazeGenerators.Maze;
import javafx.scene.input.KeyCode;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Observable;

public class MyModel  extends Observable implements IModel {

    Server generateServer;
    Server solveServer;
    ServerStrategyGenerateMaze strategyGenerateServer;
    Server.ServerStrategySolveSearchProblem strategySolveServer;
    private Maze maze;
    private int charachterPositionRow;
    private int charachterPositionColumn;

    public MyModel() {
        maze = new Maze();
        charachterPositionRow = 0;
        charachterPositionColumn = 0;

        strategyGenerateServer = new ServerStrategyGenerateMaze();
        generateServer = new Server(5400, 7000,strategyGenerateServer);
        strategySolveServer = new ServerStrategySolveSearchProblem();
        generateServer = new Server(5401, 7000,strategySolveServer);

        generateServer.start();
        solveServer.start();
    }

    @Override
    public void generateMaze(int rows, int columns) {
        try {
            Client client = new Client(InetAddress.getLocalHost(), 5400, new Client.IClientStrategy() {
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
                        byte[] decompressedMaze = new byte[1000];
                        is.read(decompressedMaze);
                        maze = new Maze(decompressedMaze);
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
        return charachterPositionRow;
    }

    @Override
    public int getCharacterPositionColumn() {
        return charachterPositionColumn;
    }

    @Override
    public void moveCharacter(KeyCode movement) {

    }

    @Override
    public void solve(Maze maze) {

    }

    @Override
    public void changeGenerateMazeSettings() {

    }

    @Override
    public void changeSolveMazeSettings() {

    }
}
