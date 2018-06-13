package View;

import java.util.Observer;

interface IView extends Observer{
    void newGame();
    void solve();
    void exit();
    void saveGame();
    void loadGame();
    void displayMaze(int[][] maze);





}
