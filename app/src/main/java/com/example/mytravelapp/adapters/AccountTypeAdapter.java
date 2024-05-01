package com.example.mytravelapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mytravelapp.databinding.ActivityRegisterBinding;

import java.util.List;

public class AccountTypeAdapter extends ArrayAdapter<String> {

    private final LayoutInflater inflater;
    private final List<String> accountTypes;

    public AccountTypeAdapter(@NonNull Context context, int resource, @NonNull List<String> accountTypes) {
        super(context, resource, accountTypes);
        inflater = LayoutInflater.from(context);
        this.accountTypes = accountTypes;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ActivityRegisterBinding binding;

        if (convertView == null) {
            binding = ActivityRegisterBinding.inflate(inflater, parent, false);
            convertView = binding.getRoot();
            convertView.setTag(binding);
        } else {
            binding = (ActivityRegisterBinding) convertView.getTag();
        }

        String accountType = accountTypes.get(position);
        binding.accountTypeSpinner.setSelection(accountTypes.indexOf(accountType));

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}

