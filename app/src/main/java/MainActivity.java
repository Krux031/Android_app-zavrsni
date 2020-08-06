package semi.kruno.opencv;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.support.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.opencv.android.OpenCVLoader;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    ImageView Slika;
    Button izaberi;
    Button obradi;
    Button trend;
    TextView tv;
    Uri ImageUri;
    private static final int PICK_IMAGE=100;
    DatabaseHelper moja_baza;

    double vodostaj=0;
    double sirina=0;
    String datum;


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        moja_baza=new DatabaseHelper(this);

        Slika=(ImageView)findViewById(R.id.imgV_slika);
        izaberi=(Button)findViewById(R.id.btn_izaberi);
        obradi=(Button)findViewById(R.id.btn_obradi);
        trend=(Button)findViewById(R.id.btn_trend);
        tv =(TextView)findViewById(R.id.sample_text);

        trend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otvoriTrend();
            }
        });

        izaberi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otvoriGaleriju();
            }
        });

        obradi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obradiSliku();
            }
        });


        tv.setText(stringFromJNI());
        if(!OpenCVLoader.initDebug()){
            tv.setText(tv.getText() + "\nOpenCVLoader ne radi.");
        }
        else{
            tv.setText(tv.getText() + "\nOpenCVLoader radi.");
            tv.setText(tv.getText() + "\n" + validate(0L, 0L));
        }
    }

    private void otvoriTrend(){
        Cursor res = moja_baza.getData();
        if(res.getCount()==0)
            Toast.makeText(MainActivity.this,"Nema podataka u bazi",Toast.LENGTH_LONG).show();
        else{
            StringBuffer buffer=new StringBuffer();
            while(res.moveToNext()){
                buffer.append("ID: "+res.getString(0)+"\n");
                buffer.append("Datum: "+res.getString(1)+"\n");
                buffer.append("Vodostaj: "+res.getString(2)+"m\n");
                buffer.append("Širina rijeke: "+res.getString(3)+"m\n");
                buffer.append("Trend: "+res.getString(4)+"cm\n\n");
            }
            prikaziBazu("Trend vodostaja", buffer.toString());
        }
    }
    public void prikaziBazu(String naslov, String poruka){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(naslov);
        builder.setMessage(poruka);
        builder.show();
    }

    private void otvoriGaleriju(){
        Intent Galerija =new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(Galerija, PICK_IMAGE);
    }

    private void obradiSliku() {
        Intent otvoriNoviProzor = new Intent(MainActivity.this, Main2Activity.class);
        if (ImageUri == null) {
            Toast.makeText(MainActivity.this, "Izaberi prvo sliku", Toast.LENGTH_LONG).show();
        } else {
            otvoriNoviProzor.putExtra("URIslike", ImageUri.toString());
            startActivityForResult(otvoriNoviProzor, 1);
        }
    }


    @Override
    protected void onActivityResult(int RequestCode, int ResultCode, Intent data){
        super.onActivityResult(RequestCode, ResultCode, data);
        String datetext="nema";
        if(ResultCode == RESULT_OK && RequestCode == PICK_IMAGE){
            ImageUri =data.getData();
            Slika.setImageURI(ImageUri);
            try {
                InputStream in;
                in=getContentResolver().openInputStream(ImageUri);
                ExifInterface intf = new ExifInterface(in);
                datetext=intf.getAttribute(ExifInterface.TAG_DATETIME);
            } catch(IOException e) {
                e.printStackTrace();
            }
            datum=datetext;
        }
        if(ResultCode==RESULT_OK && RequestCode==1){
            vodostaj=Double.parseDouble(data.getStringExtra("vodostaj"));
            sirina=Double.parseDouble(data.getStringExtra("sirina"));

            Cursor res=moja_baza.getData();
            double zadnje_mjerenje;
            int trend_value;

            if(res.getCount()==0){
                ubaci_podatke(0, "+");
            }
            else{
                res.moveToLast();
                zadnje_mjerenje=res.getDouble(2);
                zadnje_mjerenje=zadnje_mjerenje*100; //kako bi dobili zadnji zapisan vodostaj u cm
                vodostaj=vodostaj*100; //kako bi dobili trenutni vodostaj u cm
                trend_value=(int)(vodostaj-zadnje_mjerenje);
                vodostaj=vodostaj/100; //kako bi vratili trenutni vodostaj u m
                if(trend_value>=0) {
                    ubaci_podatke(trend_value, "+");
                }
                else{
                    ubaci_podatke(trend_value, ""); //predznak ce biti "-"
                }
            }

        }
    }

    public void ubaci_podatke(int vrijednost_trenda, String predznak){
        boolean provjera= moja_baza.insertData(datum,vodostaj,sirina,predznak+String.valueOf(vrijednost_trenda));
        if(provjera==true)
            Toast.makeText(MainActivity.this,"Uspješno spremljeno",Toast.LENGTH_LONG).show();
        else
            Toast.makeText(MainActivity.this,"Greška u spremanju",Toast.LENGTH_LONG).show();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native String validate(long matAddrGr, long matAddrRgba);
}
