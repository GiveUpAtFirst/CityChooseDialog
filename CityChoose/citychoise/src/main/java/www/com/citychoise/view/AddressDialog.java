package www.com.citychoise.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;
import mmt.mq.green.dao.Area;
import mmt.mq.green.dao.AreaDao;
import mmt.mq.green.dao.DaoMaster;
import mmt.mq.green.dao.DaoSession;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import www.com.citychoise.R;
import www.com.citychoise.base.App;
import www.com.citychoise.helper.DataBaseOpenHelper;

/**
 * 这是一个用来选择地址的弹窗
 */
public class AddressDialog extends Dialog {

    public DaoSession daoSession;
    public SQLiteDatabase db;
    public DaoMaster.DevOpenHelper helper;
    public DaoMaster daoMaster;
    private ListView mLv;
    private MAdapter mAdapter;
    private List<Area> mData = new ArrayList<>();
    private AreaDao areaDao;
    private Cursor cursor;
    private Subscriber<List<Area>> subscriber;

    private int clickTime=0;
    private String mProvince;
    private String mCity;
    private String mVillage;
    private AddressSelectListener mAddressSelectListener;
    private Context mContext;

    public interface AddressSelectListener{
        void addressSelect(String address);
    }
    public void setAddressSelectListener(AddressSelectListener addressSelectListener){
        mAddressSelectListener=addressSelectListener;
    }

    public AddressDialog(Context context) {
        this(context, R.style.address_dialog);
    }

    public AddressDialog(Context context, int theme) {
        super(context, theme);
        initView(context);
        areaDao = App.getInstance(App.getContext()).getDaoSession().getAreaDao();
        cursor = App.getInstance(App.getContext()).getDb().query(areaDao.getTablename(),
                areaDao.getAllColumns(), null, null, null, null, null);
        loadData(null);
        loadData(null);
        initEvent();
    }

    private void initEvent() {
        mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Area area = mData.get(position);
                //特殊情况
                String name = area.getName();
                if(name.equals("香港")||name.equals("澳门")||name.equals("台湾省")){
                    if(mAddressSelectListener!=null){
                        mAddressSelectListener.addressSelect(name);
                    }
                    AddressDialog.this.dismiss();
                    return;
                }
                //以下是通用情况
                if (clickTime == 0) {
                    mProvince = area.getName();
                } else if (clickTime == 1) {
                    mCity = area.getName();
                } else if (clickTime == 2) {
                    mVillage = area.getName();
                    if(mAddressSelectListener!=null){
                        StringBuffer sb=new StringBuffer();
                        sb.append(mProvince);
                        if(mProvince.equals(mCity)){

                        }else{
                            sb.append(","+mCity);
                        }
                        sb.append(","+mVillage);
                        mAddressSelectListener.addressSelect(sb.toString());
                    }
                    AddressDialog.this.dismiss();
                    return;
                }
                loadData(area.getCode());
                clickTime++;
            }
        });
    }

    private void loadData(String pcode) {
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

                if(areas.size()==0){
                    if(mAddressSelectListener!=null){
                        StringBuffer sb=new StringBuffer();
                        sb.append(mProvince);
                        if(mProvince.equals(mCity)){

                        }else{
                        sb.append(","+mCity);
                        }
                        sb.append(","+mVillage);
                        mAddressSelectListener.addressSelect(sb.toString());
                    }
                    AddressDialog.this.dismiss();
                }else{
                    mData.clear();
                    mData.addAll(areas);
                    mAdapter.notifyDataSetChanged();
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
        this.setContentView(R.layout.view_dialog_address_select);
        mLv = (ListView) this.findViewById(R.id.lv_dialog_address_select);
        if (mAdapter == null) {
            mAdapter = new MAdapter();
        }
        mLv.setAdapter(mAdapter);
        Window window = this.getWindow();
        window.setWindowAnimations(R.style.dialogWindowAnim);
        window.setGravity(Gravity.BOTTOM);
        WindowManager windowManager = ((Activity) context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.width = (int) (display.getWidth()); //设置宽度
        window.setAttributes(lp);
    }

    //适配器
    public class MAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = View.inflate(App.getContext(), R.layout.item_address_dialog, null);
                viewHolder.city = (TextView) convertView.findViewById(R.id.city_item_address_dialog);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.city.setText(mData.get(position).getName());
            return convertView;
        }

        class ViewHolder {
            public TextView city;
        }
    }

    @Override
    public void setOnDismissListener(OnDismissListener listener) {
        super.setOnDismissListener(listener);
        if (subscriber != null && subscriber.isUnsubscribed()) {
            subscriber.unsubscribe();
        }
    }
}
