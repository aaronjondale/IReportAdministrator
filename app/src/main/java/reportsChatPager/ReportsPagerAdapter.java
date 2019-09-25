package reportsChatPager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import approvedReports.ApprovedReportsFragment;
import myReports.MyReportsFragment;
import pendingReports.PendingReportsFragment;


class ReportsPagerAdapter extends FragmentPagerAdapter {

    public ReportsPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                PendingReportsFragment fragment_pendingReports = new PendingReportsFragment();
                return fragment_pendingReports;
            case 1:
                ApprovedReportsFragment fragment_approvedReports = new ApprovedReportsFragment();
                return fragment_approvedReports;
            case 2:
                MyReportsFragment fragment_myReports = new MyReportsFragment();
                return fragment_myReports;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "PENDING";
            case 1:
                return "APPROVED";
            case 2:
                return "MY REPORTS";
            default:
                return null;
        }
    }
}
