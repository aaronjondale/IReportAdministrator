package myReports;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.neireport.administrator.AccountFragment;
import com.neireport.administrator.EditPostActivity;
import com.neireport.administrator.R;
import com.neireport.administrator.ReportsLocationActivity;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import de.hdodenhof.circleimageview.CircleImageView;
import pendingReports.PendingReportsAdapter;

public class MyReportsAdapter extends FirestoreRecyclerAdapter<MyReports, MyReportsAdapter.MyReportsHolder> {

    private Context context;
    private View view;
    private String message = "Click yes to confirm";
    private String title_approve = "Do you want to approve report?";
    private String title_delete = "Do you want to delete report?";
    private int progress_bar_delay_time = 1500;

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public MyReportsAdapter(@NonNull FirestoreRecyclerOptions<MyReports> options, Context context, View view) {
        super(options);
        this.context = context;
        this.view = view;
    }

    @Override
    protected void onBindViewHolder(@NonNull final MyReportsAdapter.MyReportsHolder holder, int position, @NonNull final MyReports model) {
        holder.text_date.setText(getFormatPostDate(model));
        String description = model.getDescription();
        if (!description.isEmpty()) {
            holder.text_description.setVisibility(View.VISIBLE);
            holder.text_description.setText(model.getDescription());
        }
        String imageURL = model.getImageURL();
        Picasso.get().load(imageURL).into(holder.image_post);
        final String userID = model.getUserID();

        firestore.collection("Admin Accounts").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                String url_userProfileImage = document.getString("Profile Image Link");
                String field_userName = document.getString("Name");
                holder.text_username.setText(field_userName);
                Picasso.get().load(url_userProfileImage).into(holder.image_user);
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



    private void startUserProfileActivity(MyReports model) {
        Bundle bundle = new Bundle();
        bundle.putString("userID", model.getUserID());

        AppCompatActivity activity = (AppCompatActivity) view.getContext();
        Fragment myFragment = new AccountFragment();
        myFragment.setArguments(bundle);
        activity.getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, myFragment).addToBackStack(null).commit();
    }

    @NonNull
    @Override
    public MyReportsAdapter.MyReportsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_reports_more,
                parent, false);
        return new MyReportsAdapter.MyReportsHolder(v);
    }

    private String getFormatPostDate(MyReports reports) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy hh:mm a");
        String date = simpleDateFormat.format(reports.getTimestamp().toDate());
        return date;
    }

    class MyReportsHolder extends RecyclerView.ViewHolder {

        CardView cardView_post;
        TextView text_description, text_date, text_location, text_username;
        ImageButton btn_more;
        ImageView image_post;
        CircleImageView image_user;

        public MyReportsHolder(View itemView) {
            super(itemView);
            cardView_post = itemView.findViewById(R.id.main_post);
            text_description = itemView.findViewById(R.id.text_description);
            image_post = itemView.findViewById(R.id.image_post);
            btn_more = itemView.findViewById(R.id.btn_more);
            text_date = itemView.findViewById(R.id.text_date);
            image_user = itemView.findViewById(R.id.image_user);
            text_location = itemView.findViewById(R.id.text_location);
            text_username = itemView.findViewById(R.id.text_username);

            btn_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        DocumentSnapshot documentSnapshot = getSnapshots().getSnapshot(position);
                        showMenu(v, documentSnapshot, position);
                    }
                }
            });
        }


    }

    public void showMenu(View v, DocumentSnapshot documentSnapshot, int position) {
        String from = documentSnapshot.getId();
        final DocumentReference documentReference = firestore.collection("Reports").document(from);
        PopupMenu popup = new PopupMenu(context, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.edit:
                        edit(documentReference);
                        return true;
                    case R.id.delete:
                        delete(documentReference);
                        return true;
                    default:
                        return false;
                }
            }
        });
        popup.inflate(R.menu.report_more);
        popup.show();
    }

    public void edit(final DocumentReference documentReference) {
        String document_id = documentReference.getId();
        context.startActivity(new Intent(context, EditPostActivity.class).putExtra("document", document_id));
    }

    public void delete(final DocumentReference documentReference) {
        new AlertDialog.Builder(context)
                .setTitle(title_delete)
                .setMessage(message)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(context, "Successfully Deleted!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            String error = task.getException().getLocalizedMessage();
                                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }, progress_bar_delay_time);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
    
}


