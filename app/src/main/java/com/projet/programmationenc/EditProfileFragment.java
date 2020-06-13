package com.projet.programmationenc;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EditProfileFragment extends Fragment {
//    private final int code = 1;
    private static final String TAG = "EditProfileFragment";
    private FirebaseUser user;
    private String firstnameedit,lastnameedit;
    private ImageView imgvavataredit;
    private TextInputLayout edtlastnameedit,edtfirstnameedit;
    private TextView txtvchangeavatar;
    private Button btnconfirmedit;
    private ImageButton btnremoveavatar;
//    private Bitmap bitmap;
//    private DatabaseReference databaseReference;
    private Uri imgavataruri,downloadedUri;
    private StorageReference storageReference;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_editprofile,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String base_url = ((HomeActivity) getActivity()).base_url;

        storageReference = FirebaseStorage.getInstance().getReference();

        user = FirebaseAuth.getInstance().getCurrentUser();
//        databaseReference = FirebaseDatabase.getInstance().getReference();
        imgvavataredit = view.findViewById(R.id.imgvavataredit);
        edtlastnameedit = view.findViewById(R.id.edtlastnameedit);
        edtfirstnameedit = view.findViewById(R.id.edtfirstnameedit);
        btnconfirmedit = view.findViewById(R.id.btnconfirmedit);
        btnremoveavatar = view.findViewById(R.id.btnremoveavatar);
        txtvchangeavatar = view.findViewById(R.id.txtvchangeavatar);

        edtfirstnameedit.getEditText().setText(((HomeActivity) getActivity()).retrievedFirstName);
        edtlastnameedit.getEditText().setText(((HomeActivity) getActivity()).retrievedLastName);
        if(((HomeActivity) getActivity()).retrievedAvatar != null) {
            Uri imgavataruri = Uri.parse(((HomeActivity) getActivity()).retrievedAvatar);
            Glide.with(this)
                    .load(imgavataruri)
                    .apply(RequestOptions.fitCenterTransform())
                    .into(imgvavataredit);

        }

        imgvavataredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageFile();
            }
        });

        txtvchangeavatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageFile();
            }
        });

        btnremoveavatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgavataruri = Uri.parse("android.resource://com.projet.programmationenc/mipmap/ic_person_grayv2_round");
                Glide.with(getActivity())
                        .load(imgavataruri)
                        .apply(RequestOptions.fitCenterTransform())
                        .into(imgvavataredit);

