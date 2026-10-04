package com.sinopec.mmsecurity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinopec.mmsecurity.entity.MgmtLedgerCell;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MgmtLedgerCellMapper extends BaseMapper<MgmtLedgerCell> {

    /**
     * 按「列筛选」匹配行主键：relational-division。
     * 一个台账行须对其每个筛选条件都存在 (col_key, cell_text) 匹配的单元格，才算命中。
     * 用 GROUP BY row_id HAVING COUNT(DISTINCT col_key) = 筛选条件数 表达「全部满足」。
     * 标准 SQL（JOIN / GROUP BY / HAVING / 等值），三方言 H2/PG/DM 通用，不引方言特有函数。
     */
    @Select("<script>"
            + "SELECT c.row_id FROM mgmt_ledger_cell c "
            + "JOIN mgmt_ledger_row r ON r.id = c.row_id "
            + "WHERE r.domain_code = #{domain} AND ("
            + "<foreach collection='clauses' item='c' separator=' OR '>"
            + "(c.col_key = #{c.col} AND c.cell_text = #{c.val})"
            + "</foreach>"
            + ") GROUP BY c.row_id HAVING COUNT(DISTINCT c.col_key) = #{filterCount}"
            + "</script>")
    List<Long> selectRowIdsMatchingAllFilters(@Param("domain") String domain,
            @Param("clauses") List<ColVal> clauses, @Param("filterCount") int filterCount);

    /**
     * 按关键字匹配行主键：单元格文本模糊匹配（LIKE，转义 %/_/\\，避免误当通配符）。
     */
    @Select("<script>"
            + "SELECT DISTINCT c.row_id FROM mgmt_ledger_cell c "
            + "JOIN mgmt_ledger_row r ON r.id = c.row_id "
            + "WHERE r.domain_code = #{domain} AND c.cell_text LIKE #{keyword} ESCAPE '\\'"
            + "</script>")
    List<Long> selectRowIdsByKeyword(@Param("domain") String domain, @Param("keyword") String keyword);

    /** 列筛选条件 (列名, 值) 载体，供 MyBatis &lt;foreach&gt; 迭代。 */
    class ColVal {
        private final String col;
        private final String val;

        public ColVal(String col, String val) {
            this.col = col;
            this.val = val;
        }

        public String getCol() {
            return col;
        }

        public String getVal() {
            return val;
        }
    }
}
