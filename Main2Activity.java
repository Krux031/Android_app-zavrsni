package semi.kruno.opencv;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class Main2Activity extends AppCompatActivity {

    ImageView obradjena_slika;
    TextView vodostaj_text;
    Button uzmi_mjere;
    Button spremi_mjere;
    Bitmap bitmap;
    Bitmap tempbitmap;
    Uri ImageUri;

    int x1;
    int x2;
    int PixelsirinaRijeke;
    double MetarsirinaRijeke=0;
    double Vodostaj_mjera=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        obradjena_slika=(ImageView)findViewById(R.id.imgV_obradjena);
        spremi_mjere=(Button)findViewById(R.id.btn_spremi);
        uzmi_mjere=(Button)findViewById(R.id.btn_mjere);
        vodostaj_text=(TextView)findViewById(R.id.tv_vodostaj);

        Bundle slika = getIntent().getExtras();
        if(slika!=null){
            String obradjena_URI = slika.getString("URIslike");
            ImageUri=Uri.parse(obradjena_URI);
            try {
                tempbitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(ImageUri));
                Bitmap.Config config;
                if(tempbitmap.getConfig() != null){
                    config = tempbitmap.getConfig();
                }else{
                    config = Bitmap.Config.ARGB_8888;
                }

                bitmap = Bitmap.createBitmap(
                        tempbitmap.getWidth(),
                        tempbitmap.getHeight(),
                        config);

                Canvas canvasMaster = new Canvas(bitmap);
                canvasMaster.drawBitmap(tempbitmap, 0, 0, null);

                obradjena_slika.setImageBitmap(bitmap);
                crno_bijela();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        uzmi_mjere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pronadjiDesnuObalu();
                pronadjiLijevuObalu();
                pronadjiSirinuRijeke();
                pronadjiVodostaj();
            }
        });

        spremi_mjere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spremiMjerenja();
            }
        });
    }

    public void crno_bijela(){
        for(int i=0;i<=5;i++) { //zbog bolje uočljivosti ruba rijeke
            Mat image = new Mat();
            Mat grayImage = new Mat();
            Mat detectedEdges = new Mat();
            Utils.bitmapToMat(bitmap, image);
            Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
            Imgproc.blur(grayImage, detectedEdges, new Size(3, 3));
            Imgproc.Canny(detectedEdges, detectedEdges, 100, 50, 3, false);
            Utils.matToBitmap(detectedEdges, bitmap);
        }
    }

    public void pronadjiDesnuObalu(){
        for(int x = bitmap.getWidth()/2; x < bitmap.getWidth(); x++) {
            int brojac=0;
            for (int y = 0; y < bitmap.getHeight(); y++) {
                int piksel = bitmap.getPixel(x, y);
                if(Color.red(piksel)==255 && Color.blue(piksel)==255 && Color.green(piksel)==255){
                    brojac++;
                    if(brojac>700){
                        Canvas canvas=new Canvas(bitmap);
                        Paint zelena=new Paint();
                        zelena.setColor(Color.GREEN);
                        zelena.setStrokeWidth(15);
                        canvas.drawLine(x,0,x, bitmap.getHeight(),zelena);
                        x1=x; //x koordinata desne obale
                        obradjena_slika.setImageBitmap(bitmap);
                    }
                }
            }
            if(brojac>700) {
                break;
            }
        }
    }

    public void pronadjiLijevuObalu(){
        for(int x = bitmap.getWidth()/2; x >= 0; x--) {
            int brojac=0;
            for (int y = 0; y < bitmap.getHeight(); y++) {
                int piksel = bitmap.getPixel(x, y);
                if(Color.red(piksel)==255 && Color.blue(piksel)==255 && Color.green(piksel)==255){
                    brojac++;
                    if(brojac>700){
                        Canvas canvas=new Canvas(bitmap);
                        Paint zelena=new Paint();
                        zelena.setColor(Color.GREEN);
                        zelena.setStrokeWidth(15);
                        canvas.drawLine(x,0,x, bitmap.getHeight(),zelena);
                        x2=x; //x koordinata lijeve obale
                        obradjena_slika.setImageBitmap(bitmap);
                    }
                }
            }
            if(brojac>700) {
                break;
            }
        }
    }

    public void pronadjiSirinuRijeke(){
        PixelsirinaRijeke=x1-x2;
        MetarsirinaRijeke=PixelsirinaRijeke/21.3333333333333;
    } // 1 metar u stvarnosti iznosi 21.3333333333333 pixela na slici rezolucije 4000x3000

    public void pronadjiVodostaj(){
        double x;
        double y;
        if(MetarsirinaRijeke==0){
            vodostaj_text.setText(("Greška!!!"));
        }
        if(0<MetarsirinaRijeke && MetarsirinaRijeke<147.5){ //0.etapa
            y=0.08*MetarsirinaRijeke-11.6;
            vodostaj_text.setText("Vodostaj: "+String.valueOf(zaokruzi(y))+"m\nŠirina rijeke: "+ String.valueOf(zaokruzi(MetarsirinaRijeke))+"m");
            Vodostaj_mjera=zaokruzi(y);
        }
        if(147.5<=MetarsirinaRijeke && MetarsirinaRijeke<151) { //1.etapa
            x = MetarsirinaRijeke - 147.5;
            x = x / 2;
            y = (24 / 35) * x;
            y = y + 0.2;
            vodostaj_text.setText("Vodostaj: " + String.valueOf(zaokruzi(y)) + "m\nŠirina rijeke: " + String.valueOf(zaokruzi(MetarsirinaRijeke))+"m");
            Vodostaj_mjera=zaokruzi(y);
        }
        if(151<=MetarsirinaRijeke && MetarsirinaRijeke<153.2){ //<1., 2.>etapa
            vodostaj_text.setText("Vodostaj: 1.4m\nŠirina rijeke: " + String.valueOf(zaokruzi(MetarsirinaRijeke))+"m");
            Vodostaj_mjera=1.40;
        }
        if(153.2<=MetarsirinaRijeke && MetarsirinaRijeke<160) { //2.etapa
            x = MetarsirinaRijeke - 153.2;
            x = x / 2;
            y = (44 / 67) * x;
            y = y + 1.2 + 0.2;
            vodostaj_text.setText("Vodostaj: " + String.valueOf(zaokruzi(y)) + "m\nŠirina rijeke: " + String.valueOf(zaokruzi(MetarsirinaRijeke))+"m");
            Vodostaj_mjera=zaokruzi(y);
        }
        if(160<=MetarsirinaRijeke && MetarsirinaRijeke<163) { //<2., 3.>etapa
            vodostaj_text.setText("Vodostaj: 3.6m\nŠirina rijeke: " + String.valueOf(zaokruzi(MetarsirinaRijeke))+"m");
            Vodostaj_mjera=3.60;
        }
        if(163<=MetarsirinaRijeke && MetarsirinaRijeke<169.6) { //3.etapa
            x = MetarsirinaRijeke - 163;
            x = x / 2;
            y = (2 / 3) * x;
            y = y + 3.4 + 0.2;
            vodostaj_text.setText("Vodostaj: " + String.valueOf(zaokruzi(y)) + "m\nŠirina rijeke: " + String.valueOf(zaokruzi(MetarsirinaRijeke))+"m");
            Vodostaj_mjera=zaokruzi(y);
        }
        if(MetarsirinaRijeke>=169.6){
            vodostaj_text.setText(("Vodostaj: >5.8m\nŠirina rijeke: " + String.valueOf(zaokruzi(MetarsirinaRijeke))+"m"));
            Vodostaj_mjera=5.80;
        }
    }

    public double zaokruzi(double broj){ // na 2. decimale
        double novi_broj=broj*100;
        novi_broj=Math.round(novi_broj);
        novi_broj=novi_broj/100;
        return novi_broj;
    }

    public void spremiMjerenja(){
        if(Vodostaj_mjera!= 0 && MetarsirinaRijeke!=0) {
            Intent pocetni_prozor = new Intent();
            pocetni_prozor.putExtra("vodostaj", String.valueOf(Vodostaj_mjera));
            pocetni_prozor.putExtra("sirina", String.valueOf(zaokruzi(MetarsirinaRijeke)));
            setResult(RESULT_OK, pocetni_prozor);
            finish();
        }
        else
            Toast.makeText(Main2Activity.this,"Prvo uzmi mjere rijeke!", Toast.LENGTH_LONG).show();
    }
}
