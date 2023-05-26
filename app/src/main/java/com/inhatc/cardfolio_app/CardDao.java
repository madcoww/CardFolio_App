package com.inhatc.cardfolio_app;

import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CardDao {
    public Card card;
    private static CardDao cardDao = new CardDao();;
    private ArrayList<Card> otherArrayList;
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("cardFolio");

    private CardDao (){}

    public static CardDao getCardDao() {
        return cardDao;
    }

    // 대표명함 설정
    public void is_default(String c_id){
        //u_id가 일치하는 모든 데이터를 읽어와서, c_id를 읽어와서
        otherArrayList = new ArrayList<>();
        // 로그인한 사용자 명함집 읽기
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        String u_id = firebaseUser.getUid();
        Query query = mDatabaseRef.child("OtherCards").orderByChild("u_id").equalTo(firebaseUser.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot != null){
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String cId = userSnapshot.child("c_id").getValue(String.class);

                        // CardInfo에서 일치하는 c_id의 데이터를 찾아 읽어온다.
                        Query query3 = mDatabaseRef.child("CardInfo").orderByChild("u_id").equalTo(u_id);
                        query3.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                    if(userSnapshot.child("c_id").getValue(String.class).equals(c_id)) {
                                        mDatabaseRef.child("CardInfo").child(userSnapshot.child("c_id").getValue(String.class)).child("is_default").setValue(1);
                                        Log.d("명항설정O", "1" + userSnapshot.child("c_id").getValue(String.class));
                                    }else{
                                        mDatabaseRef.child("CardInfo").child(userSnapshot.child("c_id").getValue(String.class)).child("is_default").setValue(0);
                                        Log.d("명항설정X", "0" + userSnapshot.child("c_id").getValue(String.class));
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // 쿼리가 취소되었거나 실패한 경우에 대한 처리
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
