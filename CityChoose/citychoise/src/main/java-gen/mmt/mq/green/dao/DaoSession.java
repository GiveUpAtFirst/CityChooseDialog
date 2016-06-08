package mmt.mq.green.dao;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;

import mmt.mq.green.dao.Area;

import mmt.mq.green.dao.AreaDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see de.greenrobot.dao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig areaDaoConfig;

    private final AreaDao areaDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        areaDaoConfig = daoConfigMap.get(AreaDao.class).clone();
        areaDaoConfig.initIdentityScope(type);

        areaDao = new AreaDao(areaDaoConfig, this);

        registerDao(Area.class, areaDao);
    }
    
    public void clear() {
        areaDaoConfig.getIdentityScope().clear();
    }

    public AreaDao getAreaDao() {
        return areaDao;
    }

}