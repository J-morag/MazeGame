package View;

import java.util.Observer;

interface IView extends Observer{
    void newGame();
    void configurationChange(String prop, String value);
    void solve();
    void exit();
    void saveGame();
    void loadGame();




}
