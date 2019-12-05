package com.example.net_danong;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.sql.Date;
import java.util.regex.Pattern;


public class JoinFragment extends Fragment {

    private static final String TAG = "JoinFragment";
    View view;
    //비밀번호 정규식
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{4,16}$");

    //파이어베이스 인증 객체 생성
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore mDb;


    //정보 위젯
    private EditText editName, editTextEmail, editTextPassword, editPhoneNumber;
    private String name, email, password, phoneNumber;
    private com.google.firebase.Timestamp time;
    private Context context;
    public int TERMS_AGREE_1 = 0; //No Check = 0, Check = 1
    public int TERMS_AGREE_2 = 0;
    public int TERMS_AGREE_3 = 0;

    AppCompatCheckBox checkBox1; //첫번째 동의
    AppCompatCheckBox checkBox2; //두번째 동의
    AppCompatCheckBox checkBox3; //모두 동의

    // 각각의 Fragment마다 Instance를 반환해 줄 메소드를 생성
    public static JoinFragment newInstance() {
        return new JoinFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       view = inflater.inflate(R.layout.activity_join, container, false);

        //파이어베이스 인증 객체 선언
        firebaseAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();

        context = container.getContext();
        editName = view.findViewById(R.id.nameText);
        editTextEmail = view.findViewById(R.id.emailText);
        editTextPassword = view.findViewById(R.id.passwordText);
        editPhoneNumber = view.findViewById(R.id.phonenumberText);


        final CheckBox checkBox1 = (CheckBox) view.findViewById(R.id.register_check_1);
        checkBox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    TERMS_AGREE_1 = 1;
                } else {
                    TERMS_AGREE_1 = 0;
                }
            }
        });
        final CheckBox checkBox2 = (CheckBox) view.findViewById(R.id.register_check_2);
        checkBox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    TERMS_AGREE_2 = 1;
                } else {
                    TERMS_AGREE_2 = 0;
                }
            }
        });
        CheckBox checkBox3 = (CheckBox)view.findViewById(R.id.register_check_3);
        checkBox3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    checkBox1.setChecked(true);
                    checkBox2.setChecked(true);
                    TERMS_AGREE_3 = 1;
                } else {
                    checkBox1.setChecked(false);
                    checkBox2.setChecked(false);
                    TERMS_AGREE_3 = 0;
                }
            }
        });
        Button button_finish = (Button)view.findViewById(R.id.register_btn_finish);
        button_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //전체 약관 체크 여부
                if(TERMS_AGREE_3 !=1) {
                    //첫번째 약관 체크 여부
                    if(TERMS_AGREE_2 == 1) {
                        //두번째 약관 체크 여부`
                        if(TERMS_AGREE_1 == 1){
                            registerNewUser();
                        }else {
                            Toast.makeText(context, "약관을 체크해주세요.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        Toast.makeText(context, "약관을 체크해주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } //전체 약관 체크된 경우
                else {
                    registerNewUser();
                }
            }

        });
        return view;
    }

    private void registerNewUser() {
        name = editName.getText().toString().trim();
        email = editTextEmail.getText().toString().trim();
        password = editTextPassword.getText().toString().trim();
        phoneNumber = editPhoneNumber.getText().toString().trim();
        time = Timestamp.now();

        if(TextUtils.isEmpty(name)) {
            Toast.makeText(context, "이름을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(email)) {
            Toast.makeText(context, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(context, "비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(context, "전화번호를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        } /*else if (!EMAIL_PATTERN.matcher(email).matches()) {
            Toast.makeText(context, "이메일 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        } */else if(!PASSWORD_PATTERN.matcher(password).matches()) {
            Toast.makeText(context, "비밀번호 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: AuthState: " + FirebaseAuth.getInstance().getCurrentUser().getUid());
                            User user = new User(FirebaseAuth.getInstance().getUid(),name,email,phoneNumber,time);

                            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                                    .setTimestampsInSnapshotsEnabled(true)
                                    .build();
                            mDb.setFirestoreSettings(settings);

                            DocumentReference newUserRef = mDb
                                    .collection("users")
                                    .document(FirebaseAuth.getInstance().getUid());

                            newUserRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "회원 등록성공.", Toast.LENGTH_SHORT).show();
                                        //JOIN FRAGMENT 종료
                                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                        fragmentManager.beginTransaction().remove(JoinFragment.this).commit();
                                        fragmentManager.popBackStack();
                                    } else {
                                        View parentLayout = view.findViewById(android.R.id.content);
                                        Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(context, "회원가입에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
}