package com.ranying.syxw.service;

import com.ranying.syxw.constant.SyxwConstant;
import com.ranying.syxw.entity.SyxwLotteryParam;
import com.ranying.syxw.query.SyxwLotteryRecordQuery;
import com.ranying.syxw.vo.SyxwGenerateRow;
import com.ranying.syxw.vo.SyxwMaxNeglectResult;

import java.util.Date;
import java.util.List;

public interface SyxwSyncDataService {

    String getTableName(String type);

    List<SyxwGenerateRow> generate(SyxwLotteryParam lotteryParam);

    /**
     * @param date
     */
    void sync(SyxwConstant.LotteryType type,Date date);

    SyxwMaxNeglectResult searchMaxNeglect(String type, String numbers);

    SyxwLotteryRecordQuery getSyxwLotteryRecordQuery(SyxwLotteryParam lotteryParam);
}
