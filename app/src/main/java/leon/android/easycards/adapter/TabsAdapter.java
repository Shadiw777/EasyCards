package leon.android.easycards.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import leon.android.easycards.OfferFragment;
import leon.android.easycards.ViewCardFragment;

public class TabsAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public TabsAdapter(FragmentManager fm, int mNumOfTabs) {
        super(fm);
        this.mNumOfTabs = mNumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                ViewCardFragment viewCardFragment = new ViewCardFragment();
                return viewCardFragment;
            case 1:
                OfferFragment offerFragment = new OfferFragment();
                return offerFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
