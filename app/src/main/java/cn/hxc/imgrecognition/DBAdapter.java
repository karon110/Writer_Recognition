package cn.hxc.imgrecognition;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import cn.hxc.imgrecognitionSRI_OCR.R;

/**
 * Created by 刘欢 on 2018/4/21.
 */

public class DBAdapter extends ArrayAdapter{
    private int layoutId;
    private Context mContext;
    private List<DBItem> mList;

    public DBAdapter(Context context, int layoutId, List<DBItem> list) {
        super(context, layoutId, list);
        this.layoutId = layoutId;
        this.mContext = context;
        this.mList = list;
    }

    @Override
    public int getCount()
    {
        return mList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        ViewHold hold;
        if(convertView == null)
        {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listdb, null);
            hold = new ViewHold();
            hold.textView = (TextView)convertView.findViewById(R.id.db_text);
            hold.cb = (CheckBox)convertView.findViewById(R.id.db_checkbox);
            convertView.setTag(hold);
        }
        else
        {
            hold = (ViewHold)convertView.getTag();
        }

        final DBItem item = mList.get(position);
        //DBItem item = getItem(position);
        /*View view = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
        TextView db_text = (TextView) view.findViewById(R.id.db_text);
        CheckBox db_checkbox = (CheckBox) view.findViewById(R.id.db_checkbox);

        db_text.setText(item.getTxtContent());
        db_checkbox.setChecked(item.getIsCheck());*/
        hold.cb.setChecked(item.getIsCheck());
        hold.cb.setText(item.getTxtContent());
        hold.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    item.setIsCheck(true);
                }else {
                    item.setIsCheck(false);
                }
            }
        });
        return convertView;
    }
    public View getView(final int position, View convertView){

        return convertView;
    }
}

class ViewHold
{
    public TextView textView;
    public CheckBox cb;
}
