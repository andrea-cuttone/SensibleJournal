package dk.dtu.imm.sensiblejournal2013.detailedViews.utilities;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.ui.IconGenerator;
import dk.dtu.imm.sensiblejournal2013.R;

public class CustomInfoWindowAdpater implements InfoWindowAdapter {
    private final View mymarkerview;
    private Context context;

    public CustomInfoWindowAdpater(Context context) {
       mymarkerview = ((Activity) context).getLayoutInflater()
    		   			.inflate(R.layout.custom_info_window, null);
       this.context = context;
    }

    public View getInfoWindow(Marker marker) {      
        render(marker, mymarkerview);
        return mymarkerview;
    }

    public View getInfoContents(Marker marker) {
       return null;
    }

    private void render(Marker marker, View view) {
    	IconGenerator iconFactory = new IconGenerator(context);
    	iconFactory.setStyle(IconGenerator.STYLE_BLUE);
    	ImageView infoView = (ImageView) mymarkerview.findViewById(R.id.info);
    	infoView.setImageBitmap(iconFactory.makeIcon(marker.getTitle() + "\n" + marker.getSnippet()));
    }
}
