package www.com.citychoise.base;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import mmt.mq.green.dao.DaoMaster;
import mmt.mq.green.dao.DaoSession;
import www.com.citychoise.helper.DataBaseOpenHelper;

/**
 * Created by hp on 2016/6/8.
 */
public class App extends Application{
    private static Context mContext;
    public DaoSession daoSession;
    public SQLiteDatabase db;
    public DaoMaster.DevOpenHelper helper;
    public DaoMaster daoMaster;
    public static Context getContext() {
        return mContext;
    }
    @Override
    public void onCreate() {
        // 1.上下文
        mContext = getApplicationContext();
        setupDatabase();
        super.onCreate();
    }
    private void setupDatabase() {
        //对地址的
        helper = new DataBaseOpenHelper(this,"DB", null);
        db = helper.getWritableDatabase();
        // 注意：该数据库连接属于 DaoMaster，所以多个 Session 指的是相同的数据库连接。
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }
    public DaoSession getDaoSession() {
        return daoSession;
    }

    public SQLiteDatabase getDb() {
        return db;
    }

    public static App getInstance(Context context){
        return (App)context.getApplicationContext();
    }
}
