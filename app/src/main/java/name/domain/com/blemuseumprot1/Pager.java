package name.domain.com.blemuseumprot1;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;

/**
 * Created by Dezyne 2 on 10/13/2016.
 */

class Pager extends FragmentStatePagerAdapter {

    SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
    //integer to count number of tabs
    private int tabCount;

    //Constructor to the class
    public Pager(FragmentManager fm, int tabCount) {
        super(fm);
        //Initializing tab count
        this.tabCount= tabCount;
    }

    //Overriding method getItem
    @Override
    public Fragment getItem(int position) {
        //Returning the current tabs
        switch (position) {
            case 0:
                Tab_1 tab1 = new Tab_1();
                registeredFragments.put(position, tab1);
                return tab1;
            case 1:
                Tab_2 tab2 = new Tab_2();
                registeredFragments.put(position, tab2);
                return tab2;
            case 2:
                Tab_3 tab3 = new Tab_3();
                registeredFragments.put(position, tab3);
                return tab3;
            default:
                return null;
        }
    }

    //Overriden method getCount to get the number of tabs
    @Override
    public int getCount() {
        return tabCount;
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }
}