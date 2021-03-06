package cheerly.mybaseproject.test;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.List;

import cheerly.mybaseproject.base.BaseActivity;
import cheerly.mybaseproject.R;
import cheerly.mybaseproject.common.MyFragmentViewPagerAdapter;
import cheerly.mybaseproject.widget.MyTabLayout;

/**
 * Created by chenglin on 2017-9-14.
 */

public class TestTabLayoutActivity extends BaseActivity {
    private ViewPager mViewPager;
    private MyFragmentViewPagerAdapter mFragmentAdapter;
    private List<Fragment> mFragList = new ArrayList<>();
    private MyTabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentLayout(R.layout.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mTabLayout = (MyTabLayout) findViewById(R.id.tab_layout);

        for (int i = 0; i < 4; i++) {
            TestFragment fragment = new TestFragment();
            Bundle bundle = new Bundle();
            bundle.putString("index",i + "");
            fragment.setArguments(bundle);
            mFragList.add(fragment);
        }
        mFragmentAdapter = new MyFragmentViewPagerAdapter(getSupportFragmentManager(),mFragList);
        for (int i = 0; i < 4; i++) {
            mFragmentAdapter.mTitleList.add("标题");
        }
        mViewPager.setAdapter(mFragmentAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.initCustomTabItem(mViewPager);
    }
}
