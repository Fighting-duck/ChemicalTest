package com.lsy.chemicaltest_new.interfaces;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Transaction;

import com.lsy.chemicaltest_new.domain.History_multiple;

import java.util.List;

public interface MultiHistory_Data {
    /**
     * 保存多种实验结果（电学、比色、光热）到数据库，并生成一条综合的历史记录。
     *
     * 该方法的主要功能是：
     * 1. 分别保存电学、颜色和热学实验结果，并获取对应的数据库 ID 和样本 ID。
     * 2. 检查所有实验结果的样本 ID 是否一致。
     * 3. 计算综合可信度。
     * 4. 创建并保存一条包含所有实验结果信息的历史记录。
     *
     * @param context 应用程序上下文，用于访问资源或进行其他需要上下文的操作。
     * @param history_multiple 历史记录
     */
    @Transaction
    void saveHistory(Context context, History_multiple history_multiple);
    /**
     * 删除一条历史记录
     * @param history_multiple 历史记录
     */
    @Transaction
    void deleteHistory(History_multiple history_multiple);
    /**
     * 批量删除历史记录
     * @param histories 历史记录
     */
    @Transaction
    void deleteHistories(List<History_multiple> histories);

    /**
     * 并行填充历史记录（曲线信息）
     * @param history_multiple 历史记录
     */
    @Transaction
    void fillPreviewHistory_curve(@NonNull History_multiple history_multiple, @NonNull UpdateCallback callback);
    /**
     * 并行批量填充历史记录列表（实验人员、实验样本、）
     * @param histories 历史记录列表
     * @return 填充后的历史记录列表
     */
    List<History_multiple> fillAllHistories(List<History_multiple> histories);
    /**
     * 从数据库中加载历史记录（模糊查询）
     * @param date 日期
     * @param sampleId 样本id
     * @return List<History_multiple>
     * @throws Exception
     */
    List<History_multiple> FuzzySearch(String date, Integer sampleId);
}
