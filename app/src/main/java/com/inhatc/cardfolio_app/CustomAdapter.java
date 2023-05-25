package com.inhatc.cardfolio_app;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;


public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {
    private ArrayList<Card> mList;
    private String cardYype; // 내명함인지(my), 명함집인지(other)
    private Context context;
    private FirebaseStorage storage = FirebaseStorage.getInstance(); //스토리지
    private StorageReference storageRef = storage.getReference();
    private StorageReference fileRef;
    private String card_logo;

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
    public void onBindViewHolder(@NonNull CustomViewHolder viewholder, int position)
    {
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
        viewholder.u_id.setText(mList.get(position).getU_id());
        
        // 이미지 로드
        card_logo = mList.get(position).getCard_logo();
        fileRef = storageRef.child("logo").child(card_logo);
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(((Activity) context)).load(uri).into(viewholder.logo_img);
                Log.d("uri:", ":"+uri);
                Log.d("로고 로드:", "성공");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected LinearLayout id;
        protected TextView uname1, c_id, u_id;
        protected TextView default_card;   // 대표명함 여부
        protected ImageView logo_img, imageView;     // 로고, 메뉴 버튼
        protected TextView cname, team, rank, pnum, email;          // 이메일
        private PopupMenu popupMenu;
        private DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("cardFolio");
        private AlertDialog.Builder msgBuilder;

        public CustomViewHolder(View view)
        {
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
            this.u_id = (TextView) view.findViewById(R.id.u_id);

            //팝업메뉴
            if (popupMenu != null) {
                popupMenu.dismiss();
            }
            
            // 이미지 로드
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
        public void createMenu(Menu menu)
        {
            CardDao cardDao = CardDao.getCardDao();
            MenuItem.OnMenuItemClickListener menuItemClickListener = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    String c_id_val = c_id.getText().toString();
                    String u_id_val = u_id.getText().toString();

                    if(item.getTitle().equals("공유하기"))
                    {
                        Intent intent = new Intent(((Activity)context), ShareCardActivity.class);
                        intent.putExtra("c_id", c_id_val); // "key"는 전달할 데이터의 식별자, value는 전달할 데이터
                        intent.putExtra("intent_name", "qrShareIntent");
                        ((Activity) context).startActivity(intent);
                        return true;
                    }
                    else if(item.getTitle().equals("상세보기"))
                    {
                        Intent intent = new Intent(((Activity)context), ViewCardActivity.class);
                        intent.putExtra("c_id", c_id_val); // "key"는 전달할 데이터의 식별자, value는 전달할 데이터
                        intent.putExtra("u_id", u_id_val); // "key"는 전달할 데이터의 식별자, value는 전달할 데이터
                        ((Activity) context).startActivity(intent);
                        return true;
                    }
                    else if(item.getTitle().equals("대표명함 설정"))
                    {
                        cardDao.is_default(c_id_val);
                        String contextMain = MainActivity.class.getSimpleName();
                        String contextCurrent = context.getClass().getSimpleName();
                        if( contextMain.equals(contextCurrent)) {
                            Intent intent = new Intent(((Activity) context), MainActivity.class);
                            ((Activity) context).startActivity(intent); // 액티비티 새로코침
                            ((Activity) context).finish();
                        }else{
                            ((Activity) context).recreate();
                        }
                        return true;
                    }
                    else if(item.getTitle().equals("수정"))
                    {
                        updateCard(c_id_val);
                        return true;
                    }
                    else if(item.getTitle().equals("삭제"))
                    {
                        showDialog(c_id_val);
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

        // 수정
        public void updateCard(String c_id_val){
            Query query = mDatabaseRef.child("CardInfo").orderByChild("c_id").equalTo(c_id_val);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Card card = new Card();
                    for(DataSnapshot userSnapshot : dataSnapshot.getChildren())
                    {
                        card.setC_id(userSnapshot.child("c_id").getValue(String.class));
                        card.setCard_caddr(userSnapshot.child("card_caddr").getValue(String.class));
                        card.setCard_cname(userSnapshot.child("card_cname").getValue(String.class));
                        card.setCard_email(userSnapshot.child("card_email").getValue(String.class));
                        card.setCard_logo(userSnapshot.child("card_logo").getValue(String.class));
                        card.setCard_pnum(userSnapshot.child("card_pnum").getValue(String.class));
                        card.setCard_rank(userSnapshot.child("card_rank").getValue(String.class));
                        card.setCard_team(userSnapshot.child("card_team").getValue(String.class));
                        card.setCard_uname(userSnapshot.child("card_uname").getValue(String.class));
                        card.setIs_default(userSnapshot.child("is_default").getValue(Integer.class));
                        card.setU_id(userSnapshot.child("u_id").getValue(String.class));
                    }

                    Intent registerCardModifyIntent = new Intent(((Activity)context), RegisterCardActivity.class);
                    registerCardModifyIntent.putExtra("intent_name", "ModifyCard");
                    registerCardModifyIntent.putExtra("CardInfo", card); // "key"는 전달할 데이터의 식별자, value는 전달할 데이터
                    ((Activity)context).startActivity(registerCardModifyIntent);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        // 삭제
        private void showDialog(String c_id_val) {
            msgBuilder = new AlertDialog.Builder(((Activity)context))
                    .setTitle("해당 명함을 삭제하시겠습니까?")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            delete_data(c_id_val);
                            Toast.makeText(((Activity)context), "삭제 되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(((Activity)context), "취소 되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
            AlertDialog msgDlg = msgBuilder.create();
            msgDlg.show();
        }

        //firebase 데이터 삭제
        private void delete_data(String c_id_val) {
            mDatabaseRef.child("CardInfo").child(c_id_val).setValue(null);

            Intent intent = new Intent(((Activity)context), MainActivity.class);
            ((Activity)context).startActivity(intent);
            ((Activity)context).finish();
        }
    }
    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }

}
