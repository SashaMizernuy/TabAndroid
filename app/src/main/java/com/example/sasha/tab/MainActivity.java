package com.example.sasha.tab;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;



public class MainActivity extends AppCompatActivity {

    public static DBHelper dbHelper;
    public static SQLiteDatabase db;
    public static boolean chFields = false;
    //    public static ArrayMap<String, String> labels = new ArrayMap<>();
//    public static ArrayMap<String, String> types = new ArrayMap<>();
//    public static ArrayMap<String, String> nulls = new ArrayMap<>();
    public static ArrayList<String> keys = new ArrayList<>();
    public static ArrayList<Integer> widths = new ArrayList<>();
    ArrayList<ArrayMap<String, String>> peoples = new ArrayList<>();
    public  static ArrayList<ArrayMap<String, String>> fields = new ArrayList<>();
    ListOfMapsAdapter peopleAdapter;
    ListView list;

    ArrayList<View> viewsList = new ArrayList<>();
    Cursor cursorP;
    Cursor cursorF;
    final Context context=this;
    FloatingActionButton fabChange;
    FloatingActionButton fabAdd;
    FloatingActionButton fabDel;
    String imagePath;
    ImageView imageContent;
    Calendar myCalendar=Calendar.getInstance();
    EditText edData;
    TabHost tabHost;






    void showPref() {
        startActivity(new Intent(this, PrefActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting: {
                showPref();
                break;
            }
            default: {
            }
        }
        return super.onOptionsItemSelected(item);
    }

    class MyOnItemClickListener implements  AdapterView.OnItemClickListener { // Устанавливаем слушатель на элементы listView

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {//Вызывается при нажатии на элемент списка
            peopleAdapter.selectedIndex=position;
            if(peopleAdapter.selectedView!=null)
                peopleAdapter.selectedView.setBackgroundColor(Color.WHITE);
            peopleAdapter.selectedView=view;
            view.setBackgroundColor(Color.parseColor("#c0ff57"));

            int n=peopleAdapter.selectedIndex;
            ArrayMap<String,String> p=peoples.get(n);

            for(int i = 0; i< viewsList.size(); i++){
                //////////////////////
                EditText editText;
                EditText edData;
                Spinner sp;
                ImageView photo;
                View v=viewsList.get(i);
                if(v instanceof EditText) {
                    editText = (EditText)v;
                    editText.setText(p.get(keys.get(i)));

                    edData=(EditText)v;//Дата
                    edData.setText(p.get(keys.get(i)));

                }
                else if(v instanceof Spinner){
                    sp=(Spinner)v;
                    String num=p.get(keys.get(i));
                    if(num!=null)
                        sp.setSelection(Integer.parseInt(num));
                }
                else if(v instanceof ImageView){
                    photo=(ImageView) v;
                    String fn=p.get(keys.get(i));
                    Log.i("Script","ImagePath="+fn);
                    try {
                        InputStream imageStream = getContentResolver().openInputStream(Uri.parse(fn));
                        photo.setImageBitmap(BitmapFactory.decodeStream(imageStream));


                    }
                    catch (NullPointerException e) {
                        photo.setImageBitmap(null);
                        e.printStackTrace();
                    }
                    catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    class MyOnLongClickListener implements View.OnLongClickListener{
        @Override
        public boolean onLongClick(View view) {
            EditText ed=(EditText)view;
            String tel = ed.getText().toString();
            Intent call = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + tel));
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)
                startActivity(call);
            return true;
        }
    }



    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int month,
                              int dayOfMonth) {
            // TODO Auto-generated method stub

            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            month = month+1;
            String formattedMonth = "" + month;
            String formattedDayOfMonth = "" + dayOfMonth;
            edData.setText(formattedDayOfMonth+"-"+formattedMonth+"-"+year);
        }
    };

