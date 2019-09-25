package reports;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.neireport.administrator.R;
import com.neireport.administrator.ReportsLocationActivity;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ReportsAdapter extends FirestoreRecyclerAdapter<Reports, ReportsAdapter.ReportsHolder> {

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private String current_user = auth.getUid();
    private Context context;

    public ReportsAdapter(@NonNull FirestoreRecyclerOptions<Reports> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull final ReportsAdapter.ReportsHolder holder, final int position, @NonNull final Reports model) {
        holder.text_date.setText(getFormatPostDate(model));
        String description = model.getDescription();
        if (!description.isEmpty()) {
            holder.text_description.setVisibility(View.VISIBLE);
            holder.text_description.setText(model.getDescription());
        }
        String imageURL = model.getImageURL();
        Picasso.get().load(imageURL).into(holder.image_post);
        final String userID = model.getUserID();

        firestore.collection("Reports").document(String.valueOf(getSnapshots().get(position).getDocumentID())).collection("Likes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int count = 0;
                    for (DocumentSnapshot document : task.getResult()) {
                        count++;
                    }
                    if (count > 1) {
                        holder.text_likes.setText(count + " likes");
                    } else {
                        holder.text_likes.setText(count + " like");
                    }

                }
            }
        });

        firestore.collection("Reports").document(String.valueOf(getSnapshots().get(position).getDocumentID())).collection("Likes").document(current_user).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().exists()) {
                    holder.img_like.setImageDrawable(context.getDrawable(R.mipmap.thums_up_active));
                } else {
                    holder.img_like.setImageDrawable(context.getDrawable(R.mipmap.thums_up));
                }
            }
        });

        firestore.collection("Users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().exists()) {
                    DocumentSnapshot document = task.getResult();
                    String url_userProfileImage = document.getString("Profile Image Link");
                    String field_userName = document.getString("Name");
                    holder.text_username.setText(field_userName);
                    Picasso.get().load(url_userProfileImage).into(holder.image_user);
                } else {
                    firestore.collection("Admin Accounts").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
        holder.img_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firestore.collection("Reports").document(String.valueOf(getSnapshots().get(position).getDocumentID())).collection("Likes").document(current_user).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.getResult().exists()) {
                            firestore.collection("Reports").document(String.valueOf(getSnapshots().get(position).getDocumentID())).collection("Likes").document(current_user).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    firestore.collection("Reports").document(String.valueOf(getSnapshots().get(position).getDocumentID())).collection("Likes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {

                                                int count = 0;
                                                for (DocumentSnapshot document : task.getResult()) {
                                                    count++;
                                                }
                                                if (count > 1) {
                                                    holder.text_likes.setText(count + " likes");
                                                } else {
                                                    holder.text_likes.setText(count + " like");
                                                }
                                            }
                                        }
                                    });
                                }
                            });


                            holder.img_like.setImageDrawable(context.getDrawable(R.mipmap.thums_up));

                        } else {
                            Map<String, Object> map_likes = new HashMap<>();
                            map_likes.put("Timestamp", FieldValue.serverTimestamp());
                            firestore.collection("Reports").document(String.valueOf(getSnapshots().get(position).getDocumentID())).collection("Likes").document(current_user).set(map_likes).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    firestore.collection("Reports").document(String.valueOf(getSnapshots().get(position).getDocumentID())).collection("Likes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {

                                                int count = 0;
                                                for (DocumentSnapshot document : task.getResult()) {
                                                    count++;
                                                }
                                                if (count > 1) {
                                                    holder.text_likes.setText(count + " likes");
                                                } else {
                                                    holder.text_likes.setText(count + " like");
                                                }
                                            }
                                        }
                                    });
                                }
                            });
                            holder.img_like.setImageDrawable(context.getDrawable(R.mipmap.thums_up_active));
                        }
                    }
                });
            }
        });
    }

    @NonNull
    @Override
    public ReportsAdapter.ReportsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_reports,
                parent, false);
        return new ReportsAdapter.ReportsHolder(v);
    }

    private String getFormatPostDate(Reports reports) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy hh:mm a");
        String date = simpleDateFormat.format(reports.getTimestamp().toDate());
        return date;
    }

    class ReportsHolder extends RecyclerView.ViewHolder {

        CardView cardView_post;
        TextView text_description, text_date, text_likes, text_location, text_username;
        ImageView image_post, img_like;
        CircleImageView image_user;

        public ReportsHolder(View itemView) {
            super(itemView);
            cardView_post = itemView.findViewById(R.id.main_post);
            text_description = itemView.findViewById(R.id.text_description);
            image_post = itemView.findViewById(R.id.image_post);
            text_date = itemView.findViewById(R.id.text_date);
            img_like = itemView.findViewById(R.id.img_like);
            image_user = itemView.findViewById(R.id.image_user);
            text_likes = itemView.findViewById(R.id.text_likes);
            text_location = itemView.findViewById(R.id.text_location);
            text_username = itemView.findViewById(R.id.text_username);

        }
    }
}


