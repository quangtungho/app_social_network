
package vn.techres.line.view;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import vn.techres.line.R;
import vn.techres.line.helper.emoji.Emojicon;
import vn.techres.line.helper.emoji.Emojicon;


/**
 * @author Ankush Sachdeva (sankush@yahoo.co.in)
 */
class EmojiAdapter extends ArrayAdapter<Emojicon> {
	EmojiconGridView.OnEmojiconClickedListener emojiClickListener;
    public EmojiAdapter(Context context, List<Emojicon> data) {
        super(context, R.layout.emojicon_item, data);
    }

    public EmojiAdapter(Context context, Emojicon[] data) {
        super(context, R.layout.emojicon_item, data);
    }
    
    public void setEmojiClickListener(EmojiconGridView.OnEmojiconClickedListener listener){
    	this.emojiClickListener = listener;
    }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = View.inflate(getContext(), R.layout.emojicon_item, null);
            ViewHolder holder = new ViewHolder();
            holder.icon = v.findViewById(R.id.emojicon_icon);
            v.setTag(holder);
        }
        Emojicon emoji = getItem(position);
        ViewHolder holder = (ViewHolder) v.getTag();
        holder.icon.setText(emoji.getEmoji());
        holder.icon.setOnClickListener(v1 -> emojiClickListener.onEmojiconClicked(getItem(position)));
        return v;
    }

    class ViewHolder {
        TextView icon;
    }
}