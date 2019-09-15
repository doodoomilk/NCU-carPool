package even.carpool;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.util.Log;

public class OfficialCountManTimeThread extends Thread {


    public OfficialCountManTimeThread() {
        // TODO Auto-generated constructor stub
    }
    public void run() {
        try {
            String url = "http://140.115.197.16/?school=ncu&app=NCUCarpool";
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);
            try {
                HttpResponse response = client.execute(request);
            } catch (Exception e) {
                // Exception
            }

        }catch (Exception e){
            e.printStackTrace();
            Log.e("Exception", e.toString());
        }
    }

}
