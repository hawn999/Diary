package com.example.diary;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EditActivity extends AppCompatActivity {

    private String username;
    private String title="";
    private String time="";
    private String id="";
    private EditText title_text;
    private EditText content_text;
    private MyDatabaseHelper dbHelper;
    private Button back,save,take_photo,choose_photo,delete_photo;
//    private Uri imageUri;
//    public static String imagePath =""; //图片路径
//    public ImageView imageView;
//    public static final int TAKE_PHOTO = 1;
//    public static final int CHOOSE_PHOTO = 2;
//    private boolean isNew=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        final Intent intent=getIntent();
        //用户名，标题，创建时间
        username=intent.getStringExtra("name");
        if (title.length()!=0)
            title=intent.getStringExtra("title").substring(3);
        if(time.length()!=0)
            time=intent.getStringExtra("time").substring(5);



        //加载组件
        back=(Button)findViewById(R.id.back);
        back.setOnClickListener(new backButton());
        save=(Button)findViewById(R.id.save);
        save.setOnClickListener(new saveButton());
        title_text=(EditText)findViewById(R.id.title);
        content_text=(EditText)findViewById(R.id.content);

        dbHelper =new MyDatabaseHelper(this,"Diary.db",null,1);
        SQLiteDatabase db=dbHelper.getWritableDatabase();

        //若显示数据库中的日记
        Cursor cursor=db.query("diary",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                String temp_title=cursor.getString(cursor.getColumnIndex("diary_title"));
                String temp_time=cursor.getString(cursor.getColumnIndex("diary_time"));
//                Log.d("find","找到2");
                if (temp_time.equals(time)&&temp_title.equals(title)){//数据库中存在点进的item
                    //添加到内容文本
                    Log.d("find","找到");
                    String content=cursor.getString(cursor.getColumnIndex("diary_content"));
                    content_text.setText(content);
                    title_text.setText(temp_title);
                    id=temp_title+temp_time;//获取对应位置的id
                    title=temp_title;
                    time=temp_time;
                }
            }while(cursor.moveToNext());
        }


    }

    //返回监听器
    class backButton implements View.OnClickListener {
        public void onClick(View view) {
            AlertDialog.Builder builder=new AlertDialog.Builder(EditActivity.this);
            builder.setCancelable(false);
            builder.setTitle("提示");
            builder.setMessage("若退出则不会自动保存，请问是否退出？");
            builder.setPositiveButton("退出",new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog,int which){
                    Intent intent=new Intent(EditActivity.this,MainActivity.class);
                    dialog.dismiss();
                    startActivity(intent);
                    onBackPressed();
                }
            });
            builder.setNegativeButton("取消",null);
            builder.show();
        }
    }

    //保存监听器
    class saveButton implements View.OnClickListener {
        public void onClick(View view) {
            SQLiteDatabase db=dbHelper.getWritableDatabase();
            ContentValues values=new ContentValues();
            if (id.equals("")){//新的日记
                //获取保存时的时间
                SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss");
                time=ft.format(new Date());
                //获取标题
                title=title_text.getText().toString();
                //获取id
                id=title+time;
                values.put("diary_id",id);
                values.put("diary_title",title);
                values.put("diary_content",content_text.getText().toString());
                values.put("diary_time",time);
                values.put("diary_author",username);
                db.insert("diary",null,values);
            }else {//旧的日记
                values.put("diary_id",title_text.getText().toString()+time);
                values.put("diary_title",title_text.getText().toString());
                values.put("diary_content",content_text.getText().toString());
                values.put("diary_time",time);
                values.put("diary_author",username);
                String[] titletoedit={title_text.getText().toString()};
                String[] contenttoedit={content_text.getText().toString()};
                String[] newid={title_text.getText().toString()+time};
                db.update("diary",values,"diary_id=?",newid);
                db.update("diary",values,"diary_title=?",titletoedit);
                db.update("diary",values,"diary_content=?",contenttoedit);
            }

            Toast.makeText(EditActivity.this,"保存成功！",Toast.LENGTH_SHORT).show();
        }
    }
}
