package vn.techres.line.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.AppUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import vn.techres.line.data.model.BranchGift;
import vn.techres.line.databinding.ItemBranchSpinnerBinding;
import vn.techres.line.databinding.ItemBranchSpinnerDropBinding;
import vn.techres.line.helper.Utils;

public class BranchSpinnerAdapter extends ArrayAdapter<vn.techres.line.data.model.BranchGift>{

    public BranchSpinnerAdapter(@NonNull Context context, int resource, @NonNull List<BranchGift> objects) {
        super(context, resource, objects);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @NotNull
    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, @NotNull ViewGroup parent) {
        ItemBranchSpinnerBinding binding = ItemBranchSpinnerBinding.inflate(LayoutInflater.from(getContext()));
        binding.txtResName.setText(Objects.requireNonNull(this.getItem(position)).getName());
        binding.txtResAddress.setText(Objects.requireNonNull(this.getItem(position)).getDescription());
        binding.txtResAddress.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        binding.txtResAddress.setMarqueeRepeatLimit(-1);
        binding.txtResAddress.setSelected(true);

        binding.txtResName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        binding.txtResName.setMarqueeRepeatLimit(-1);
        binding.txtResName.setSelected(true);

        return binding.getRoot();
    }

    @Override
    public View getDropDownView(int position, View convertView, @NotNull ViewGroup parent) {
        ItemBranchSpinnerDropBinding binding = ItemBranchSpinnerDropBinding.inflate(LayoutInflater.from(getContext()));
        binding.txtResName.setText(Objects.requireNonNull(this.getItem(position)).getName());
        binding.txtResAddress.setText(Objects.requireNonNull(this.getItem(position)).getDescription());

        binding.txtResAddress.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        binding.txtResAddress.setMarqueeRepeatLimit(-1);
        binding.txtResAddress.setSelected(true);

        binding.txtResName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        binding.txtResName.setMarqueeRepeatLimit(-1);
        binding.txtResName.setSelected(true);

        return binding.getRoot();
    }
}
