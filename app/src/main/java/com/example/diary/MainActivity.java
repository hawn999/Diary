package com.example.diary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText username;
    private Button edit_username_button,addDiary;
    private String name="";//用户名(初始值为空,每次修改都会更新)
    private MyDatabaseHelper dbHelper;
    private List<Diary> diaryList=new ArrayList<>();
    private Diary diary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //启动组件
        username=(EditText)findViewById(R.id.username);
        edit_username_button=(Button)findViewById(R.id.edit_username);
        addDiary=(Button)findViewById(R.id.add_diary);
        final ListView listView=(ListView) findViewById(R.id.list_view);

//        //实例化SharedPreferences对象
//        SharedPreferences.Editor editor=getSharedPreferences("data",MODE_PRIVATE).edit();
//        editor.putString("username"+name,"默认用户名");
//        editor.commit();

        //读取SharedPreferences中默认的用户名,并在EditText中显示
        SharedPreferences prefs=getSharedPreferences("data",MODE_PRIVATE);
        String edit_name=prefs.getString("username"+name,"默认用户名");
        username.setText(edit_name);
        //设置编辑用户名按钮事件
        edit_username_button.setOnClickListener(new edit_username());


        //设置添加按钮监听器
        addDiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,EditActivity.class);
                //作者名字
                intent.putExtra("name",username.getText().toString());
                startActivity(intent);
            }
        });
        //设置ListView监听器
        // 如果点击则跳转
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Diary diary=diaryList.get(position);
                Intent intent=new Intent(MainActivity.this,EditActivity.class);
                //传输标题
                intent.putExtra("title",diary.getTitle());
                //传输时间
                intent.putExtra("time",diary.getTime());
                //作者名字
                intent.putExtra("name",username.getText().toString());
                //记录点击的标题
                Log.d("DiaryTitleActivity",diary.getTitle());
                startActivity(intent);
            }
        });
        //长按删除
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,final int position, long id) {
                //长按则显示出一个对话框
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("提示") //无标题
                        .setMessage("是否删除日记") //内容
                        .setCancelable(false)//点击对话框外对话框不会消失
                        .setNegativeButton("取消",null) //连个按钮
                        .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                            //删除键
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Diary diary=diaryList.get(position);
                                dbHelper =new MyDatabaseHelper(MainActivity.this,"Diary.db",null,1);
                                SQLiteDatabase db=dbHelper.getWritableDatabase();
                                //item的id
                                String a=diary.getTitle().substring(3);
                                String b=diary.getTime().substring(5);
                                String[] id={a+b};
                                db.delete("diary", "diary_id=?",id );    //把对应数据删除
                                Cursor cursor=db.query("diary",null,null,null,null,null,null);
                                diaryList.clear();  //清空当前ListView重新写入
                                if(cursor.moveToFirst()){
                                    do{
                                        String title=cursor.getString(cursor.getColumnIndex("diary_title"));
                                        String time=cursor.getString(cursor.getColumnIndex("diary_time"));
                                        diary=new Diary("标题:"+title,"创建时间:"+time);
                                        diaryList.add(diary);
                                    }while (cursor.moveToNext());
                                }
                                DiaryAdapter adapter=new DiaryAdapter(MainActivity.this,R.layout.item,diaryList);
                                ListView listView=(ListView) findViewById(R.id.list_view);

                                listView.setAdapter(adapter);
                            }
                        }).show();
                return false;
            }
        });
    }

    //点击修改作者用户名
    class edit_username implements View.OnClickListener{
        public void onClick(View view){
            String changed_name=username.getText().toString();
            if (!changed_name.equals(name)){
                SharedPreferences.Editor editor=getSharedPreferences("data",MODE_PRIVATE).edit();
                editor.putString("username"+changed_name,changed_name);
                editor.commit();
                name=changed_name;
            }else{
                Toast.makeText(MainActivity.this,"两次用户名不能相同,请重新输入！",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {//从数据库加载ListView
        super.onStart();
        dbHelper =new MyDatabaseHelper(this,"Diary.db",null,1);
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        //清空列表
        diaryList.clear();
        //加载所有日记
        Cursor cursor=db.query("diary",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                String title=cursor.getString(cursor.getColumnIndex("diary_title"));
                String time=cursor.getString(cursor.getColumnIndex("diary_time"));
                diary=new Diary("标题:"+title,"创建时间:"+time);
                diaryList.add(diary);
            }while (cursor.moveToNext());
        }

        DiaryAdapter adapter=new DiaryAdapter(MainActivity.this,R.layout.item,diaryList);
        ListView listView=(ListView) findViewById(R.id.list_view);

        listView.setAdapter(adapter);
    }
}

class DiaryAdapter extends ArrayAdapter<Diary> {
    private int resourceId;
    public DiaryAdapter(@NonNull Context context, int resource, List<Diary> objects) {
        super(context, resource,objects);
        resourceId=resource;
    }
    @Override
    //修改getView，提高性能
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Diary diary=getItem(position);
        View view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        TextView item_title=(TextView) view.findViewById(R.id.item_title);
        TextView item_time=(TextView) view.findViewById(R.id.item_time);
        item_title.setText(diary.getTitle());
        item_time.setText(diary.getTime());
        return view;
    }
}
