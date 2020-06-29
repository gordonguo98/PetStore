package com.training.petstore.view.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.hbisoft.pickit.PickiT;
import com.hbisoft.pickit.PickiTCallbacks;
import com.training.petstore.MyApplication;
import com.training.petstore.R;
import com.training.petstore.model.bean.Pet;
import com.training.petstore.util.common.CommonUtil;
import com.training.petstore.util.constants.IntConstants;
import com.training.petstore.util.constants.StringConstants;
import com.training.petstore.util.net.NetImplementation;
import com.zhihu.matisse.Matisse;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManagePetsActivity extends AppCompatActivity implements PickiTCallbacks {

    private static final int REQUEST_CODE_CHOOSE = 300;
    private static final String TAG = ManagePetsActivity.class.getSimpleName();

    PickiT pickiT;
    Pet pet = null;
    MyHandler myHandler;

    private TextView title;
    private EditText petName;
    private EditText petVariety;
    private TextInputEditText petPrice;
    private EditText petDescription;
    private Switch petOnSell;
    private ImageView imageBtn;
    private ImageButton closeBtn;
    private ImageButton doneBtn;
    private EditText birthText;
    private ProgressDialog progressDialog;

    private String action = null;
    private String imagePath = null;

    List<Uri> mSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_pets);

        Log.d(TAG, "onCreate: " + "完成");

        pickiT = new PickiT(this, this, this);
        myHandler = new MyHandler(this);

        Intent intent = getIntent();
        if(intent != null)
            action = intent.getStringExtra("action");

        initView();

    }

    public void initView(){

        title = findViewById(R.id.activity_manage_pets_title);
        petName = findViewById(R.id.activity_manage_pets_name_edit_text);
        petVariety = findViewById(R.id.activity_manage_pets_variety_edit_text);
        petPrice = findViewById(R.id.activity_manage_pets_price_edit_text);
        petDescription = findViewById(R.id.activity_manage_pets_description_edit_text);
        petOnSell = findViewById(R.id.activity_manage_pets_on_sell);
        imageBtn = findViewById(R.id.activity_manage_pets_pet_image);
        closeBtn = findViewById(R.id.activity_manage_pets_bt_close);
        doneBtn = findViewById(R.id.activity_manage_pets_done_btn);
        birthText = findViewById(R.id.activity_manage_pets_birth_edit_text);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        petPrice.addTextChangedListener(new CommonUtil.MyTextWatcher(petPrice, "价格必须为整数", CommonUtil.CHECK_ASSET));

        title.setText(action.equals(StringConstants.ACTION_CREATE_PET) ? "注册宠物" : "修改宠物");

        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtil.pickImage(ManagePetsActivity.this, REQUEST_CODE_CHOOSE);
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });

        birthText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyBoard();
                pickDate();
            }
        });

        if(action.equals(StringConstants.ACTION_MODIFY_PET)){
            Intent intent = getIntent();
            Bundle bundle;
            if(intent != null && (bundle = intent.getBundleExtra("pet")) != null)
                pet = (Pet) bundle.getSerializable("pet");
            if(pet != null){
                Glide.with(this)
                        .load(pet.getPhoto_url())
                        .into(imageBtn);
                petName.setText(pet.getName());
                birthText.setText(pet.getBirth());
                petVariety.setText(pet.getVariety());
                petPrice.setText(String.valueOf(pet.getPrice()));
                petDescription.setText(pet.getDescription());
                petOnSell.setChecked(pet.getOn_sell().equals("1"));
            }
        }

    }

    private void pickDate(){

        TimePickerView pvTime = new TimePickerBuilder(this, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                birthText.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(date));
            }
        }).build();
        pvTime.setDate(Calendar.getInstance());
        pvTime.show();
    }

    public void submit(){
        if(!action.equals(StringConstants.ACTION_MODIFY_PET) && (imagePath == null || !new File(imagePath).exists())){
            Toast.makeText(this, "检测不到图片，请重新选择", Toast.LENGTH_SHORT).show();
            return;
        }
        if(petName.getText().toString().equals("")
        ||birthText.getText().toString().equals("")
        ||petVariety.getText().toString().equals("")
        ||petPrice.getText().toString().equals("")
        ||petDescription.getText().toString().equals("")){
            Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
        }

        if(action.equals(StringConstants.ACTION_MODIFY_PET))
            createOrModifyPet(pet.getPhoto_url());
        else getPhotoUrl();
    }

    public void getPhotoUrl(){
        NetImplementation.uploadImage(this, imagePath, myHandler);
    }

    public void onUploadImageFailed(){
        Toast.makeText(this, "上传图片失败，请重试", Toast.LENGTH_SHORT).show();
    }

    public void onUploadImageSucceeded(){
        Toast.makeText(this, "上传图片成功", Toast.LENGTH_SHORT).show();
    }

    public void createOrModifyPet(String photo_url){

        if(action != null){
            String name = petName.getText().toString();
            String birth = birthText.getText().toString();
            String variety = petVariety.getText().toString();
            String description = petDescription.getText().toString();
            String on_sell = petOnSell.isChecked() ? "1" : "0";
            int price = Integer.parseInt(petPrice.getText().toString());
            String user_id = MyApplication.getUser().getUserId();

            Pet newPet = new Pet(null, name, birth, variety,
                    description, photo_url, on_sell, price, user_id);

            if(action.equals(StringConstants.ACTION_CREATE_PET)){

                progressDialog.setTitle("提示");
                progressDialog.setMessage("正在注册...");
                progressDialog.show();

                NetImplementation.petRegister(newPet, myHandler);
            }else if(action.equals(StringConstants.ACTION_MODIFY_PET)){
                newPet.setPet_id(pet.getPet_id());

                progressDialog.setTitle("提示");
                progressDialog.setMessage("正在修改...");
                progressDialog.show();

                NetImplementation.petModify(newPet, myHandler);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            mSelected = Matisse.obtainResult(data);
            //imageBtn.setImageURI(mSelected.get(0));
            Glide.with(this)
                    .load(mSelected.get(0))
                    .into(imageBtn);
            pickiT.getPath(mSelected.get(0), Build.VERSION.SDK_INT);
            Log.d("Matisse", "mSelected: " + mSelected);
        }
    }

    @Override
    public void PickiTonUriReturned() {
        // Nothing to do
    }

    @Override
    public void PickiTonStartListener() {
        // Nothing to do
    }

    @Override
    public void PickiTonProgressUpdate(int progress) {
        // Nothing to do
    }

    @Override
    public void PickiTonCompleteListener(String path, boolean wasDriveFile, boolean wasUnknownProvider, boolean wasSuccessful, String Reason) {
        imagePath = path;
        Log.d(TAG, "路径: " + imagePath);
    }

    static class MyHandler extends Handler {

        ManagePetsActivity managePetsActivity;

        public MyHandler(ManagePetsActivity managePetsActivity){
            this.managePetsActivity = managePetsActivity;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what == IntConstants.UPLOAD_IMAGE_FAILED){
                if(msg.obj != null)
                    managePetsActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(managePetsActivity, (String) msg.obj, Toast.LENGTH_SHORT).show();
                        }
                    });
                managePetsActivity.onUploadImageFailed();
            }else if(msg.what == IntConstants.UPLOAD_IMAGE_SUCCEEDED){
                managePetsActivity.onUploadImageSucceeded();
                String photo_url = (String) msg.obj;
                managePetsActivity.createOrModifyPet(photo_url);
            }else if(msg.what == IntConstants.CODE_REGISTER_FAIL){
                managePetsActivity.onFail((String) msg.obj);
            }else if(msg.what == IntConstants.CODE_REGISTER_SUCCESS){
                managePetsActivity.onSuccess((String) msg.obj);
            }else if(msg.what == IntConstants.CODE_MODIFY_FAIL){
                managePetsActivity.onFail((String) msg.obj);
            }else if(msg.what == IntConstants.CODE_MODIFY_SUCCESS){
                managePetsActivity.onSuccess((String) msg.obj);
            }

        }
    }

    private void onSuccess(String obj) {
        Toast.makeText(this, obj, Toast.LENGTH_SHORT).show();
        if(progressDialog.isShowing())
            progressDialog.dismiss();
        setResult(RESULT_OK);
        finish();
    }

    private void onFail(String obj) {
        Toast.makeText(this, obj, Toast.LENGTH_SHORT).show();
        if(progressDialog.isShowing())
            progressDialog.dismiss();
    }

    public void hideKeyBoard() {
        //拿到InputMethodManager
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //如果window上view获取焦点 && view不为空
        if (imm.isActive() && getCurrentFocus() != null) {
            //拿到view的token 不为空
            if (getCurrentFocus().getWindowToken() != null) {
                //表示软键盘窗口总是隐藏，除非开始时以SHOW_FORCED显示。
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

}