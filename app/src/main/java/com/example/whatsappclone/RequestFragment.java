package com.example.whatsappclone;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private View requestFragmentView;
    private RecyclerView myRequestList;

    private DatabaseReference chatRequestRef,usersRef,contactsRef;

    private FirebaseAuth mAuth;
    private String currentUserId;


    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestFragmentView = inflater.inflate(R.layout.fragment_request, container, false);

        myRequestList = (RecyclerView)requestFragmentView.findViewById(R.id.chat_request_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        return requestFragmentView;
    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(chatRequestRef.child(currentUserId), Contacts.class)
                        .build();


        final FirebaseRecyclerAdapter<Contacts, RequestsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Contacts model) {
               holder.itemView.findViewById(R.id.accept_request_button).setVisibility(View.VISIBLE);
               holder.itemView.findViewById(R.id.decline_request_button).setVisibility(View.VISIBLE);

               final String listUserId = getRef(position).getKey();

               final DatabaseReference getTypeRef = getRef(position).child("request type").getRef();

               getTypeRef.addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       if(dataSnapshot.exists()){
                           String type = dataSnapshot.getValue().toString();
                           if (type.equals("received")){
                               usersRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                   @Override
                                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                       if (dataSnapshot.hasChild("image")){
                                           final String requestUserImage = dataSnapshot.child("image").getValue().toString();

                                           Picasso.get().load(requestUserImage).placeholder(R.drawable.profile_image).into(holder.profileImage);

                                       }
                                       final String requestProfileName = dataSnapshot.child("name").getValue().toString();
                                       final String requestProfileStatus = dataSnapshot.child("status").getValue().toString();

                                       holder.username.setText(requestProfileName);
                                       holder.userStatus.setText("Wants To connect with you");

                                       holder.itemView.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View view) {
                                               CharSequence options[] = new CharSequence[]{
                                                 "Accept",
                                                 "Refuse"
                                               };
                                               AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                               builder.setTitle(requestProfileName + " Chat Request");
                                               builder.setItems(options, new DialogInterface.OnClickListener() {
                                                   @Override
                                                   public void onClick(DialogInterface dialogInterface, final int i) {
                                                       if (i == 0){
                                                           contactsRef.child(currentUserId).child(listUserId).child("Contacts")
                                                                   .setValue("Saved")
                                                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                       @Override
                                                                       public void onComplete(@NonNull Task<Void> task) {
                                                                           if (task.isSuccessful()){
                                                                               contactsRef.child(listUserId).child(currentUserId).child("Contacts")
                                                                                       .setValue("Saved")
                                                                                       .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                           @Override
                                                                                           public void onComplete(@NonNull Task<Void> task) {
                                                                                               if (task.isSuccessful()){
                                                                                                   chatRequestRef.child(currentUserId).child(listUserId)
                                                                                                   .removeValue()
                                                                                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                       @Override
                                                                                                       public void onComplete(@NonNull Task<Void> task) {
                                                                                                           if (task.isSuccessful()){
                                                                                                                   chatRequestRef.child(listUserId).child(currentUserId)
                                                                                                                           .removeValue()
                                                                                                                           .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                               @Override
                                                                                                                               public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                   if (task.isSuccessful()){
                                                                                                                                       Toast.makeText(getContext(),"New Contact Added",Toast.LENGTH_SHORT).show();


                                                                                                                                   }
                                                                                                                               }
                                                                                                                           });

                                                                                                               }
                                                                                                       }
                                                                                                   });

                                                                                               }
                                                                                           }
                                                                                       });

                                                                           }
                                                                       }
                                                                   });

                                                       }
                                                       if (i == 1){
                                                           chatRequestRef.child(currentUserId).child(listUserId)
                                                                   .removeValue()
                                                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                       @Override
                                                                       public void onComplete(@NonNull Task<Void> task) {
                                                                           if (task.isSuccessful()){
                                                                               chatRequestRef.child(listUserId).child(currentUserId)
                                                                                       .removeValue()
                                                                                       .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                           @Override
                                                                                           public void onComplete(@NonNull Task<Void> task) {
                                                                                               if (task.isSuccessful()){
                                                                                                   Toast.makeText(getContext(),"Request refused",Toast.LENGTH_SHORT).show();
                                                                                               }
                                                                                           }
                                                                                       });
                                                                           }
                                                                       }
                                                                   });

                                                       }
                                                   }
                                               });

                                           builder.show();
                                           }
                                       });
                                   }

                                   @Override
                                   public void onCancelled(@NonNull DatabaseError databaseError) {

                                   }
                               });
                           }else if (type.equals("send")){
                               Button requestSentBtn = holder.itemView.findViewById(R.id.accept_request_button);
                               requestSentBtn.setText("Request Sent");

                               holder.itemView.findViewById(R.id.decline_request_button).setVisibility(View.INVISIBLE);

                               usersRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                   @Override
                                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                       if (dataSnapshot.hasChild("image")){
                                           final String requestUserImage = dataSnapshot.child("image").getValue().toString();

                                           Picasso.get().load(requestUserImage).placeholder(R.drawable.profile_image).into(holder.profileImage);

                                       }
                                       final String requestProfileName = dataSnapshot.child("name").getValue().toString();
                                       final String requestProfileStatus = dataSnapshot.child("status").getValue().toString();

                                       holder.username.setText(requestProfileName);
                                       holder.userStatus.setText("You have sent a request to"+requestProfileName);

                                       holder.itemView.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View view) {
                                               CharSequence options[] = new CharSequence[]{
                                                     "Cancel Chat Request"
                                               };
                                               AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                               builder.setTitle("Already sent request");
                                               builder.setItems(options, new DialogInterface.OnClickListener() {
                                                   @Override
                                                   public void onClick(DialogInterface dialogInterface, final int i) {
                                                       if (i == 0){
                                                           chatRequestRef.child(currentUserId).child(listUserId)
                                                                   .removeValue()
                                                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                       @Override
                                                                       public void onComplete(@NonNull Task<Void> task) {
                                                                           if (task.isSuccessful()){
                                                                               chatRequestRef.child(listUserId).child(currentUserId)
                                                                                       .removeValue()
                                                                                       .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                           @Override
                                                                                           public void onComplete(@NonNull Task<Void> task) {
                                                                                               if (task.isSuccessful()){
                                                                                                   Toast.makeText(getContext(),"You have canceled chat request",Toast.LENGTH_SHORT).show();
                                                                                               }
                                                                                           }
                                                                                       });
                                                                           }
                                                                       }
                                                                   });

                                                       }
                                                   }
                                               });

                                               builder.show();
                                           }
                                       });
                                   }

                                   @Override
                                   public void onCancelled(@NonNull DatabaseError databaseError) {

                                   }
                               });


                           }


                       }

                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError databaseError) {

                   }
               });


            }

            @NonNull
            @Override
            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                RequestsViewHolder holder = new RequestsViewHolder(view);
                return holder;
            }
        };

        myRequestList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder{

        TextView username,userStatus;
        CircleImageView profileImage;
        Button acceptRequest,declineRequest;


        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.public_users_profile_name);
            userStatus = itemView.findViewById(R.id.public_users_profile_status);
            profileImage = itemView.findViewById(R.id.public_users_profile_image);
            acceptRequest = itemView.findViewById(R.id.accept_request_button);
            declineRequest = itemView.findViewById(R.id.decline_request_button);
        }
    }

}
