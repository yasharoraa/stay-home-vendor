package com.stayhome.vendor.Adapters;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.stayhome.vendor.Interfaces.OrderClickListener;
import com.stayhome.vendor.Models.Order.Order;
import com.stayhome.vendor.R;
import com.stayhome.vendor.Utils.SafeClickViewHolder;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    private OrderClickListener listener;

    private List<Order> orders;


    public OrdersAdapter(OrderClickListener listener) {
        this.listener = listener;
        orders = new ArrayList<>();
    }

    public void addAll(List<Order> list) {
        orders.addAll(list);
        notifyDataSetChanged();
    }

    public List<Order> getList() {
        return orders;
    }

    public void clear(){
        orders.clear();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order,parent,false);
        return new OrderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        if (order == null) return;
        holder.newDot.setVisibility(order.getStatus() < 200 ? View.VISIBLE : View.GONE);
        holder.orderIdTextView.setText(String.format("OID-%s", order.getSlug()));
        holder.setDate(order.getPlaceDate());
        holder.distanceTextView.setText(String.format("%s km away", order.getDistance()));
        holder.setStatusTextAndDrawable(order.getStatus());
    }


    @Override
    public int getItemCount() {
        return orders.size();
    }


    @Override
    public long getItemId(int position) {
        return Long.parseLong(orders.get(position).getSlug());
    }

    class OrderViewHolder extends SafeClickViewHolder {

        @BindView(R.id.new_dot)
        ImageView newDot;

        @BindView(R.id.date_text_view)
        TextView dateTextView;

        @BindView(R.id.order_id_text_view)
        TextView orderIdTextView;

        @BindView(R.id.distance_text_view)
        TextView distanceTextView;

        @BindView(R.id.status_text_view)
        AppCompatTextView statusTextView;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void onSafeClick(View view) {
            listener.onClick(orders.get(getAdapterPosition()).getId());
        }

        private void setDate(String createdOn){
            String[] dates = createdOn.split("T");
            String[] date = dates[0].split("-");
            dateTextView.setText(String.format("%s %s %s", date[2], new DateFormatSymbols().getMonths()[Integer.parseInt(date[1]) - 1], date[0]));
        }

        private void setStatusTextAndDrawable(int status) {
            switch (status) {
                case 101 :
                   setStatusValue("Action\npending",R.drawable.ic_radio_button_checked_black_24dp,R.color.add_green);
                   break;
                case 201 :
                    setStatusValue("Action\naccepted",R.drawable.ic_check_circle_black_24dp,R.color.add_green);
                    break;
                case 202 :
                    setStatusValue("Out for\ndelivery",R.drawable.ic_local_shipping_black_24dp,R.color.add_green);
                    break;
                case 204 :
                    setStatusValue("Order\ndelivered",R.drawable.ic_shopping_basket_black_24dp,R.color.add_green);
                    break;
                case 300 :
                case 301:
                    setStatusValue("Order\ncancelled",R.drawable.ic_cancel_black_24dp,R.color.jalapino_red);
                    break;
                default:
                    setStatusValue("Status\nunknown",R.drawable.ic_radio_button_checked_black_24dp,R.color.dot);
            }

        }

        private void setStatusValue(String text, int img, int color){
            statusTextView.setText(text);
            Drawable drawable = ContextCompat.getDrawable(itemView.getContext(),img);
            assert drawable != null;
            drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(itemView.getContext(),color), PorterDuff.Mode.SRC_IN));
            statusTextView.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
        }
    }
}
