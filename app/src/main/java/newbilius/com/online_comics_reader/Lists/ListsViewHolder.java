package newbilius.com.online_comics_reader.Lists;

import android.graphics.Typeface;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import newbilius.com.online_comics_reader.SimpleComicsReaderApplication;
import newbilius.com.online_comics_reader.Net.BaseUrls;
import newbilius.com.online_comics_reader.R;

class ListsViewHolder extends RecyclerView.ViewHolder {

    private final TextView titleTextView;
    private final TextView ratingTextView;
    private final TextView episodeCountTextView;
    private final TextView descriptionTextView;
    private final ImageView coverImageView;
    private final TextView comicsCompleteTextView;
    private final View view;

    ListsViewHolder(View itemView) {
        super(itemView);
        view = itemView;
        titleTextView = (TextView) itemView.findViewById(R.id.titleTextView);
        ratingTextView = (TextView) itemView.findViewById(R.id.ratingTextView);
        episodeCountTextView = (TextView) itemView.findViewById(R.id.episodeCountTextView);
        descriptionTextView = (TextView) itemView.findViewById(R.id.descriptionTextView);
        coverImageView = (ImageView) itemView.findViewById(R.id.coverImageView);
        comicsCompleteTextView = (TextView) itemView.findViewById(R.id.comicsCompleteTextView);
    }

    void changeData(ComicsListData data,
                    int position) {
        view.setTag(position);
        comicsCompleteTextView.setVisibility(data.Completed ? View.VISIBLE : View.GONE);
        titleTextView.setText(data.Title);
        ratingTextView.setText(SimpleComicsReaderApplication.getAppContext().getString(R.string.ratingPlaceholder, data.Rating));
        episodeCountTextView.setText(data.Pages);
        episodeCountTextView.setTypeface(null, data.PagesBold ? Typeface.BOLD : Typeface.NORMAL);
        if (data.Description.length() > 1)
            data.Description = data.Description.substring(0, 1).toUpperCase() + data.Description.substring(1);
        descriptionTextView.setText(data.Description);
        coverImageView.setImageResource(R.drawable.cell_comics_stub);
        Picasso.with(coverImageView.getContext())
                .load(BaseUrls.BASE_URL + data.CoverUrl)
                .placeholder(R.drawable.cell_comics_stub)
                .into(coverImageView);
    }
}
