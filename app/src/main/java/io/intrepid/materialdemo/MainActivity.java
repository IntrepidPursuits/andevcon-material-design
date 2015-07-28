package io.intrepid.materialdemo;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final String INTREPID_URL = "http://www.intrepid.io";
    public static final String INTREPID_DRIBBLE_URL = "https://dribbble.com/Intrepid";
    public static final String SOURCE_CODE_URL = "http://bit.ly/andevcon-material";
    private static final int SELECT_PHOTO = 11191;
    private static final int MAX_PHOTO_SIZE = 700;

    private CardView cardView;
    private ImageView imageView;
    private DrawerLayout drawerLayout;
    private FloatingActionButton floatingActionButton;
    private CardView textCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.image);
        cardView = (CardView) findViewById(R.id.card_view);
        textCardView = (CardView) findViewById(R.id.card_view_text);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        /*
        This line ensures that the activity will use your custom Toolbar as an ActionBar
        It will add the appropriate title and inflate the associated menu.
         */
        setSupportActionBar(toolbar);

        /*
        This code sets up the hamburger button, including the new transition to back arrow
        drawerLayout contains the sliding navigation menu (shown in later slides)
         */
        drawerLayout = (DrawerLayout) findViewById(R.id.main_drawer);
        ActionBarDrawerToggle actionBarDrawerToggle =
                new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.closed);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeButtonEnabled(true);
        }
        actionBarDrawerToggle.syncState();

        //finish setting up UI
        setupFab();
        setupNavigationView();
    }

    private void setupFab() {
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floating_action_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.select_photo)), SELECT_PHOTO);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PHOTO) {
                try {
                    Uri selectedImg = data.getData();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImg);
                    showImage(bitmap);
                } catch (Exception e) {
                    Toast.makeText(this, getString(R.string.image_error), Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    private void showImage(Bitmap bitmap) {
        //we don't really need to display the full-sized image right now - scale it down.
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        while (height > MAX_PHOTO_SIZE || width > MAX_PHOTO_SIZE) {
            height /= 2;
            width /= 2;
        }
        imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, width, height, false));
        findViewById(R.id.image_placeholder_text).setVisibility(View.INVISIBLE);

        //This can also be done synchronously, but you'll want to be working on a different thread
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                cardView.setBackgroundColor(palette.getDarkMutedColor(Color.DKGRAY));
                textCardView.setBackgroundColor(palette.getLightVibrantColor(Color.GRAY));
                floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(palette.getVibrantColor(Color.DKGRAY)));
                showSecretInfoButton(palette);
            }
        });
    }

    private void showSecretInfoButton(Palette palette) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Button mainButton = (Button) findViewById(R.id.main_button);
            mainButton.setVisibility(View.VISIBLE);

            //Here's how to create a ripple programmatically
            ColorDrawable mainColor = new ColorDrawable(palette.getLightVibrantColor(Color.GRAY));
            ColorStateList rippleColor = ColorStateList.valueOf(getResources().getColor(R.color.ripple_white));
            RippleDrawable rippleDrawable = new RippleDrawable(rippleColor, mainColor, null);

            mainButton.setBackground(rippleDrawable);
            mainButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showInfo();
                }
            });
        }
        //else: This device is too old! No point in displaying the secret "ripple" button
    }

    private void showInfo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.info_prompt);
        builder.setNeutralButton(R.string.ok, null);
        builder.create().show();
    }

    private void setupNavigationView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                String urlToLoad = null;
                switch (menuItem.getItemId()) {
                    case R.id.menu_intrepid:
                        urlToLoad = INTREPID_URL;
                        break;
                    case R.id.menu_intrepid_dribble:
                        urlToLoad = INTREPID_DRIBBLE_URL;
                        break;
                    case R.id.menu_source_code:
                        urlToLoad = SOURCE_CODE_URL;
                }
                if (!TextUtils.isEmpty(urlToLoad)) {
                    loadUrl(urlToLoad);
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    return true;
                }
                return false;
            }
        });
    }

    private void loadUrl(String urlToLoad) {
        Intent urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlToLoad));
        startActivity(urlIntent);
    }

}
