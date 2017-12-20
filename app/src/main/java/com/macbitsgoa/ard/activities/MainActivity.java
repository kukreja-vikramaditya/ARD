package com.macbitsgoa.ard.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.macbitsgoa.ard.R;
import com.macbitsgoa.ard.fragments.ChatFragment;
import com.macbitsgoa.ard.fragments.ForumFragment;
import com.macbitsgoa.ard.fragments.HomeFragment;
import com.macbitsgoa.ard.interfaces.ChatFragmentListener;
import com.macbitsgoa.ard.interfaces.ForumFragmentListener;
import com.macbitsgoa.ard.keys.AuthActivityKeys;
import com.macbitsgoa.ard.services.MessagingService;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Main activity of app.
 *
 * @author Vikramaditya Kukreja
 */
public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        BottomNavigationView.OnNavigationItemSelectedListener,
        ForumFragmentListener,
        ChatFragmentListener {

    /**
     * Bottom navigation view.
     */
    @BindView(R.id.bottom_nav_activity_main)
    BottomNavigationView bottomNavigationView;

    /**
     * Fragment manager used to handle the 3 fragments.
     */
    private FragmentManager fragmentManager;

    /**
     * ForumFragment object.
     */
    private ForumFragment forumFragment;

    /**
     * HomeFragment object.
     */
    private HomeFragment homeFragment;

    /**
     * ChatFragment object.
     */
    private ChatFragment chatFragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        //Check if authorised
        if (!auth(getIntent())) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        startService(new Intent(this, MessagingService.class));
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    /**
     * Start {@link AuthActivity} if Firebase user object is null.
     * This also closes the current {@link MainActivity} before launching Auth.
     *
     * @param intent Intent object. Should not be null. See <b>MainActivityTest</b>.
     * @return boolean true if auth is successful, false otherwise.
     */
    public boolean auth(@NonNull final Intent intent) {
        return !intent.getBooleanExtra(AuthActivityKeys.USE_DEFAULT, true)
                || FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    /**
     * All initialisation are done here.
     */
    private void init() {
        ButterKnife.bind(this);

        fragmentManager = getSupportFragmentManager();

        forumFragment = ForumFragment.newInstance(getString(R.string.bottom_nav_forum_activity_main));
        homeFragment = HomeFragment.newInstance(null);
        chatFragment = ChatFragment.newInstance(getString(R.string.bottom_nav_chat_activity_main));

        bottomNavigationView.setSelectedItemId(R.id.bottom_nav_home);
        bottomNavigationView.getMenu().findItem(R.id.bottom_nav_home).setChecked(true);
        fragmentManager.beginTransaction().replace(R.id.frame_content_main, homeFragment,
                getString(R.string.bottom_nav_home_activity_main)).commit();
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        bottomNavigationView.getMenu().findItem(R.id.bottom_nav_home).setChecked(true);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        // Handle navigation view item clicks here.
        //TODO add backstack as well.
        final int id = item.getItemId();

        if (id == R.id.bottom_nav_forum) {
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_content_main, forumFragment)
                    .commit();
            return true;
        } else if (id == R.id.bottom_nav_home) {
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_content_main, homeFragment)
                    .commit();
            return true;
        } else {
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_content_main, chatFragment)
                    .commit();
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void updateChatFragment() {

    }

    @Override
    public void updateForumFragment() {

    }
}
