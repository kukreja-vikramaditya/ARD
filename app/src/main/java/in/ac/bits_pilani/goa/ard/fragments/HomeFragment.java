package in.ac.bits_pilani.goa.ard.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.ac.bits_pilani.goa.ard.R;
import in.ac.bits_pilani.goa.ard.utils.AHC;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * @author Vikramadtiya Kukreja
 */
public class HomeFragment extends Fragment {

    /**
     * This is the key that is passed while returning a fragment.
     */
    private static final String FRAGMENT_TITLE_KEY = AHC.FRAGMENT_TITLE_KEY;

    /**
     * Tag for this fragment.
     */
    private final String TAG = AHC.TAG + ".fragments.Home";

    /**
     * Fragment title to be used.
     */
    private String fragmentTitle;

    /**
     * Used to communicate with activity.
     */
    private OnFragmentInteractionListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param fragmentTitle Name of the Fragment.
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance(final String fragmentTitle) {
        final HomeFragment fragment = new HomeFragment();
        final Bundle args = new Bundle();
        args.putString(FRAGMENT_TITLE_KEY, fragmentTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fragmentTitle = getArguments().getString(FRAGMENT_TITLE_KEY);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mListener.onFragmentInteraction(AHC.HOME_FID);
        Log.d(TAG, fragmentTitle);
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Custom fragment event listener.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(int type);
    }
}
