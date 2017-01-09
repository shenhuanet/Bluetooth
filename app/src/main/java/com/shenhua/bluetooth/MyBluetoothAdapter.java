package com.shenhua.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.text.TextUtils;

import java.util.List;

/**
 * Created by shenhua on 1/9/2017.
 * Email shenhuanet@126.com
 */
public class MyBluetoothAdapter extends BaseRecyclerAdapter<BluetoothDevice> {

    public MyBluetoothAdapter(Context context, List<BluetoothDevice> datas) {
        super(context, datas);
    }

    @Override
    public int getItemViewId() {
        return R.layout.item_device;
    }

    @Override
    public void bindData(BaseRecyclerViewHolder holder, int position, BluetoothDevice item) {
        String name = item.getName();
        if (name == null || TextUtils.isEmpty(name))
            holder.setText(R.id.tv_name, item.getAddress());
        else holder.setText(R.id.tv_name, name);
    }
}
