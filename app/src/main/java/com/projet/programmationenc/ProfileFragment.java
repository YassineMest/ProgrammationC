package com.projet.programmationenc;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
    private CircleImageView imgvavatarprofile;
    private TextView txtvfullnameprofile, txtvstatusprofile;
    private ImageButton btnchangestatus;
    private Button btnsendrequest,btncancelrequest;
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private String fullname, status;
    private Uri uri;
    private Student S;
    private String friendstatus = "notfriends";
    private String key;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgvavatarprofile = view.findViewById(R.id.imgvavatarprofile);
        txtvfullnameprofile = view.findViewById(R.id.txtvfullnameprofile);
        txtvstatusprofile = view.findViewById(R.id.txtvstatusprofile);
        btnchangestatus = view.findViewById(R.id.btnchangestatus);
        btnsendrequest = view.findViewById(R.id.btnsendrequest);
        btncancelrequest = view.findViewById(R.id.btncancelrequest);

        user = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();


        if (getArguments() != null) {
            key = getArguments().getString("key");
            RetrieveOtherProfile(key);
        } else {
            RetrieveStudentProfile();
        }

        btnchangestatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("status", status);
                ChangeStatusFragment changeStatusFragment = new ChangeStatusFragment();
                changeStatusFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragcontainer, changeStatusFragment).addToBackStack(null).commit();
            }
        });

        btnsendrequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnsendrequest.setEnabled(false);
                if (friendstatus.equals("notfriends")) {
                    SendRequestFriend();
                } else if (friendstatus.equals("sent")) {
                    CancelRequestFriend();
                }
                else if(friendstatus.equals("received")) {
                    AcceptRequestFriend();
                }
                else if(friendstatus.equals("friends")) {
                    DeleteFriend();
                }
            }
        });

        btncancelrequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeclineRequestFriend();
            }
        });
    }

    public void RetrieveStudentProfile() {
        databaseReference.child("Students").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    S = dataSnapshot.getValue(Student.class);
                    fullname = S.getFirstName() + " " + S.getLastName();
                    uri = Uri.parse(S.getAvatar());
                    status = S.getStatus();

                    Glide.with(ProfileFragment.this)
                            .load(uri)
                            .apply(RequestOptions.fitCenterTransform())
                            .into(imgvavatarprofile);

                    txtvfullnameprofile.setText(fullname);
                    txtvstatusprofile.setText(status);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void RetrieveOtherProfile(String key) {
        databaseReference.child("Students").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    S = dataSnapshot.getValue(Student.class);
                    fullname = S.getFirstName() + " " + S.getLastName();
                    uri = Uri.parse(S.getAvatar());
                    status = S.getStatus();

                    Glide.with(ProfileFragment.this)
                            .load(uri)
                            .apply(RequestOptions.fitCenterTransform())
                            .into(imgvavatarprofile);

                    txtvfullnameprofile.setText(fullname);
                    txtvstatusprofile.setText(status);
                    btnchangestatus.setVisibility(View.GONE);
                    btnsendrequest.setVisibility(View.VISIBLE);

                    databaseReference.child("Requests").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(key)) {
                                String req = dataSnapshot.child(key).child("type").getValue(String.class);
                                if(req != null && req.equals("received")) {
                                    friendstatus = "received";
                                    btnsendrequest.setText("Accepter l'invitation");
                                    btnsendrequest.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_person_add_20, 0, 0, 0);
                                    btncancelrequest.setVisibility(View.VISIBLE);
                                }
                                else if(req != null && req.equals("sent")) {
                                    friendstatus = "sent";
                                    btnsendrequest.setText("Annuler l'invitation");
                                    btnsendrequest.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_person_add_disabled_20, 0, 0, 0);
                                }
                            }
                            else {
                                databaseReference.child("Friends").child(user.getUid()).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()) {
                                            friendstatus = "friends";
                                            btnsendrequest.setText("Supprimer " + fullname);
                                            btnsendrequest.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_person_20, 0, 0, 0);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void SendRequestFriend() {
        databaseReference.child("Requests").child(user.getUid()).child(key).child("type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    databaseReference.child("Requests").child(key).child(user.getUid()).child("type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                HashMap<String,String> hashMap = new HashMap<>();
                                hashMap.put("from",user.getUid());
                                hashMap.put("type","Invitation d'amitié");
                                databaseReference.child("Notifications").child(key).push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            friendstatus = "sent";
                                            btnsendrequest.setText("Annuler l'invitation");
                                            btnsendrequest.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_person_add_disabled_20, 0, 0, 0);
                                            Toast.makeText(getActivity(), "Invitation envoyée !", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                            btnsendrequest.setEnabled(true);
                        }
                    });
                }
            }
        });
    }

    public void CancelRequestFriend() {
        databaseReference.child("Requests").child(user.getUid()).child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    databaseReference.child("Requests").child(key).child(user.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                friendstatus = "notfriends";
                                btnsendrequest.setText("Envoyer une invitation");
                                btnsendrequest.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_person_add_20, 0, 0, 0);
                                Toast.makeText(getActivity(), "Invitation annulée !", Toast.LENGTH_SHORT).show();
                            }
                            btnsendrequest.setEnabled(true);
                        }
                    });
                }
            }
        });
    }

    public void AcceptRequestFriend() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        databaseReference.child("Friends").child(user.getUid()).child(key).setValue(sdf.format(new Date())).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    databaseReference.child("Friends").child(key).child(user.getUid()).setValue(sdf.format(new Date())).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                databaseReference.child("Requests").child(user.getUid()).child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            databaseReference.child("Requests").child(key).child(user.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        btncancelrequest.setVisibility(View.GONE);
                                                        friendstatus = "friends";
                                                        btnsendrequest.setText("Supprimer " + fullname);
                                                        btnsendrequest.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_person_20, 0, 0, 0);
                                                        Toast.makeText(getActivity(), "Personne ajoutée !", Toast.LENGTH_SHORT).show();
                                                    }
                                                    btnsendrequest.setEnabled(true);
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

    public void DeclineRequestFriend() {
        databaseReference.child("Requests").child(user.getUid()).child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    databaseReference.child("Requests").child(key).child(user.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                btncancelrequest.setVisibility(View.GONE);
                                friendstatus = "notfriends";
                                btnsendrequest.setText("Envoyer une invitation");
                                btnsendrequest.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_person_add_20, 0, 0, 0);
                                Toast.makeText(getActivity(), "Invitation déclinée !", Toast.LENGTH_SHORT).show();
                            }
                            btnsendrequest.setEnabled(true);
                        }
                    });
                }
            }
        });
    }

    public void DeleteFriend() {
        databaseReference.child("Friends").child(user.getUid()).child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    databaseReference.child("Friends").child(key).child(user.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                friendstatus = "notfriends";
                                btnsendrequest.setText("Envoyer une invitation");
                                btnsendrequest.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_person_add_20, 0, 0, 0);
                                Toast.makeText(getActivity(), "Personne supprimée !", Toast.LENGTH_SHORT).show();
                            }
                            btnsendrequest.setEnabled(true);
                        }
                    });
                }
            }
        });
    }
}
