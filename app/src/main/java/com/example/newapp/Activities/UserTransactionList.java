package com.example.newapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.newapp.Adapter.TransactionAdapter;
import com.example.newapp.DataModel.Customer;
import com.example.newapp.DataModel.Transaction;
import com.example.newapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserTransactionList extends Fragment {

    private ArrayList<String> userTransactionIds;
    private ArrayList<Transaction> transactions;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TransactionAdapter transactionAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    TransactionAdapter.OnTransactionClickListener onTransactionClickListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_user_transaction_list, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        userTransactionIds = new ArrayList<>();
        transactions = new ArrayList<>();

        progressBar = getView().findViewById(R.id.progressbar_transaction_list);
        recyclerView = getView().findViewById(R.id.recycler_transaction_list);
        swipeRefreshLayout = getView().findViewById(R.id.swip_ref_completed_trans_list);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getUserTransactions();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        getUserTransactions();


        onTransactionClickListener = new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionsClicked(int position) {
                Intent intent1 = new Intent(getActivity(), UserTransactionDetailsActivity.class);
                intent1.putExtra("transaction", transactions.get(position));
                startActivity(intent1);
            }
        };

    }


    private void getUserTransactions() {

        String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("transactions");
        databaseReference.orderByChild("userUID").equalTo(userUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        transactions.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Transaction transaction = dataSnapshot.getValue(Transaction.class);
                            if (transaction != null && transaction.isTransactionComplete()) {
                                transactions.add(transaction);
                            }
                        }
                        setAdapter(transactions);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    // Setting up the adapter to show the list of companies in the arraylist.
    private void setAdapter(ArrayList<Transaction> arrayList) {
        transactionAdapter = new TransactionAdapter(arrayList, getActivity(), onTransactionClickListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setAdapter(transactionAdapter);
        transactionAdapter.notifyDataSetChanged();

    }


}