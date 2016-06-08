package www.com.citychoise.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;
import mmt.mq.green.dao.Area;
import mmt.mq.green.dao.AreaDao;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import www.com.citychoise.R;
import www.com.citychoise.base.App;

/// ViewPager版的地址选择弹窗


public class AddressDialogVP extends Dialog {
    private RecyclerView mRv;
    private ViewPager mVp;
    private RAdapter mRAdapter;
    private VAdapter mVAdapter;
    private List<String> mTopData = new ArrayList<>();
    private List<List<Area>> mDownData = new ArrayList<>();
    private AreaDao areaDao;
    private Cursor cursor;
    private Subscriber<List<Area>> subscriber;

    public AddressDialogVP(Context context) {
        this(context, R.style.address_dialog);
    }

    public AddressDialogVP(Context context, int theme) {
        super(context, theme);
        initView(context);
        areaDao = App.getInstance(App.getContext()).getDaoSession().getAreaDao();
        cursor = App.getInstance(App.getContext()).getDb().query(areaDao.getTablename(),
                areaDao.getAllColumns(), null, null, null, null, null);
        initData();
    }

    //初始化数据
    private void initData() {
        loadData(null, "中国", -1);
    }

    private void loadData(String pcode, final String name, final int position) {
        if (subscriber != null) {
            subscriber.unsubscribe();
            subscriber = null;
        }
        subscriber = new Subscriber<List<Area>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(List<Area> areas) {

                if (areas != null && areas.size() > 0) {
                    //说明存在数据
                    //先进行数据清除
                    if(mDownData.size()>(position+1)){
                        for(int i=(mDownData.size()-1);i>=(position+1);i--){
                            mDownData.remove(i);
                        }
                    }
                    if(mTopData.size()>(position+1)){
                        for (int i =(mTopData.size()-1);i>=(position+1);i--){
                            mTopData.remove(i);
                        }
                    }
                    mVAdapter.notifyDataSetChanged();
                    //进行数据添加
                    mTopData.add(name);
                    mRAdapter.notifyDataSetChanged();

                    mDownData.add(areas);
                    mVAdapter.notifyDataSetChanged();
                    mVp.setCurrentItem(mDownData.size() - 1);
                } else {
                    //说明没有下级数据,将地址返回
                    if(mAddressSelectListener!=null){
                        StringBuffer sb=new StringBuffer();
                        List<String> data=new ArrayList<>();
                        data.addAll(mTopData);
                        data.add(name);
                        for(String s:data){
                            if(s.equals("香港")){
                                sb.append("香港");
                                break;
                            }
                            if(s.equals("澳门")){
                                sb.append("澳门");
                                break;
                            }

                            if(s.equals("台湾省")){
                                sb.append("台湾省");
                                break;
                            }

                            if(s.equals("市辖区")||s.equals("县")){
                                continue;
                            }

                            if(sb.toString().contains(s)){
                                continue;
                            }
                            sb.append(s);
                        }
                        mAddressSelectListener.addressSelect(sb.toString());
                        AddressDialogVP.this.dismiss();
                    }
                }
            }
        };
        queryTable(subscriber, pcode);
    }

    public void queryTable(Subscriber<List<Area>> subscriber, final String pcode) {
        Observable.create(new Observable.OnSubscribe<List<Area>>() {
            @Override
            public void call(Subscriber<? super List<Area>> subscriber) {
                QueryBuilder<Area> builder = areaDao.queryBuilder();
                QueryBuilder<Area> where = null;
                if (pcode == null) {
                    where = builder.where(AreaDao.Properties.Pcode.isNull());
                } else {
                    where = builder.where(AreaDao.Properties.Pcode.eq(pcode));
                }
                Query<Area> build = where.build();

                List<Area> list = build.list();
                cursor.requery();
                subscriber.onNext(list);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    private void initView(Context context) {
        this.setContentView(R.layout.view_dialog_vp_address_select);

        mRv = (RecyclerView) this.findViewById(R.id.rv_view_dialog_vp_address_select);
        mRv.setLayoutManager(new LinearLayoutManager(context, LinearLayout.HORIZONTAL, false));
        if (mRAdapter == null) {
            mRAdapter = new RAdapter();
        }
        mRv.setAdapter(mRAdapter);

        mVp = (ViewPager) this.findViewById(R.id.vp_view_dialog_vp_address_select);
        if (mVAdapter == null) {
            mVAdapter = new VAdapter();
        }
        mVp.setAdapter(mVAdapter);


        Window window = this.getWindow();
        window.setWindowAnimations(R.style.dialogWindowAnim);
        window.setGravity(Gravity.BOTTOM);
        WindowManager windowManager = ((Activity) context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.width = (int) (display.getWidth()); //设置宽度
        window.setAttributes(lp);
    }

    class RAdapter extends RecyclerView.Adapter {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = View.inflate(App.getContext(), R.layout.item_top_view_dialog_vp_address_select, null);
            return new TopVieHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            TopVieHolder topVieHolder = (TopVieHolder) holder;
            topVieHolder.tv.setText(mTopData.get(position));
        }

        @Override
        public int getItemCount() {
            return mTopData.size();
        }

        class TopVieHolder extends RecyclerView.ViewHolder {
            public TextView tv;

            public TopVieHolder(View itemView) {
                super(itemView);
                tv = (TextView) itemView.findViewById(R.id.tv_item_top_view_dialog_vp_address_select);

                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getPosition();
                        if (position < mDownData.size()) {
                            mVp.setCurrentItem(getPosition());
                        }
                    }
                });
            }
        }
    }

    class VAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return mDownData.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View v = View.inflate(App.getContext(), R.layout.item_down_view_dialog_vp_address_select, null);

            ListView lv = (ListView) v.findViewById(R.id.lv_item_down_view_dialog_vp_address_select);
            final List<Area> list = mDownData.get(position);
            final int currentPosition = position;
            lv.setAdapter(new LAdapter(list));

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Area area = list.get(position);
                    loadData(area.getCode(), area.getName(),currentPosition);
                }
            });
            container.addView(v);
            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    class LAdapter extends BaseAdapter {
        List<Area> data = new ArrayList<>();

        public LAdapter(List<Area> data) {
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = View.inflate(App.getContext(), R.layout.item_item_view_dialog_vp_address_select, null);
                viewHolder.tv = (TextView) convertView.findViewById(R.id.tv_item_item_view_dialog_vp_address_select);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.tv.setText(data.get(position).getName());
            return convertView;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        class ViewHolder {
            TextView tv;
        }
    }

    public interface AddressSelectListener{
        void addressSelect(String address);
    }
    private AddressSelectListener mAddressSelectListener;
    public void setAddressSelectListener(AddressSelectListener addressSelectListener){
        mAddressSelectListener=addressSelectListener;
    }
}
