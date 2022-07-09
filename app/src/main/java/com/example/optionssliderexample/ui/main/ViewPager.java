package com.example.optionssliderexample.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.optionssliderexample.R;

import java.util.List;

//Super generic ViewPager, nothing special is done here
public class ViewPager extends Fragment {
    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;

    private List<Fragment> pageList;
    private int currPosition;

    public static ViewPager newInstance(List<Fragment> pageList, int currPosition) {
        ViewPager viewPager = new ViewPager();
        viewPager.pageList = pageList;
        viewPager.currPosition = currPosition;
        return viewPager;
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.viewpager, container, false);

        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = view.findViewById(R.id.viewpager);
        pagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager.setCurrentItem(currPosition, false);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                System.out.println("Page changed");
                currPosition = position;
            }
        });
    }




    private class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(Fragment fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            return pageList.get(position);
        }
        @Override
        public int getItemCount() {
            return (pageList == null) ? 0 : pageList.size();
        }
    }
}