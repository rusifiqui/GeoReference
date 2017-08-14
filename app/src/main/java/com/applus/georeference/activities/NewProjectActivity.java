package com.applus.georeference.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.applus.georeference.MainActivity;
import com.applus.georeference.R;
import com.applus.georeference.helpers.DbHelper;

public class NewProjectActivity extends AppCompatActivity {

    private EditText projectName;
    private EditText projectDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_project);

        projectName = (EditText) findViewById(R.id.editTextProjectName);
        projectDesc = (EditText) findViewById(R.id.editTextProjectDesc);
        Button createProject = (Button) findViewById(R.id.buttonCreateProject);

        if (createProject != null) {
            createProject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(validateFields()){
                        if(createProject()){
                            Toast toast = Toast.makeText(getApplicationContext(), R.string.project_created, Toast.LENGTH_SHORT);
                            toast.show();
                            // Se vuelve a la actividad principal
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(intent);
                        }else{
                            Toast toast = Toast.makeText(getApplicationContext(), R.string.project_error, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }else{
                        Toast toast = Toast.makeText(getApplicationContext(), R.string.complete_fields, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            });
        }
    }

    /**
     * Método encargado de validar los campos que pueden ser introducidos por el usuario.
     * @return true si son correctos, false en caso contrario.
     */
    private boolean validateFields(){
        if(projectName.getText().length() == 0 || projectName.getText().toString().equals("")){
            return false;
        }
        if(projectDesc.getText().length() == 0 || projectDesc.getText().toString().equals("")){
            return false;
        }
        return true;
    }

    private boolean createProject(){
        return DbHelper.saveProject(projectName.getText().toString(), projectDesc.getText().toString(), this.getApplicationContext());
    }
}
