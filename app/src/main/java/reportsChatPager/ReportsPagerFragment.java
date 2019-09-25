package reportsChatPager;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.neireport.administrator.MainActivity;
import com.neireport.administrator.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReportsPagerFragment extends Fragment {

    private ViewPager viewPager;
    private ReportsPagerAdapter adapter_reportsPager;
    private TabLayout tabLayout;

    public ReportsPagerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reports_pager, container, false);

        viewPager = view.findViewById(R.id.viewPager);
        adapter_reportsPager = new ReportsPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(adapter_reportsPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
        return view;
    }

}
