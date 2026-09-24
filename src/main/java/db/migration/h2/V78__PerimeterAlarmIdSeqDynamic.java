package db.migration.h2;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * H2 方言：自愈式复位 fac_perimeter_alarm 自增主键序列。
 *
 * 根因（反复出现的 409 来源）：
 *   1) V29 种子显式写入 id=1,2，H2 的 AUTO_INCREMENT 在「显式插入自增列」时不会自动推进内部序列；
 *   2) 更隐蔽的是——失败的 INSERT（主键冲突）仍会消耗一个自增序号且不会回退。
 *      于是反复点击「新增」失败时，序列被一路推高（实测文件库曾被推到 35），而真实数据 max(id) 仍是 4。
 *      一旦运行中实例持有的序列值回落到 <= max(id)，下一次录入又会撞主键 → 409。
 *
 * 修复：动态复位到 MAX(id)+1，不写死具体数字，对「种子新增行 / 失败插入吃掉序号」都自愈。
 *   注：H2 的 ALTER ... RESTART WITH 只接受字面量，无法内嵌子查询，故用 Java 迁移在运行时计算。
 */
public class V78__PerimeterAlarmIdSeqDynamic extends BaseJavaMigration {
  @Override
  public void migrate(Context context) throws Exception {
    try (Statement stmt = context.getConnection().createStatement()) {
      long next = 1L;
      try (ResultSet rs = stmt.executeQuery("SELECT MAX(\"ID\") FROM \"FAC_PERIMETER_ALARM\"")) {
        if (rs.next()) {
          long max = rs.getLong(1);
          if (!rs.wasNull()) {
            next = max + 1L;
          }
        }
      }
      stmt.execute("ALTER TABLE \"FAC_PERIMETER_ALARM\" ALTER COLUMN \"ID\" RESTART WITH " + next);
    }
  }
}
