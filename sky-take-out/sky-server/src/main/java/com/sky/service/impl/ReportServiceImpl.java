package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.IReportService;
import com.sky.service.IWorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
public class ReportServiceImpl implements IReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private IWorkspaceService workspaceService;

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        // 1.准备日期列表数据 dateList    ---> 近7日：5-14，5-15...5-20
        List<LocalDate> dateList = getDateList(begin, end);
        // 2.准备营业额列表数据 turnoverList
        List<Double> turnoverList = new ArrayList<>();
        // 营业额=订单状态已完成的订单金额
        // 查询orders表，条件：状态-已完成，下单时间
        for (LocalDate date : dateList) {
            Map map = new HashMap<String, Object>();
            map.put("status", Orders.COMPLETED);
            map.put("beginTime", LocalDateTime.of(date, LocalTime.MIN));
            map.put("endTime", LocalDateTime.of(date, LocalTime.MAX));
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }
        // 3.构造TurnoverReportVO对象并返回
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ",")) // [5-14，5-15...5-20] --> "5-14,5-15,....5-20"
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 获取日期列表数据
     *
     * @param begin
     * @param end
     * @return
     */
    private List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        while (!begin.isAfter(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        log.info("dateList = {}", dateList);
        return dateList;
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        // 1.构造dateList数据
        List<LocalDate> dateList = getDateList(begin, end);

        // 2.构造newUserList数据，新增用户列表
        List<Integer> newUserList = new ArrayList<>();
        // 3.totalUserList数据，总用户列表
        List<Integer> totalUserList = new ArrayList<>();

        // 循环遍历日期列表统计每日的新增用户数---user
        dateList.forEach(date -> {
            // select count(*) from user where create_time >= 当天开始时间 and create_time <= 当天结束时间
            Map map = new HashMap();
            map.put("beginTime", LocalDateTime.of(date, LocalTime.MIN));    // 2024-05-14 00:00
            map.put("endTime", LocalDateTime.of(date, LocalTime.MAX));      // 2024-05-14 23:59:59.999999999
            Integer newUser = userMapper.countByMap(map);
            newUserList.add(newUser);
            // select count(*) from user where create_time <=  当天结束时间
            map.put("beginTime", null);    // 2024-05-14 00:00
            Integer totalUser = userMapper.countByMap(map);
            totalUserList.add(totalUser);
        });

        // 4. 构造UserReportVO对象并返回
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    @Override
    public OrderReportVO orderStatistics(LocalDate begin, LocalDate end) {
        // 1.获取日期列表数据 dateList
        List<LocalDate> dateList = getDateList(begin, end);

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        Integer totalOrderCount = 0;
        Integer validOrderCount = 0;
        // 循环遍历日期列表进行订单统计
        for (LocalDate date : dateList) {
            // 2.获取每日总订单列表数 orderCountList
            // 统计每日orders数量，条件：下单时间 >= 当天起始时间 and 下单时间 <= 当天结束时间
            Map map = new HashMap();
            map.put("beginTime", LocalDateTime.of(date, LocalTime.MIN));    // 2024-05-14 00:00
            map.put("endTime", LocalDateTime.of(date, LocalTime.MAX));      // 2024-05-14 23:59:59.999999999
            Integer totalOrder = orderMapper.countByMap(map);
            orderCountList.add(totalOrder);

            // 3.获取每日有效订单列表数 validOrderCountList
            // 统计每日有效orders数量，条件：状态=有效（已完成）、 下单时间 >= 当天起始时间 and 下单时间 <= 当天结束时间
            map.put("status", Orders.COMPLETED);
            Integer validOrder = orderMapper.countByMap(map);
            validOrderCountList.add(validOrder);

            // 4.获取区间订单总数 totalOrderCount
            // select count(*) from orders where 下单时间 >= '2024-05-14 00:00' 下单时间 <= '2024-05-20 23:59:59.999999999'
            totalOrderCount += totalOrder;

            // 5.获取有效订单数 validOrderCount
            validOrderCount += validOrder;
        }

        // 6.计算完成率 orderCompletionRate
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = (validOrderCount + 0.0) / totalOrderCount;
        }

        // 7.封装OrderReportVO对象并返回
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        // 1. 构造nameList，商品名称列表
        List<String> nameList = new ArrayList<>();

        // 2. 构造numberList，商品销量（份数）列表
        List<Integer> numberList = new ArrayList<>();

        // 查询订单明细表order_detail+ 订单表orders， 条件：订单状态-已完成，下单时间
        Map map = new HashMap();
        map.put("status", Orders.COMPLETED);
        map.put("beginTime", LocalDateTime.of(begin, LocalTime.MIN));   // 2024-05-14 00:00
        map.put("endTime", LocalDateTime.of(end, LocalTime.MAX));      // 2024-05-20 23:59:59.999999999
        List<GoodsSalesDTO> list = orderMapper.sumTop10(map);

        for (GoodsSalesDTO dto : list) {
            nameList.add(dto.getName());
            numberList.add(dto.getNumber());
        }

        // 3.封装 SalesTop10ReportVO 对象并返回
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
    }

    @Override
    public void export(HttpServletResponse response) {
        // 1.查询近30天的运营数据
        LocalDate now = LocalDate.now();
        LocalDate end = now.minusDays(1);
        LocalDate begin = now.minusDays(30);
        // 转化为最低的和最高的
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        // 根据时间段统计营业数据
        BusinessDataVO businessData = workspaceService.getBusinessData(beginTime, endTime);
        // 2.往Excel表中写入数据
        // 根据类加载器读取项目中的模板文件, 基于模板文件创建Excel对象
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(is);
            XSSFSheet sheet = workbook.getSheet("Sheet1");
            sheet.getRow(1).getCell(1).setCellValue("时间：" + begin + "至" + end);
            sheet.getRow(3).getCell(2).setCellValue(businessData.getTurnover());    //营业额
            sheet.getRow(3).getCell(4).setCellValue(businessData.getOrderCompletionRate());    //订单完成率
            sheet.getRow(3).getCell(6).setCellValue(businessData.getNewUsers());    //新增用户数

            sheet.getRow(4).getCell(2).setCellValue(businessData.getValidOrderCount()); //有效订单
            sheet.getRow(4).getCell(4).setCellValue(businessData.getUnitPrice());       //平均客单价

            // 30天明细数据填充

            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.plusDays(i);
                beginTime = LocalDateTime.of(date, LocalTime.MIN); //4-23 00:00
                endTime = LocalDateTime.of(date, LocalTime.MAX);   //4-23 23:59:59.999999
                businessData = workspaceService.getBusinessData(beginTime, endTime);
                sheet.getRow(7+i).getCell(1).setCellValue(date.toString());
                sheet.getRow(7+i).getCell(2).setCellValue(businessData.getTurnover());
                sheet.getRow(7+i).getCell(3).setCellValue(businessData.getValidOrderCount());
                sheet.getRow(7+i).getCell(4).setCellValue(businessData.getOrderCompletionRate());
                sheet.getRow(7+i).getCell(5).setCellValue(businessData.getUnitPrice());
                sheet.getRow(7+i).getCell(6).setCellValue(businessData.getNewUsers());
            }
            // 3.通过输出流将Excel文件对象下载到客户端浏览器
            ServletOutputStream os = response.getOutputStream();
            workbook.write(os);

            // 4.释放资源
            os.close();
            workbook.close();
            is.close();
        }catch (Exception e){
            log.error("文件写入失败！！！{}",e.getMessage());
        }
    }
}
