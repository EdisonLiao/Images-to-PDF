package edison.readpdf.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;

import androidx.fragment.app.Fragment;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.Settings;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import edison.readpdf.BuildConfig;
import edison.readpdf.R;
import edison.readpdf.ads.AdmobAdMgr;
import edison.readpdf.ads.IAdAdmobNativeListener;
import edison.readpdf.fragment.ImageToPdfFragment;
import edison.readpdf.providers.fragmentmanagement.FragmentManagement;
import edison.readpdf.util.Constants;
import edison.readpdf.util.FeedbackUtils;
import edison.readpdf.util.DirectoryUtils;
import edison.readpdf.util.PermissionsUtils;
import edison.readpdf.util.ThemeUtils;

import static edison.readpdf.util.Constants.LAUNCH_COUNT;
import static edison.readpdf.util.Constants.REQUEST_CODE_FOR_WRITE_PERMISSION;
import static edison.readpdf.util.Constants.THEME_BLACK;
import static edison.readpdf.util.Constants.THEME_DARK;
import static edison.readpdf.util.Constants.THEME_SYSTEM;
import static edison.readpdf.util.Constants.THEME_WHITE;
import static edison.readpdf.util.Constants.WRITE_PERMISSIONS;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FeedbackUtils mFeedbackUtils;
    private NavigationView mNavigationView;
    private SharedPreferences mSharedPreferences;
    private SparseIntArray mFragmentSelectedMap;
    private FragmentManagement mFragmentManagement;

    private boolean mSettingsActivityOpenedForManageStoragePermission = false;
    private NativeAd mNativeAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.getInstance().setThemeApp(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });

        mNavigationView = findViewById(R.id.nav_view);

        setThemeOnActivityExclusiveComponents();

        checkAndAskForStoragePermission();

        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        setSupportActionBar(toolbar);

        // Set navigation drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.app_name, R.string.app_name);

        //Replaced setDrawerListener with addDrawerListener because it was deprecated.
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // initialize values
        initializeValues();

        setXMLParsers();
        // Check for app shortcuts & select default fragment
        Fragment fragment = mFragmentManagement.checkForAppShortcutClicked();

        // Check if  images are received
        handleReceivedImagesIntent(fragment);

        String adId = "ca-app-pub-7094078041880308/6130222400";
        if (BuildConfig.DEBUG){
            adId = "ca-app-pub-3940256099942544/2247696110";
        }

        AdmobAdMgr.sIns.requestNativeAd(this,adId,new AdmobNativeCallback());
    }

    private class AdmobNativeCallback implements IAdAdmobNativeListener{

        @Override
        public void onNativeAdLoadSuccess(NativeAd nativeAd) {
            mNativeAd = nativeAd;
            inflateAdView();
        }

        @Override
        public void onNativeAdLoadFail(String msg, int errorCode) {}
    }

    private void inflateAdView(){
        NativeAdView adView =
                (NativeAdView) this.getLayoutInflater().inflate(R.layout.ad_unified, null);

        AdmobAdMgr.sIns.populateNativeAdView(mNativeAd,adView,findViewById(R.id.fl_ad));
    }

    /**
     * Set suitable xml parsers for reading .docx files.
     */
    private  void setXMLParsers() {
        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory",
                "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory",
                "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory",
                "com.fasterxml.aalto.stax.EventFactoryImpl");
    }

    /**
     * A method for the feedback and whats new dialogs.
     */
    private void displayFeedBackAndWhatsNew() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int count = mSharedPreferences.getInt(LAUNCH_COUNT, 0);
        if (count > 0 && count % 15 == 0) {
            mFeedbackUtils.rateUs();
        }
        if (count != -1) {
            mSharedPreferences.edit().putInt(LAUNCH_COUNT, count + 1).apply();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (PermissionsUtils.getInstance().checkRuntimePermissions(this, WRITE_PERMISSIONS)) {
            DirectoryUtils.makeAndClearTemp();
        }

        if (mNativeAd != null){
            mNativeAd.destroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSettingsActivityOpenedForManageStoragePermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    askStorageManagerPermission();
                }
            }
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        if (actionBar != null)
            actionBar.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_favourites, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_favourites_item) {
            setTitle(R.string.favourites);
            mFragmentManagement.favouritesFragmentOption();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Ininitializes default values
     */
    private void initializeValues() {
        mFeedbackUtils = new FeedbackUtils(this);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.setCheckedItem(R.id.nav_home);

        mFragmentManagement = new FragmentManagement(this, mNavigationView);
        setTitleMap();
    }

    /**
     * Checks if images are received in the intent
     *
     * @param fragment - instance of current fragment
     */
    private void handleReceivedImagesIntent(Fragment fragment) {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (type == null || !type.startsWith("image/"))
            return;

        if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            handleSendMultipleImages(intent, fragment); // Handle multiple images
        } else if (Intent.ACTION_SEND.equals(action)) {
            handleSendImage(intent, fragment); // Handle single image
        }
    }

    /**
     * Get image uri from intent and send the image to homeFragment
     *
     * @param intent   - intent containing image uris
     * @param fragment - instance of homeFragment
     */
    private void handleSendImage(Intent intent, Fragment fragment) {
        Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        ArrayList<Uri> imageUris = new ArrayList<>();
        imageUris.add(uri);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(getString(R.string.bundleKey), imageUris);
        fragment.setArguments(bundle);
    }

    /**
     * Get ArrayList of image uris from intent and send the image to homeFragment
     *
     * @param intent   - intent containing image uris
     * @param fragment - instance of homeFragment
     */
    private void handleSendMultipleImages(Intent intent, Fragment fragment) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(getString(R.string.bundleKey), imageUris);
            fragment.setArguments(bundle);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            boolean shouldExit = mFragmentManagement.handleBackPressed();
            if (shouldExit)
                super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        setTitleFragment(mFragmentSelectedMap.get(item.getItemId()));
        return mFragmentManagement.handleNavigationItemSelected(item.getItemId());
    }

    public void setNavigationViewSelection(int id) {
        mNavigationView.setCheckedItem(id);
    }

    private void checkAndAskForStoragePermission() {
        if (!PermissionsUtils.getInstance().checkRuntimePermissions(this, WRITE_PERMISSIONS)) {
            getRuntimePermissions();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    askStorageManagerPermission();
                }
            }
        }
    }

    private void getRuntimePermissions() {
        PermissionsUtils.getInstance().requestRuntimePermissions(this,
                    WRITE_PERMISSIONS,
                    REQUEST_CODE_FOR_WRITE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionsUtils.getInstance().handleRequestPermissionsResult(this, grantResults,
                requestCode, REQUEST_CODE_FOR_WRITE_PERMISSION, this::askStorageManagerPermission);
    }

    private void askStorageManagerPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.one_more_thing_text)
                        .setMessage(R.string.storage_manager_permission_rationale)
                        .setCancelable(false)
                        .setPositiveButton(R.string.allow_text, (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            mSettingsActivityOpenedForManageStoragePermission = true;
                            startActivity(intent);
                            dialog.dismiss();
                        }).setNegativeButton(R.string.close_app_text, ((dialog, which) -> dialog.dismiss()))
                        .show();
            }
        }
    }

    /**
     * puts image uri's in a bundle and start ImageToPdf fragment with this bundle
     * as argument
     *
     * @param imageUris - ArrayList of image uri's in temp directory
     */
    public void convertImagesToPdf(ArrayList<Uri> imageUris) {
        Fragment fragment = new ImageToPdfFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(getString(R.string.bundleKey), imageUris);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
    }

    // Hashmap for setting the mFragmentSelectedMap.
    private void setTitleMap() {
        mFragmentSelectedMap = new SparseIntArray();
        mFragmentSelectedMap.append(R.id.nav_home, R.string.app_name);
        mFragmentSelectedMap.append(R.id.nav_camera, R.string.images_to_pdf);
        mFragmentSelectedMap.append(R.id.nav_qrcode, R.string.qr_barcode_pdf);
        mFragmentSelectedMap.append(R.id.nav_add_text, R.string.add_text);
        mFragmentSelectedMap.append(R.id.nav_gallery, R.string.viewFiles);
        mFragmentSelectedMap.append(R.id.nav_merge, R.string.merge_pdf);
        mFragmentSelectedMap.append(R.id.nav_split, R.string.split_pdf);
        mFragmentSelectedMap.append(R.id.nav_text_to_pdf, R.string.text_to_pdf);
        mFragmentSelectedMap.append(R.id.nav_history, R.string.history);
        mFragmentSelectedMap.append(R.id.nav_add_password, R.string.add_password);
        mFragmentSelectedMap.append(R.id.nav_remove_password, R.string.remove_password);
        mFragmentSelectedMap.append(R.id.nav_extract_images, R.string.extract_images);
        mFragmentSelectedMap.append(R.id.nav_pdf_to_images, R.string.pdf_to_images);
        mFragmentSelectedMap.append(R.id.nav_remove_pages, R.string.remove_pages);
        mFragmentSelectedMap.append(R.id.nav_rearrange_pages, R.string.reorder_pages);
        mFragmentSelectedMap.append(R.id.nav_compress_pdf, R.string.compress_pdf);
        mFragmentSelectedMap.append(R.id.nav_add_images, R.string.add_images);
        mFragmentSelectedMap.append(R.id.nav_remove_duplicate_pages, R.string.remove_duplicate_pages);
        mFragmentSelectedMap.append(R.id.nav_invert_pdf, R.string.invert_pdf);
        mFragmentSelectedMap.append(R.id.nav_add_watermark, R.string.add_watermark);
        mFragmentSelectedMap.append(R.id.nav_zip_to_pdf, R.string.zip_to_pdf);
        mFragmentSelectedMap.append(R.id.nav_rotate_pages, R.string.rotate_pages);
        mFragmentSelectedMap.append(R.id.nav_excel_to_pdf, R.string.excel_to_pdf);
        mFragmentSelectedMap.append(R.id.nav_local_pdf, R.string.str_select_local_pdf);
    }

    /**
     * Sets fragment title
     * @param title - string resource id
     */
    private void setTitleFragment(int title) {
        if (title != 0)
            setTitle(title);
    }

    private void setThemeOnActivityExclusiveComponents() {
        RelativeLayout toolbarBackgroundLayout = findViewById(R.id.toolbar_background_layout);
        MaterialCardView content = findViewById(R.id.content);
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String themeName = mSharedPreferences.getString(Constants.DEFAULT_THEME_TEXT,
                Constants.DEFAULT_THEME);
        switch (themeName) {
            case THEME_WHITE:
                toolbarBackgroundLayout.setBackgroundResource(R.drawable.toolbar_bg);
                content.setCardBackgroundColor(getResources().getColor(R.color.lighter_gray));
                mNavigationView.setBackgroundResource(R.color.white);
                break;
            case THEME_BLACK:
                toolbarBackgroundLayout.setBackgroundResource(R.color.black);
                content.setCardBackgroundColor(getResources().getColor(R.color.black));
                mNavigationView.setBackgroundResource(R.color.black);
                mNavigationView.setItemTextColor(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                mNavigationView.setItemIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                mNavigationView.setItemBackgroundResource(R.drawable.navigation_item_selected_bg_selector_dark);
                break;
            case THEME_DARK:
                toolbarBackgroundLayout.setBackgroundResource(R.color.colorBlackAltLight);
                content.setCardBackgroundColor(getResources().getColor(R.color.colorBlackAlt));
                mNavigationView.setBackgroundResource(R.color.colorBlackAlt);
                mNavigationView.setItemTextColor(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                mNavigationView.setItemIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                mNavigationView.setItemBackgroundResource(R.drawable.navigation_item_selected_bg_selector_dark);
                break;
            case THEME_SYSTEM:
            default:
                if ((this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                    toolbarBackgroundLayout.setBackgroundResource(R.color.colorBlackAltLight);
                    content.setCardBackgroundColor(getResources().getColor(R.color.colorBlackAlt));
                    mNavigationView.setBackgroundResource(R.color.colorBlackAlt);
                    mNavigationView.setItemTextColor(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                    mNavigationView.setItemIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                    mNavigationView.setItemBackgroundResource(R.drawable.navigation_item_selected_bg_selector_dark);
                } else {
                    toolbarBackgroundLayout.setBackgroundResource(R.drawable.toolbar_bg);
                    content.setCardBackgroundColor(getResources().getColor(R.color.lighter_gray));
                    mNavigationView.setBackgroundResource(R.color.white);
                }
        }
    }
}
