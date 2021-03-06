package com.verisence.zoackadventures.UI;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.ramotion.foldingcell.FoldingCell;
import com.squareup.picasso.Picasso;
import com.verisence.zoackadventures.Mpesa.ApiClient;
import com.verisence.zoackadventures.Mpesa.model.AccessToken;
import com.verisence.zoackadventures.Mpesa.model.STKPush;
import com.verisence.zoackadventures.Mpesa.utils;
import com.verisence.zoackadventures.R;
import com.verisence.zoackadventures.models.Hotel;

import org.parceler.Parcels;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.google.firebase.storage.FirebaseStorage.getInstance;
import static com.verisence.zoackadventures.Mpesa.AppConstants.BUSINESS_SHORT_CODE;
import static com.verisence.zoackadventures.Mpesa.AppConstants.CALLBACKURL;
import static com.verisence.zoackadventures.Mpesa.AppConstants.PARTYB;
import static com.verisence.zoackadventures.Mpesa.AppConstants.PASSKEY;
import static com.verisence.zoackadventures.Mpesa.AppConstants.TRANSACTION_TYPE;

public class HotelFragment extends Fragment implements View.OnClickListener {
    @BindView(R.id.hotelImageView)
    ImageView mImageLabel;
    @BindView(R.id.hotelNameTextView)
    TextView mNameLabel;
    @BindView(R.id.hotelDescTextView)
    TextView mHotelDescription;
    @BindView(R.id.phoneTextView)
    TextView mHotelPhone;
    @BindView(R.id.addressTextView)
    TextView mHotelAddress;
    @BindView(R.id.hotelPrice)
    TextView mHotelPrice;
    @BindView(R.id.bookHotel)
    Button mBookHotel;



    private Hotel mHotel;

    public static TextView fromdate;
    public static TextView todate;
    public static TextView top_text;
    public static TextView top_text_two;
    public static Button startBtn;
    public static Button endBtn;
    public static Button adult_add_btn;
    public static Button adult_minus_btn;
    public static TextView adult_count;
    public static Button child_add_btn;
    public static Button child_minus_btn;
    public static TextView child_count;
    public static TextView travelers_count;
    public static TextView totalPrice;
    public static final String DATE_FORMAT = "d/M/yyyy";
    public static Button payBtn;
    private ApiClient mApiClient;
    private ProgressDialog mProgressDialog;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    String phoneNumber;

    public static Fragment newInstance(Hotel hotel) {
        // Required empty public constructor
        HotelFragment hotelFragment = new HotelFragment();
        Bundle args = new Bundle();
        args.putParcelable("hotel", Parcels.wrap(hotel));
        hotelFragment.setArguments(args);
        return hotelFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHotel = Parcels.unwrap(getArguments().getParcelable("hotel"));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hotel_list, container, false);
        ButterKnife.bind(this, view);
        Picasso.get().load(mHotel.getImageUrl()).into(mImageLabel);
        mNameLabel.setText(mHotel.getName());
        mHotelPrice.setText("Kshs. "+String.valueOf(mHotel.getPrice()));
        mHotelDescription.setText(mHotel.getDescription());
        mHotelPhone.setText(mHotel.getPhone());
        mHotelAddress.setText(mHotel.getAddress());
        mBookHotel.setOnClickListener(this);

        mHotelPhone.setOnClickListener(this);
        mHotelAddress.setOnClickListener(this);


