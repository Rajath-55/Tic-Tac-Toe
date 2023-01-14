package androidsamples.java.tictactoe;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

/**
 * Fragment for implementing the Login Logic and the Login page.
 * Allows users to log in with email and password.
 */
public class LoginFragment extends Fragment {
    // a private mAuth Instance
    private FirebaseAuth mAuth;
    private static final String TAG = "LoginFragment";
    private String mEmail, mPassword;
    private EditText mEmailBox, mPasswordBox;
    private DatabaseReference mDatabaseReference;
    FirebaseDatabase database;


    /**
     * Overrides the oncreate instance. We set a player instance and add references to
     * the realtime firebase database as well.
     * @param savedInstanceState Bundle for saved instances.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = database.getReference();
        Player newPlayer = new Player("ok@ok.com", "bRWxustnfuNlc0jzOPFmp2ygMfw1todo");
        mDatabaseReference.child("players").child(newPlayer.getUid()).setValue(newPlayer);

    }

    /**
     * Overrides the onCreateView of the Fragment class. It adds the login listener.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return A view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        mEmailBox = view.findViewById(R.id.edit_email);
        mPasswordBox = view.findViewById(R.id.edit_password);


        view.findViewById(R.id.btn_log_in)
                .setOnClickListener(v -> {
                    mEmail = mEmailBox.getText().toString();
                    mPassword = mPasswordBox.getText().toString();
                    if(mPassword.isEmpty() || mEmail.isEmpty()){
                        Toast.makeText(getActivity(), "Email and Password cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    checkForUserAndLogin(mEmail, mPassword);
                });

        return view;
    }

    /**
     * Checks if the user exists and if the user does exist in the players database, then we sign in
     * else we create this user instance, set it in the database and login.
     * @param email
     * @param password
     */
    private void checkForUserAndLogin(String email, String password) {
        mDatabaseReference.child("players").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Checking if user exists");

                if(!snapshot.exists()){
                    createAccount(email, password);
                }else{
                    loginToAccount(email, password);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**
     * A function to use the signInWithEmailAndPassword method of FirebaseAuth. It has a listener to wait for sign in
     * to be completed and then we redirect to the dashboard fragment.
     * @param email
     * @param password
     */
    private void loginToAccount(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener((Activity) requireContext(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    Log.d(TAG, "Logged in!");
                    reload();
                }
                else {
                    Snackbar.make(requireActivity().findViewById(android.R.id.content),
                            Objects.requireNonNull(Objects.requireNonNull(task.getException()).getLocalizedMessage()), Snackbar.LENGTH_LONG).show();
                }

            }
        });
    }

    /**
     * On starting, we check if its already a logged in user and redirect accordinly.
     */
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            reload();
        }
    }

    /**
     * Helper function to actually use nav actions and safe args to
     * navigate to Dashboard Fragment.
     */
    private void reload() {
        NavDirections action = LoginFragmentDirections.actionLoginSuccessful();
        Navigation.findNavController(requireView()).navigate(action);
    }

    /**
     * Function to create a new user and store in the database using the createUserWithEmailAndPassword
     * method of Firebaseauth.
     * @param email
     * @param password
     */
    private void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener((Activity) requireContext(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    Log.d(TAG, "Logged in!");
                    FirebaseUser user = mAuth.getCurrentUser();
                    assert user != null;
                    String uid = user.getUid();
                    Player newPlayer = new Player(email, uid);
                    mDatabaseReference.child("players").child(newPlayer.getUid()).setValue(newPlayer);
                    reload();

                }
                else {
                    Snackbar.make(requireActivity().findViewById(android.R.id.content),
                            Objects.requireNonNull(Objects.requireNonNull(task.getException()).getLocalizedMessage()), Snackbar.LENGTH_LONG).show();
                }

            }
        });
    }


    // No options menu in login fragment.
}