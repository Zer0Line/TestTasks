package ru.gurucode.wmoney;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import ru.gurucode.emailtextview.OnChipClickListener;
import ru.gurucode.emailtextview.EmailTextView;

public class RootActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root);

        EmailTextView etv = (EmailTextView)findViewById(R.id.emailtv);
        //Назначить callback для клика по чипу
        etv.onChipClickListener = new OnChipClickListener() {
            @Override
            public void onChipClick(String email) {
                Toast.makeText(RootActivity.this, email, Toast.LENGTH_SHORT).show();
            }
        };

        etv.setText("123 sergey@gurucode.ru and this is \n mtrololo@asd.com email" +
                        "\n mtrololo@asd.co 456464");
        etv.mFoolEmailInChip = true;
        etv.setBackground(getResources().getDrawable(R.drawable.shape_background));
    }
}
