package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Dashboard fragment to store data and update wins and losses
 * and show open games.
 */
public class DashboardFragment extends Fragment {

  private static final String TAG = "DashboardFragment";

  private NavController mNavController;
  private FirebaseAuth mAuth;
  private Player mPlayer;
  private FirebaseDatabase mDatabase;
  private DatabaseReference mReference;
  private TextView mScoreTextView;
  private TextView mWinsTextView, mLossesTextView, mInfo;
  private RecyclerView mRecyclerView;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public DashboardFragment() {
    mAuth = FirebaseAuth.getInstance();
  }

  /**
   * Overrides the onCreate instance and sets the database references.
   * @param savedInstanceState
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");
    mAuth = FirebaseAuth.getInstance();
    mDatabase = FirebaseDatabase.getInstance();
    mReference = mDatabase.getReference();


    setHasOptionsMenu(true); // Needed to display the action menu for this fragment
  }

  /**
   * Checks if the user is logged in.
   * @return
   */
  private boolean isLoggedIn() {
    FirebaseUser mUser = mAuth.getCurrentUser();
    return mUser != null;
  }

  /**
   * Inflates the view for the fragment.
   * @param inflater
   * @param container
   * @param savedInstanceState
   * @return
   */
  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_dashboard, container, false);
  }

  /**
   * Overrides the onViewCreated param. Retrieves open games and sets the recyclerview adapter.
   * Sets the onclick listener for the add games button.
   * @param view
   * @param savedInstanceState
   */
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mNavController = Navigation.findNavController(view);
    mInfo = view.findViewById(R.id.open_display);
    mWinsTextView = view.findViewById(R.id.won_score);
    mLossesTextView = view.findViewById(R.id.lost_score);
    mRecyclerView = view.findViewById(R.id.list);


    if(!isLoggedIn()){
      NavDirections action = DashboardFragmentDirections.actionNeedAuth();
      mNavController.navigate(action);
    }
    FirebaseUser user = mAuth.getCurrentUser();
    assert user != null;
    mPlayer = new Player(user.getEmail(), user.getUid());
    updateUI();

    ArrayList<Game> gameList = new ArrayList<>();

    mReference.child("games").addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        gameList.clear();
        for (DataSnapshot shot : snapshot.getChildren()) {
          Game game = shot.getValue(Game.class);
          assert game != null;
          if (game.isOpen() && !game.getHost().equals(mAuth.getCurrentUser().getUid())) gameList.add(game);
        }
        mRecyclerView.setAdapter(new OpenGamesAdapter(gameList, mNavController));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        String userEmail = mPlayer.getEmail();
        mInfo.setText(gameList.isEmpty() ? userEmail + "\nNo Open Games Available :(" : userEmail + "\nOpen Games");
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {


      }
    });

    // Show a dialog when the user clicks the "new game" button
    view.findViewById(R.id.fab_new_game).setOnClickListener(v -> {

      // A listener for the positive and negative buttons of the dialog
      DialogInterface.OnClickListener listener = (dialog, which) -> {
        String gameType = "No type";
        String newGameID = "Single Game";
        if (which == DialogInterface.BUTTON_POSITIVE) {
          gameType = getString(R.string.two_player);
          newGameID = mReference.child("games").push().getKey();
          assert newGameID != null;
          mReference.child("games").child(newGameID).setValue(new Game(mPlayer.getUid(), newGameID));
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
          gameType = getString(R.string.one_player);
        }
        Log.d(TAG, "New Game: " + gameType);

        // Passing the game type as a parameter to the action
        // extract it in GameFragment in a type safe way
        NavDirections action = DashboardFragmentDirections.actionGame(gameType, newGameID);
        mNavController.navigate(action);
      };

      // create the dialog
      AlertDialog dialog = new AlertDialog.Builder(requireActivity())
          .setTitle(R.string.new_game)
          .setMessage(R.string.new_game_dialog_message)
          .setPositiveButton(R.string.two_player, listener)
          .setNegativeButton(R.string.one_player, listener)
          .setNeutralButton(R.string.cancel, (d, which) -> d.dismiss())
          .create();
      dialog.show();
    });
  }

  /**
   * Sets the text for wins and losses of the player.
   */
  private void updateUI() {
    mReference.child("players").child(mPlayer.getUid()).addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        if (snapshot.exists()) {
          Player player = snapshot.getValue(Player.class);
          assert player != null;
          mWinsTextView.setText(String.valueOf(player.getNumberOfWins()));
          mLossesTextView.setText(String.valueOf(player.getNumberOfLosses()));
        }
      }
      @Override
      public void onCancelled(@NonNull DatabaseError error) {

      }
    });

  }




  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_logout, menu);
    // this action menu is handled in MainActivity
  }


}