package rawe.gordon.com.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import rawe.gordon.com.SpiManager;
import rawe.gordon.com.framework.IDemoService;
import rawe.gordon.com.framework.IInheritService;
import rawe.gordon.com.services.InheritServiceA;
import rawe.gordon.com.services.InheritServiceB;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn).setOnClickListener(this);
        findViewById(R.id.btn_a).setOnClickListener(this);
        findViewById(R.id.btn_b).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn) {
            Toast.makeText(this,
                    SpiManager.getInstance()
                            .getService(IDemoService.class).tellMeWhoAreYou(),
                    Toast.LENGTH_LONG).show();
        } else if (R.id.btn_a == v.getId()) {
            Toast.makeText(this,
                    SpiManager.getInstance()
                            .getServices(IInheritService.class, InheritServiceA.class)
                            .showIdentity(),
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this,
                    SpiManager.getInstance()
                            .getServices(IInheritService.class, InheritServiceB.class)
                            .showIdentity(),
                    Toast.LENGTH_LONG).show();
        }
    }
}
