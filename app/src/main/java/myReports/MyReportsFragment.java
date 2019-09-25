package myReports;


import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.neireport.administrator.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyReportsFragment extends Fragment {

    private FirebaseAuth authentication;
    private FirebaseFirestore firestore;

    private SwipeRefreshLayout swipeRefreshLayout;

    private MyReportsAdapter adapter;


    private ProgressBar progressBar;

    public MyReportsFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_reports, container, false);

        progressBar = view.findViewById(R.id.progressBar);
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
        Query query = firestore.collection("Reports").whereEqualTo("userID", userID).orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<MyReports> options = new FirestoreRecyclerOptions.Builder<MyReports>()
                .setQuery(query, MyReports.class)
                .build();

        adapter = new MyReportsAdapter(options, getActivity(), view);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }





}