//                Glide.with(getActivity())
//                                .load(bitmap)
//                                .apply(RequestOptions.fitCenterTransform())
//                                .into(imgvavataredit);
            }
        });

        btnconfirmedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstnameedit = edtfirstnameedit.getEditText().getText().toString();
                lastnameedit = edtlastnameedit.getEditText().getText().toString();
                final String emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z]+\\.[a-zA-Z]{2,6}";
                boolean flag = true;

                if(firstnameedit.isEmpty()) {
                    edtfirstnameedit.setError("Veuillez saisir le prénom.");
                    flag = false;
                }

                if(lastnameedit.isEmpty()) {
                    edtlastnameedit.setError("Veuillez saisir le nom.");
                    flag = false;
                }

                if(!flag) {
                    return;
                }
                else {

//                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                    bitmap.compress(Bitmap.CompressFormat.JPEG ,100, baos);
//                    byte[] b = baos.toByteArray();
//                    String temp= Base64.encodeToString(b, Base64.DEFAULT);

//                    try {
//                        bitmap = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), imgavataruri);
//                        Log.e(TAG, "onClick: dkhol l try catch bitmap");
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                    StorageReference ref = storageReference.child("students_avatars/"+imgavataruri.getLastPathSegment());
                    ref.putFile(imgavataruri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        Task<Uri> downloadedUri = taskSnapshot.getStorage().getDownloadUrl();
                            Log.e(TAG, "onSuccess: image uploaded");
                            if (taskSnapshot.getMetadata() != null) {
                                if (taskSnapshot.getMetadata().getReference() != null) {
                                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Retrofit retrofit = new Retrofit.Builder()
                                                    .baseUrl(base_url)
                                                    .addConverterFactory(GsonConverterFactory.create())
                                                    .build();

                                            ApiInterface apiInterface = retrofit.create(ApiInterface.class);
                                            Call<Student> call = apiInterface.updateStudent(user.getUid(),firstnameedit,lastnameedit,uri.toString());

                                            call.enqueue(new Callback<Student>() {
                                                @Override
                                                public void onResponse(Call<Student> call, Response<Student> response) {
                                                    if(!response.isSuccessful()) {
                                                        Log.e(TAG, "onResponse: Code " + response.code());
                                                        return;
                                                    }
                                                    Log.e(TAG, "onResponse: " + "Data updates");
                                                    Toast.makeText(getContext(),"Modification réussie !",Toast.LENGTH_SHORT).show();

//                                                    getActivity().finish();
//                                                    getActivity().overridePendingTransition(0, 0);
//                                                    startActivity(getActivity().getIntent());
//                                                    getActivity().overridePendingTransition(0, 0);

                                                }

                                                @Override
                                                public void onFailure(Call<Student> call, Throwable t) {
                                                    Log.e(TAG, "onFailure: " + t.getMessage());
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "onFailure: image not uploaded");
                        }
                    });


//                    Log.e(TAG, "onClick: downloadeduri after anonymous class" + downloadedUri.toString());
//                    updateStudentProfile(base_url);

                }
            }
        });

    }

    private void openImageFile() {
        CropImage.activity()
                .setAspectRatio(1,1)
                .start(getContext(), this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == getActivity().RESULT_OK) {
                imgavataruri = result.getUri();

                Glide.with(this)
                        .load(imgavataruri)
                        .apply(RequestOptions.fitCenterTransform())
                        .into(imgvavataredit);
//                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), imgavataruri);
            }
        }
    }

//    public String BitMapToString() {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG ,100, baos);
//        byte[] b = baos.toByteArray();
//        return Base64.encodeToString(b, Base64.DEFAULT);
//    }

//    public void updateStudentProfile(String base_url) {
////        ByteArrayOutputStream baos = new ByteArrayOutputStream();
////        bitmap.compress(Bitmap.CompressFormat.JPEG ,100, baos);
////        byte[] b = baos.toByteArray();
////        String temp = Base64.encodeToString(b, Base64.DEFAULT);
//
////        Log.e(TAG, "onClick: temp : " + temp);
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(base_url)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
//        Call<Student> call = apiInterface.updateStudent(user.getUid(),firstnameedit,lastnameedit,downloadedUri.toString());
//
//        call.enqueue(new Callback<Student>() {
//            @Override
//            public void onResponse(Call<Student> call, Response<Student> response) {
//                if(!response.isSuccessful()) {
//                    Log.e(TAG, "onResponse: Code " + response.code());
//                    return;
//                }
//                Log.e(TAG, "onResponse: " + "Data updates");
//                Toast.makeText(getContext(),"Modification réussie !",Toast.LENGTH_SHORT).show();
//
//                getActivity().finish();
//                getActivity().overridePendingTransition(0, 0);
//                startActivity(getActivity().getIntent());
//                getActivity().overridePendingTransition(0, 0);
//
//            }
//
//            @Override
//            public void onFailure(Call<Student> call, Throwable t) {
//                Log.e(TAG, "onFailure: " + t.getMessage());
//            }
//        });
//    }

}





//                    storageReference.child("students_avatars/"+user.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                        @Override
//                        public void onSuccess(Uri uri) {
//                            downloadedUri = uri;
//                        }
//                    });








//                            if(taskSnapshot.getMetadata() != null) {
//                                if(taskSnapshot.getMetadata().getReference() != null) {
//                                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
//                                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                        @Override
//                                        public void onSuccess(Uri uri) {
//                                            final Uri downloadUri = uri;
//                                            Log.e(TAG, "onSuccess: URI downloaded" + downloadUri);
//                                        }
//                                    });
//                                }
//                            }

