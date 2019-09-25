package pendingReports;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.neireport.administrator.AccountFragment;
import com.neireport.administrator.R;
import com.neireport.administrator.ReportsLocationActivity;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class PendingReportsAdapter extends FirestoreRecyclerAdapter<PendingReports, PendingReportsAdapter.PendingReportsHolder> {

    private OnItemClickListener itemClickListener;
    private OnLongClickListener longClickListener;
    private Context context;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private View view;

    public PendingReportsAdapter(@NonNull FirestoreRecyclerOptions<PendingReports> options, Context context, View view) {
        super(options);
        this.context = context;
        this.view = view;
    }

    @Override
    protected void onBindViewHolder(@NonNull final PendingReportsHolder holder, int position, @NonNull final PendingReports model) {
        holder.text_date.setText(getFormatPostDate(model));
        holder.text_description.setText(model.getDescription());
        String imageURL = model.getImageURL();
        Picasso.get().load(imageURL).into(holder.image_post);
        String userID = model.getUserID();
        firestore.collection("Users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    String url_userProfileImage = document.getString("Profile Image Link");
                    String field_userName = document.getString("Name");
                    holder.text_username.setText(field_userName);
                    Picasso.get().load(url_userProfileImage).into(holder.image_user);
                }
            }
        });
        holder.text_location.setText(model.getLocation());
        holder.text_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, ReportsLocationActivity.class)
                        .putExtra("location", model.getLocation())
                        .putExtra("latitude", model.getLatitude())
                        .putExtra("longitude", model.getLongitude()));
            }
        });

        holder.image_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startUserProfileActivity(model);
            }
        });

        holder.text_username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startUserProfileActivity(model);
            }
        });
    }

    private void startUserProfileActivity(PendingReports model) {
        Bundle bundle = new Bundle();
        bundle.putString("userID", model.getUserID());

        AppCompatActivity activity = (AppCompatActivity) view.getContext();
        Fragment myFragment = new AccountFragment();
        myFragment.setArguments(bundle);
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, myFragment).addToBackStack(null).commit();
    }

    @NonNull
    @Override
    public PendingReportsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_pending_reports,
                parent, false);
        return new PendingReportsHolder(v);
    }

    private String getFormatPostDate(PendingReports pendingReports) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy hh:mm a", Locale.getDefault());
        String date = simpleDateFormat.format(pendingReports.getTimestamp().toDate());
        return date;
    }

    class PendingReportsHolder extends RecyclerView.ViewHolder {

        CardView cardView_post;
        TextView text_description, text_date, text_location, text_username;
        Button button_approve, button_delete;
        ImageView image_post;
        CircleImageView image_user;

        public PendingReportsHolder(View itemView) {
            super(itemView);
            button_approve = itemView.findViewById(R.id.button_approve);
            button_delete = itemView.findViewById(R.id.button_delete);
            cardView_post = itemView.findViewById(R.id.main_post);
            text_description = itemView.findViewById(R.id.text_description);
            image_post = itemView.findViewById(R.id.image_post);
            text_date = itemView.findViewById(R.id.text_date);
            image_user = itemView.findViewById(R.id.image_user);
            text_location = itemView.findViewById(R.id.text_location);
            text_username = itemView.findViewById(R.id.text_username);

            button_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                        itemClickListener.onDelete(getSnapshots().getSnapshot(position), position);
                    }
                }
            });

            button_approve.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                        itemClickListener.onApprove(getSnapshots().getSnapshot(position), position);
                    }
                }
            });

        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
        void onDelete(DocumentSnapshot documentSnapshot, int position);
        void onApprove(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface OnLongClickListener {
        void onLongClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnLongClickListener(OnLongClickListener longClickListener){ this.longClickListener = longClickListener; }
}
