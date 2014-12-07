package gl.iglou.studio.nanosense;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by metatomato on 07.12.14.
 */
public class MPFragment extends Fragment{

    private String mTextContent = "Holà señor metaTomato!!";

    public MPFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mp, container, false);
        TextView text = (TextView)rootView.findViewById(R.id.section_label);
        text.setText(mTextContent);
        return rootView;
    }
}
