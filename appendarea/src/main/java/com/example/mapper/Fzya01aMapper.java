package com.example.mapper;
//MyBatis 注解导入
import com.example.entity.Fzya01a;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;
//前端POST上传两个xlsx Excel文件 →
// 后端校验文件格式 → 保存到本地临时目录 →
// 调用Service完成面积匹配生成新Excel → 返回生成文件路径
// →自动删除本地临时文件；
// 出现文件读写/业务异常时返回错误信息并记录日志。
@Mapper
//告知 MyBatis 这是 DAO 数据库操作接口，自动生成代理对象，无需写实现类；
public interface Fzya01aMapper
//定义 Mapper 接口，接口名对应业务表FXQ_FZYA01A。
{
    /** 根据防控编号FZYA01A001更新面积 */
    @Update("UPDATE FXQ_FZYA01A SET AREA = #{area} WHERE FZYA01A001 = #{fzya01a001}")
    int updateAreaByFkbh(Fzya01a entity);

    // 查询全表所有数据
    @Select("SELECT * FROM FXQ_FZYA01A")
    List<Fzya01a> selectAll();
}

//原本代码改正： 查询全表所有数据
//List<Fzya01a> allList = Fzya01aMapper.selectAll();//接口不允许写这种赋值变量
//static List<Fzya01a> selectAll()//静态空方法；失效，无法查库，要重写为标准mybtis写法
// {
//return List.of();
//}
//}