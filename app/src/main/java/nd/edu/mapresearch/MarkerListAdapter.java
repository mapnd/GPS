package nd.edu.mapresearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by JoaoGuilherme on 7/16/2015.
 * Adapter used by the Marker Listview
 */
public class MarkerListAdapter extends BaseAdapter{

    private List<ParseObject> list;
    private Context ctx;

    public MarkerListAdapter(List<ParseObject> list, Context ctx) {
        this.list = list;
        this.ctx = ctx;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public ParseObject getItem(int position) {
        if (list.size() > 0) {
            return list.get(position);
        } else {
            return null;
        }


    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder{

        public TextView text;
        public ImageView image;

    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        ViewHolder holder;
        LayoutInflater li = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(convertView==null){

            /****** Inflate tabitem.xml file for each row ( Defined below ) *******/
            //TextView tv = new TextView(ctx);
            //1vi = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE).

            vi = li.inflate(R.layout.marker_list_item, null);

            /****** View Holder Object to contain tabitem.xml file elements ******/

            holder = new ViewHolder();
            holder.text = (TextView)vi.findViewById(R.id.markerListItemTV);
            holder.image = (ImageView)vi.findViewById(R.id.markerListItemIV);

            /************  Set holder with LayoutInflater ************/
            vi.setTag( holder );
        }
        else
            holder=(ViewHolder)vi.getTag();

        if(list.size()<=0)
        {
            holder.text.setText("No Messages");

        } else {
            /***** Get each Model object from Arraylist ********/
            ParseObject obj = list.get(position);

            String display = "";
            String notes = obj.getString(Utils.PLACE_OBJECT_NOTES);
            String group = obj.getString(Utils.PLACE_OBJECT_GROUP);
            String creator = obj.getString(Utils.PLACE_OBJECT_USERNAME);

            display = "Group: " + group + ", created by: " + creator + "\n" + "Notes: " + notes;
            holder.text.setText(display);

            String icon = obj.getString(Utils.PLACE_OBJECT_ICON);
            for(int j = 0; j < Utils.eventsPlotted.length;j++){
                if(icon.equals(Utils.eventsPlotted[j])){
                    String mDrawName = Utils.eventsPlotted[j].toString().toLowerCase();
                    mDrawName = mDrawName.replaceAll("\\s","");
                    int resId = ctx.getResources().getIdentifier(mDrawName , "mipmap", ctx.getPackageName());
                    holder.image.setImageResource(resId);
                    //nearObject.icon(BitmapDescriptorFactory.fromResource(resId));
                }
            }
        }
        return vi;
    }

    private String converDateToDateString(Date date) {
        // Setting up the creation time
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);

        String tempMinutes = "";
        if (calendar.get(Calendar.MINUTE) < 10) {
            tempMinutes = "0" + String.valueOf(calendar.get(Calendar.MINUTE));
        } else {
            tempMinutes = String.valueOf(calendar.get(Calendar.MINUTE));
        }

        String amorpm = "";
        String tempHour = "";
        if (calendar.get(Calendar.HOUR_OF_DAY) == 0) {
            tempHour = "12";
            amorpm = "AM";
        }
        else if (calendar.get(Calendar.HOUR_OF_DAY) > 12) {
            tempHour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY) - 12);
            amorpm = "PM";
        }
        else if (calendar.get(Calendar.HOUR_OF_DAY) < 12) {
            tempHour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
            amorpm = "AM";
        }
        else {
            tempHour = "12";
            amorpm = "PM";
        }

        String hourCreated = tempHour + ":" + tempMinutes + " " + amorpm;
        String month = "";
        if (calendar.get(Calendar.MONTH) + 1 > 9) {
            month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        } else {
            month = "0" + String.valueOf(calendar.get(Calendar.MONTH) + 1);
        }
        String day = "";
        if (calendar.get(Calendar.DAY_OF_MONTH) > 9) {
            day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        } else {
            day = "0" + String.valueOf(calendar.get(Calendar.MONTH));
        }
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        return hourCreated + " of " + month + "/" + day + "/" + year;

    }
}
