package com.example.newapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newapp.Adapter.CompanyAdapter;
import com.example.newapp.DataModel.Company;
import com.example.newapp.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UnauthorisedCompanyList extends Fragment {

    private ArrayList<Company> companyArrayList;
    private SearchView searchCompany;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private CompanyAdapter companyAdapter;
    private String loginMode;
    private SwipeRefreshLayout swipeRefreshLayout;
    CompanyAdapter.OnCompanyClickListener onCompanyClickListener;
    private TextView no_auth_tv;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_unauthorised_company_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FirebaseApp.initializeApp(getActivity());

        searchCompany = getView().findViewById(R.id.srchCompany_unauthorized);

        companyArrayList = new ArrayList<>();

        progressBar = getView().findViewById(R.id.progressbar_unauthorized);
        recyclerView = getView().findViewById(R.id.recycler_unauthorized);
        swipeRefreshLayout = getView().findViewById(R.id.swip_ref_unauthorised_list);
        no_auth_tv = getView().findViewById(R.id.no_auth_tv);

        Intent intent1 = getActivity().getIntent();
        loginMode = intent1.getStringExtra("loginMode");

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getUnauthorizedCompanies(searchCompany.getQuery().toString());
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        getUnauthorizedCompanies(searchCompany.getQuery().toString());


        onCompanyClickListener = new CompanyAdapter.OnCompanyClickListener() {
            @Override
            public void onCompaniesClicked(int position) {
                Intent intent = new Intent(getActivity(), CompanyDetailsActivity.class);
                intent.putExtra("loginMode", loginMode);
                intent.putExtra("companyID", companyArrayList.get(position).getCompanyId());
                intent.putExtra("company_name", companyArrayList.get(position).getName());
                intent.putExtra("company_desc", companyArrayList.get(position).getDescription());
                intent.putExtra("company_img", companyArrayList.get(position).getImageUrl());
                intent.putExtra("company_license", companyArrayList.get(position).getLicenseUrl());
                intent.putExtra("isAuthorised", companyArrayList.get(position).getOperational());
                startActivity(intent);
            }
        };

        // fetch the companies and set them to adapter based upon query in searchView
        searchCompany.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                getUnauthorizedCompanies(newText);
                return false;
            }
        });

    }

    // fetch the unauthorized companies and set them to adapter based upon query in searchView
    private void getUnauthorizedCompanies(String userQuery) {
        companyArrayList.clear();
        try {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("company");
            Query query = databaseReference;
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    companyArrayList.clear();
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot companySnapShot : dataSnapshot.getChildren()) {
                            Company company = companySnapShot.getValue(Company.class);
                            if (company != null) {
                                if (!company.getLicenseUrl().isEmpty() &&
                                        !company.getOperational() &&
                                        company.getName().toLowerCase().contains(userQuery.toLowerCase())) {
                                    companyArrayList.add(company);
                                }
                            }
                        }
                    }
                    setAdapter(companyArrayList);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getActivity(), databaseError.getMessage().toString(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Slow Internet Connection", Toast.LENGTH_SHORT).show();
        }
    }


    // Setting up the adapter to show the list of companies in the arraylist.
    private void setAdapter(ArrayList<Company> arrayList) {
        if(arrayList.size()==0){
            no_auth_tv.setVisibility(View.VISIBLE);
        }
        else{
            no_auth_tv.setVisibility(View.GONE);
        }
        companyAdapter = new CompanyAdapter(arrayList, getActivity(), onCompanyClickListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setAdapter(companyAdapter);
        companyAdapter.notifyDataSetChanged();

    }

}