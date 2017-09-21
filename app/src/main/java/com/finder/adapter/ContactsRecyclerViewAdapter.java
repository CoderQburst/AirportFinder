package com.finder.adapter;

import android.content.Context;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airport.finder.R;
import com.finder.activity.BaseActivity;
import com.finder.fragment.ContactsFragment.ContactsFragmentInteractionListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.finder.values.Constants.ON_CLICK_DELAY;

public class ContactsRecyclerViewAdapter extends
        RecyclerView.Adapter<ContactsRecyclerViewAdapter.ViewHolder> {

    private final ArrayList<String> mAirports;
    private final ArrayList<String> mPhoneNo;
    private final ContactsFragmentInteractionListener mContactsListener;
    private Context mContext;
    private long mLastClickTime;

    public ContactsRecyclerViewAdapter(Context context, ArrayList<String> airports
            , ArrayList<String> phoneNo, ContactsFragmentInteractionListener contactsListener) {
        this.mAirports = airports;
        this.mContext = context;
        this.mPhoneNo = phoneNo;
        this.mContactsListener = contactsListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_contacts, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        if (mAirports.get(position) != null && mPhoneNo.get(position) != null) {
            holder.airplaneImage.setImageResource(R.drawable.plane_icon);
            holder.airportNameTextField.setText(mAirports.get(position));
            holder.phoneNoTextField.setText(mPhoneNo.get(position));
            callOnPress(holder, position);
            moveToMarkerLocation(holder, position);
        } else {
            ((BaseActivity) mContext)
                    .createAlert(mContext.getResources().getString(R.string.dataMissing));
            holder.viewHolder.setVisibility(View.GONE);
        }
    }

    private void moveToMarkerLocation(ViewHolder holder, final int position) {
        holder.viewHolder.setOnClickListener((View v) -> {
            if (null != mContactsListener) {
                // Adds a delay of two seconds between each item click
                if (SystemClock.elapsedRealtime() - mLastClickTime > ON_CLICK_DELAY) {
                    mLastClickTime = SystemClock.elapsedRealtime();
                    mContactsListener.moveCameraToMarker(mAirports.get(position));
                }
            }
        });
    }

    private void callOnPress(final ViewHolder holder, final int position) {
        holder.phoneNoTextField.setOnClickListener((View v) -> {
            if (null != mContactsListener) {
                // Adds a delay of two seconds between each item click
                if (SystemClock.elapsedRealtime() - mLastClickTime > ON_CLICK_DELAY) {
                    mLastClickTime = SystemClock.elapsedRealtime();
                    mContactsListener.makeCallFromFragment(mPhoneNo.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mAirports.size();
    }

    @SuppressWarnings("WeakerAccess")
    protected class ViewHolder extends RecyclerView.ViewHolder {
        private final View viewHolder;
        private final CircleImageView airplaneImage;
        private final TextView airportNameTextField;
        private final TextView phoneNoTextField;

        private ViewHolder(View view) {
            super(view);
            viewHolder = view;
            airplaneImage = viewHolder.findViewById(R.id.imageview_contacts);
            airportNameTextField = viewHolder.findViewById(R.id.textview_contacts_airportname);
            phoneNoTextField = viewHolder.findViewById(R.id.textView_contacts_phoneNo);
        }
    }
}