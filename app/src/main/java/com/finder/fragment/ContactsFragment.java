package com.finder.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airport.finder.R;
import com.finder.activity.BaseActivity;
import com.finder.adapter.ContactsRecyclerViewAdapter;
import com.finder.values.Airports;

import java.util.ArrayList;

import static com.finder.values.Constants.AIRPORT;

public class ContactsFragment extends Fragment {

    public final static String sTag = "contact_frag";
    private ArrayList<String> mAirportNames = new ArrayList<>();
    private ArrayList<String> mPhoneNo = new ArrayList<>();
    private ContactsFragmentInteractionListener mContactsListener;

    public ContactsFragment() {
        // Required empty public constructor
    }

    public static ContactsFragment newInstance(ArrayList<Airports> airports) {
        ContactsFragment fragment = new ContactsFragment();
        Bundle data = new Bundle();
        data.putParcelableArrayList(AIRPORT, airports);
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            ArrayList<Airports> airports
                    = getArguments().getParcelableArrayList(AIRPORT);
            if (airports != null) {
                for (Airports object : airports) {
                    mAirportNames.add(object.getAirport());
                    mPhoneNo.add(object.getPhoneNo());
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts_list, container, false);

        if (mAirportNames != null && mPhoneNo != null) {
            // Set the adapter
            if (view instanceof RecyclerView) {
                Context context = view.getContext();
                RecyclerView recyclerView = (RecyclerView) view;
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                recyclerView.setAdapter(new ContactsRecyclerViewAdapter(this.getContext()
                        , mAirportNames, mPhoneNo, mContactsListener));
            }
        } else {
            ((BaseActivity) getActivity()).mGoingToExitApp = true;
            displayDataMissingAlert(getResources().getString(R.string.dataError));
        }
        return view;
    }

    private void displayDataMissingAlert(String string) {
        ((BaseActivity) getActivity())
                .createAlert(string);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ContactsFragmentInteractionListener) {
            mContactsListener = (ContactsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ContactsFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContactsListener = null;
    }

    public interface ContactsFragmentInteractionListener {

        void makeCallFromFragment(String phoneNo);

        void moveCameraToMarker(String markerTitle);
    }
}