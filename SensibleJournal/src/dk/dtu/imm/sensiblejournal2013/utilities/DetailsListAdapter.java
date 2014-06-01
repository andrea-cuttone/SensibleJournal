package dk.dtu.imm.sensiblejournal2013.utilities;

import java.util.ArrayList;
import java.util.List;

import dk.dtu.imm.sensiblejournal2013.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailsListAdapter extends BaseAdapter implements OnClickListener {
    private Context context;
    private List<TwoStringListObject> details;
    private ArrayList<Integer> listIcons;
    private int rowLayout;

    public DetailsListAdapter(Context context, List<TwoStringListObject> itineraryDetails, ArrayList<Integer> listIcons, int rowLayout) {
        this.context = context;
        this.details = itineraryDetails;
        this.listIcons = listIcons;
        this.rowLayout = rowLayout;        
    }

    public int getCount() {
        return details.size();
    }

    public Object getItem(int position) {
        return details.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup viewGroup) {
    	TwoStringListObject entry = details.get(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(rowLayout, null);
        }
        TextView address = (TextView) convertView.findViewById(R.id.list_row_address);
        address.setTextColor(context.getResources().getColor(R.color.color_main_text));
        address.setText(entry.getString1());

        TextView time = (TextView) convertView.findViewById(R.id.list_row_details);
        time.setTextColor(context.getResources().getColor(R.color.color_secondary_text));
        time.setText(entry.getString2());
        
        if (listIcons != null) {
        	ImageView icon = (ImageView) convertView.findViewById(R.id.details_list_icon);
        	icon.setImageResource(listIcons.get(position));
        }

        return convertView;
    }

    @Override
    public void onClick(View view) {    	
    }
}