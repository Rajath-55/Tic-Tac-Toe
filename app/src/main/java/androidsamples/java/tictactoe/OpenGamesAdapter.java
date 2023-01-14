package androidsamples.java.tictactoe;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * The recycler view adapter class for showing the list of open games in the dashboard fragment.
 */
public class OpenGamesAdapter extends RecyclerView.Adapter<OpenGamesAdapter.ViewHolder> {
  private ArrayList<Game> openGames;



  public OpenGamesAdapter(ArrayList<Game> openGames, NavController mNavController) {
    this.openGames = openGames;
  }

  /**
   * Overriden method that inflates the view.
   * @param parent
   * @param viewType
   * @return
   */
  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.fragment_item, parent, false);
    return new ViewHolder(view);
  }

  /**
   * On binding to the view holder, we call the bind method of the viewholder which
   * sets the data in the list item of the recycler view.
   * @param holder
   * @param position
   */
  @Override
  public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
    holder.bind(openGames.get(position), position + 1);
  }


  @Override
  public int getItemCount() {
    return openGames == null ? 0 : openGames.size();
  }

  /**
   * Inner viewHolder Class for The recycler view. This holds all the TextViews and
   * sets data using the bind() method.
   */
  public class ViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final TextView mIdView;
    public final TextView mContentView;

    public ViewHolder(View view) {
      super(view);
      mView = view;
      mIdView = view.findViewById(R.id.item_number);
      mContentView = view.findViewById(R.id.content);
    }

    @NonNull
    @Override
    public String toString() {
      return super.toString() + " '" + mContentView.getText() + "'";
    }

    /**
     * The bind method sets the open game data, fetching the email of the host for the open game as well
     * as the game ID.
     * @param game
     * @param index
     */
    public void bind(Game game, int index) {

        final String[] email = {""};
        FirebaseDatabase.getInstance().getReference("players").child(game.getHost()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
          @Override
          public void onComplete(@NonNull Task<DataSnapshot> task) {
             email[0] = task.getResult().getValue(Player.class).getEmail();
             Log.d("Recycler View", email[0]);
            mContentView.setText(game.getGameID() + " : By " + email[0]);
            mIdView.setText("# " + index);
            mView.setOnClickListener(v -> {
              NavDirections action = DashboardFragmentDirections.actionGame("No Type", game.getGameID());
              Navigation.findNavController(mView).navigate(action);
            });
          }
        });

    }
  }
}