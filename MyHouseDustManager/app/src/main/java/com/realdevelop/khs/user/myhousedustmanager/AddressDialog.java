package com.realdevelop.khs.user.myhousedustmanager;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AddressDialog extends Dialog {

    Button button_ok;
    TextView edit_ip;

    public AddressDialog(@NonNull Context context, String initialtext) {
        super(context);
        setContentView(R.layout.address_dialog);
        setCancelable(false);

        edit_ip = findViewById(R.id.edit_ip);
        edit_ip.setText(initialtext);

        button_ok = findViewById(R.id.btn_confirm);
        button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.serverUri = edit_ip.getText().toString();
                dismiss();
            }
        });
    }

}
