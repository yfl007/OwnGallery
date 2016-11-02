package org.ece.owngallery.activity;

import java.util.ArrayList;

import org.ece.owngallery.ApplicationOwnGallery;
import org.ece.owngallery.R;
import org.ece.owngallery.adapter.BaseFragmentAdapter;
import org.ece.owngallery.component.PhoneMediaControl;
import org.ece.owngallery.component.PhoneMediaControl.AlbumEntry;
import org.ece.owngallery.component.PhoneMediaControl.loadAlbumPhoto;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class GalleryFragment extends Fragment {

	public static final String PACKAGE = "org.ece.owngallery";
	private static final String TAG = "GalleryFragment";
	private TextView emptyView;
	private GridView mView;
	private Context mContext;
	
	
	public static ArrayList<PhoneMediaControl.AlbumEntry> albumsSorted = null;
	private Integer cameraAlbumId = null;
	private PhoneMediaControl.AlbumEntry selectedAlbum = null;
	private int itemWidth = 100;
	private ListAdapter mAdapter;

	private ModeCallback mCallback;
	private MenuInflater mMenuInflater;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		/** Inflating the layout for this fragment **/
		mContext = this.getActivity();
		View v = inflater.inflate(R.layout.fragment_gallery, null);
		initializeView(v);
		return v;
	}
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		mMenuInflater = inflater;
		inflater.inflate(R.menu.main,menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_multichoice) {
			Log.d(TAG,"onOptionsItemSelected:action_multichoice");
			mView.setItemChecked(0,true);
			mView.clearChoices();
			mCallback.updateSeletedCount();
		}
		return super.onOptionsItemSelected(item);
	}

	private void initializeView(View v){ 
		mView=(GridView)v.findViewById(R.id.grid_view);
        emptyView = (TextView)v.findViewById(R.id.searchEmptyView);
        emptyView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        emptyView.setText("NoPhotos");
		mView.setAdapter(mAdapter = new ListAdapter(mContext));
        int position = mView.getFirstVisiblePosition();
        int columnsCount = 2;
        mView.setNumColumns(columnsCount);
        itemWidth = (ApplicationOwnGallery.displaySize.x - ((columnsCount + 1) * ApplicationOwnGallery.dp(4))) / columnsCount;
        mView.setColumnWidth(itemWidth);

        mAdapter.notifyDataSetChanged();
        mView.setSelection(position);
        mView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            	Intent mIntent=new Intent(mContext,AlbumActivity.class);
            	Bundle mBundle=new Bundle();
            	mBundle.putString("Key_ID", position+"");
            	mBundle.putString("Key_Name", albumsSorted.get(position).bucketName+"");
            	mIntent.putExtras(mBundle);
            	mContext.startActivity(mIntent);
            }
        });
       //For mark option
		mCallback = new ModeCallback();
		mView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		mView.setMultiChoiceModeListener(mCallback);
	/*	mView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position,
									long id) {
				Toast.makeText(ChoiceModeMultipleModalActivity.this, "选择了一个item", 300).show();
			}
		});*/
		LoadAllAlbum();
	}
	
	
	private void LoadAllAlbum(){
		PhoneMediaControl mediaControl=new PhoneMediaControl();
		mediaControl.setLoadalbumphoto(new loadAlbumPhoto() {
			
			@Override
			public void loadPhoto(ArrayList<AlbumEntry> albumsSorted_) {
				albumsSorted =new ArrayList<PhoneMediaControl.AlbumEntry>(albumsSorted_);
				if (mView != null && mView.getEmptyView() == null) {
					mView.setEmptyView(null);
		        }
		        if (mAdapter != null) {
		            mAdapter.notifyDataSetChanged();
		        }
			}
		});
		mediaControl.loadGalleryPhotosAlbums(mContext,0);
	}
	
	

	private class ListAdapter extends BaseFragmentAdapter {
		private Context mContext;
		private DisplayImageOptions options;
		private ImageLoader imageLoader = ImageLoader.getInstance();

		public ListAdapter(Context context) {
			mContext = context;
			options = new DisplayImageOptions.Builder()
					.showImageOnLoading(R.drawable.nophotos)
					.showImageForEmptyUri(R.drawable.nophotos)
					.showImageOnFail(R.drawable.nophotos).cacheInMemory(true)
					.cacheOnDisc(true).considerExifParams(true).build();
			imageLoader.init(ImageLoaderConfiguration.createDefault(context));
		}

		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}

		@Override
		public boolean isEnabled(int i) {
			return true;
		}

		@Override
		public int getCount() {
			if (selectedAlbum != null) {
				return selectedAlbum.photos.size();
			}
			return albumsSorted != null ? albumsSorted.size() : 0;
		}

		@Override
		public Object getItem(int i) {
			return i;
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			if (view == null) {
				LayoutInflater li = (LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = li.inflate(R.layout.photo_picker_album_layout,
						viewGroup, false);
			}
			ViewGroup.LayoutParams params = view.getLayoutParams();
			params.width = itemWidth;
			params.height = itemWidth;
			view.setLayoutParams(params);

			PhoneMediaControl.AlbumEntry albumEntry = albumsSorted.get(i);
			final ImageView imageView = (ImageView) view
					.findViewById(R.id.media_photo_image);
			if (albumEntry.coverPhoto != null
					&& albumEntry.coverPhoto.path != null) {
				imageLoader.displayImage(
						"file://" + albumEntry.coverPhoto.path, imageView,
						options);
			} else {
				imageView.setImageResource(R.drawable.nophotos);
			}
			TextView textView = (TextView) view.findViewById(R.id.album_name);
			textView.setText(albumEntry.bucketName);
			if (cameraAlbumId != null && albumEntry.bucketId == cameraAlbumId) {

			} else {

			}
			textView = (TextView) view.findViewById(R.id.album_count);
			textView.setText("" + albumEntry.photos.size());
			updateBackground(i, textView);
			return view;
		}
		private void updateBackground(int position, View view) {
			int backgroundId;
			if (mView.isItemChecked(position)) {
				Log.d(TAG,"isItemChecked:i=="+position);
				backgroundId = R.drawable.checkmark;
			} else {
				Log.d(TAG,"isNotItemChecked:i=="+position);
		backgroundId = R.color.ripple_material_light;
			}
			Drawable background = mContext.getResources().getDrawable(backgroundId);
			view.setBackground(background);
		}

		@Override
		public int getItemViewType(int i) {
			if (selectedAlbum != null) {
				return 1;
			}
			return 0;
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public boolean isEmpty() {
			if (selectedAlbum != null) {
				return selectedAlbum.photos.isEmpty();
			}
			return albumsSorted == null || albumsSorted.isEmpty();
		}
	}

	public class ModeCallback implements GridView.MultiChoiceModeListener{
		private View mMultiSelectActionBarView;
		private TextView mSelectedCount;

		@Override
		public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
			updateSeletedCount();
			actionMode.invalidate();
			mAdapter.notifyDataSetChanged();
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// actionmode的菜单处理
			MenuInflater inflater = mMenuInflater;
			inflater.inflate(R.menu.multi_select_menu, menu);
			if (mMultiSelectActionBarView == null) {
				mMultiSelectActionBarView = LayoutInflater.from(mContext)
						.inflate(R.layout.list_multi_select_actionbar, null);

				mSelectedCount =
						(TextView)mMultiSelectActionBarView.findViewById(R.id.selected_conv_count);
			}
			mode.setCustomView(mMultiSelectActionBarView);
			((TextView)mMultiSelectActionBarView.findViewById(R.id.title)).setText(R.string.select_item);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
			if (mMultiSelectActionBarView == null) {
				ViewGroup v = (ViewGroup)LayoutInflater.from(mContext)
						.inflate(R.layout.list_multi_select_actionbar, null);
				actionMode.setCustomView(v);
				mSelectedCount = (TextView)v.findViewById(R.id.selected_conv_count);
			}
			//更新菜单的状态
			MenuItem selectItem = menu.findItem(R.id.action_select);
			if(mView.getCheckedItemCount() == mAdapter.getCount()){
				selectItem.setTitle(R.string.action_deselect_all);
			}else{
				selectItem.setTitle(R.string.action_select_all);
			}
			MenuItem deleteItem = menu.findItem(R.id.action_delete);
//			deleteItem.setTitle(R.)
			if (mView.getCheckedItemCount()!=0){
				deleteItem.setVisible(true);
			}else {
				deleteItem.setVisible(false);
			}
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
			switch (menuItem.getItemId()) {
				case R.id.action_select:
					if(mView.getCheckedItemCount() == mAdapter.getCount()){
						unSelectedAll();
					}else{
						selectedAll();
					}
					mAdapter.notifyDataSetChanged();
					break;
				case R.id.action_delete:

					break;
				default:
					break;
			}
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode actionMode) {
			mView.clearChoices();
		}
		public void updateSeletedCount(){
			mSelectedCount.setText(Integer.toString(mView.getCheckedItemCount()));
		}
		public void selectedAll(){
			for(int i= 0; i< mAdapter.getCount(); i++){
				mView.setItemChecked(i, true);
			}
			updateSeletedCount();
		}

		public void unSelectedAll(){
			mView.clearChoices();
			mView.setItemChecked(0,false);
			updateSeletedCount();
		}
	}
}
