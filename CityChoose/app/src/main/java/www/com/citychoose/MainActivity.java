package www.com.citychoose;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import www.com.citychoise.view.AddressDialogVP;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AddressDialogVP dialog=new AddressDialogVP(this);
        dialog.show();
        dialog.setAddressSelectListener(new AddressDialogVP.AddressSelectListener() {
            @Override
            public void addressSelect(String address) {
                Log.e("address",address);
            }
        });
    }
}
