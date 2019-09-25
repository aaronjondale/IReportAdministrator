package approvedReports;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.neireport.administrator.R;

import reports.Reports;

/**
 * A simple {@link Fragment} subclass.
 */
public class ApprovedReportsFragment extends Fragment {


    private String accountType = "", city ="";

    private FirebaseAuth authentication;
    private FirebaseFirestore firestore;

    private SwipeRefreshLayout swipeRefreshLayout;

    private ApprovedReportsAdapter adapter;

    public ApprovedReportsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_reports, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        authentication = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        setUpRecyclerView(view);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                delayRecyclerViewSetup();
            }
        });

        return view;
    }

    public void delayRecyclerViewSetup() {
        int delayTime = 3000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
                adapter.startListening();
            }
        }, delayTime);
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
                    Query query = firestore.collection("Reports").whereEqualTo("city", city).whereEqualTo("category", accountType).orderBy("timestamp", Query.Direction.DESCENDING);

                    FirestoreRecyclerOptions<ApprovedReports> options = new FirestoreRecyclerOptions.Builder<ApprovedReports>()
                            .setQuery(query, ApprovedReports.class)
                            .build();

                    adapter = new ApprovedReportsAdapter(options, getActivity(), view);

                    RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    recyclerView.setAdapter(adapter);
                    adapter.startListening();
                }
            }
        });
    }
}