        return view;
    }

    @Override
    public void onClick(View v) {

//        if (v == mViewHotels) {
//            Intent i = new Intent(getActivity(), HotelsActivity.class);
//            startActivity(i);
//            ((Activity) getActivity()).overridePendingTransition(0, 0);
//        }
        if (v==mHotelAddress){
            Double lat = mHotel.getLatitude();
            Double longitude = mHotel.getLongitude();
            Uri gmmIntentUri = Uri.parse("geo:" + lat
                    + "," + longitude
                    + "?q=(" + mHotel.getName() + ")");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        }

        if (v == mHotelPhone) {
            Intent phoneIntent = new Intent(Intent.ACTION_DIAL,
                    Uri.parse("tel:" + mHotel.getPhone()));
            startActivity(phoneIntent);
        }

        if(v == mBookHotel){
//            Toast.makeText(getContext(),String.valueOf(mHotel.getPrice()),Toast.LENGTH_LONG).show();
            final Dialog dialog = new Dialog(getContext());
            dialog.setContentView(R.layout.book_card);
            top_text = (TextView) dialog.findViewById(R.id.top_text);
            top_text_two = (TextView) dialog.findViewById(R.id.top_text_two);
            startBtn = (Button) dialog.findViewById(R.id.start_date_btn);
            endBtn = (Button) dialog.findViewById(R.id.end_date_btn);
            fromdate = (TextView) dialog.findViewById(R.id.fromDate);
            todate = (TextView) dialog.findViewById(R.id.toDate);
            adult_add_btn = (Button)dialog.findViewById(R.id.adult_add_btn);
            adult_minus_btn = (Button) dialog.findViewById(R.id.adult_minus_btn);
            adult_count = (TextView) dialog.findViewById(R.id.adult_count);
            child_add_btn = (Button) dialog.findViewById(R.id.child_add_btn);
            child_minus_btn = (Button) dialog.findViewById(R.id.child_minus_btn);
            child_count = (TextView) dialog.findViewById(R.id.child_count);
            travelers_count = (TextView) dialog.findViewById(R.id.traveler_count);
            totalPrice = (TextView) dialog.findViewById(R.id.total_price);
            payBtn = (Button) dialog.findViewById(R.id.payBtn);
            final long[] price = {0};
            final int[] childNumber = {0};
            final int[] adultNumber = {0};
            NumberFormat myFormat = NumberFormat.getInstance();
            myFormat.setGroupingUsed(true);
            mProgressDialog = new ProgressDialog(getContext());
            mApiClient = new ApiClient();
            mApiClient.setIsDebug(true); //Set True to enable logging, false to disable.

            getAccessToken();

            firebaseAuth = FirebaseAuth.getInstance();
            user = firebaseAuth.getCurrentUser();
            firebaseDatabase = FirebaseDatabase.getInstance();
            databaseReference = firebaseDatabase.getReference("Users");
            storageReference = getInstance().getReference();

            Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot ds : dataSnapshot.getChildren()){

                        phoneNumber = "" + ds.child("phone").getValue();

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            startBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showTruitonDatePickerDialog(view);
                }
            });
            endBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showToDatePickerDialog(view);
                }
            });
            final FoldingCell fc = (FoldingCell) dialog.findViewById(R.id.folding_cell);
            final FoldingCell fc2 = (FoldingCell) dialog.findViewById(R.id.folding_cell_two);

            fc.initialize(500, Color.TRANSPARENT, 2);
            fc2.initialize(500, Color.TRANSPARENT, 2);

            fc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fc.toggle(false);
                }
            });

            fc2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fc2.toggle(false);

                    if(adultNumber[0] > 0 && childNumber[0] > 0){
                        String children = childNumber[0] > 1 ? String.valueOf(childNumber[0]) + " children": String.valueOf(childNumber[0]) + " child";
                        String adult = adultNumber[0] > 1 ? String.valueOf(adultNumber[0]) + " adults" : String.valueOf(adultNumber[0]) + " adult";
                        travelers_count.setText(adult +" and "+ children);
                    }else if(adultNumber[0] > 0 && childNumber[0] == 0){
                        String adult = adultNumber[0] > 1 ? String.valueOf(adultNumber[0]) + " adults" : String.valueOf(adultNumber[0]) + " adult";
                        travelers_count.setText(adult);
                    }
                }
            });

            adult_add_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    adultNumber[0] = adultNumber[0] +1;
                    adult_count.setText(String.valueOf(adultNumber[0]));
                }
            });
            adult_minus_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    adultNumber[0] = adultNumber[0] -1;
                    if (adultNumber[0] > 0) {
                        adult_count.setText(String.valueOf(adultNumber[0]));
                    } else {
                        adultNumber[0] = 0;
                        adult_count.setText(String.valueOf(adultNumber[0]));
                    }
                }
            });

            child_add_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    childNumber[0] = childNumber[0] +1;
                    child_count.setText(String.valueOf(childNumber[0]));
                }
            });
            child_minus_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    childNumber[0] = childNumber[0] -1;
                    if (childNumber[0] > 0) {
                        child_count.setText(String.valueOf(childNumber[0]));
                    } else {
                        childNumber[0] = 0;
                        child_count.setText(String.valueOf(childNumber[0]));
                    }


                }
            });
            totalPrice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (getDaysBetweenDates(fromdate.getText().toString(), todate.getText().toString()) > 0 && (childNumber[0] > 0 || adultNumber[0] > 0)) {
                        price[0] = getDaysBetweenDates(fromdate.getText().toString(), todate.getText().toString()) * mHotel.getPrice() * adultNumber[0];
                        totalPrice.setText("Ksh. " + String.valueOf(myFormat.format(price[0])));
                    }
                }
            });

            payBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //need to get the proper phone number
                    performSTKPush(phoneNumber,String.valueOf(price[0]));
                }
            });
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded);
            dialog.show();


        }
    }
    public void performSTKPush(String phone_number, String amount) {
        mProgressDialog.setMessage("Sending Mpesa payment request to " + phone_number);
        mProgressDialog.setTitle("Please Wait...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        String timestamp = utils.getTimestamp();
        STKPush stkPush = new STKPush(
                BUSINESS_SHORT_CODE,
                utils.getPassword(BUSINESS_SHORT_CODE, PASSKEY, timestamp),
                timestamp,
                TRANSACTION_TYPE,
                String.valueOf(amount),
                utils.sanitizePhoneNumber(phone_number),
                PARTYB,
                utils.sanitizePhoneNumber(phone_number),
                CALLBACKURL,
                "test", //The account reference
                "test"  //The transaction description
        );

        mApiClient.setGetAccessToken(false);

        mApiClient.mpesaService().sendPush(stkPush).enqueue(new Callback<STKPush>() {
            @Override
            public void onResponse(@NonNull Call<STKPush> call, @NonNull Response<STKPush> response) {
                mProgressDialog.dismiss();
                try {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(),"Payment successful",Toast.LENGTH_LONG).show();
                        Timber.d("post submitted to API. %s", response.body());
                    } else {
                        Toast.makeText(getContext(),"Failed to process payment",Toast.LENGTH_LONG).show();
                        Timber.e("Response %s", response.errorBody().string());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(@NonNull Call<STKPush> call, @NonNull Throwable t) {
                mProgressDialog.dismiss();
                Toast.makeText(getContext(),"Failed to process payment",Toast.LENGTH_LONG).show();
                Timber.e(t);
            }
        });
    }
    public static long getDaysBetweenDates(String start, String end) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        Date startDate, endDate;
        long numberOfDays = 0;
        try {
            startDate = dateFormat.parse(start);
            endDate = dateFormat.parse(end);
            numberOfDays = getUnitBetweenDates(startDate, endDate, TimeUnit.DAYS);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return numberOfDays;
    }
    private static long getUnitBetweenDates(Date startDate, Date endDate, TimeUnit unit) {
        long timeDiff = endDate.getTime() - startDate.getTime();
        return unit.convert(timeDiff, TimeUnit.MILLISECONDS);
    }
    public void showTruitonDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void showToDatePickerDialog(View v) {
        DialogFragment newFragment = new ToDatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog;
            datePickerDialog = new DatePickerDialog(getActivity(),this, year,
                    month,day);
            return datePickerDialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            fromdate.setTextColor(Color.WHITE);
            fromdate.setTextSize(20);
            fromdate.setText(day + "/" + month  + "/" + year);
        }

    }

    public static class ToDatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {
        // Calendar startDateCalendar=Calendar.getInstance();
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            String getfromdate = fromdate.getText().toString().trim();
            String getfrom[] = getfromdate.split("/");
            int year, month, day;
            year = Integer.parseInt(getfrom[2]);
            month = Integer.parseInt(getfrom[1]);
            day = Integer.parseInt(getfrom[0]);
            final Calendar c = Calendar.getInstance();
            c.set(year, month, day + 1);
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
            datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
            return datePickerDialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {


            todate.setText(day + "/" + month + "/" + year);
            todate.setTextSize(20);
            todate.setTextColor(Color.WHITE);

            long Days = getDaysBetweenDates(fromdate.getText().toString(),todate.getText().toString());
            String DaysString = String.valueOf(Days);
            top_text.setText(DaysString + " Days");
            top_text_two.setText(fromdate.getText().toString() + " - " + todate.getText().toString());


        }
    }
    public void getAccessToken() {
        mApiClient.setGetAccessToken(true);
        mApiClient.mpesaService().getAccessToken().enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(@NonNull Call<AccessToken> call, @NonNull Response<AccessToken> response) {

                if (response.isSuccessful()) {
                    mApiClient.setAuthToken(response.body().accessToken);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AccessToken> call, @NonNull Throwable t) {

            }
        });
    }

}
