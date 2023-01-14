package androidsamples.java.tictactoe;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

/**
 * THe main activity class. Implements the logout method for the whole game by using the
 * toolbar options.
 */
public class MainActivity extends AppCompatActivity {
  private FirebaseAuth mAuth;
  private FirebaseUser mUser;
  private static final String TAG = "MainActivity";


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mAuth = FirebaseAuth.getInstance();
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

  }

  /**
   * To check if user has selected the logout menu item, then call the logout method
   * @param item
   * @return
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_logout) {
      Log.d(TAG, "logout clicked");
      logout();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }


  /**
   * Function to log the user out. Uses the FirebaseAuth signout method and we
   * navigate to the loginFragment in this case.
   */
  private void logout() {
    Log.d(TAG, "Logging out");
    mAuth.signOut();

    NavController navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment);
    navController.navigateUp();
    navController.navigate(R.id.loginFragment);
  }
}