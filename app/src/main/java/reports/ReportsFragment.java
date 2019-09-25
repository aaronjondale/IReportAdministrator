package reports;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.neireport.administrator.R;



/**
 * A simple {@link Fragment} subclass.
 */
public class ReportsFragment extends Fragment {


    private ReportsAdapter adapter;

    private SwipeRefreshLayout swipeRefreshLayout;
    
    private FirebaseFirestore firestore;
    
    public ReportsFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_reports, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
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
        int splashTime = 3000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
                adapter.startListening();
            }
        }, splashTime); //Timeout
    }

    private void setUpRecyclerView(final View view) {
        Query query = firestore.collection("Reports").orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Reports> options = new FirestoreRecyclerOptions.Builder<Reports>()
                .setQuery(query, Reports.class)
                .build();

        adapter = new ReportsAdapter(options, getActivity());

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
