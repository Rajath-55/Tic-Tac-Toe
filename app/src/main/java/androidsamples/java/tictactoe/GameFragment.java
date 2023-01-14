package androidsamples.java.tictactoe;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * The main GameFragment class that handles all the game logic.
 */
public class GameFragment extends Fragment {
  private static final String TAG = "GameFragment";
  private static final int GRID_SIZE = 9;
  private int dialogShown = 0;

  private final Button[] mButtons = new Button[GRID_SIZE];
  private NavController mNavController;

  //all variables needed
  private boolean isInProgress = false;
  private boolean isCurrentPlayerTurn;
  private String[] values = new String[GRID_SIZE];
  private HashSet<Integer> availablePositions;
  private String gameType;
  private boolean isHost = true;
  private DatabaseReference mGameReference, mPlayerReference;
  private String gameID;
  private String myPlayerChar = "X", opponentPlayerChar = "O";
  private FirebaseUser mCurrentUser;
  private TextView playerOne, playerTwo, display;
  GameViewModel mGameViewModel;


  /**
   * The overriden onCreate method. Sets all the references and variables needed
   * and the onpressedcallback overrider for pressing the back button .
   * @param savedInstanceState
   */
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true); // Needed to display the action menu for this fragment

    // Extract the argument passed with the action in a type-safe way
    GameFragmentArgs args = GameFragmentArgs.fromBundle(getArguments());
    availablePositions = new HashSet<>();
    isInProgress = true;
    isCurrentPlayerTurn = true;
    mPlayerReference = FirebaseDatabase.getInstance().getReference("players");
    mGameReference = FirebaseDatabase.getInstance().getReference("games");
    Log.d(TAG, "New game type = " + args.getGameType());
    gameType = args.getGameType();
    gameID = args.getGameId();
    mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
    mGameViewModel = new ViewModelProvider(this).get(GameViewModel.class);

    for(int i = 0; i < GRID_SIZE; ++i){
      values[i] = "";
      availablePositions.add(i);
    }
    if (!gameType.equals("One-Player")) {
      setParams();

    }


    // Handle the back press by adding a confirmation dialog, and updating losses for
    // forfeiture.
    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        Log.d(TAG, "Back pressed");

        if (isInProgress) {
          AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                  .setTitle(R.string.confirm)
                  .setMessage(R.string.forfeit_game_dialog_message)
                  .setPositiveButton(R.string.yes, (d, which) -> {
                    updateLosses();
                    showDialog("Oh no, you lost!");
                    mNavController.popBackStack();
                  })
                  .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
                  .create();
          dialog.show();
        }else{
          mNavController.popBackStack();
        }
      }
    };
    requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
  }

  /**
   * The function updates losses when the user forfeits a gae. It also sets the game to be complete and
   * sets the id of the player who lost.
   */
  private void updateLosses() {
    mPlayerReference.child(mCurrentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
      @Override
      public void onComplete(@NonNull Task<DataSnapshot> task) {
        if(task.isSuccessful()){
          Player player = task.getResult().getValue(Player.class);
          assert player != null;
          player.setNumberOfLosses(player.getNumberOfLosses() + 1);
          Log.d(TAG, "Email : " + player.getEmail());
          mPlayerReference.child(mCurrentUser.getUid()).setValue(player);
          mGameReference.child("complete").setValue(true);
          mGameReference.child("lostID").setValue(mCurrentUser.getUid());
        }else{
          Log.d(TAG, "Database error!");
        }
      }
    });

  }

  /**
   * It sets initial parameters for a game that a user joins from the recycler view. Sets the user's character
   * and other user's character as well. Then it updates the UI, setting the appropriate labels for player and if its the
   * if current player's turn or if the player has to wait.
   */
  private  void setParams() {
    mGameReference = FirebaseDatabase.getInstance().getReference("games").child(gameID);
    mGameReference.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        Log.d(TAG, "Player entered current game!");
        mGameViewModel.setmGameInstance(snapshot.getValue(Game.class));
        assert mGameViewModel.getmGameInstance() != null;
        values = (mGameViewModel.getmGameInstance().getGameValues()).toArray(new String[9]);
        for(int i = 0; i < GRID_SIZE; ++i){
          if(!values[i].isEmpty())
            availablePositions.remove(i);
        }
        if (mGameViewModel.getmGameInstance().getTurn() == 1) {
          if (mGameViewModel.getmGameInstance().getHost().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
            isHost = true;
            isCurrentPlayerTurn = true;
            myPlayerChar = "X";
            opponentPlayerChar = "O";
            playerOne.setText(R.string.you);
            playerTwo.setText(R.string.opponent);
          } else {
            isHost = false;
            isCurrentPlayerTurn = false;
            myPlayerChar = "O";
            opponentPlayerChar = "X";
            playerTwo.setText(R.string.you);
            playerOne.setText(R.string.opponent);
            display.setText(R.string.waiting);
          }
        } else {
          if (!mGameViewModel.getmGameInstance().getHost().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
            isCurrentPlayerTurn = true;
            myPlayerChar = "X";
            opponentPlayerChar = "O";
            isHost = false;
            playerTwo.setText(R.string.you);
            playerOne.setText(R.string.opponent);
          } else {
            isHost = true;
            isCurrentPlayerTurn = false;
            myPlayerChar = "O";
            opponentPlayerChar = "X";
            playerOne.setText(R.string.you);
            playerTwo.setText(R.string.opponent);
            display.setText(R.string.waiting);
          }
        }
        if(gameType.equals("No Type")) {
          mGameReference.child("open").setValue(false);
          mGameReference.child("sendNotification").setValue("SEND_NOTIFICATION");
        }
        updateUI();
      }
      @Override
      public void onCancelled(@NonNull DatabaseError error) {

      }
    });
  }




  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_game, container, false);
  }

  /**
   * Overrides the onViewCreated method. Sets the onclick listeners for the buttons and also handles calling
   * the logic for the oneplaeer and two player moves.
   * @param view
   * @param savedInstanceState
   */
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mNavController = Navigation.findNavController(view);

    mButtons[0] = view.findViewById(R.id.button0);
    mButtons[1] = view.findViewById(R.id.button1);
    mButtons[2] = view.findViewById(R.id.button2);

    mButtons[3] = view.findViewById(R.id.button3);
    mButtons[4] = view.findViewById(R.id.button4);
    mButtons[5] = view.findViewById(R.id.button5);

    mButtons[6] = view.findViewById(R.id.button6);
    mButtons[7] = view.findViewById(R.id.button7);
    mButtons[8] = view.findViewById(R.id.button8);

    playerOne = view.findViewById(R.id.player_one);
    playerTwo = view.findViewById(R.id.player_two);
    display = view.findViewById(R.id.display);


    if(gameType.equals("No Type")){
      twoPlayerGameLogic();
    }

    for (int i = 0; i < mButtons.length; i++) {
      int finalI = i;
      mButtons[i].setOnClickListener(v -> {
        Log.d(TAG, "Button " + finalI + " clicked");
        if(mButtons[finalI].isClickable() && mButtons[finalI].getText().toString().isEmpty()){
          if(isCurrentPlayerTurn){
            mButtons[finalI].setText(myPlayerChar);
            mButtons[finalI].setClickable(false);
            availablePositions.remove(finalI);
            values[finalI] = myPlayerChar;
            isCurrentPlayerTurn = !isCurrentPlayerTurn;
            int isWinValue = checkForWin();
            if(isWinValue == 1 || isWinValue == -1){
              showDialogAndExit(isWinValue);
              return;
            }else if(checkForDraw()){
              showDialogAndExit(0);
              return;
            }
            if(gameType.equals("One-Player")){
              singlePlayerLogic();
            }else{
              mGameReference.child("gameValues").setValue(Arrays.asList(values));
              if (mGameViewModel.getmGameInstance().getTurn() == 1) {
                mGameViewModel.getmGameInstance().setTurn(2);
              } else {
                mGameViewModel.getmGameInstance().setTurn(1);
              }
              mGameReference.child("turn").setValue(mGameViewModel.getmGameInstance().getTurn());
              isCurrentPlayerTurn = updateTurn(mGameViewModel.getmGameInstance().getTurn());
              if(!isCurrentPlayerTurn){
                display.setText(R.string.waiting);
              }else{
                display.setText(R.string.your_turn);
              }
              twoPlayerGameLogic();
            }
          }else{
            Toast.makeText(requireContext(), "Not your turn, please wait!", Toast.LENGTH_SHORT).show();
          }
        }
      });
    }


  }

  /**
   * Settind the buttons text to the values we have set.
   */
  private void updateUI(){
    for(int i = 0; i < GRID_SIZE; ++i){
      mButtons[i].setText(values[i]);
    }
  }

  /**
   * Checks if turn should be updated, if turn is 1 and isHost is true then yes
   * else if turn is 2 and not the host then yes
   * @param turn
   * @return
   */
  private boolean updateTurn(int turn){
    return (turn == 1) == isHost;
  }


  /**
   * Main two player game logic. Adds game event listeners for change in game, checks if the button values
   * have changed, updates them accordingly, checks if the user has forfeited on the other side and accordinly sets the
   * dialog, and changes turns and waits for the other player to play.
   */
  private void twoPlayerGameLogic() {
    display.setText(R.string.waiting);
    mGameReference.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        Log.d(TAG, "OnDataChange in two player");
        mGameViewModel.setmGameInstance(snapshot.getValue(Game.class));
        assert mGameViewModel.getmGameInstance() != null;
        Log.d(TAG, mGameViewModel.getmGameInstance().toString());
        if(mGameViewModel.getmGameInstance().isComplete() && !mGameViewModel.getmGameInstance().getLostID().isEmpty()){
          if(!mGameViewModel.getmGameInstance().getLostID().equals(mCurrentUser.getUid()))
            showDialogAndExit(1);
        }
        List<String>gameVals = mGameViewModel.getmGameInstance().getGameValues();
        values = gameVals.toArray(new String[GRID_SIZE]);
        for(int i = 0; i < GRID_SIZE; ++i){
          if(!values[i].isEmpty()) availablePositions.remove(i);
        }
        updateUI();
        isCurrentPlayerTurn = updateTurn(mGameViewModel.getmGameInstance().getTurn());
        if(!isCurrentPlayerTurn){
          display.setText(R.string.waiting);
        }else{
          display.setText(R.string.your_turn);
        }
//        display.setText(R.string.your_turn);
        int win = checkForWin();
        Log.d(TAG, "WIN CHECK CALLED IN TWOPLAYER " + win);
        if(win == 1 || win == -1){
          showDialogAndExit(win);
        }else if(checkForDraw()) showDialogAndExit(0);
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {

      }
    });


  }

  /**
   * Single player logic is where the user plays against the computer. A random index that is clickable is
   * selected and the cmoputer plays an "O". If the user wins, appropriate dialog is shown.
   */
  private void singlePlayerLogic(){
      // if no available indices.
    if(checkForDraw()){
      showDialogAndExit(0);
      return;
    }
    Random random = new Random();
    int randIdx = random.nextInt(9);
    while(!availablePositions.contains(randIdx)) randIdx = random.nextInt(9);
    values[randIdx] = "O";
    mButtons[randIdx].setClickable(false);
    mButtons[randIdx].setText("O");
    availablePositions.remove(randIdx);
    Log.d(TAG, "Random index selected : " + randIdx);
    isCurrentPlayerTurn = true;
    int winValue = checkForWin();
    if(winValue == 1 || winValue == -1){
      showDialogAndExit(winValue);
    }
    else if(checkForDraw()) showDialogAndExit(0);
  }

  /**
   * Checks if the game has been drawn.
   * @return
   */
  private boolean checkForDraw() {
    return availablePositions.isEmpty();
  }

  /**
   * Checks all 8 possible combinations for a win, where the three characters match.
   * Its a win if the matching characters are that of the current user else it is a loss.
   * otherwise if there is no match its a draw.
   * @return
   */
  private int checkForWin() {
    Log.d(TAG, "Checking for a win");
    String winChar;
    if  (values[0].equals(values[1]) && values[1].equals(values[2]) && !values[0].isEmpty()) winChar = values[0];
    else if (values[3].equals(values[4]) && values[4].equals(values[5]) && !values[3].isEmpty()) winChar = values[3];
    else if (values[6].equals(values[7]) && values[7].equals(values[8]) && !values[6].isEmpty()) winChar = values[6];
    else if (values[0].equals(values[3]) && values[3].equals(values[6]) && !values[0].isEmpty()) winChar = values[0];
    else if (values[4].equals(values[1]) && values[1].equals(values[7]) && !values[1].isEmpty()) winChar = values[1];
    else if (values[2].equals(values[5]) && values[5].equals(values[8]) && !values[2].isEmpty()) winChar = values[2];
    else if (values[0].equals(values[4]) && values[4].equals(values[8]) && !values[0].isEmpty()) winChar = values[0];
    else if (values[6].equals(values[4]) && values[4].equals(values[2]) && !values[2].isEmpty()) winChar = values[2];
    else return 0;

    return (winChar.equals(myPlayerChar)) ? 1 : -1;
  }


  /**
   * Shows a win, lose or draw dialog and exits from the game into the dashboard fragment again.
   * @param i
   */
  private void showDialogAndExit(int i) {
      Log.d(TAG, "Complete game invoked");
      if(!gameType.equals("One-Player")){
        mGameReference.child("gameValues").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
          @Override
          public void onComplete(@NonNull Task<DataSnapshot> task) {
            if(task.isSuccessful()){
              boolean flag = true;
              List<String>gameValues = (List<String>) task.getResult().getValue();
              for(int i = 0; i < GRID_SIZE; ++i){
                assert gameValues != null;
                if(!Objects.equals(gameValues.get(i), values[i])){
                  flag = false;
                }
              }
              if(!flag){
                mGameReference.child("gameValues").setValue(Arrays.asList(values));
              }
            }
          }
        });

      }
      if(i == 1){
        mPlayerReference.child(mCurrentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
          @Override
          public void onComplete(@NonNull Task<DataSnapshot> task) {
            if(task.isSuccessful()){
              Player player = task.getResult().getValue(Player.class);
              assert player != null;
              player.setNumberOfWins(player.getNumberOfWins() + 1);
              mPlayerReference.child(mCurrentUser.getUid()).setValue(player);
            }else{
              Log.d(TAG, "Database error!");
            }
          }
        });
        if(dialogShown == 0){
          showDialog("Yay, you won!");
          dialogShown++;
        }

        if(gameType.equals("Two-Player") || gameType.equals("No Type")){
          deleteCurrentGame();
        }
      }else if(i == 0){
        if(dialogShown == 0) {
          showDialog("Aw shucks, it's a Draw!");
          dialogShown++;
        }
        if(gameType.equals("Two-Player") || gameType.equals("No Type")){
          deleteCurrentGame();
        }
      }else if(i == -1){
        mPlayerReference.child(mCurrentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
          @Override
          public void onComplete(@NonNull Task<DataSnapshot> task) {
            if(task.isSuccessful()){
              Player player = task.getResult().getValue(Player.class);
              assert player != null;
              player.setNumberOfLosses(player.getNumberOfLosses() +  1);
              mPlayerReference.child(mCurrentUser.getUid()).setValue(player);
            }else{
              Log.d(TAG, "Database error!");
            }
          }
        });
        if(dialogShown == 0) {
          showDialog("Oh no, you lost! :(");
          dialogShown++;
        }
        if(gameType.equals("Two-Player") || gameType.equals("No Type")){
          deleteCurrentGame();
        }
      }
  }

  /**
   * Deletes the completed game.
   */
  private void deleteCurrentGame() {
    DatabaseReference mCurrentGameReference = mGameReference.child(gameID);
    mCurrentGameReference.removeValue();
  }


  /**
   * Helper to show the dialog for win loss or draw.
   * @param title
   */
  private void showDialog(String title){
    AlertDialog dialog;
    if(getActivity()!=null){
      dialog = new AlertDialog.Builder(requireActivity())
              .setTitle(title)
              .setPositiveButton("Return to Dashboard", (d, which) -> {
                mNavController.popBackStack();
              })
              .create();
      dialog.show();
    }
  }


  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_logout, menu);
    // this action menu is handled in MainActivity
  }
}




