package semi.kruno.opencv;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Trend_vodostaja.db";
    public static final String TABLE_NAME = "trend_table";
    public static final String COL_ID = "ID";
    public static final String COL_DATUM = "DATUM";
    public static final String COL_VODOSTAJ = "VODOSTAJ";
    public static final String COL_SIRINA = "ŠIRINA";
    public static final String COL_TREND = "TREND";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        //SQLiteDatabase db=this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+TABLE_NAME+"(ID INTEGER PRIMARY KEY, DATUM TEXT, VODOSTAJ DOUBLE, ŠIRINA DOUBLE, TREND VARCHAR(7));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String datum, double vodostaj, double sirina, String trend){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(COL_DATUM, datum);
        contentValues.put(COL_VODOSTAJ, vodostaj);
        contentValues.put(COL_SIRINA, sirina);
        contentValues.put(COL_TREND, trend);
        long result = db.insert(TABLE_NAME, null, contentValues);
        if(result==-1)
            return false;
        else
            return true;
    }

    public Cursor getData(){
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor res=db.rawQuery("SELECT * FROM "+TABLE_NAME,null);
        return res;
    }
}
