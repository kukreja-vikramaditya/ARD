package com.macbitsgoa.ard.fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.vivchar.viewpagerindicator.ViewPagerIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.macbitsgoa.ard.R;
import com.macbitsgoa.ard.activities.AnnActivity;
import com.macbitsgoa.ard.adapters.AnnSlideshowAdapter;
import com.macbitsgoa.ard.adapters.HomeAdapter;
import com.macbitsgoa.ard.adapters.SlideshowAdapter;
import com.macbitsgoa.ard.keys.AnnItemKeys;
import com.macbitsgoa.ard.keys.HomeItemKeys;
import com.macbitsgoa.ard.keys.SlideshowItemKeys;
import com.macbitsgoa.ard.models.AnnItem;
import com.macbitsgoa.ard.models.SlideshowItem;
import com.macbitsgoa.ard.services.HomeService;
import com.macbitsgoa.ard.utils.AHC;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * A simple {@link Fragment} subclass to display home content.
 *
 * @author Vikramaditya Kukreja
 */
public class HomeFragment extends BaseFragment implements AppBarLayout.OnOffsetChangedListener {

    /**
     * TAG for this class.
     */
    public static final String TAG = HomeFragment.class.getSimpleName();

    /**
     * RecyclerView to display Home content.
     */
    @BindView(R.id.recyclerView_fragment_home)
    public RecyclerView homeRV;

    /**
     * Viewpager indicator.
     */
    @BindView(R.id.ci_fragment_home)
    public ViewPagerIndicator pagerIndicator;

    /**
     * ViewPager for image slideshow.
     */
    @BindView(R.id.vp_fragment_home_slideshow)
    public ViewPager slideshowVP;

    @BindView(R.id.ab_fragment_home)
    AppBarLayout appBarLayout;

    @BindView(R.id.vp_vh_announcement)
    ViewPager annVP;

    @BindView(R.id.nsv_fragment_home)
    NestedScrollView nsv;

    Handler handler;
    Runnable update;
    Handler annSlideshowHandler;
    Runnable annSlideshowRunable;

    private RealmResults<AnnItem> annItems;

    /**
     * Unbinder for ButterKnife.
     */
    private Unbinder unbinder;

    /**
     * Slideshow adapter.
     */
    private SlideshowAdapter slideshowAdapter;

    /**
     * Slideshow list.
     */
    private List<SlideshowItem> slideshowItems;

    /**
     * Firebase database reference to home content.
     */
    private DatabaseReference homeRef = getRootReference().child(AHC.FDR_HOME);

    /**
     * Firebase database reference to announcement content.
     */
    private DatabaseReference annRef = getRootReference().child(AHC.FDR_ANN);

    /**
     * Reference to slide show image data.
     */
    private DatabaseReference imageSlideshowRef = getRootReference().child(AHC.FDR_EXTRAS).child("home").child("slideshow");

    /**
     * Value event listener for {@link #homeRef}.
     */
    private ValueEventListener homeRefVEL;

    /**
     * Value event listener for {@link #annRef}.
     */
    private ValueEventListener annRefVEL;

    /**
     * Value event listener for {@link #imageSlideshowRef}.
     */
    private ValueEventListener imageSlideShowVEL;

