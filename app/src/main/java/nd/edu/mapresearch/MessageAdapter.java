package nd.edu.mapresearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by JoaoGuilherme on 7/4/2015.
 * Adapter used by the message listview
 */
public class MessageAdapter extends BaseAdapter {

    private ArrayList<ParseObject> list;
    private Context ctx;

    public MessageAdapter(ArrayList<ParseObject> list, Context ctx) {
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

            vi = li.inflate(R.layout.read_message_list_item, null);

            /****** View Holder Object to contain tabitem.xml file elements ******/

            holder = new ViewHolder();
            holder.text = (TextView)vi.findViewById(R.id.ReadMessageItemTV);

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
            display = "From: " + obj.getString(Utils.MESSAGE_DATA_SENDER) + ", ";
            display += "At: " + converDateToDateString(obj.getCreatedAt());
            String message = obj.getString(Utils.MESSAGE_DATA_TEXT);

            boolean isRead = obj.getBoolean(Utils.MESSAGE_DATA_READ);
            if (isRead) {
                display+= ", Read!";
            } else {
                display += ", Not read!";
            }
            String trimmedMessage = message.substring(0, Math.min(message.length(), 10)); //get first 10 characters
            display += "\nPreview: " + trimmedMessage;
            holder.text.setText(display);

            //vi.setOnClickListener(new OnItemClickListener( position ));
            //Put onClickListener to display Message
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
