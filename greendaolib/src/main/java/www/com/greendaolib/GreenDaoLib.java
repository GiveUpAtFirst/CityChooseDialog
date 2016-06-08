package www.com.greendaolib;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class GreenDaoLib {
    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(1, "mmt.mq.green.dao");
        addArea(schema);
        new DaoGenerator().generateAll(schema, "E://library/CityChoose/citychoise/src/main/java-gen");
    }

    //创建一个表格，容纳省市区信息
    private static void addArea(Schema schema) {
        Entity area = schema.addEntity("Area");
        area.addStringProperty("code");
        area.addStringProperty("name");
        area.addStringProperty("pcode");
    }
}
