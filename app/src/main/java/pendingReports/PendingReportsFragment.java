package pendingReports;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.neireport.administrator.R;

public class PendingReportsFragment extends Fragment {

    private String accountType = "", city ="";
    private String message = "Click yes to confirm";
    private String title_approve = "Do you want to approve report?";
    private String title_delete = "Do you want to delete report?";
    private int progress_bar_delay_time = 1500;
    

    private FirebaseAuth authentication;
    private FirebaseFirestore firestore;

    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private PendingReportsAdapter adapter;

    public PendingReportsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_pending_reports, container, false);

        authentication = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        
        progressBar = view.findViewById(R.id.progressBar);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        setUpRecyclerView(view);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                delayRecyclerViewSetup();
            }
        });

        return view;
    }

    private void setUpRecyclerView(final View view) {
        String userID = authentication.getUid();
        CollectionReference collection_adminAccounts = firestore.collection("Admin Accounts");
        collection_adminAccounts.document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    accountType = document.getString("Account Type");
                    city = document.getString("City");

                    Query query = firestore.collection(accountType).whereEqualTo("city", city).orderBy("timestamp", Query.Direction.DESCENDING);

                    FirestoreRecyclerOptions<PendingReports> options = new FirestoreRecyclerOptions.Builder<PendingReports>()
                            .setQuery(query, PendingReports.class)
                            .build();

                    adapter = new PendingReportsAdapter(options, getActivity(), view);

                    RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    recyclerView.setAdapter(adapter);
                    adapter.setOnItemClickListener(new PendingReportsAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(DocumentSnapshot documentSnapshot, int position) {

                        }

                        @Override
                        public void onDelete(DocumentSnapshot documentSnapshot, int position) {
                            String from = documentSnapshot.getId();
                            final DocumentReference documentReference;
                            if(accountType.equalsIgnoreCase("Fire Station")){
                                documentReference = firestore.collection("Fire Station").document(from);
                            } else if(accountType.equalsIgnoreCase("Municipality")) {
                                documentReference = firestore.collection("Fire Station").document(from);

                            } else if(accountType.equalsIgnoreCase("Police Station")){
                                documentReference = firestore.collection("Police Station").document(from);
                            } else {
                                documentReference = firestore.collection("Other").document(from);
                            }
                            delete(documentReference);
                        }

                        @Override
                        public void onApprove(DocumentSnapshot documentSnapshot, int position) {
                            String from = documentSnapshot.getId();
                            final DocumentReference fromPath;
                            final DocumentReference toPath = firestore.collection("Reports").document(from);
                            if (accountType.equalsIgnoreCase("Fire Station")){
                                fromPath = firestore.collection("Fire Station").document(from);
                            } else if (accountType.equalsIgnoreCase("Municipality")) {
                                fromPath = firestore.collection("Municipality").document(from);
                            } else if (accountType.equalsIgnoreCase("Police Station")){
                                fromPath = firestore.collection("Police Station").document(from);
                            } else {
                                fromPath = firestore.collection("Other").document(from);
                            }
                            approve(fromPath, toPath);
                            
                        }
                    });
                    adapter.startListening();
                } else {
                    String error = task.getException().getLocalizedMessage();
                    Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


    private void moveTo(final DocumentReference from, final DocumentReference to){
        from.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot!=null){
                        to.set(documentSnapshot.getData()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                from.delete();
                            }
                        });
                    }
                }
            }
        });
    }
    
    public void approve(final DocumentReference fromPath, final DocumentReference toPath) {
        new AlertDialog.Builder(getActivity())
                .setTitle(title_approve)
                .setMessage(message)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressBar.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                moveTo(fromPath,toPath);
                                Toast.makeText(getActivity(), "Successfully Approved!", Toast.LENGTH_SHORT).show();
                            }
                        }, progress_bar_delay_time);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void delete(final DocumentReference documentReference) {
        new AlertDialog.Builder(getActivity())
                .setTitle(title_delete)
                .setMessage(message)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressBar.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                    progressBar.setVisibility(View.GONE);
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getActivity(), "Successfully Deleted!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            String error = task.getException().getLocalizedMessage();
                                            Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
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

    public void delayRecyclerViewSetup() {
        int splashTime = 3000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
                setUpRecyclerView(getView());
            }
        }, splashTime); //Timeout
    }

}
