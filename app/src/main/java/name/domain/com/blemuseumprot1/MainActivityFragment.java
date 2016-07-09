package name.domain.com.blemuseumprot1;

import android.content.res.TypedArray;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    ImageView img;
    TextView tv;
    public MainActivityFragment() {
    }
    public void setter(String val,int resid)
    {
        img.setImageResource(resid);
        tv.setText(val);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_main, container, false);;
        img=(ImageView)v.findViewById(R.id.imageView);
        tv=(TextView)v.findViewById(R.id.textview);
        return v;
    }
}
