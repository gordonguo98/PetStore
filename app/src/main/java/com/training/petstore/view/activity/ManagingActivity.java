package com.training.petstore.view.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.jaeger.library.StatusBarUtil;
import com.training.petstore.R;
import com.training.petstore.view.fragment.ManageUserFragment;
import com.training.petstore.view.fragment.DealWithReversalFragment;

import java.util.ArrayList;
import java.util.List;

public class ManagingActivity extends AppCompatActivity {

    private TabLayout fragmentTab;
    private ViewPager fragmentViewPager;
    private Toolbar toolbar;

    private List<Fragment> fragments = new ArrayList<>();
    List<String> titles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managing);

        initView();

    }

    public void initView(){

        StatusBarUtil.setColor(this, getResources().getColor(R.color.colorPrimaryDark));
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        fragmentTab = findViewById(R.id.activity_managing_tab_layout);
        fragmentViewPager = findViewById(R.id.activity_managing_view_pager);
        toolbar = findViewById(R.id.activity_managing_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        fragments.add(ManageUserFragment.newInstance());
        fragments.add(new DealWithReversalFragment());

        titles.add("用户");
        titles.add("仲裁");

        fragmentViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return fragments.size();
            }

            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                super.destroyItem(container, position, object);
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return titles.get(position);
            }
        });

        fragmentTab.setupWithViewPager(fragmentViewPager);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fragments.get(0).onActivityResult(requestCode, resultCode, data);
    }
}