    /**
     * {@link View#offsetTopAndBottom(int)} of {@link #appBarLayout}.
     */
    private int appBarOffset = 0;

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, view);
        homeRV.setHasFixedSize(true);
        homeRV.setLayoutManager(new LinearLayoutManager(getContext()));
        homeRV.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        homeRV.setAdapter(new HomeAdapter(getContext()));

        setupSlideshow();
        imageSlideShowVEL = getImageSlideShowVEL();
        imageSlideshowRef.addValueEventListener(imageSlideShowVEL);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        appBarLayout.addOnOffsetChangedListener(this);
        scrollToTop();
        //hide app bar if orientation is landscape on starting
        hideAppBar();

        homeRefVEL = getHomeRefVEL();
        annRefVEL = getAnnRefVEL();

        homeRef.orderByChild(HomeItemKeys.DATE + "/time").limitToLast(5).addValueEventListener(homeRefVEL);
        annRef.addValueEventListener(annRefVEL);

        setupAnnouncementSlideshow();
        appBarLayout.offsetTopAndBottom(appBarOffset);
    }

    @Override
    public void onStop() {
        handler.removeCallbacks(update);
        if (annSlideshowHandler != null && annSlideshowRunable != null) {
            annSlideshowHandler.removeCallbacks(annSlideshowRunable);
        }
        //Remove firebase database listeners
        homeRef.removeEventListener(homeRefVEL);
        annRef.removeEventListener(annRefVEL);

        annItems.removeAllChangeListeners();

        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        imageSlideshowRef.removeEventListener(imageSlideShowVEL);
        unbinder.unbind();
    }

    @Override
    public void onOffsetChanged(final AppBarLayout appBarLayout, final int verticalOffset) {
        appBarOffset = verticalOffset;
    }

    private void setupSlideshow() {
        slideshowItems = new ArrayList<>();
        slideshowAdapter = new SlideshowAdapter(slideshowItems);

        handler = new Handler();
        update = () -> {
            if (slideshowVP == null || slideshowAdapter == null) return;
            int newPos = slideshowVP.getCurrentItem() + 1;
            newPos %= slideshowAdapter.getCount();
            slideshowVP.setCurrentItem(newPos, true);
        };

        handler.postDelayed(update, 5000);
        slideshowVP.setAdapter(slideshowAdapter);
        pagerIndicator.setupWithViewPager(slideshowVP);
        pagerIndicator.addOnPageChangeListener(getVopl());
        slideshowVP.addOnPageChangeListener(getVopl());
    }

    @NonNull
    private ViewPager.OnPageChangeListener getVopl() {
        return new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset,
                                       final int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(final int position) {

            }

            @Override
            public void onPageScrollStateChanged(final int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    handler.removeCallbacks(update);
                    handler.postDelayed(update, 5000);
                }
            }
        };
    }

    private void setupAnnouncementSlideshow() {
        annItems = database.where(AnnItem.class).findAllSorted(AnnItemKeys.DATE, Sort.DESCENDING);
        final List<String> annItemsText = new ArrayList<>();
        for (AnnItem ai : annItems) annItemsText.add(ai.getData());
        setTextData(annItemsText);
        annItems.addChangeListener((collection, changeSet) -> {
            annItemsText.clear();
            for (AnnItem ai : annItems) annItemsText.add(ai.getData());
            setTextData(annItemsText);
        });
    }

    private ValueEventListener getHomeRefVEL() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                AHC.logd(TAG, "query snapshot is " + dataSnapshot.toString());
                new Thread(() -> HomeService.saveHomeSnapshotToRealm(dataSnapshot)).start();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database access error." + databaseError.toString());
            }
        };
    }

    private ValueEventListener getAnnRefVEL() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                new Thread(() -> HomeService.saveAnnSnapshotToRealm(dataSnapshot));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Database access error." + databaseError.toString());
            }
        };
    }

    private ValueEventListener getImageSlideShowVEL() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot == null) return;
                //Delete old values
                slideshowItems.clear();
                for (final DataSnapshot cs :
                        dataSnapshot.getChildren()) {
                    if (!cs.hasChild(SlideshowItemKeys.PHOTO_URL)
                            || !cs.hasChild(SlideshowItemKeys.PHOTO_DATE)) continue;
                    final SlideshowItem ssi = new SlideshowItem();
                    ssi.setPhotoUrl(cs.child(SlideshowItemKeys.PHOTO_URL).getValue(String.class));
                    ssi.setPhotoDate(cs.child(SlideshowItemKeys.PHOTO_DATE).getValue(Date.class));
                    ssi.setPhotoTitle(cs.child(SlideshowItemKeys.PHOTO_TITLE).getValue(String.class));
                    ssi.setPhotoDesc(cs.child(SlideshowItemKeys.PHOTO_DESC).getValue(String.class));
                    ssi.setPhotoTag(cs.child(SlideshowItemKeys.PHOTO_TAG).getValue(String.class));
                    ssi.setPhotoTagColor(cs.child(SlideshowItemKeys.PHOTO_TAG_COLOR).getValue(String.class));
                    ssi.setPhotoTagTextColor(cs.child(SlideshowItemKeys.PHOTO_TAG_TEXT_COLOR).getValue(String.class));
                    slideshowItems.add(ssi);
                }
                slideshowAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                Log.e(TAG, "Error while getting slidehshow images\n" + databaseError.getDetails());
            }
        };
    }

    public void scrollToTop() {
        //App crashes on removing this check
        //TODO fix required
        if (nsv != null) nsv.scrollTo(0, 0);
    }

    public void setTextData(final List<String> data) {
        if (annVP == null) return;
        final AnnSlideshowAdapter adapter = new AnnSlideshowAdapter(data);
        if (annSlideshowHandler == null) annSlideshowHandler = new Handler();
        if (annSlideshowRunable == null) annSlideshowRunable = new Runnable() {
            @Override
            public void run() {
                if (adapter.getCount() == 0) return;
                int viewpagerpos = annVP.getCurrentItem();
                viewpagerpos++;
                viewpagerpos %= adapter.getCount();
                annVP.setCurrentItem(viewpagerpos);
                annSlideshowHandler.postDelayed(annSlideshowRunable, 2500);
            }
        };
        annVP.setAdapter(adapter);
        annSlideshowHandler.removeCallbacks(annSlideshowRunable);
        annSlideshowHandler.postDelayed(annSlideshowRunable, 2500);
    }

    @OnClick(R.id.ann_card_fragment_home)
    public void openAnnActivity() {
        startActivity(new Intent(getContext(), AnnActivity.class));
    }

    //called everytime orientation changes
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        hideAppBar();
    }

    //function to hide appbar
    private void hideAppBar() {
        final CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) nsv.getLayoutParams();

        // Checks the orientation of the screen
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.setBehavior(null);
            appBarLayout.setVisibility(View.GONE);
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            params.setBehavior(new AppBarLayout.ScrollingViewBehavior());
            appBarLayout.setVisibility(View.VISIBLE);
        }
        nsv.requestLayout();
    }
}
