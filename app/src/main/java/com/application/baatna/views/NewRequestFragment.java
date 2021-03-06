package com.application.baatna.views;

import android.app.Activity;
import android.app.Fragment;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.application.baatna.R;
import com.application.baatna.data.Categories;
import com.application.baatna.utils.BTracker;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.IconView;
import com.application.baatna.utils.RequestWrapper;
import com.application.baatna.utils.UploadManager;

import java.util.ArrayList;
import java.util.List;

public class NewRequestFragment extends Fragment {

	private View rootView;
	private LayoutInflater mInflater;
	private int width;
	private Activity mContext;
	private ListView mCategoriesListView;
	private RequestCategoryAdapter mCategoriesAdapter;
	private GetCategoriesList mAsyncTaskRunning;
	private SharedPreferences prefs;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mInflater = inflater;
		rootView = mInflater.inflate(R.layout.new_request_fragment, null);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mContext = getActivity();
		width = mContext.getWindowManager().getDefaultDisplay().getWidth();
		prefs = mContext.getSharedPreferences(CommonLib.APP_SETTINGS, 0);

		mCategoriesListView = (ListView) rootView.findViewById(R.id.category_list_view);
		// refreshView();
		setCategories(CommonLib.getCategoriesList());
		fixSizes();
		setListeners();


	}

	private void fixSizes() {

		rootView.findViewById(R.id.category_et).setPadding(width / 20+width/40, width / 20, width / 20+width/40, width / 40);
		rootView.findViewById(R.id.days_hint).setPadding(0,0,width/20+width/40,0);
		rootView.findViewById(R.id.time_duration).setPadding(width / 20+width/40, width / 20, width / 20+width/40, width / 40);
		((LinearLayout.LayoutParams) rootView.findViewById(R.id.category_separator1).getLayoutParams())
				.setMargins(width / 20+width/40, 0, width / 20+width/40, 0);
		((LinearLayout.LayoutParams) rootView.findViewById(R.id.category_separator2).getLayoutParams())
				.setMargins(width / 20+width/40, 0, width / 20+width/40, 0);


		rootView.findViewById(R.id.description_et).setPadding(width / 20+width/40, width / 10, width / 20+width/40, width / 10);

		((LinearLayout.LayoutParams) rootView.findViewById(R.id.post).getLayoutParams()).setMargins(width / 20,
				width / 20, 0, width / 20);

		rootView.findViewById(R.id.post).setPadding(width / 20, width / 40, width / 20, width / 40);

		rootView.findViewById(R.id.category_selection_label).setPadding(width / 20+width/40, width / 20, width / 20+width/40, width / 20);

		mCategoriesListView.setDivider(null);

		rootView.findViewById(R.id.new_request_progress_container).setVisibility(View.GONE);
		rootView.findViewById(R.id.post).setVisibility(View.GONE);
	}

	private void setListeners() {
		rootView.findViewById(R.id.description_et).setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (rootView != null) {
					View scrollView = rootView.findViewById(R.id.new_request_scroll_container);
					((ScrollView) scrollView).requestDisallowInterceptTouchEvent(true);
				}
				v.onTouchEvent(event);
				return false;
			}
		});
		final EditText description = (EditText)rootView.findViewById(R.id.description_et);
		description.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().contains("\n")) {
					String temp[] = s.toString().split("\n");
					Log.e("length is", "" + temp.length);
					if (temp.length >= 6) {
//						description.setEnabled(false);
//						description.setImeOptions(EditorInfo.IME_ACTION_NEXT);
//						description.invalidate();
					} else {
//						description.setEnabled(true);
//						description.setImeOptions(EditorInfo.IME_ACTION_NONE);
//						description.invalidate();
					}
				}
			}
		});
		rootView.findViewById(R.id.empty_view_retry_container).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshView();
			}
		});

		rootView.findViewById(R.id.post).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BTracker.logGAEvent(mContext, BTracker.CATEGORY_WIDGET_ACTION, BTracker.ACTION_WISH_POST_PRESSED, "");
				int timeDuration = -1;
				try {
					timeDuration = Integer.parseInt( ((TextView) rootView.findViewById(R.id.time_duration)).getText().toString());
				} catch(NumberFormatException e) {
					e.printStackTrace();
				}
				String title = ((TextView) rootView.findViewById(R.id.category_et)).getText().toString();
				String description = ((TextView) rootView.findViewById(R.id.description_et)).getText().toString();

				if (title == null || title.length() < 1) {
					Toast.makeText(mContext, "Please enter title of the request", Toast.LENGTH_SHORT).show();
					((TextView) rootView.findViewById(R.id.category_et)).requestFocus();
					return;
				}

				if (timeDuration == -1 ) {
					Toast.makeText(mContext, "Please enter time duration of the request", Toast.LENGTH_SHORT).show();
					((TextView) rootView.findViewById(R.id.time_duration)).requestFocus();
					return;
				}


				if (description == null || description.length() < 30) {
					Toast.makeText(mContext, "Please enter description of the request of at least 30 characters", Toast.LENGTH_SHORT).show();
					((TextView) rootView.findViewById(R.id.description_et)).requestFocus();
					return;
				}
				
				UploadManager.postNewRequest(prefs.getString("access_token", ""), title, description, timeDuration);
				try {
					InputMethodManager imm = (InputMethodManager) mContext
							.getSystemService(Service.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		final EditText et=(EditText)rootView.findViewById(R.id.time_duration);
		et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					rootView.findViewById(R.id.days_hint).setVisibility(View.VISIBLE);
					et.setHint("");
				} else{
					if(et.getText().toString().equals("")){
						et.setHint(R.string.time_duration);
						rootView.findViewById(R.id.days_hint).setVisibility(View.GONE);
					}
					if(et.getText().toString().equals("1")){
						((TextView)rootView.findViewById(R.id.days_hint)).setText("Day");
					}
					else
						((TextView)rootView.findViewById(R.id.days_hint)).setText("Days");

					//rootView.findViewById(R.id.days_hint).setVisibility(View.GONE);
//					int timeDurartion=Integer.parseInt( ((TextView) rootView.findViewById(R.id.time_duration)).getText().toString());
//					if(timeDurartion>31)
//					{Toast.makeText(mContext, "Time Duration has to be less than a month", Toast.LENGTH_SHORT).show();
//					rootView.findViewById(R.id.days_hint).requestFocus();}
//
				}
			}
		});
	}

	private void refreshView() {
		if (mAsyncTaskRunning != null)
			mAsyncTaskRunning.cancel(true);
		(mAsyncTaskRunning = new GetCategoriesList()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private class GetCategoriesList extends AsyncTask<Object, Void, Object> {

		@Override
		protected void onPreExecute() {
			rootView.findViewById(R.id.new_request_progress_container).setVisibility(View.VISIBLE);

			rootView.findViewById(R.id.new_request_scroll_container).setAlpha(1f);

			rootView.findViewById(R.id.new_request_scroll_container).setVisibility(View.GONE);

			rootView.findViewById(R.id.empty_view).setVisibility(View.GONE);
			super.onPreExecute();
		}

		// execute the api
		@Override
		protected Object doInBackground(Object... params) {
			try {
				CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
				String url = "";
				url = CommonLib.SERVER + "wish/categories?";
				Object info = RequestWrapper.RequestHttp(url, RequestWrapper.CATEGORIES_LIST, RequestWrapper.FAV);
				CommonLib.ZLog("url", url);
				return info;

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			if (!isAdded())
				return;

			rootView.findViewById(R.id.new_request_progress_container).setVisibility(View.GONE);

			if (result != null) {
				rootView.findViewById(R.id.new_request_scroll_container).setVisibility(View.VISIBLE);
				if (result instanceof ArrayList<?>)
					setCategories((ArrayList<Categories>) result);
			} else {
				if (CommonLib.isNetworkAvailable(mContext)) {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.error_try_again),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.no_internet_message),
							Toast.LENGTH_SHORT).show();

					rootView.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);

					rootView.findViewById(R.id.new_request_scroll_container).setVisibility(View.GONE);
				}
			}

		}
	}

	private void setCategories(ArrayList<Categories> categories) {
		mCategoriesAdapter = new RequestCategoryAdapter(mContext, R.layout.new_request_fragment, categories);
		mCategoriesListView.setAdapter(mCategoriesAdapter);
		setListViewHeightBasedOnChildren(mCategoriesListView);
	}

	private void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			// pre-condition
			return;
		}

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
		listView.requestLayout();
	}

	public class RequestCategoryAdapter extends ArrayAdapter<Categories> {

		private List<Categories> categories;
		private Activity mContext;
		private int width;

		public RequestCategoryAdapter(Activity context, int resourceId, List<Categories> categories) {
			super(context.getApplicationContext(), resourceId, categories);
			mContext = context;
			this.categories = categories;
			width = mContext.getWindowManager().getDefaultDisplay().getWidth();
		}

		@Override
		public int getCount() {
			if (categories == null) {
				return 0;
			} else {
				return categories.size();
			}
		}

		protected class ViewHolder {
			TextView category_label;
			IconView category_image;
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {
			final Categories categoryName = categories.get(position);
			if (v == null || v.findViewById(R.id.request_category_root) == null) {
				v = LayoutInflater.from(mContext).inflate(R.layout.request_category_adapter, null);
			}

			ViewHolder viewHolder = (ViewHolder) v.getTag();
			if (viewHolder == null) {
				viewHolder = new ViewHolder();
				viewHolder.category_label = (TextView) v.findViewById(R.id.category_title);
				viewHolder.category_image = (IconView) v.findViewById(R.id.category_image);
				v.setTag(viewHolder);
			}

			if (position % 2 == 0) {
				v.findViewById(R.id.request_category_root)
						.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.zhlbuttonfollow));
			} else {
				v.findViewById(R.id.request_category_root)
						.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.whitebuttoncustomback));
			}

			v.findViewById(R.id.proceed_icon).setPadding(width / 20+width/40, 0, width / 20+width/20, 0);

			viewHolder.category_label.setText(categoryName.getCategory());
			viewHolder.category_image.setText(categoryName.getCategoryIcon());
			viewHolder.category_label.setPadding(width / 40, width / 40, width / 20, width / 40);
			viewHolder.category_image.setPadding(width / 20+width/40, width / 40, width / 40+width/40, width / 40);
			viewHolder.category_label.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					TextView view = (TextView) v;
					if (view != null && view.getText() != null) {
						// setSelectedCategory(view.getText().toString());
						Intent intent = new Intent(mContext, CategoryItemSelectionFragment.class);
						intent.putExtra("category_id", categoryName.getCategoryId());
						intent.putExtra("category_name", categoryName.getCategory());
						mContext.startActivityForResult(intent, CategoryItemSelectionFragment.requestCode);
					}
				}
			});

			return v;
		}

	}

	private void setSelectedCategory(String category) {
		if (rootView != null) {
			((TextView) rootView.findViewById(R.id.category_et)).setText(category);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CategoryItemSelectionFragment.requestCode) {
			if (resultCode == CategoryItemSelectionFragment.RESULT_ITEM_SELECTED && data != null) {
				if (data.hasExtra("title")) {
					((TextView) rootView.findViewById(R.id.category_et))
							.setText(String.valueOf(data.getStringExtra("title")));
				}
			}
		}
	}

	public String getSelectedCategory() {
		return ((TextView) rootView.findViewById(R.id.category_et)).getText().toString();
	}

}
