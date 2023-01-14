package androidsamples.java.tictactoe;

import androidx.lifecycle.ViewModel;

public class GameViewModel extends ViewModel {
    private Game mGameInstance;

    public GameViewModel(){
        mGameInstance = new Game();
    }

    public Game getmGameInstance() {
        return mGameInstance;
    }

    public void setmGameInstance(Game mGameInstance) {
        this.mGameInstance = mGameInstance;
    }
}