    class OnDateClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view){
            new DatePickerDialog( context,date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            edData=(EditText)view;

        }

    }

    class MyOnTabChangeListener implements TabHost.OnTabChangeListener {
        @Override
        public void onTabChanged(String tabId) {
            Toast.makeText(getBaseContext(), "tabId = " + tabId, Toast.LENGTH_SHORT).show();

        }
    }



    public void delPeople(View view) {
        int n = peopleAdapter.selectedIndex;
        if (n <= -1) {
            Snackbar.make(view, "Сперва выбирите поле удаления!", Snackbar.LENGTH_LONG).show();
        } else {
            String id = peoples.get(n).get(keys.get(0));
            long rowID = db.delete("peoples", keys.get(0) + "=" + id, null);
            if (rowID != -1) {
                peoples.remove(n);
                Snackbar.make(view, "Поле удалено!", Snackbar.LENGTH_LONG).show();
                peopleAdapter.notifyDataSetChanged();
            }
        }
    }


    public void changePeople(View view) {
        ArrayMap<String, String> p = new ArrayMap<>();
        EditText edId = (EditText) viewsList.get(0);
        if (edId.length() == 0)
            Snackbar.make(view, "Сперва выберите поле для изменений!", Snackbar.LENGTH_LONG).show();
        else {
            String id = edId.getText().toString();
            ContentValues cv = new ContentValues();// создаем объект для данных
            for (int i = 0; i < keys.size(); i++) {
                String type = fields.get(i).get("type");
                String val ="";
                if(Integer.parseInt(type)==FieldType.LIST)
                    val=String.valueOf(((Spinner)viewsList.get(i)).getSelectedItemPosition());
                else if(Integer.parseInt(type)==FieldType.PHOTO) {
                    val = imagePath;
                }
                else if(Integer.parseInt(type)==FieldType.DATA){
                    val=edData.getText().toString();
                }
                else
                    val = ((EditText) (viewsList.get(i))).getText().toString().trim();
                p.put(keys.get(i), val);
                if (i > 0)
                    cv.put(keys.get(i), val);
            }
            long rowID = db.update("peoples", cv, keys.get(0) + "=" + id, null);
            if (rowID != -1) {
                peoples.set(peopleAdapter.selectedIndex, p);
                Snackbar.make(view, "Поле изменено!", Snackbar.LENGTH_LONG).show();
                peopleAdapter.notifyDataSetChanged();
            }
        }
    }

    public void addPeople(View view) {
        ArrayMap<String, String> p = new ArrayMap<>();
        ContentValues cv = new ContentValues();// создаем объект для данных
        for (int i = 1; i < keys.size(); i++) {
            String empty = fields.get(i).get("empty");
            String label = fields.get(i).get("name");
            String type = fields.get(i).get("type");
            String val ="";
            if(Integer.parseInt(type)==FieldType.LIST)
                val=String.valueOf(((Spinner)viewsList.get(i)).getSelectedItemPosition());
            else if(Integer.parseInt(type)==FieldType.PHOTO) {
                val = imagePath;
            }
            else if(Integer.parseInt(type)==FieldType.DATA){
                val=edData.getText().toString();
            }
            else
                val=((EditText)viewsList.get(i)).getText().toString().trim();

            if (empty.equals("1") && val.equals("")) {
                Snackbar.make(view, "Поле " + label + " не может быть пустым!", Snackbar.LENGTH_LONG).show();
                return;
            } else {
                p.put(keys.get(i), val);
                cv.put(keys.get(i), val);
            }
        }
        long rowID = db.insert("peoples", null, cv);
        if (rowID != -1) {
            p.put(keys.get(0), "" + rowID);
            peoples.add(p);
            Snackbar.make(view, "Контакт добавлен!", Snackbar.LENGTH_LONG).show();
            peopleAdapter.notifyDataSetChanged();
        }
    }


    class MyOnImageClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view){
            LinearLayout l=(LinearLayout) view.getParent();
            imageContent=(ImageView) l.getChildAt(1);
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);//Вызываем стандартную галерею для выбора изображения с помощью Intent.ACTION_PICK:
            photoPickerIntent.putExtra("line","good");
            photoPickerIntent.setType("image/*");//Тип получаемых объектов - image:
            startActivityForResult(photoPickerIntent, 1);//Запускаем переход с ожиданием обратного результата в виде информации об изображении:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if(requestCode==1) {
            if(resultCode == RESULT_OK){
                Log.i("Script","Intent="+imageReturnedIntent.getStringExtra("line"));
                Uri imageUri = imageReturnedIntent.getData();
                imagePath=imageUri.toString();
                try {
                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    imageContent.setImageBitmap(BitmapFactory.decodeStream(imageStream));

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fabChange=(FloatingActionButton) findViewById(R.id.fabChange);
        fabAdd=(FloatingActionButton) findViewById(R.id.fabAdd);
        fabDel=(FloatingActionButton) findViewById(R.id.fabDelate);
        tabHost=(TabHost)findViewById(R.id.tabhost);


        initFields();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (chFields) initFields();
    }

    public void initFields() {
        chFields = false;
        keys.clear();
        widths.clear();
        peoples.clear();
        fields.clear();


        dbHelper = new DBHelper(this);//создаем объект для создания и управления версиями БД
        db = dbHelper.getWritableDatabase();// подключаемся к БД
        cursorP = db.query("peoples", null, null, null, null, null, null);// делаем запрос всех данных из таблицы mytable, получаем Cursor
        cursorF = db.query("fields", null, null, null, null, null, null);// делаем запрос всех данных из таблицы mytable, получаем Cursor
        String[] dbFields = cursorP.getColumnNames();
        for (int i = 0; i < dbFields.length; i++)
            keys.add(dbFields[i]);
        if (cursorF.moveToFirst()) {// ставим позицию курсора на первую строку выборки если в выборке нет строк, вернется false
            do {
                ArrayMap<String,String> am=new ArrayMap<>();
                int n = cursorF.getColumnIndex("fId");
                String fId = cursorF.getString(n);
                n = cursorF.getColumnIndex("name");
                String lb = cursorF.getString(n);
                am.put("name", lb);
                n = cursorF.getColumnIndex("type");
                String tp = cursorF.getString(n);
                am.put("type", tp);
                n = cursorF.getColumnIndex("empty");
                String np = cursorF.getString(n);
                am.put("empty", np);
                n = cursorF.getColumnIndex("width");
                String wp = cursorF.getString(n);
                am.put("width",wp);
                n=cursorF.getColumnIndex("list");
                String ls=cursorF.getString(n);
                Log.i("Script","List="+ls);
                am.put("list",ls);

                n=cursorF.getColumnIndex("tab");
                String tb=cursorF.getString(n);
                am.put("tab",tb);


                fields.add(am);
                widths.add(Integer.parseInt(wp));
            } while (cursorF.moveToNext());
        }
        else cursorF.close();

        final LinearLayout ll = (LinearLayout) findViewById(R.id.edTabContainer);
        final LinearLayout ll1 = (LinearLayout) findViewById(R.id.edTabContainer1);

        tabHost.setup();// инициализация


        TabHost.TabSpec tabSpec;

        // создаем вкладку и указываем тег
        tabSpec = tabHost.newTabSpec("tag1");
        // название вкладки
        tabSpec.setIndicator("Вкладка 1");
        // указываем id компонента из FrameLayout, он и станет содержимым
        tabSpec.setContent(R.id.edTabContainer);
        // добавляем в корневой элемент
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag2");
        tabSpec.setIndicator("Вкладка 2");
        tabSpec.setContent(R.id.edTabContainer1);
        tabHost.addTab(tabSpec);

        tabHost.setCurrentTabByTag("tag1");// первая вкладка будет выбрана по умолчанию
        tabHost.setOnTabChangedListener(new MyOnTabChangeListener());


        ll.removeAllViews();
        ll1.removeAllViews();
        LinearLayout.LayoutParams layoutParamsll = new LinearLayout.LayoutParams(170, LinearLayout.LayoutParams.WRAP_CONTENT);
        viewsList.clear();


        for (int i = 0; i < keys.size(); i++) {
            String fName = keys.get(i);
            String fLabel = fields.get(i).get("name");
            String fType=fields.get(i).get("type");
            String fList=fields.get(i).get("list");


            String fTab=fields.get(i).get("tab");



            LinearLayout l = new LinearLayout(this);
            l.setOrientation(LinearLayout.HORIZONTAL);
            TextView tv = new TextView(this);
            tv.setLayoutParams(layoutParamsll);
            tv.setText(fLabel);
            l.addView(tv);

            Spinner sp=new Spinner(this);
            final EditText ed = new EditText(this);
            ImageView photo=new ImageView(this);
            EditText edData=new EditText(this);

            switch (Integer.parseInt(fType)){
                case FieldType.TEXT:
                case FieldType.TEL:
                case FieldType.DIGIT:
                    if (i == 0)
                        ed.setEnabled(false);
                    ed.setLayoutParams(layoutParamsll);
                    viewsList.add(ed);
                    l.addView(ed);
                    if (Integer.parseInt(fType)==FieldType.TEL)
                        ed.setOnLongClickListener(new MyOnLongClickListener()); //Листенер длинный с выводом лога
                    break;
                case  FieldType.LIST:
                    sp.setLayoutParams(layoutParamsll);
                    viewsList.add(sp);
                    l.addView(sp);
                    String [] spData =fList.split(";");
                    ArrayAdapter spinnerValueAdapter=new ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,spData);
                    sp.setAdapter(spinnerValueAdapter);
                    break;
                case FieldType.PHOTO:
                    Log.i("Script","PHOTO");
                    Button bt=new Button(this);
                    bt.setText("+");
                    bt.setOnClickListener(new MyOnImageClickListener());
                    photo.setLayoutParams(new LinearLayout.LayoutParams(60,60));
                    viewsList.add(photo);
                    l.addView(photo);
                    l.addView(bt);
                    break;
                case FieldType.DATA:
                    edData.setOnClickListener(new OnDateClickListener());
                    edData.setLayoutParams(layoutParamsll);
                    viewsList.add(edData);
                    l.addView(edData);
                    break;

            }
                ll.addView(l);
        }
        if(cursorP.moveToFirst()) {// ставим позицию курсора на первую строку выборки если в выборке нет строк, вернется false
            do {
                ArrayMap<String,String> p=new ArrayMap<>();
                for(int i=0;i<keys.size();i++) {
                    String fld=keys.get(i);
                    int n= cursorP.getColumnIndex(fld);
                    p.put(fld, cursorP.getString(n));
                }
                peoples.add(p);
            } while (cursorP.moveToNext());
        }
        else cursorP.close();
        list=(ListView) findViewById(R.id.people);
        peopleAdapter=new ListOfMapsAdapter(this,peoples,R.layout.list_item);
        peopleAdapter.keys=keys;
        Log.i("Script","KEYS="+keys);
        peopleAdapter.widths=widths;
        list.setAdapter(peopleAdapter);

        list.setOnItemClickListener(new MyOnItemClickListener()); // Устанавливаем слушатель на элементы listView





    }
}
//[{f1->"1",f2->"Саша"},{f1->"2",f2->"Ира"}] peoples
//[{name->"Номер",type->"2",empty->"0"},{name->"Имя",type->"0",empty->"1"}]