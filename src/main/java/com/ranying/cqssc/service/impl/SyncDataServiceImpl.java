package com.ranying.cqssc.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ranying.cqssc.constant.LotteryConstant;
import com.ranying.cqssc.dao.LotteryParamDAO;
import com.ranying.cqssc.dao.LotteryRecordDAO;
import com.ranying.cqssc.entity.LotteryParam;
import com.ranying.cqssc.entity.LotteryRecord;
import com.ranying.cqssc.query.LotteryParamQuery;
import com.ranying.cqssc.service.SyncDataService;
import com.ranying.util.DateFormat;
import com.ranying.util.HttpClientUtil;
import com.ranying.cqssc.vo.GenerateResultVO;
import com.ranying.cqssc.vo.GenerateRow;
import com.ranying.cqssc.vo.GenerateVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SyncDataServiceImpl implements SyncDataService {

    @Resource
    private LotteryRecordDAO lotteryRecordDAO;

    @Resource
    private LotteryParamDAO lotteryParamDAO;

    @Value("${lottery.api}")
    private String url;

    /**
     * 同步重庆时时彩的方法
     */
    @Override
    public void sync() {

        // 请求接口
        LotteryRecord record = lotteryRecordDAO.selectLatestPeriod();
        if (record != null) {
            // 获取期数
            String date = DateFormat.dateFormateYmd(record.getOpentime());
            url = url + "&date=" + date;
        } else {
            String date = DateFormat.dateFormateYmd(new Date());
            url = url + "&date=" + date;
        }
        System.out.println(url);
        try {
            Thread.sleep(4000);
        } catch (Exception e) {
            //do nothing
        }
        String resultStr = HttpClientUtil.doGet(url);
        System.out.println(resultStr);
        List<LotteryRecord> records = getRecords(resultStr);
        lotteryRecordDAO.batchInsert(records);
    }

    @Override
    public void sync(Date date) {
        // 获取期数
        String dateStr = DateFormat.dateFormateYmd(date);
        url = url + "&date=" + dateStr;
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
            //do nothing
        }
        String resultStr = HttpClientUtil.doGet(url);
        System.out.println(resultStr);
        List<LotteryRecord> records = getRecords(resultStr);
        lotteryRecordDAO.batchInsert(records);
    }

    /**
     * 解析接口同步的彩票结果数据
     *
     * @param resultStr 结果Str
     * @return List
     */
    private List<LotteryRecord> getRecords(String resultStr) {

        JSONObject jsonObject = JSONObject.parseObject(resultStr);
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        List<LotteryRecord> records = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject obj = (JSONObject) jsonArray.get(i);
            LotteryRecord lotteryRecord = new LotteryRecord();
            String expect = obj.getString("expect");
            lotteryRecord.setExpect(expect);
            String opencode = obj.getString("opencode");
            String[] numbers = opencode.split(",");
            lotteryRecord.setOpencode1(Integer.valueOf(numbers[0]));
            lotteryRecord.setOpencode2(Integer.valueOf(numbers[1]));
            lotteryRecord.setOpencode3(Integer.valueOf(numbers[2]));
            lotteryRecord.setOpencode4(Integer.valueOf(numbers[3]));
            lotteryRecord.setOpencode5(Integer.valueOf(numbers[4]));
            lotteryRecord.setOpentime(obj.getDate("opentime"));
            lotteryRecord.setOpentimestamp(obj.getLong("opentimestamp"));
            records.add(lotteryRecord);
        }
        return records;
    }

    @Override
    public List<GenerateRow> generate(LotteryParam lotteryParam) {
        return  this.generate(lotteryParam,null);
    }

    @Override
    public List<GenerateRow> generate(LotteryParam lotteryParam,Integer warningFlag) {

        List<GenerateRow> rows = new ArrayList<>();
        List<String> queryList = lotteryParam.getNumberList();

        //位置一  万千百
        List<GenerateVO> list = lotteryRecordDAO.queryGenerate(queryList, LotteryConstant.Arrange.WQB.getValue());
        rows.add(getLocationRow(list, LotteryConstant.Arrange.WQB, lotteryParam.getWqb()));

        //位置二 万千十
        list = lotteryRecordDAO.queryGenerate(queryList, LotteryConstant.Arrange.WQS.getValue());
        rows.add(getLocationRow(list, LotteryConstant.Arrange.WQS, lotteryParam.getWqs()));

        //位置三 万千个
        list = lotteryRecordDAO.queryGenerate(queryList, LotteryConstant.Arrange.WQG.getValue());
        rows.add(getLocationRow(list, LotteryConstant.Arrange.WQG, lotteryParam.getWqg()));

        //位置四 万百十
        list = lotteryRecordDAO.queryGenerate(queryList, LotteryConstant.Arrange.WBS.getValue());
        rows.add(getLocationRow(list, LotteryConstant.Arrange.WBS, lotteryParam.getWbs()));

        //位置五 万百个
        list = lotteryRecordDAO.queryGenerate(queryList, LotteryConstant.Arrange.WBG.getValue());
        rows.add(getLocationRow(list, LotteryConstant.Arrange.WBG, lotteryParam.getWbg()));

        //位置六 万十个
        list = lotteryRecordDAO.queryGenerate(queryList, LotteryConstant.Arrange.WSG.getValue());
        rows.add(getLocationRow(list, LotteryConstant.Arrange.WSG, lotteryParam.getWsg()));

        //位置七 千百十
        list = lotteryRecordDAO.queryGenerate(queryList, LotteryConstant.Arrange.QBS.getValue());
        rows.add(getLocationRow(list, LotteryConstant.Arrange.QBS, lotteryParam.getQbs()));

        //位置八 千百个
        list = lotteryRecordDAO.queryGenerate(queryList, LotteryConstant.Arrange.QBG.getValue());
        rows.add(getLocationRow(list, LotteryConstant.Arrange.QBG, lotteryParam.getQbg()));

        //位置九 千十个
        list = lotteryRecordDAO.queryGenerate(queryList, LotteryConstant.Arrange.QSG.getValue());
        rows.add(getLocationRow(list, LotteryConstant.Arrange.QSG, lotteryParam.getQsg()));

        //位置十 百十个
        list = lotteryRecordDAO.queryGenerate(queryList, LotteryConstant.Arrange.BSG.getValue());
        rows.add(getLocationRow(list, LotteryConstant.Arrange.BSG, lotteryParam.getBsg()));

        return rows;
    }

    private GenerateRow getLocationRow(List<GenerateVO> list, LotteryConstant.Arrange location, Integer warning) {
        GenerateRow row = new GenerateRow();
        if (list == null || list.isEmpty()) {
            lotteryRecordDAO.countPeriod(null);
            row.setWaringFlag(1);
            return row;
        }
        GenerateVO generateVO = list.get(0);
        row.setLatestExpect(generateVO.getLatestExpect());
        row.setTimes(list.size());
        int period = lotteryRecordDAO.countPeriod(row.getLatestExpect());
        row.setPeriod(period);
        row.setWaringFlag(Math.floor(warning * 0.4) <= period ? 1 : 0);
        row.setLocation(location.getName());
        row.setMaxNeglect(warning);
        return row;
    }

    @Override
    public void save(LotteryParam lotteryParam) {
        lotteryParam.setName(lotteryParam.getName().trim());
        lotteryParam.setNumbers(lotteryParam.getNumbers().trim());
        LotteryParamQuery query = new LotteryParamQuery();
        lotteryParam.setAmount(lotteryParam.getNumbers().split("\\s+").length);
        query.setName(lotteryParam.getName());
        List<LotteryParam> list = lotteryParamDAO.listByQuery(query);
        if (!list.isEmpty()) {
            throw new RuntimeException("存在名称" + lotteryParam.getName());
        }
        query = new LotteryParamQuery();
        query.setNumbers(lotteryParam.getName());
        list = lotteryParamDAO.listByQuery(query);
        if (!list.isEmpty()) {
            throw new RuntimeException("存在数据:" + lotteryParam.getNumbers());
        }
        lotteryParamDAO.insert(lotteryParam);
    }

    @Override
    public GenerateResultVO analyze(Integer id) {
        GenerateResultVO resultVO = new GenerateResultVO();
        LotteryParam lotteryParam = lotteryParamDAO.selectByPrimaryKey(id);
        List<GenerateRow> rows = this.generate(lotteryParam);
        resultVO.setGenerateRows(rows);
        resultVO.setLotteryParam(lotteryParam);
        return resultVO;
    }

}
