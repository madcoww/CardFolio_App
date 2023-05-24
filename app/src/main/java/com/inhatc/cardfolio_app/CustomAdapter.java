package com.inhatc.cardfolio_app;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {
    private ArrayList<Card> mList;
    private String cardYype; // 내명함인지(my), 명함집인지(other)
    private Context context;

    public CustomAdapter(ArrayList<Card> list, Context context, String cardYype) {
        this.mList = list;
        this.cardYype = cardYype;
        this.context = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recycle_card_item, viewGroup, false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder viewholder, int position) {
        viewholder.uname1.setText(mList.get(position).getCard_uname());
        if(mList.get(position).getIs_default() == 0){
            viewholder.default_card.setVisibility(View.INVISIBLE);
        }
        viewholder.cname.setText(mList.get(position).getCard_cname());
        viewholder.team.setText(mList.get(position).getCard_team());
        viewholder.rank.setText(mList.get(position).getCard_rank());
        viewholder.pnum.setText("T. " + mList.get(position).getCard_pnum());
        viewholder.email.setText("E-mail. " + mList.get(position).getCard_email());
        viewholder.c_id.setText(mList.get(position).getC_id());
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected LinearLayout id;
        protected TextView uname1, c_id;
        protected TextView default_card;   // 대표명함 여부
        protected ImageView logo_img, imageView;     // 로고, 메뉴 버튼
        protected TextView cname, team, rank, pnum, email;          // 이메일
        private PopupMenu popupMenu;

        public CustomViewHolder(View view) {
            super(view);
            this.id = (LinearLayout) view.findViewById(R.id.recycle_card_item);
            this.uname1 = (TextView) view.findViewById(R.id.uname1);
            this.default_card = (TextView) view.findViewById(R.id.default_card);
            this.logo_img = (ImageView) view.findViewById(R.id.logo_img);
            this.imageView = (ImageView) view.findViewById(R.id.imageView);
            this.cname = (TextView) view.findViewById(R.id.cname);
            this.team = (TextView) view.findViewById(R.id.team);
            this.rank = (TextView) view.findViewById(R.id.rank);
            this.pnum = (TextView) view.findViewById(R.id.pnum);
            this.email = (TextView) view.findViewById(R.id.email);
            this.c_id = (TextView) view.findViewById(R.id.c_id);

            if (popupMenu != null) {
                popupMenu.dismiss();
            }
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupMenu = new PopupMenu(v.getContext(), v, Gravity.END);
                    createMenu(popupMenu.getMenu());

                    popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                        @Override
                        public void onDismiss(PopupMenu menu) {
                            popupMenu = null;
                        }
                    });
                    popupMenu.show();
                }
            });
        }
        public void createMenu(Menu menu){
            CardDao cardDao = CardDao.getCardDao();
            MenuItem.OnMenuItemClickListener menuItemClickListener = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    String c_id_val = c_id.getText().toString();

                    if(item.getTitle().equals("공유하기")){
                        Intent intent = new Intent(((Activity)context), ShareCardActivity.class);
                        intent.putExtra("c_id", c_id_val); // "key"는 전달할 데이터의 식별자, value는 전달할 데이터
                        ((Activity) context).startActivity(intent);
                        return true;
                    }else if(item.getTitle().equals("상세보기")){
                        Intent intent = new Intent(((Activity)context), ViewCardActivity.class);
                        intent.putExtra("c_id", c_id_val); // "key"는 전달할 데이터의 식별자, value는 전달할 데이터
                        ((Activity) context).startActivity(intent);
                        return true;
                    }else if(item.getTitle().equals("대표명함 설정")){
                        Log.d("Menu:", "대표명함");
                        cardDao.is_default(c_id_val);
                        ((Activity)context).recreate(); // 액티비티 새로코침
                        return true;
                    }else if(item.getTitle().equals("수정")){
                        Log.d("Menu:", "수정");
                        return true;
                    }else if(item.getTitle().equals("삭제")){
                        Log.d("Menu:", "삭제");
                        return true;
                    }
                    return true;
                }
            };
            menu.add(1, R.id.main_btn_menu1, 1,"공유하기").setOnMenuItemClickListener(menuItemClickListener);
            menu.add(1, R.id.main_btn_menu2, 2,"상세보기").setOnMenuItemClickListener(menuItemClickListener);;
            if(cardYype.equals("my")) {
                menu.add(1, R.id.main_btn_menu3, 3,"대표명함 설정").setOnMenuItemClickListener(menuItemClickListener);;
                menu.add(1, R.id.main_btn_menu4, 4,"수정").setOnMenuItemClickListener(menuItemClickListener);;
            }
            menu.add(1, R.id.main_btn_menu5, 5,"삭제").setOnMenuItemClickListener(menuItemClickListener);;
        }

    }
    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }
}
